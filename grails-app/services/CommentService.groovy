import com.tds.asset.*
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
import org.codehaus.groovy.grails.web.util.WebUtils
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import UserPreferenceEnum as PREF

/**
 * CommentService class contains methods used to manage comments/tasks
 * @author jmartin
 *
 */
class CommentService {
	
	boolean transactional = true
	BindDynamicMethod bindData = new BindDynamicMethod()

	def mailService					// SendMail MailService class
	def jdbcTemplate
	def partyRelationshipService
	def quartzScheduler
	def securityService
	def taskService
	def userPreferenceService
	def assetEntityService
	def sequenceService

	// TODO : This should use an array defined in AssetCommentCategory instead as that is where people will add new statuses
	private final List<String> statusToSendEmailFor = [
		AssetCommentCategory.GENERAL,
		AssetCommentCategory.DISCOVERY,
		AssetCommentCategory.PLANNING,
		AssetCommentCategory.WALKTHRU,
		AssetCommentCategory.PREMOVE,
		AssetCommentCategory.POSTMOVE
	]
    
	/**
	 * Used to persist changes to the AssetComment and CommentNote from various forms for both Tasks and Comments
	 * @param session - the user session
	 * @param params - the request params
	 * @param isNew - a flag to indicate of the request was for a new (true) or existing (false)
	 * @param flash - the controller flash message object to stuff messages into (YUK!!!)
	 * @return map of the AssetComment data used to refresh the view
	 */
	def saveUpdateCommentAndNotes(session, params, isNew=true, flash) {
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		def canEditAsset = securityService.hasPermission(userLogin.person, 'AssetEdit')
		
		def tzId = session.getAttribute( TimeUtil.TIMEZONE_ATTR )?.CURR_TZ
		def userDTFormat = session.getAttribute( TimeUtil.DATE_TIME_FORMAT_ATTR )?.CURR_DT_FORMAT
		
		def date = new Date()
		def assetEntity
		def assetComment
		def commentProject
		def map=[:]
		def errorMsg
		
		// log.info "saveUpdateCommentAndNotes: tzId -- ${session.getAttribute( "CURR_TZ" )} / ${session.getAttribute( "CURR_TZ" )?.CURR_TZ}"
		
		// Wrap the whole routine so that we can break out when we hit fatal situations
		while (true) {
			if (! project) {
				log.warn "saveUpdateCommentAndNotes: User has no currently selected project"
				errorMsg = "You have no currently selected project"
				break
			}

			// if assetEntity is passed, then validate that it valid and that the user has access to it (belongs to the current project)
			if ( params.assetEntity && params.assetEntity != 'NaN') {
				if (! params.assetEntity.isNumber() && params.assetEntity!='null') {
					log.warn "saveUpdateCommentAndNotes: Invalid asset id (${params.assetEntity}"
					errorMsg = "An unexpected asset id was received"
					break
				}
				// Now see if it exists and belongs to the project
				if(params.containsKey("assetEntity")){
					assetEntity = params.assetEntity !='null' ? AssetEntity.read(params.assetEntity) : null
				}
				
				if (assetEntity) {
					def assetProject = assetEntity.project
					if (assetProject.id != project.id) {
						log.error "saveUpdateCommentAndNotes: Asset(${assetEntity.id}/${assetProject}) not associated with user(${userLogin}) project (${project})"
						errorMsg = "It appears that you do not have permission to view the specified task"
						break
					}
				}
			}

			// Create or load the assetComment object appropriately
			if (isNew) {
				// Only tasks can be created that are not associated with an Asset
				if ( ! assetEntity && params.commentType != AssetCommentType.TASK ) {
					log.error "saveUpdateCommentAndNotes: Asset id was not properly supplied to add or update a comment"
					errorMsg = 'The asset id was missing in the request'
					break
				}

				// Check if the user can create comments
				if ((params.commentType != AssetCommentType.TASK) && (!canEditAsset)) {
					log.error "saveUpdateCommentAndNotes: User don't have permission to create comments"
					errorMsg = "User don't have permission to create comments"
					securityService.reportViolation("User don't have permission to create comments", userLogin)
					break
				}
			
				// Let's create a new Comment/Task
				assetComment = new AssetComment()
				assetComment.createdBy = userLogin.person
				assetComment.project = project
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
				if (! assetComment ) {
					// TODO : handle failure for invalid comment id
					log.error "saveUpdateCommentAndNotes: Specified comment [id:${params.id}] was not found"
					errorMsg = 'Was unable to find the task for the specified id'
					break
				}

				// Check if the user can edit comments
				if ((assetComment.commentType != AssetCommentType.TASK) && (!canEditAsset)) {
					log.error "saveUpdateCommentAndNotes: User don't have permission to edit comments"
					errorMsg = "User don't have permission to edit comments"
					securityService.reportViolation("User don't have permission to edit comments", userLogin)
					break
				}

				// If params.currentStatus is passed along, the form is expecting the status to be in this state so if it has changed 
				// then someone else beat the user to changing the status so we are going to stop and warn the user.
				if (params.currentStatus) {
					if (assetComment.status != params.currentStatus) {
						log.warn "saveUpdateCommentAndNotes() user ${userLogin} attempted to change task (${assetComment.id}) status but was already changed"
						// TODO - assignedTo may be changing at the same time, which is assigned below. Need to review this as it is a potential edge case.
						def whoDidIt = (assetComment.status == AssetCommentStatus.DONE) ? assetComment.resolvedBy : assetComment.assignedTo
						switch (assetComment.status) {
							case AssetCommentStatus.READY:
								// No need to error in this situation
								break
							case AssetCommentStatus.STARTED:
								// Check to see if this is an project admin because they can update a task for users
								def isPM = partyRelationshipService.staffHasFunction(project.id, userLogin.person.id, 'PROJ_MGR')

								log.debug "Task Already STARTED - isPM? $isPM, whoDidIt=$whoDidIt, person=${userLogin.person}"
								// We'll allow the same user to click Start and Done 
								if (whoDidIt != userLogin.person && ! isPM) {
									errorMsg = "The task was previously STARTED by ${whoDidIt}"
								}
								break
							case AssetCommentStatus.DONE:
								errorMsg = "The task was previously COMPLETED by ${whoDidIt}"
								break
							default:
								errorMsg = "The task status was changed to '${assetComment.status}'"
								break
						}
					}
				}

				if (errorMsg) {
					break
				}
			
				commentProject = assetComment.project
				if (params.containsKey("assetEntity")){
					assetComment.assetEntity = assetEntity
				}
				// Make sure that the comment about to be updated is associated to the user's current project
				if ( commentProject.id != project.id ) {
					log.error "saveUpdateCommentAndNotes: The comment (${params.id}/${commentProject.id}) is not associated with user's current project [${project.id}]"
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
			if (params.durationLocked != null)  assetComment.durationLocked = params.durationLocked
			if (params.commentType) assetComment.commentType = params.commentType
			if (params.comment) assetComment.comment = params.comment
			if (params.category) assetComment.category = params.category
			if (params.displayOption) assetComment.displayOption = params.displayOption
			if (params.attribute) assetComment.attribute = params.attribute
		    assetComment.resolution = params.resolution
			if (params.containsKey('estStart')) {
				// log.info "saveUpdateCommentAndNotes: estStart=[${params.estStart}]"
				assetComment.estStart = params.estStart ? TimeUtil.parseDateTime(session, params.estStart) : null
			}
			if (params.containsKey('estFinish')) {
				assetComment.estFinish = params.estFinish ? TimeUtil.parseDateTime(session, params.estFinish)  : null
			}
			if (params.containsKey('role')) {
				assetComment.role = params.role ?: ''
			}
			if (params.containsKey('instructionsLink')){
				assetComment.instructionsLink = params.instructionsLink ?: null
			}
			if (assetComment.commentType == 'comment' && params.isResolved?.isNumber()) {
				if (Integer.parseInt(params.isResolved) == 0) {
					assetComment.setDateResolved(null)
					assetComment.resolvedBy = null
				} else {
					assetComment.setDateResolved(TimeUtil.nowGMT())
					assetComment.resolvedBy = userLogin.person
				}
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
						log.warn "saveUpdateCommentAndNotes: Invalid workflowTransition id (${params.workflowTransition})"
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
			if (params.durationScale) {
				assetComment.durationScale = TimeScale.asEnum(params.durationScale.toUpperCase())		
				log.debug "saveUpdateCommentAndNotes - TimeScale=${assetComment.durationScale}"
			}
			
			// Issues (aka tasks) have a number of additional properties to be managed 
			if ( assetComment.commentType == AssetCommentType.TASK ) {
				if ( params.containsKey('moveEvent') ) {
					if ( params.moveEvent.isNumber() ) {
					 	if (params.moveEvent == "0" ) {
							assetComment.moveEvent = null
						} else {
							def moveEvent = MoveEvent.get(params.moveEvent)
							if (moveEvent) {
								// Validate that this is a legit moveEvent for this project
								if (moveEvent.project.id != project.id) {
									// TODO: handle failure of moveEvent not being in project
									log.error "saveUpdateCommentAndNotes: moveEvent.project (${moveEvent.id}) does not match user's current project (${project.id})"
									errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
									break
								}
								assetComment.moveEvent = moveEvent
							} else {
								log.error "saveUpdateCommentAndNotes: Specified moveEvent (${moveEvent.id}) was not found})"
								errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
								break
							}
						}
					} else if (params.moveEvent == '') {
						assetComment.moveEvent = null
					}
				}
			
				if (params.containsKey('assignedTo')) {
					if (params.assignedTo && params.assignedTo.isNumber()){
						if(params.assignedTo == '0'){ // if unassigned is selected .
							assetComment.assignedTo = null
						} else {
							def person = Person.read(params.assignedTo)
							if (person) {
								def projectStaff = partyRelationshipService.getProjectStaff( project.id )
								if (projectStaff.find { "${it.staff.id}" == params.assignedTo }) {
									assetComment.assignedTo = person
								} else {
									log.error "User (${userLogin}) attempted to assign unrelated person (${params.assignedTo}) to project (${project})"
								}
							}
						}
					}
				}

				if (params.containsKey('dueDate') ) { 
					// log.info "saveUpdateCommentAndNotes: dueDate=[${params.dueDate}]"
					assetComment.dueDate = params.dueDate ? TimeUtil.parseDate(session, params.dueDate)  : null
				}
	
			}

			// Use the service to update the Status because it does a number of things that we don't need to duplicate. This 
			// should be the last update to Task properties before saving.	
			taskService.setTaskStatus( assetComment, params.status )		
        
			// Only send email if the originator of the change is not the assignedTo as one doesn't need email to one's self.
			Person byWhom = userLogin.person	// load so that we don't have a lazyInit issue
			boolean addingNote = assetComment.commentType == AssetCommentType.TASK && params.note?.size() > 0

			// Note that shouldSendNotification has to be called before calling save on the object
			boolean shouldSendNotification = shouldSendNotification(assetComment, byWhom, isNew, addingNote) 

			if (! assetComment.hasErrors() && assetComment.save(flush:true)) {
				// Deal with Notes if there are any
				if (assetComment.commentType == AssetCommentType.TASK && params.note){
					// TODO The adding of commentNote should be a method on the AssetComment instead of reverse injections plus the save above can handle both. Right now if this fails, everything keeps on as though it didn't which is wrong.
					def commentNote = new CommentNote();
					commentNote.createdBy = userLogin.person
					commentNote.dateCreated = date
					commentNote.note = params.note
					commentNote.isAudit=0
					// assetComment.addToNotes(this)
					commentNote.assetComment = assetComment
					if ( commentNote.hasErrors() || ! commentNote.save(flush:true)){
						// TODO error won't bubble up to the user
						log.error "saveUpdateCommentAndNotes: Saving comment notes faild - " + GormUtil.allErrorsString(commentNote)
						errorMsg = 'An unexpected error occurred while saving your comment'
						break
					}
				}
			
				// Now handle creating / updating task dependencies if the "manageDependency flag was passed
				if (params.manageDependency) {
					def taskDependencies = params.list('taskDependency[]')
				    def taskSuccessors = params.list('taskSuccessor[]')
					def deletedPreds = params.deletedPreds
					
					// If we're updating, we'll delete the existing dependencies and then readd them following
					if (! isNew && deletedPreds) {
						TaskDependency.executeUpdate("DELETE TaskDependency t WHERE t.id in ( ${deletedPreds} ) ")
					}
					// Iterate over the predecessor ids and validate that the exist and are associated with the project
					taskDependencies.each { preds ->
						def taskDepId = preds.split("_")[0]
						def predecessorId = preds.split("_")[1]
						def predecessor = AssetComment.get( predecessorId )
						if (! predecessor) {
							log.error "saveUpdateCommentAndNotes: invalid predecessor id (${predecessorId})"
						} else {
							def predProject = predecessor.project
							if ( predProject?.id != project.id ) {
								log.warn "saveUpdateCommentAndNotes: predecessor project id (${predProject?.id}) different from current project (${project.id})"
							} else {
                                errorMsg = saveAndUpdateTaskDependency( assetComment, predecessor, taskDepId, predecessorId )
							}
						}
					}
					taskSuccessors.each { succ ->
                        def taskSuccId = succ.split("_")[0]
                        def successorId = succ.split("_")[1]
                        def successor = AssetComment.get( successorId )
                        if (! successor) {
                            log.error "saveUpdateCommentAndNotes: invalid successor id (${successorId})"
                        } else {
                            def predProject = successor.project
                            if ( predProject?.id != project.id ) {
                                log.warn "saveUpdateCommentAndNotes: successor project id (${predProject?.id}) different from current project (${project.id})"
                            } else {
                                errorMsg = saveAndUpdateTaskDependency( successor, assetComment, taskSuccId, successorId )
                            }
                        }
					}
				}

				// TODO - comparison of the assetComment.dueDate may not work if the dueDate is stored in GMT
				def css =  (assetComment.dueDate < date ? 'Lightpink' : 'White')
				def status = (assetComment.commentType == AssetCommentType.TASK && assetComment.isResolved == 0)	

				map = [ 
					assetComment : assetComment,
					status : status ? true : false,
					cssClass:css,
					statusCss: taskService.getCssClassForStatus(assetComment.status ), 
					assignedToName: assetComment.assignedTo?(assetComment.assignedTo.firstName + " " + assetComment.assignedTo.lastName):"",
					lastUpdatedDate: TimeUtil.formatDateTime(session, assetComment.lastUpdated, TimeUtil.FORMAT_DATE_TIME_13) 
				]
				
				// Now refine if the task should be sent based on it being new or updated
				if (shouldSendNotification) {
					dispatchTaskEmail([taskId:assetComment.id, tzId:tzId, isNew:isNew, userDTFormat: userDTFormat])
				}
			
				break
				
			} else {
				log.error "saveUpdateCommentAndNotes: Saving comment changes - " + GormUtil.allErrorsString(assetComment)
				errorMsg = 'An unexpected error occurred while saving your change'
				break
			}
			break
		} // while (true)
		
		if (errorMsg) {
			return [error:errorMsg]
		} else {
			return map
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
	private boolean shouldSendNotification(AssetComment task, Person triggeredByWhom, boolean isNew, boolean addingNote) {
		boolean shouldNotify = ( 
			task.commentType == AssetCommentType.TASK && 
			task.sendNotification &&
			task.isPublished &&
			(task.status == AssetCommentStatus.READY || task.status == AssetCommentStatus.STARTED) &&
			task.assignedTo	&& 
			task.assignedTo.id != triggeredByWhom.id
		) 

		// Now refine if the task should be sent based on it being new or updated
		if (shouldNotify) {
			if (isNew) { //IS NEW? don't send if was AUTOGENERATED
				// Only send notices for tasks if they are manually created
				shouldNotify = ! task.autoGenerated
			} else {
				if (! addingNote) { //NOT ADDING NOTES? Only send if any of the watched properties was modified
					List watchProps = ['actStart', 'assetEntity', 'assignedTo', 'comment', 'dueDate', 'estFinish', 'estStart', 'moveEvent', 'priority', 'role', 'sendNotification', 'status']
					List dirtyPropNames = task.dirtyPropertyNames
					// log.debug "shouldSendNotification() the dirty properties include $dirtyPropNames"
					shouldNotify = false
					for(int i = 0; i < dirtyPropNames.size(); i++ ) {
						if (watchProps.contains(dirtyPropNames[i])) {
							shouldNotify = true
							break
						}
					}
				}
			}
		}
		//log.info "shouldSendNotification() returns $shouldNotify"
		return 	shouldNotify	
	}
	
    /**
     *  This method is responsible for creating the Quartz job for sending Emails for comments
     *  @param : paramsMap ["taskId":taskId, "tzId":tzId, "isNew":isNew]
     *  @return : create Trigger
     */
    def dispatchTaskEmail(Map params) {
		Trigger trigger = new SimpleTriggerImpl("tm-sendEmail-${params.taskId}" + System.currentTimeMillis(), null, new Date(System.currentTimeMillis() + 5000) )
        trigger.jobDataMap.putAll( [ 'taskId':params.taskId, 'tzId':params.tzId, 'userDTFormat':params.userDTFormat, 'isNew':params.isNew,'tries':0L])
		trigger.setJobName('SendTaskEmailJob')
		trigger.setJobGroup('tdstm')
  
		quartzScheduler.scheduleJob(trigger)
		log.info "dispatchTaskEmail ${params}"
        
    }
	
	/**
	 * Used to send the Task email to the appropriate user for the comment passed to the method. This is typically called from
	 * a Quartz Job so that it happens outside of the User controller request handler for performance reasons.
	 * 
	 * @param assetComment
	 * @param tzId
	 * @return
	 */
	def sendTaskEMail(taskId, tzId, userDTFormat, isNew=true) {
		// Perform this withNewSession because it runs in a separate thread and the session would otherwise be lost
		// TODO re-enable the withNewSession after upgrade to 2.x as there is a bug in 1.3 that we ran into
		// https://github.com/grails/grails-core/commit/9a8e765e4a139f67bb150b6dd9f7e67b16ecb21e
		// AssetComment.withNewSession { session ->
		def assetComment = AssetComment.read(taskId)
		if (!assetComment) {
			log.error "sendTaskEMail: Invalid AssetComment ID [${taskId}] referenced in call"
			return "reschedule"
		}
		
		// log.info "sendTaskEMail: commentType: ${assetComment.commentType}, category: ${assetComment.category}"
			
		// Only send emails out for issues in the categories up to premove
		if ( assetComment.commentType != AssetCommentType.TASK || ! statusToSendEmailFor.contains(assetComment.category) ) {
			return
		}

		def assignedTo = assetComment.assignedTo
		
		// Must have an email address
		if ( ! assignedTo?.email) {
			log.warn "sendTaskEMail: No valid email address for assigned individual"
			return
		}
		
		// Truncate long comments to make manageable subject line
		// TODO : Use Apache commons StringUtil and get rid of this function
		def sub = leftString(getLine(assetComment.comment,0), 40)
		sub = (isNew ? '' : 'Re: ') + ( (sub == null || sub.size() == 0) ? "Task ${assetComment.id}" : (assetComment.taskNumber ? assetComment.taskNumber+":"+sub : sub) )
		
		log.info "sendTaskEMail: taskId=${taskId} to=${assignedTo.id}/${assignedTo.email}"
		
		if(assignedTo?.userLogin){
			def dateFormat = userPreferenceService.getPreferenceByUserAndCode(assignedTo?.userLogin, PREF.CURR_DT_FORMAT)
			if(dateFormat){
				userDTFormat = dateFormat
			}
		}
		
		mailService.sendMail {
			to assignedTo.email
			subject "${sub}"
			body (
				view:"/assetEntity/_taskEMailTemplate",
				model: assetCommentModel(assetComment, tzId, userDTFormat)
			)
		}

	}
	
	/**
	 * Returns a map of variables for the AssetComment and notes
	 * @param assetComment - the assetComment object to create a model of
	 * @return map
	 */
	def assetCommentModel(assetComment, tzId, userDTFormat) {
		def notes = assetComment.notes?.sort{it.dateCreated}
		def assetName = assetComment.assetEntity ? "${assetComment.assetEntity.assetName} (${assetComment.assetEntity.assetType})" : null
		def createdBy = assetComment.createdBy
		def resolvedBy = assetComment.resolvedBy
		def dtCreated 
		def dueDate
		def dtResolved
		
		dtCreated = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dateCreated, TimeUtil.FORMAT_DATE_TIME)
		
		if(assetComment.dateResolved) {
			dtResolved = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dateResolved, TimeUtil.FORMAT_DATE_TIME)
		}
		if(assetComment.dueDate){
			dueDate = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, assetComment.dueDate, TimeUtil.FORMAT_DATE)
		}
		
		[	assetComment:assetComment,
			assetName:assetName,
			moveEvent:assetComment.moveEvent,
			createdBy:createdBy, dtCreated:dtCreated, dtResolved:dtResolved, dueDate:dueDate,
			resolvedBy:resolvedBy, assignedTo:assetComment.assignedTo,
			dateFormat: userDTFormat,
			notes:notes ]
	}
	
	// TODO : move the leftString and getLine methods into a reusable class - perhaps extending string with @Delegate
	
	/**
	 * Returns the left of a string to an optional length limit
	 * @param str - string to return
	 * @param len - optional length of string to return
	 * @return String
	 */
	def leftString(str, len=null) {
		if (str == null) return null
		def size = str.size()
		size = (len != null && size > len) ? len : size
		size = size==0 ? 1 : size
		return str[0..(size-1)]
	}

	/**
	 * Returns a specified line within a string and null if line number does not exist, defaulting to the first if no
	 * @param str - string to act upon
	 * @param lineNum - line number to return starting with zero, default of 0
	 * @return String
	 */
	def getLine(str, lineNum=0) {
		ArrayList lines = str.readLines()
		return ( (lineNum+1) > lines.size() ) ? null : lines[lineNum]
	}
    /**
     * 
     * @param task, assetComment
     * @param dependent, predecessor 
     * @param taskDepId, existing dependency Id
     * @param depId, successor/predecessor
     * @return validate message
     */
    def saveAndUpdateTaskDependency( def task, def dependent, def taskDepId, def depId ){
        def errorMsg = ""
        def taskDependency = TaskDependency.get(taskDepId)
        if ( taskDependency && taskDependency.predecessor.id != Long.parseLong( depId )  ) {
            taskDependency.predecessor = dependent
            taskDependency.assetComment = task
        } else if( !taskDependency ) {
            taskDependency = new TaskDependency()
            taskDependency.predecessor = dependent
            taskDependency.assetComment = task
        }
        if ( taskDependency.hasErrors() || ! taskDependency.save(flush:true) ) {
            log.error "saveUpdateCommentAndNotes: Saving comment successor failed - " + GormUtil.allErrorsString(taskDependency)
            errorMsg = 'An unexpected error occurred while saving your change'
        }
        return errorMsg
    }
	
	def showOrEditTask(params){
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved
		def project = securityService.getUserCurrentProject()
		def session = WebUtils.retrieveGrailsWebRequest().session
		def assetComment = AssetComment.get(params.id)
		if(assetComment){
			if(assetComment.createdBy){
				personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")?.toString()
				dtCreated = TimeUtil.formatDateTime(session, assetComment.dateCreated)
			}
			if(assetComment.dateResolved){
				personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")?.toString()
				dtResolved = TimeUtil.formatDateTime(session, assetComment.dateResolved)
			}
			
			def etStart =  assetComment.estStart ? TimeUtil.formatDateTime(session, assetComment.estStart) : ''
			
			def etFinish = assetComment.estFinish ? TimeUtil.formatDateTime(session, assetComment.estFinish) : ''
			
			def atStart = assetComment.actStart ? TimeUtil.formatDateTime(session, assetComment.actStart) : ''
			
			def dueDate = assetComment.dueDate ? TimeUtil.formatDate(session, assetComment.dueDate) : ''
	
			def workflowTransition = assetComment?.workflowTransition
			def workflow = workflowTransition?.name
			
			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				def dateCreated = it.dateCreated ? TimeUtil.formatDateTime(session, it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3) : ''
				notes << [ dateCreated , it.createdBy.toString() ,it.note]
			}
			
			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)
			def predecessorTable = ""
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies.size() > 0) {
				taskDependencies = taskDependencies.sort{ it.predecessor.taskNumber }
				predecessorTable = new StringBuffer('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each() { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					predecessorTable.append("""<tr class="${css}"><td>${task.category}</td><td>${task.taskNumber ? task.taskNumber+':' :''}${taskDesc}</td></tr>""")
			    }
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor( assetComment )
			def successorsCount= taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			def successorTable = ""
			if (taskSuccessors.size() > 0) {
				taskSuccessors = taskSuccessors.sort{ it.assetComment.taskNumber }
				successorTable = new StringBuffer('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each() { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
					successorTable.append("""<tr class="${css}" ><td>${task.category}</td><td>${task}</td>""")
				}
				successorTable.append("""</tbody></table>""")
			
			}
			def cssForCommentStatus = taskService.getCssClassForStatus(assetComment.status)
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			commentList = [ 
				assetComment:assetComment, personCreateObj:personCreateObj, personResolvedObj:personResolvedObj, dtCreated:dtCreated ?: "",
				dtResolved:dtResolved ?: "", assignedTo:assetComment.assignedTo?.toString() ?:'', assetName:assetComment.assetEntity?.assetName ?: "",
				eventName:assetComment.moveEvent?.name ?: "", dueDate:dueDate, etStart:etStart, etFinish:etFinish,atStart:atStart,notes:notes,
				workflow:workflow,roles:roles, predecessorTable:predecessorTable, successorTable:successorTable,
				cssForCommentStatus:cssForCommentStatus, statusWarn:taskService.canChangeStatus ( assetComment ) ? 0 : 1, 
				successorsCount:successorsCount, predecessorsCount:predecessorsCount, assetId:assetComment.assetEntity?.id ?: "" ,
				assetType:assetComment.assetEntity?.assetType ,staffRoles:taskService.getRolesForStaff(), servers : entities.servers, 
				applications : entities.applications, dbs : entities.dbs, files : entities.files,  networks :entities.networks,moveBundleList:moveBundleList,
				assetDependency : new AssetDependency(), dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus]
		}else{
		 def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - ${params.id} "
		 log.error "showComment: show comment view - "+errorMsg
		 commentList = [error:errorMsg]
		}
		return commentList
	}
	
}