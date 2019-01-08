package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommentSaveUpdateCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.search.AssetCommentQueryBuilder
import net.transitionmanager.security.Permission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Methods to manage comments/tasks.
 * @author jmartin
 */
@Slf4j
class CommentService implements ServiceMethods {

	private static final List<String> watchProps = [
		'actStart', 'assetEntity', 'assignedTo', 'comment', 'dueDate',
		'estFinish', 'estStart', 'moveEvent', 'priority', 'role',
		'sendNotification', 'status']

	// These are the default values that are remembered during user session for preferences
	// when creating new tasks (see TM-5696)
	Map<PREF, Object> TASK_CREATE_DEFAULTS = [
		(PREF.TASK_CREATE_EVENT): PREF.MOVE_EVENT,
		(PREF.TASK_CREATE_CATEGORY): AssetCommentCategory.GENERAL,
		(PREF.TASK_CREATE_STATUS): AssetCommentStatus.READY
	].asImmutable()

	def mailService					// SendMail MailService class
	AssetEntityService assetEntityService
	ApiActionService apiActionService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	Scheduler quartzScheduler
	SecurityService securityService
	TaskService taskService
	SequenceService sequenceService
	UserPreferenceService userPreferenceService

	/**
	 * Used to persist changes to the AssetComment and CommentNote from various forms for both Tasks and Comments
	 * @param tzId - the user's time zone id
	 * @param userDTFormat - the user's time date format
	 * @param params - the request params
	 * @param isNew - a flag to indicate of the request was for a new (true) or existing (false)
	 * @param flash - the controller flash message object to stuff messages into (YUK!!!)
	 * @return map of the AssetComment data used to refresh the view
	 */
	@Transactional
	def saveUpdateCommentAndNotes(String tzId, String userDTFormat, Map params, boolean isNew=true, flash) {
		boolean canEditAsset = securityService.hasPermission(Permission.AssetEdit)
		String currentUsername = securityService.currentUsername
		Person currentPerson = securityService.loadCurrentPerson()
		Project project = securityService.userCurrentProject

		def date = new Date()
		def assetEntity
		def assetComment
		def commentProject
		def map = [:]
		def errorMsg

		// Wrap the whole routine so that we can break out when we hit fatal situations
		while (true) {
			if (!project) {
				log.warn "saveUpdateCommentAndNotes: User has no currently selected project"
				errorMsg = "You have no currently selected project"
				break
			}

			// if assetEntity is passed, then validate that it's valid and that the user has access to it (belongs to the current project)
			if (params.assetEntity && params.assetEntity != 'NaN') {
				if (params.assetEntity != 'null' &&  !params.assetEntity.isNumber()) {
					log.warn "saveUpdateCommentAndNotes: Invalid asset id ($params.assetEntity)"
					errorMsg = "An unexpected asset id was received"
					break
				}
				// Now see if it exists and belongs to the project
				if (params.containsKey("assetEntity")) {
					assetEntity = params.assetEntity != 'null' ? AssetEntity.read(params.assetEntity) : null
				}

				if (assetEntity) {
					def assetProject = assetEntity.project
					if (assetProject.id != project.id) {
						log.error "saveUpdateCommentAndNotes: Asset($assetEntity.id/$assetProject) not associated with user($currentUsername) project ($project)"
						errorMsg = "It appears that you do not have permission to view the specified task"
						break
					}
				}
			}

			// Create or load the assetComment object appropriately
			if (isNew) {
				// Only tasks can be created that are not associated with an Asset
				if (!assetEntity && params.commentType != AssetCommentType.TASK) {
					log.error "saveUpdateCommentAndNotes: Asset id was not properly supplied to add or update a comment"
					errorMsg = 'The asset id was missing in the request'
					break
				}

				// Check if the user can create comments
				if ((params.commentType != AssetCommentType.TASK) && !securityService.hasPermission(Permission.CommentCreate)) {
					log.error "saveUpdateCommentAndNotes: User don't have permission to create comments"
					errorMsg = "User don't have permission to create comments"
					securityService.reportViolation("User don't have permission to create comments")
					break
				}

				// Let's create a new Comment/Task
				assetComment = new AssetComment(createdBy: currentPerson, project: project)
				if (params.commentType == AssetCommentType.TASK) {
					assetComment.taskNumber = sequenceService.next(project.client.id, 'TaskNumber')
				}
				if (assetEntity) {
					assetComment.assetEntity = assetEntity
					commentProject = assetEntity.project
				}
			} else {
				// Load existing comment
				// TODO : Test that the id is a number and by user's project
				assetComment = AssetComment.get(params.id)
				if (!assetComment) {
					// TODO : handle failure for invalid comment id
					log.error "saveUpdateCommentAndNotes: Specified comment [id:$params.id] was not found"
					errorMsg = 'Was unable to find the task for the specified id'
					break
				}

				// Check if the user can edit comments
				if ((assetComment.commentType != AssetCommentType.TASK) && !canEditAsset) {
					log.error "saveUpdateCommentAndNotes: User don't have permission to edit comments"
					errorMsg = "User don't have permission to edit comments"
					securityService.reportViolation("User don't have permission to edit comments")
					break
				}

				// If params.currentStatus is passed along, the form is expecting the status to be in this state so if it has changed
				// then someone else beat the user to changing the status so we are going to stop and warn the user.
				if (params.currentStatus) {
					if (assetComment.status != params.currentStatus) {
						log.warn "saveUpdateCommentAndNotes() user $currentUsername attempted to change task ($assetComment.id) status but was already changed"
						// TODO - assignedTo may be changing at the same time, which is assigned below. Need to review this as it is a potential edge case.
						Person whoDidIt = (assetComment.status == AssetCommentStatus.COMPLETED) ? assetComment.resolvedBy : assetComment.assignedTo
						switch (assetComment.status) {
							case AssetCommentStatus.READY:
								// No need to error in this situation
								break
							case AssetCommentStatus.STARTED:
								// Check to see if this is an project admin because they can update a task for users
								def isPM = partyRelationshipService.staffHasFunction(project, currentPerson.id, 'PROJ_MGR')

								log.debug "Task Already STARTED - isPM? $isPM, whoDidIt=$whoDidIt, person=$currentUsername"
								// We'll allow the same user to click Start and Done
								if (whoDidIt.id != currentPerson.id && ! isPM) {
									errorMsg = "The task was previously STARTED by $whoDidIt"
								}
								break
							case AssetCommentStatus.COMPLETED:
								errorMsg = "The task was previously COMPLETED by $whoDidIt"
								break
							default:
								errorMsg = "The task status was changed to '$assetComment.status'"
								break
						}
					}
				}

				if (errorMsg) {
					break
				}

				commentProject = assetComment.project
				if (params.containsKey("assetEntity")) {
					assetComment.assetEntity = assetEntity
				}
				// Make sure that the comment about to be updated is associated to the user's current project
				if (commentProject.id != project.id) {
					log.error "saveUpdateCommentAndNotes: The comment ($params.id/$commentProject.id) is not associated with user's current project [$project.id]"
					errorMsg = 'It appears that you are not allowed to view this task'
					break
				}
			}

			/*
			 * Now do the general binding of params to the new or updated object
			 */

			//	def bindArgs = [assetComment, params, [ exclude:['assignedTo', 'assetEntity', 'moveEvent', 'project', 'dueDate', 'status'] ] ]
			//	def bindArgs = [assetComment, params, [ include:['comment', 'category', 'displayOption', 'attribute'] ] ]
			//	bindData.invoke( assetComment, 'bind', (Object[])bindArgs )
			// TODO - assignedTo is getting set even though it is in exclude list (seen in debugger with issue type)

			// Assign the general params for all types.  Was having an issue with the above binding, which was
			// setting the assignedTo automatically with a blank Person object even though it was excluded.
			// TODO : should only set properties base on the commentType

			if(!session.hasProperty('assetCommentDef')){
				session.assetCommentDef = [:]
			}

			if (params.commentType) assetComment.commentType = params.commentType
			if (params.comment) assetComment.comment = params.comment
			if (params.category){
				assetComment.category = params.category
				userPreferenceService.setPreference(PREF.TASK_CREATE_CATEGORY, params.category)
			}

			if (params.displayOption) assetComment.displayOption = params.displayOption
			if (params.attribute) assetComment.attribute = params.attribute
			assetComment.resolution = params.resolution
			if (params.containsKey('estStart')) {
				// log.info "saveUpdateCommentAndNotes: estStart=[$params.estStart]"
				assetComment.estStart = TimeUtil.parseDateTime(params.estStart)
			}
			if (params.containsKey('estFinish')) {
				assetComment.estFinish = TimeUtil.parseDateTime(params.estFinish)
			}
			if (params.containsKey('role')) {
				assetComment.role = params.role ?: ''
			}
			if (params.containsKey('instructionsLink')) {
				assetComment.instructionsLink = params.instructionsLink ?: null
			}
			if (assetComment.commentType == 'comment' && params.isResolved?.isNumber()) {
				if (NumberUtil.toInteger(params.isResolved) == 0) {
					assetComment.setDateResolved(null)
					assetComment.resolvedBy = null
				} else {
					assetComment.setDateResolved(TimeUtil.nowGMT())
					assetComment.resolvedBy = currentPerson
				}
			}
			// TM-10112 - Make sure isResolved == 0 (setDateResolved() sets isResolved to 0 internally)
			// TM-11379 - (updated) isResolved property doesn't exist anymore, code is still ok
			if (isNew && assetComment.commentType == 'comment') {
				assetComment.setDateResolved(null)
			}

			// Actual Start/Finish are handled by the statusUpdate function
			// if(params.actStart) assetComment.actStart = estformatter.parse(params.actStart)
			if (params.containsKey('workflowTransition')) {
				if (params.workflowTransition?.isNumber()) {
					// TODO : should validate that this workflow is associated with that of the workflow for the move bundle
					def wft = WorkflowTransition.get(params.workflowTransition)
					if (wft) {
						assetComment.workflowTransition = wft
					} else {
						log.warn "saveUpdateCommentAndNotes: Invalid workflowTransition id ($params.workflowTransition)"
					}
				} else {
					assetComment.workflowTransition = null
				}
			}
			if (params.hardAssigned?.isNumber()) assetComment.hardAssigned = Integer.parseInt(params.hardAssigned)

			// TM-4765: @tavo_luna: assign the new Notification Value with a NULL safe operation that defaults to 'true'
			assetComment.sendNotification = (params.sendNotification ?: true).toBoolean()

			if (params.priority?.isNumber()) assetComment.priority = Integer.parseInt(params.priority)
			if (params.override?.isNumber()) assetComment.workflowOverride = Integer.parseInt(params.override)
			if (params.duration?.isInteger()) assetComment.duration = Integer.parseInt(params.duration)
			if(params.durationLocked) assetComment.durationLocked = params.durationLocked.toBoolean()
			if (params.durationScale) {
				assetComment.durationScale = TimeScale.asEnum(params.durationScale.toUpperCase())
				log.debug "saveUpdateCommentAndNotes - task(id:${assetComment.id}, num:${assetComment.taskNumber}) TimeScale=$assetComment.durationScale"
			}

			// Issues (aka tasks) have a number of additional properties to be managed
			if (assetComment.commentType == AssetCommentType.TASK) {
				if (params.containsKey('moveEvent')) {
					if (params.moveEvent.isNumber()) {
						if (params.moveEvent == "0") {
							assetComment.moveEvent = null
						} else {
							userPreferenceService.setPreference(PREF.TASK_CREATE_EVENT, params.moveEvent)
							def moveEvent = MoveEvent.get(params.moveEvent)
							if (moveEvent) {
								// Validate that this is a legit moveEvent for this project
								if (moveEvent.project.id != project.id) {
									// TODO: handle failure of moveEvent not being in project
									log.error "saveUpdateCommentAndNotes: moveEvent.project ($moveEvent.id) does not match user's current project ($project.id)"
									errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
									break
								}
								assetComment.moveEvent = moveEvent
							} else {
								log.error "saveUpdateCommentAndNotes: Specified moveEvent ($moveEvent.id) was not found})"
								errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
								break
							}
						}
					} else if (params.moveEvent == '') {
						assetComment.moveEvent = null
					}
				}

				if (params.containsKey('assignedTo')) {
					if (params.assignedTo && params.assignedTo.isNumber()) {
						if (params.assignedTo == '0') { // if unassigned is selected .
							assetComment.assignedTo = null
						} else {
							def person = Person.read(params.assignedTo)
							if (person) {
								def projectStaff = partyRelationshipService.getProjectStaff(project.id)
								if (projectStaff.find { it.staff.id.toString() == params.assignedTo }) {
									assetComment.assignedTo = person
								} else {
									log.error "User ($currentUsername) attempted to assign unrelated person ($params.assignedTo) to project ($project)"
								}
							}
						}
					}
				}

				if (params.containsKey('dueDate')) {
					// log.info "saveUpdateCommentAndNotes: dueDate=[$params.dueDate]"
					assetComment.dueDate = TimeUtil.parseDate(params.dueDate)
				}
			}

			// Assign ApiAction to task
			if(params.containsKey("apiActionId")){
				Long id = NumberUtil.toLong(params.apiActionId)
				ApiAction apiAction = null
				if (id) {
					apiAction = apiActionService.find(id, assetComment.project)
				}
				assetComment.apiAction = apiAction

			}

			// Use the service to update the Status because it does a number of things that we don't need to duplicate. This
			// should be the last update to Task properties before saving.
			//store default value for the status
			userPreferenceService.setPreference(PREF.TASK_CREATE_STATUS, params.status)
			taskService.setTaskStatus(assetComment, params.status)

			// Only send email if the originator of the change is not the assignedTo as one doesn't need email to one's self.
			boolean addingNote = assetComment.commentType == AssetCommentType.TASK && params.note

			// Note that shouldSendNotification has to be called before calling save on the object
			boolean shouldSendNotification = shouldSendNotification(assetComment, currentPerson, isNew, addingNote)

			if (!assetComment.hasErrors() && assetComment.save(flush: true)) {
				// Deal with Notes if there are any
				if (assetComment.commentType == AssetCommentType.TASK && params.note) {
					// TODO The adding of commentNote should be a method on the AssetComment instead of reverse injections plus the save above can handle both. Right now if this fails, everything keeps on as though it didn't which is wrong.
					def commentNote = new CommentNote(createdBy: currentPerson, dateCreated: date, note: params.note,
					                                  isAudit: 0, assetComment: assetComment)
					if (commentNote.hasErrors() || !commentNote.save(flush: true)) {
						// TODO error won't bubble up to the user
						log.error "saveUpdateCommentAndNotes: Saving comment notes faild - ${GormUtil.allErrorsString(commentNote)}"
						errorMsg = 'An unexpected error occurred while saving your comment'
						break
					}
				}

				// Now handle creating / updating task dependencies if the "manageDependency flag was passed
				if (params.manageDependency) {
					def taskDependencies = params['taskDependency']
					def taskSuccessors = params['taskSuccessor']
					def deletedPreds = params.deletedPreds

					// If we're updating, we'll delete the existing dependencies and then readd them following
					if (!isNew && deletedPreds) {
						TaskDependency.executeUpdate("DELETE TaskDependency t WHERE t.id in ( $deletedPreds ) ")
					}
					// Iterate over the predecessor ids and validate that the exist and are associated with the project
					taskDependencies.each { preds ->
						def taskDepId = preds.split("_")[0]
						def predecessorId = preds.split("_")[1]
						def predecessor = AssetComment.get(predecessorId)
						if (!predecessor) {
							log.error "saveUpdateCommentAndNotes: invalid predecessor id ($predecessorId)"
						} else {
							def predProject = predecessor.project
							if (predProject?.id != project.id) {
								log.warn "saveUpdateCommentAndNotes: predecessor project id (${predProject?.id}) different from current project ($project.id)"
							} else {
								errorMsg = saveAndUpdateTaskDependency(assetComment, predecessor, taskDepId, predecessorId)
							}
						}
					}
					taskSuccessors.each { succ ->
						def taskSuccId = succ.split("_")[0]
						def successorId = succ.split("_")[1]
						def successor = AssetComment.get(successorId)
						if (!successor) {
                      log.error "saveUpdateCommentAndNotes: invalid successor id ($successorId)"
                  } else {
							def predProject = successor.project
							if (predProject?.id != project.id) {
                          log.warn "saveUpdateCommentAndNotes: successor project id (${predProject?.id}) different from current project ($project.id)"
                      } else {
								errorMsg = saveAndUpdateTaskDependency(successor, assetComment, taskSuccId, successorId)
							}
						}
					}
				}

				// TODO - comparison of the assetComment.dueDate may not work if the dueDate is stored in GMT
				def css = (assetComment.dueDate < date ? 'Lightpink' : 'White')
				boolean status = assetComment.commentType == AssetCommentType.TASK && !assetComment.isResolved()

				map = [
					assetComment: assetComment,
					status: status,
					cssClass: css,
					statusCss: taskService.getCssClassForStatus(assetComment.status),
					assignedToName: assetComment.assignedTo ? (assetComment.assignedTo.firstName + " " + assetComment.assignedTo.lastName): "",
					lastUpdatedDate: TimeUtil.formatDateTime(assetComment.lastUpdated, TimeUtil.FORMAT_DATE_TIME_13)
				]

				// Now refine if the task should be sent based on it being new or updated
				if (shouldSendNotification) {
					dispatchTaskEmail([taskId: assetComment.id, tzId: tzId, isNew: isNew, userDTFormat: userDTFormat])
				}

				break

			} else {
				log.error "saveUpdateCommentAndNotes: Saving comment changes - ${GormUtil.allErrorsString(assetComment)}"
				errorMsg = 'An unexpected error occurred while saving your change'
				break
			}
			break
		} // while (true)

		if (errorMsg) {
			[error:errorMsg]
		} else {
			map
		}
	}

