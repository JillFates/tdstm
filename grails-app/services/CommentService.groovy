import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import org.jsecurity.SecurityUtils
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

import com.tdssrc.grails.GormUtil

import com.tds.asset.*
import com.tds.asset.AssetComment

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
	
	/**
	 * Used to persist changes to the AssetComment and CommentNote
	 * @param params 
	 * @isNew - boolean flag that indicates if it is new or an update
	 * @return map of the AssetComment data used to refresh the view
	 */
	def saveUpdateCommentAndNotes(session, params, isNew = true) {
		// Getting the loginUser should be abstracted into a service
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def formatter = new SimpleDateFormat("MM/dd/yyyy");
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def date = new Date()
		def project = Project.get(session.CURR_PROJ.CURR_PROJ)
		def assetComment
		def commentProject

		if (! project) {
			log.error "User has no current project"
			// TODO : Handle failure
			return []
		}
		
		// Create or load the assetComment object appropriately
		if (isNew) {
			assetComment = new AssetComment()
			assetComment.createdBy = loginUser.person
			assetComment.project = project
			def lastTask = jdbcTemplate.queryForInt("select max(task_number) FROM asset_comment WHERE project_id = ${project.id}")
			assetComment.taskNumber = lastTask + 1
			if ( params.commentType != 'issue' && (! params.assetEntity || ! params.assetEntity.isNumber() ) ) {
				log.error "Asset id was not properly supplied to add or update a comment"
				// TODO : handle failure where an asset id is necessary but not supplied
				return []
			}
			if ( params.assetEntity && params.assetEntity.isNumber() ) {
				def assetEntity = AssetEntity.get(params.assetEntity)
				if (assetEntity) {
					assetComment.assetEntity = assetEntity
					commentProject = assetEntity.project
				} else {
					// TODO : handle failure for missing Asset
					log.error "Specified asset [id:${params.assetEntity}] was not found while creating comment"
					return []
				}
			}
		} else {
			// Load existing comment
			assetComment = AssetComment.get(params.id)
			if (! assetComment ) {
				// TODO : handle failure for invalid comment id
				log.error "Specified comment [id:${params.id}] was not found while updating comment"
				return []
			}
		}
		commentProject = assetComment.assetEntity ? assetComment.assetEntity.project : assetComment.project
		// Make sure that the comment about to be updated/created is associated to the user's current project
		if ( commentProject.id != project.id ) {
			log.error "The project [${commentProject.id}] for comment [${assetComment.id}] was not associated with user's current project [${project.id}]"
			// TODO: handle failure of bad assetComment id passed for the user's current project (could be a hack)
			return []
		}
//		def bindArgs = [assetComment, params, [ exclude:['assignedTo', 'assetEntity', 'moveEvent', 'project', 'dueDate', 'status'] ] ]
//		def bindArgs = [assetComment, params, [ include:['comment', 'category', 'displayOption', 'attribute'] ] ]
//		bindData.invoke( assetComment, 'bind', (Object[])bindArgs )
		// TODO - assignedTo is getting set even though it is in exclude list (seen in debugger with issue type)
		
		// Assign the general params for all types.  Was having an issue with the above binding, which was 
		// setting the assignedTo automatically with a blank Person object even though it was excluded.
		if (params.comment) assetComment.comment = params.comment
		if (params.category) assetComment.category = params.category
		if (params.displayOption) assetComment.displayOption = params.displayOption
		if (params.attribute) assetComment.attribute = params.attribute
		if (params.commentType) assetComment.commentType = params.commentType
	    assetComment.resolution = params.resolution
		if(params.estStart) assetComment.estStart = estformatter.parse(params.estStart)
		if(params.estFinish) assetComment.estFinish = estformatter.parse(params.estFinish)
		if(params.actStart) assetComment.actStart = estformatter.parse(params.actStart)
		assetComment.workflowTransition = WorkflowTransition.get(params.workflowTransition)
		if(params.hardAssigned) assetComment.hardAssigned = Integer.parseInt(params.hardAssigned)
		if(params.priority) assetComment.priority = Integer.parseInt(params.priority)
		if(params.duration) assetComment.duration = Integer.parseInt(params.duration)
		if(params.durationScale) assetComment.durationScale = params.durationScale
		if(params.overRide) assetComment.workflowOverride = Integer.parseInt(params.overRide)
		 assetComment.role = params.role
		 
		 
        
		// Issues (aka tasks) have a number of additional properties to be managed 
		if ( assetComment.commentType == 'issue' ) {
			if ( params.moveEvent && params.moveEvent.isNumber() ){
				def moveEvent = MoveEvent.get(params.moveEvent)
				if (moveEvent) {
					// Validate that this is a legit moveEvent for this project
					if (moveEvent.project.id != project.id) {
						// TODO: handle failure of moveEvent not being in project
						return []
					}
					assetComment.moveEvent = moveEvent
				} else {
					// TODO : Handle error if moveEvent not found
				}
			} else {
				assetComment.moveEvent = null
			}
			
			if (params.assignedTo && params.assignedTo.isNumber()){
				// TODO - SECURITY - Need to validate that the assignedTo is a member of the project
				def person = Person.get(params.assignedTo)
				assetComment.assignedTo = person
			} else {
				assetComment.assignedTo = null
			}
			
			
			// Update the resolved properties based on status being Completed
			if(params.status=='Completed'){
				assetComment.isResolved = 1
				assetComment.resolvedBy = loginUser.person
				assetComment.dateResolved = GormUtil.convertInToGMT( "now", tzId )
			}else{
				assetComment.isResolved = 0
				assetComment.resolvedBy = null
				assetComment.dateResolved = null
			}
			
			if(params.dueDate){
				assetComment.dueDate = formatter.parse(params.dueDate)
			}
			
			assetComment.status = params.status
		}

		if (! assetComment.hasErrors() && assetComment.save(flush:true)) {
		
			if (assetComment.commentType == 'issue' && params.note){
				// TODO The adding of assetNote should be a method on the AssetComment instead of reverse injections plus the save above can handle both. Right now if this fails, everything keeps on as though it didn't which is wrong.
				def assetNote = new CommentNote();
				assetNote.createdBy = loginUser.person
				assetNote.dateCreated = date
				assetNote.note = params.note
				assetNote.assetComment = assetComment
				if (!assetNote.save(flush:true)){
					// TODO error won't bubble up to the user
					assetNote.errors.allErrors.each{println it}
				}
				
			}
			def taskDependency = params["taskDependency[]"]
			taskDependency.each {
			   def commentInstance = AssetComment.get(it)
			   def taskDepenencyInstance = new TaskDependency()
			   taskDepenencyInstance.predecessor = commentInstance
			   taskDepenencyInstance.assetComment = assetComment
			   if(!taskDepenencyInstance.save(flush:true)){taskDepenencyInstance.errors.allErrors.each{println it}}
			}
			// TODO - comparison of the assetComment.dueDate may not work if the dueDate is stored in GMT
			def css =  assetComment.dueDate < date ? 'Lightpink' : 'White'
			def status = (assetComment.commentType == "issue" && assetComment.isResolved == 0) ? true : false
			
			    def statusCss = 'asset_process'
			
				if(assetComment.status=='Ready' ||assetComment.status=='' || assetComment.status==null){
					statusCss='asset_ready'
				}else if(assetComment.status=='Completed'){
					statusCss='asset_done'
				}else if(assetComment.status=='Hold'){
					statusCss='asset_hold'
				}else if(assetComment.status=='Planned'||assetComment.status=='Pending'){
					statusCss='asset_pending'
				}
			
			def map = [ assetComment : assetComment, status : status ? true : false , cssClass:css,statusCss:statusCss ]

			// Only send email if the originator of the change is not the assignedTo as one doesn't need email to one's self.
			def loginPerson = loginUser.person	// load so that we don't have a lazyInit issue
			if ( assetComment.commentType == 'issue' && assetComment.assignedTo && assetComment.assignedTo.id != loginPerson.id ) {
				// Send email in separate thread to prevent delay to user
				// TODO renable Thread.start once we upgrade to 2.x (see sendTaskEMail below for additional code re-enablement).
				//Thread.start {
					 sendTaskEMail(assetComment.id, tzId, isNew)
				//}
			}
			
			return map
		} else {
			def etext = "Unable to create Assetcomment" +
                GormUtil.allErrorsString( assetComment )
			log.error( etext )
			return []
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
			def assetComment = AssetComment.get(taskId)
			if (! assetComment) {
				log.error "Invalid AssetComment ID [${taskId}] referenced in call"
				return
			}
			
			// Only send emails out for issues in the categories up to premove
			if ( assetComment.commentType != 'issue' || ! ['general', 'discovery', 'planning','walkthru','premove'].contains(assetComment.category) ) {
				return
			}

			// Must have an email address
			if ( ! assetComment.assignedTo?.email) {
				log.error "No valid email address for assigned individual"
				return
			}
			
			// Truncate long comments to make manageable subject line
			def sub = leftString(getLine(assetComment.comment,0), 40)
			sub = (isNew ? '' : 'Re: ') + ( (sub == null || sub.size() == 0) ? "Task ${assetComment.id}" : sub )
	
			mailService.sendMail {
				to assetComment.assignedTo.email
				subject "${sub}"
				body (
					view:"/assetEntity/_taskEMailTemplate",
					model: assetCommentModel(assetComment, tzId)
				)
			}
//		}
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

}
