import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import org.jsecurity.SecurityUtils
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil

import com.tds.asset.*
import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus

import org.quartz.SimpleTrigger
import org.quartz.Trigger

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
	def taskService
	def securityService
	def quartzScheduler
    
	/**
	 * Used to persist changes to the AssetComment and CommentNote from various forms
	 * @param params 
	 * @isNew - boolean flag that indicates if it is new or an update
	 * @return map of the AssetComment data used to refresh the view
	 */
	def saveUpdateCommentAndNotes(session, params, isNew = true,flash) {
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		
		def formatter = new SimpleDateFormat("MM/dd/yyyy");
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		
		def date = new Date()
		def assetEntity
		def assetComment
		def commentProject
		def map=[:]
		def errorMsg
		
		log.info "saveUpdateCommentAndNotes: tzId -- ${session.getAttribute( "CURR_TZ" )} / ${session.getAttribute( "CURR_TZ" )?.CURR_TZ}"
		
		// Wrap the whole routine so that we can break out when we hit fatal situations
		while (true) {
			if (! project) {
				log.error "saveUpdateCommentAndNotes: User has no currently selected project"
				errorMsg = "You have no currently selected project"
				break
			}

			// if assetEntity is passed, then validate that it valid and that the user has access to it (belongs to the current project)
			if ( params.assetEntity ) {
				if (! params.assetEntity.isNumber() ) {
					log.error "saveUpdateCommentAndNotes: Invalid asset id (${params.assetEntity}"
					errorMsg = "An unexpected asset id was received"
					break
				}
				// Now see if it exists and belongs to the project
				assetEntity = AssetEntity.get(params.assetEntity)
				if (assetEntity) {
					def assetProject = assetEntity.project
					if (assetProject.id != project.id) {
						log.error "saveUpdateCommentAndNotes: Asset(${assetEntity.id}/${assetProject}) not associated with user(${userLogin}) project (${project})"
						errorMsg = "It appears that you do not have permission to view the specified task"
						break
					}
				} else {
					// TODO : handle failure for missing Asset
					log.error "saveUpdateCommentAndNotes: Specified asset [id:${params.assetEntity}] was not found while creating comment"
					errorMsg = "An invalid asset id was specified"
					break
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
			
				// Let's create a new Comment/Task
				assetComment = new AssetComment()
				assetComment.createdBy = userLogin.person
				assetComment.project = project
				def lastTask = jdbcTemplate.queryForInt("SELECT MAX(task_number) FROM asset_comment WHERE project_id = ${project.id}")
				assetComment.taskNumber = lastTask + 1
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
			
				// If params.currentStatus is passed along, the form is expecting the status to be in this state so if it has changed 
				// then someone else beat the user to changing the status so we are going to stop and warn the user.
				if (params.currentStatus) {
					if (assetComment.status != params.currentStatus) {
						log.warn "saveUpdateCommentAndNotes() user ${userLogin} attempted to change task (${assetComment.id}) status but was already changed"
						def whoDidIt = (assetComment.status == AssetCommentStatus.DONE) ? assetComment.resolvedBy : assetComment.assignedTo
						switch (assetComment.status) {
							case AssetCommentStatus.STARTED:
								errorMsg = "The task was STARTED by ${whoDidIt}"; break
							case AssetCommentStatus.DONE:
								errorMsg = "The task was COMPLETED by ${whoDidIt}"; break
							default:
								errorMsg = "The task status was changed to '${assetComment.status}'"
						}
						break
					}
				}
			
				commentProject = assetComment.project
			
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
			if (params.commentType) assetComment.commentType = params.commentType
			if (params.comment) assetComment.comment = params.comment
			if (params.category) assetComment.category = params.category
			if (params.displayOption) assetComment.displayOption = params.displayOption
			if (params.attribute) assetComment.attribute = params.attribute
		    assetComment.resolution = params.resolution
			if (params.containsKey('estStart')) {
				// log.info "saveUpdateCommentAndNotes: estStart=[${params.estStart}]"
				assetComment.estStart = params.estStart ? TimeUtil.convertInToGMT(estformatter.parse(params.estStart), tzId) : null
			}
			if (params.containsKey('estFinish')) {
				assetComment.estFinish = params.estFinish ? TimeUtil.convertInToGMT(estformatter.parse(params.estFinish), tzId)  : null
			}
			if (params.containsKey('role')) {
				assetComment.role = params.role ?: null
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
			if (params.priority?.isNumber()) assetComment.priority = Integer.parseInt(params.priority)
			if (params.override?.isNumber()) assetComment.workflowOverride = Integer.parseInt(params.override)
			if (params.duration?.isNumber()) assetComment.duration = Integer.parseInt(params.duration)
			if (params.durationScale) assetComment.durationScale = params.durationScale		
		 
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
						// TODO - SECURITY - Need to validate that the assignedTo is a member of the project
						def subject = SecurityUtils.subject
						def person = Person.get(params.assignedTo)
						if (!isNew && subject.hasRole("PROJ_MGR")){
							assetComment.assignedTo = person
						}else if(isNew){
							assetComment.assignedTo = person
						}
					} else {
						assetComment.assignedTo = null
					}
				}

				if (params.containsKey('dueDate') ) { 
					// log.info "saveUpdateCommentAndNotes: dueDate=[${params.dueDate}]"
					assetComment.dueDate = params.dueDate ? formatter.parse(params.dueDate) : null
				}
	
			}

			// Use the service to update the Status because it does a number of things that we don't need to duplicate. This 
			// should be the last update to Task properties before saving.	
			taskService.setTaskStatus( assetComment, params.status )		
        
			if (! assetComment.hasErrors() && assetComment.save(flush:true)) {
			
				// Deal with Notes if there are any
				if (assetComment.commentType == AssetCommentType.TASK && params.note){
					// TODO The adding of commentNote should be a method on the AssetComment instead of reverse injections plus the save above can handle both. Right now if this fails, everything keeps on as though it didn't which is wrong.
					def commentNote = new CommentNote();
					commentNote.createdBy = userLogin.person
					commentNote.dateCreated = date
					commentNote.note = params.note
					// assetComment.addToNotes(this)
					commentNote.assetComment = assetComment
					if ( commentNote.hasErrors() || ! commentNote.save(flush:true)){
						// TODO error won't bubble up to the user
						log.error "saveUpdateCommentAndNotes: Saving comment notes faild - " + GormUtil.allErrorsString(commentNote)
						errorMsg = 'An unexpected error occurred while saving your change'
						break
					}
				}
			
				// Now handle creating / updating task dependencies if the "manageDependency flag was passed
				if (params.manageDependency) {
					def taskDependencies = params.list('taskDependency[]')
				
					// If we're updating, we'll delete the existing dependencies and then readd them following
					if (! isNew ) {
						TaskDependency.executeUpdate("DELETE TaskDependency WHERE assetComment = ? ",[assetComment])
					}
					// Iterate over the predecessor ids and validate that the exist and are associated with the project
					taskDependencies.each {
						def predecessor = AssetComment.get(it)
						if (! predecessor) {
							log.error "saveUpdateCommentAndNotes: invalid predecessor id (${it})"
						} else {
							def predProject = predecessor.project
							if ( predProject?.id != project.id ) {
								log.warn "saveUpdateCommentAndNotes: predecessor project id (${predProject?.id}) different from current project (${project.id})"
							} else {
								def taskDependency = new TaskDependency()
								taskDependency.predecessor = predecessor
								taskDependency.assetComment = assetComment
								if ( taskDependency.hasErrors() || ! taskDependency.save(flush:true) ) {
									log.error "saveUpdateCommentAndNotes: Saving comment predecessors failed - " + GormUtil.allErrorsString(taskDependency)
									errorMsg = 'An unexpected error occurred while saving your change'
								}
							}
						}
					}
				}
				
				// Create workflow transition if runbook is enabled
				if (project.runbookOn == 1) {
					taskService.createTransition( assetComment, userLogin, params.status )
				}
			
				// TODO - comparison of the assetComment.dueDate may not work if the dueDate is stored in GMT
				def css =  assetComment.dueDate < date ? 'Lightpink' : 'White'
			
				def status = (assetComment.commentType == AssetCommentType.TASK && assetComment.isResolved == 0) ? true : false
			
				def statusCss = taskService.getCssClassForStatus(assetComment.status )
				def lastUpadatedFormatter  = new SimpleDateFormat("MM/dd kk:mm:ss");
				map = [ assetComment : assetComment, status : status ? true : false , cssClass:css, statusCss:statusCss, 
						lastUpdatedDate:lastUpadatedFormatter.format(GormUtil.convertInToUserTZ(assetComment.lastUpdated,tzId)) ]

				// Only send email if the originator of the change is not the assignedTo as one doesn't need email to one's self.
				def loginPerson = userLogin.person	// load so that we don't have a lazyInit issue
				if ( assetComment.commentType == AssetCommentType.TASK && assetComment.assignedTo && assetComment.assignedTo.id != loginPerson.id ) {
					// Send email in separate thread to prevent delay to user
					// TODO renable Thread.start once we upgrade to 2.x (see sendTaskEMail below for additional code re-enablement).
					//Thread.start {
						 //dispatchTaskEmail([taskId:assetComment.id, tzId:tzId, isNew:isNew])
						 sendTaskEMail(assetComment.id, tzId, isNew)
					//}
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
	 * Used to send the Task email to the appropriate user for the comment passed to the method
	 * @param assetComment
	 * @param tzId
	 * @return
	 */
	def sendTaskEMail(taskId, tzId, isNew=true) {
		// Perform this withNewSession because it runs in a separate thread and the session would otherwise be lost
		// TODO re-enable the withNewSession after upgrade to 2.x as there is a bug in 1.3 that we ran into
		// https://github.com/grails/grails-core/commit/9a8e765e4a139f67bb150b6dd9f7e67b16ecb21e
		// AssetComment.withNewSession { session ->
		def assetComment = AssetComment.read(taskId)
		if (! assetComment) {
			log.error "sendTaskEMail: Invalid AssetComment ID [${taskId}] referenced in call"
			return
		}
		
		// TODO: Need to refactor these AssetComment.category parameters into an ENUM
		def statusToSendEmailFor = [
			AssetCommentCategory.GENERAL,
			AssetCommentCategory.DISCOVERY,
			AssetCommentCategory.PLANNING,
			AssetCommentCategory.WALKTHRU,
			AssetCommentCategory.PREMOVE,
			AssetCommentCategory.POSTMOVE ]

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
		sub = (isNew ? '' : 'Re: ') + ( (sub == null || sub.size() == 0) ? "Task ${assetComment.id}" : sub )
		
		log.info "sendTaskEMail: sending email to ${assignedTo.email} for task id ${taskId}"
		
		mailService.sendMail {
			to assignedTo.email
			subject "${sub}"
			body (
				view:"/assetEntity/_taskEMailTemplate",
				model: assetCommentModel(assetComment, tzId)
			)
		}
	}
	
	/**
	 * Returns a map of variables for the AssetComment and notes
	 * @param assetComment - the assetComment object to create a model of
	 * @return map
	 */
	def assetCommentModel(assetComment, tzId) {
		def formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
		def dateFormatter = new SimpleDateFormat("MM/dd/yyyy ");
		def notes = assetComment.notes?.sort{it.dateCreated}
		def assetName = assetComment.assetEntity ? "${assetComment.assetEntity.assetName} (${assetComment.assetEntity.assetType})" : null
		def createdBy = assetComment.createdBy
		def resolvedBy = assetComment.resolvedBy
		def dtCreated = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
		def dueDate
		def dtResolved
		
		if(assetComment.dateResolved) {
			dtResolved = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateResolved, tzId));
		}
		if(assetComment.dueDate){
			dueDate = dateFormatter.format(assetComment.dueDate);
		}
		
		[	assetComment:assetComment,
			assetName:assetName,
			moveEvent:assetComment.moveEvent,
			createdBy:createdBy, dtCreated:dtCreated, dtResolved:dtResolved, dueDate:dueDate,
			resolvedBy:resolvedBy, assignedTo:assetComment.assignedTo,
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
     *  This method is responsible for creating the Quartz job
     *  @param : paramsMap ["taskId":taskId, "tzId":tzId, "isNew":isNew]
     *  @return : create Trigger
     */
    def dispatchTaskEmail(Map paramsMap){
        Trigger trigger = new SimpleTrigger("tm-sendEmail-${paramsMap.taskId}" + System.currentTimeMillis(), GormUtil.convertInToGMT( "now", paramsMap.tzId ))
        trigger.jobDataMap.putAll(["taskId":paramsMap.taskId, "tzId":paramsMap.tzId, "isNew":paramsMap.isNew])
  
        SendTaskEmailJob.schedule( trigger )
        
    }
	
}