	/**
	 * Internal method used to determine if any of the Task comments were modified that would trigger a notification
	 * NOTE: This method should be called before the task is actual saved as it checks for dirty properties which get
	 * wiped out on the save.
	 * @param task - the task to check if a notification is warranted
	 * @param triggeredByWhom - the person that created/modified the task
	 * @param isNew - a flag that indicates if the task was just created
	 * @return true if a notification should be sent
	 */
	boolean shouldSendNotification(AssetComment task, Person triggeredByWhom, boolean isNew, boolean addingNote, boolean ignoreDirty=false) {
		boolean shouldNotify = (
			task.commentType == AssetCommentType.TASK
			&& task.sendNotification
			&& task.isPublished
			&& task.status in [AssetCommentStatus.READY, AssetCommentStatus.STARTED]
			&& task.assignedTo && task.assignedTo.id != triggeredByWhom.id
		)

		// Now refine if the task should be sent based on it being new or updated
		if (shouldNotify) {
			if (isNew) { //IS NEW? don't send if was AUTOGENERATED
				// Only send notices for tasks if they are manually created
				shouldNotify = !task.autoGenerated
			} else {
				if (!addingNote && !ignoreDirty) { //NOT ADDING NOTES? Only send if any of the watched properties was modified
					List<String> dirtyPropNames = task.dirtyPropertyNames
					// log.debug "shouldSendNotification() the dirty properties include $dirtyPropNames"
					shouldNotify = false
					for (name in dirtyPropNames) {
						if (watchProps.contains(name)) {
							shouldNotify = true
							break
						}
					}
				}
			}
		}
		//log.info "shouldSendNotification() returns $shouldNotify"
		return shouldNotify
	}

	/**
	 *  This method is responsible for creating the Quartz job for sending Emails for comments
	 * @param : paramsMap ["taskId":taskId, "tzId":tzId, "isNew":isNew]
	 * @return : create Trigger
	 */
	@Transactional
	def dispatchTaskEmail(Map params) {
		Trigger trigger = new SimpleTriggerImpl('tm-sendEmail-' + params.taskId + System.currentTimeMillis(),
			null, new Date(System.currentTimeMillis() + 5000) )
		trigger.jobDataMap.putAll(taskId: params.taskId, tzId: params.tzId, userDTFormat: params.userDTFormat,
		                          isNew: params.isNew, tries: 0)
		trigger.setJobName('SendTaskEmailJob')
		trigger.setJobGroup('tdstm')

		quartzScheduler.scheduleJob(trigger)
		log.info "dispatchTaskEmail $params"
	}

	/**
	 * Used to send the Task email to the appropriate user for the comment passed to the method. This is typically called from
	 * a Quartz Job so that it happens outside of the User controller request handler for performance reasons.
	 */
	@Transactional
	def sendTaskEMail(taskId, String tzId, String userDTFormat, boolean isNew = true) {
		// Perform this withNewSession because it runs in a separate thread and the session would otherwise be lost
		// TODO re-enable the withNewSession after upgrade to 2.x as there is a bug in 1.3 that we ran into
		// https://github.com/grails/grails-core/commit/9a8e765e4a139f67bb150b6dd9f7e67b16ecb21e
		// AssetComment.withNewSession { session ->
		def assetComment = AssetComment.read(taskId)
		if (!assetComment) {
			log.error "sendTaskEMail: Invalid AssetComment ID [$taskId] referenced in call"
			return "reschedule"
		}

		// log.info "sendTaskEMail: commentType: $assetComment.commentType, category: $assetComment.category"

		// Only send emails out for issues in the categories up to premove
		if (assetComment.commentType != AssetCommentType.TASK ) {
			return
		}

		// Must have an email address
		def assignedTo = assetComment.assignedTo
		if (!assignedTo?.email) {
			log.warn "sendTaskEMail: No valid email address for assigned individual"
			return
		}

		// Truncate long comments to make manageable subject line
		// TODO : Use Apache commons StringUtil and get rid of this function
		String sub = leftString(getLine(assetComment.comment,0), 40)
		sub = (isNew ? '' : 'Re: ') + ( (sub == null || sub.size() == 0) ? 'Task ' + assetComment.id : (assetComment.taskNumber ? assetComment.taskNumber+":"+sub : sub) )

		log.info "sendTaskEMail: taskId=$taskId to=$assignedTo.id/$assignedTo.email"

		if (assignedTo?.userLogin) {
			String dateFormat = userPreferenceService.getPreference(assignedTo?.userLogin, PREF.CURR_DT_FORMAT)
			if (dateFormat) {
				userDTFormat = dateFormat
			}
		}

		try{
			mailService.sendMail {
				to assignedTo.email
				subject sub
				body(view: "/assetEntity/_taskEMailTemplate",
					 model: assetCommentModel(assetComment, tzId, userDTFormat))
			}
		}catch(e){
			log.warn "problem sending email: ${e.getMessage()}"
			return "reschedule"
		}
	}

	/**
	 * Returns a map of variables for the AssetComment and notes
	 * @param assetComment - the assetComment object to create a model of
	 * @return map
	 */
	def assetCommentModel(assetComment, tzId, userDTFormat) {
		def notes = assetComment.notes?.sort { it.dateCreated }
		def assetName = assetComment.assetEntity ? "$assetComment.assetEntity.assetName ($assetComment.assetEntity.assetType)" : null
		def createdBy = assetComment.createdBy
		def resolvedBy = assetComment.resolvedBy
		def dtCreated
		def dueDate
		def dtResolved

		dtCreated = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dateCreated, TimeUtil.FORMAT_DATE_TIME)

		if (assetComment.dateResolved) {
			dtResolved = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dateResolved, TimeUtil.FORMAT_DATE_TIME)
		}
		if (assetComment.dueDate) {
			dueDate = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dueDate, TimeUtil.FORMAT_DATE)
		}

		[assetComment: assetComment, assetName: assetName, moveEvent: assetComment.moveEvent, createdBy: createdBy,
		 dtCreated: dtCreated, dtResolved: dtResolved, dueDate: dueDate, resolvedBy: resolvedBy,
		 assignedTo: assetComment.assignedTo, dateFormat: userDTFormat, notes: notes, projectName: assetComment?.project?.projectCode]
	}

	// TODO : move the leftString and getLine methods into a reusable class - perhaps extending string with @Delegate

	/**
	 * Returns the left of a string to an optional length limit
	 * @param str - string to return
	 * @param len - optional length of string to return
	 * @return String
	 */
	def leftString(str, len = null) {
		if (str == null) return null
		def size = str.size()
		size = (len != null && size > len) ? len : size
		size = size == 0 ? 1 : size
		return str[0..(size - 1)]
	}

	/**
	 * Returns a specified line within a string and null if line number does not exist, defaulting to the first if no
	 * @param str - string to act upon
	 * @param lineNum - line number to return starting with zero, default of 0
	 * @return the line
	 */
	String getLine(String str, int lineNum = 0) {
		List lines = str.readLines()
		lineNum > lines.size() - 1 ? null : lines[lineNum]
	}

	@Transactional
	def saveAndUpdateTaskDependency(def task, def dependent, def taskDepId, def depId) {
        String errorMsg = ''
		/* This method is called from different places. Some send the taskDepId as a long,
			while others send a string. Therefore, it's best to ensure the value is actually
			a long. */
		taskDepId = NumberUtil.toLong(taskDepId)
		def taskDependency = TaskDependency.get(taskDepId)
		if (taskDependency && taskDependency.predecessor.id != Long.parseLong(depId)) {
			taskDependency.predecessor = dependent
			taskDependency.assetComment = task
        } else if( !taskDependency ) {
            taskDependency = new TaskDependency(predecessor: dependent, assetComment: task)
		}
		if (taskDependency.hasErrors() || !taskDependency.save(flush: true)) {
            log.error "saveUpdateCommentAndNotes: Saving comment successor failed - ${GormUtil.allErrorsString(taskDependency)}"
			errorMsg = 'An unexpected error occurred while saving your change'
		}
		return errorMsg
	}

	def showOrEditTask(Map params){
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved
		Project project = securityService.userCurrentProject
		def assetComment = AssetComment.get(params.id)
		if (assetComment) {
			if (assetComment.createdBy) {
				personCreateObj = assetComment.createdBy?.toString()
				dtCreated = TimeUtil.formatDateTime(assetComment.dateCreated)
			}
			if (assetComment.dateResolved) {
				personResolvedObj = assetComment.resolvedBy?.toString()
				dtResolved = TimeUtil.formatDateTime(assetComment.dateResolved)
			}
			def etStart =  TimeUtil.formatDateTime(assetComment.estStart)
			def etFinish = TimeUtil.formatDateTime(assetComment.estFinish)
			def atStart = TimeUtil.formatDateTime(assetComment.actStart)
			def dueDate = TimeUtil.formatDate(assetComment.dueDate)

			def workflowTransition = assetComment?.workflowTransition
			def workflow = workflowTransition?.name

			def notes = []
			assetComment.notes.sort { it.dateCreated }.each {
				def dateCreated = TimeUtil.formatDateTime(it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3)
				notes << [dateCreated, it.createdBy.toString(), it.note]
			}

			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)
			StringBuilder predecessorTable
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies) {
				taskDependencies = taskDependencies.sort { it.predecessor.taskNumber }
				predecessorTable = new StringBuilder('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length() > 50 ? task.comment.substring(0, 50) : task.comment
					predecessorTable.append("""<tr class="$css"><td>$task.category</td><td>${task.taskNumber ? task.taskNumber+':' :''}$taskDesc</td></tr>""")
				}
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor(assetComment)
			def successorsCount = taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			StringBuilder successorTable
			if (taskSuccessors) {
				taskSuccessors = taskSuccessors.sort { it.assetComment.taskNumber }
				successorTable = new StringBuilder('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
					successorTable.append("""<tr class="$css" ><td>$task.category</td><td>$task</td>""")
				}
				successorTable.append("""</tbody></table>""")
			}

			def cssForCommentStatus = taskService.getCssClassForStatus(assetComment.status)
			def entities = assetEntityService.entityInfo(project)
			def moveBundleList = MoveBundle.findAllByProject(project, [sort: 'name'])
			// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			commentList = [
				assetComment: assetComment, personCreateObj: personCreateObj, personResolvedObj: personResolvedObj,
				dtCreated: dtCreated ?: '', dtResolved: dtResolved ?: '', assignedTo: assetComment.assignedTo?.toString() ?: '',
				assetName: assetComment.assetEntity?.assetName ?: '', eventName: assetComment.moveEvent?.name ?: '',
				dueDate: dueDate, etStart: etStart, etFinish: etFinish, atStart: atStart, notes: notes, workflow: workflow,
				roles: roles, predecessorTable: predecessorTable ?: '', successorTable: successorTable ?: '',
				cssForCommentStatus: cssForCommentStatus, statusWarn: taskService.canChangeStatus(assetComment) ? 0: 1,
				successorsCount: successorsCount, predecessorsCount: predecessorsCount, applications: entities.applications,
				assetId: assetComment.assetEntity?.id ?: '', assetType: assetComment.assetEntity?.assetType,
				staffRoles: taskService.getRolesForStaff(), networks: entities.networks, moveBundleList: moveBundleList,
				dbs: entities.dbs, files: entities.files, assetDependency: new AssetDependency(), servers: entities.servers,
				dependencyType: entities.dependencyType, dependencyStatus: entities.dependencyStatus]
		}
		else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "showComment: show comment view - " + errorMsg
			commentList = [error: errorMsg]
		}
		return commentList
	}

	Map getTaskCreateDefaults() {
		return TASK_CREATE_DEFAULTS.collectEntries { PREF pref, ref ->
			if(ref instanceof PREF){
				ref = userPreferenceService.getPreference(ref)
			}
			String defaultVal = String.valueOf(ref)

			[(pref.name()): userPreferenceService.getPreference(null, pref, defaultVal)]
		}
	}

	/**
	 *
	 *  Finds All Comments by an assetEntity.
	 *
	 *  A Comment instance is an AssetComment instance with AssetComment#commentType equals to AssetCommentType.COMMENT
	 *
	 * @param assetEntity
	 */
	def findAllByAssetEntity(def assetEntity) {
		AssetComment.findAllByAssetEntityAndCommentType(assetEntity, AssetCommentType.COMMENT)
	}


	/**
	 * Delete an AssetComment based on its id and the user's project.
	 * @param project
	 * @param assetCommentId
	 */
	@Transactional
	void deleteComment(Project project, Long assetCommentId) {
		// Fetch the asset comment
		AssetComment comment = GormUtil.findInProject(project, AssetComment, assetCommentId, true)
		// Delete Task Dependencies.
		TaskDependency.where {
			assetComment == comment || predecessor == comment
		}.deleteAll()
		// Delete the comment.
		comment.delete()
	}

	/**
	 * Create or Update an AssetComment
	 * @param project
	 * @param command
	 */
	void saveOrUpdateAssetComment(Project project, AssetCommentSaveUpdateCommand command) {

		// Fetch the corresponding asset.
		AssetEntity asset = GormUtil.findInProject(project, AssetEntity, command.assetEntityId, true)

		// Fetch the person updating/creating the comment.
		Person currentPerson = securityService.loadCurrentPerson()

		AssetComment assetComment

		if (!command.id) {
			assetComment = new AssetComment(project: project)
			assetComment.createdBy = currentPerson
		} else {
			assetComment = GormUtil.findInProject(project, AssetComment, command.id, true)
		}
		assetComment.with {
			assetEntity = asset
			if(!isResolved() && command.isResolved) {
				dateResolved = TimeUtil.nowGMT()
				resolvedBy = currentPerson
			} else if (!command.isResolved) {
				dateResolved = null
			}
			commentType = AssetCommentType.COMMENT
			comment = command.comment
			category = command.category
			if (command.status) {
				status = command.status
			} else{
				status = 'Ready'
			}
		}

		assetComment.save(failOnError: true)
	}


	/**
	 * Find tasks based on the params received sorting and limiting results accordingly.
	 * @param project - user's project
	 * @param params - params as provided by the front-end.
	 * @param sortIndex - column to sort on.
	 * @param sortOrder - asc or desc
	 * @param maxRows - max number of records to be fetched.
	 * @param rowOffset - used for paginating results.
	 * @return A map with the tasks found plus the total number of tasks.
	 */
	Map filterTasks(Project project, Map params, boolean viewUnpublished, String sortIndex, String sortOrder, Integer maxRows, Integer rowOffset) {

		List<AssetComment> results = []
		Integer totalCount = 0
		AssetCommentQueryBuilder queryBuilder = new AssetCommentQueryBuilder(project, params, sortIndex, sortOrder, viewUnpublished)
		Map queryInfo = queryBuilder.buildQueries()
		if (!queryInfo.invalidCriterion) {
			Map metaParams = [max: maxRows, offset: rowOffset, readOnly: true]
			results = AssetComment.executeQuery(queryInfo['query'], queryInfo['queryParams'], metaParams)
			totalCount = AssetComment.executeQuery(queryInfo.countQuery, queryInfo.queryParams)[0]
		}

		return [
			tasks: results,
			totalCount: totalCount
		]

	}
}
