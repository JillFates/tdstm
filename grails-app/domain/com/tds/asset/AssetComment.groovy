package com.tds.asset

import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.TimeScale
import com.tdsops.tm.enums.domain.TimeConstraintType

class AssetComment {

	String comment					// This is also the title of an issue or task
	String commentType
	Integer mustVerify = 0			// Flag used in MoveTech to have the user verify an instruction

	// These three properties all relating the project
	AssetEntity assetEntity
	Project project
	MoveEvent moveEvent

	Date dateCreated
	Date lastUpdated
	Date statusUpdated				// Updated when the status changes so we can compute the elapsed time that a task is in a status

	Integer isResolved = 0
	Date dateResolved 
	String resolution
	Person resolvedBy
	Person createdBy
	Person assignedTo				// FKA owner
	Integer hardAssigned = 0			// Flags a task that can ONLY be done by an individual TODO : constraint 1/0 default 0 type tinyint
	String commentCode 
	String category = AssetCommentCategory.GENERAL
	String displayOption = "U"		// Used in dashboard to control display of user entered test (comment) or a generic message
	String attribute = 'default'	// TODO : What is attribute used for?  See if we're using and remove if not
	String commentKey				// TODO : What is commentKey used for?  See if we're using and remove if not
	
	String status
	Date dueDate
	
	Integer duration = 0			// # of minutes/hours/days/weeks to perform task
	TimeScale durationScale = TimeScale.M	// Scale that duration represents m)inute, h)our, d)ay, w)eek
	Integer priority=3				// An additional option to score the order that like tasks should be processed where 1=highest and 5=lowest
	
	Date estStart
	Date estFinish	
	Date actStart
	Date constraintTime				// Used for tasks that have a constraint on the time that it can start or finish (typically used for start event or start testing)
	TimeConstraintType constraintType	// The type of constraint for time (e.g. MSO-Must Start On, )
	// Date actFinish		// Alias of dateResolved
	
	Integer slack					// Indicated the original or recalculated slack time that this task has based on other predecessors of successors of this task
	WorkflowTransition workflowTransition	// The transition that this task was cloned from
	Integer workflowOverride = 0			// Flag that the Transition values (duration) has been overridden
	String role			// This is the team that will perform the task
	Integer taskNumber	// TODO : constraint type short int min 1, max ?, nullable 
	Integer score		// Derived property that calculates the weighted score for sorting on priority

	/** The batch from which the task was generated */
	TaskBatch taskBatch
	/** Whether or not a task will send email notifications  */
	Boolean sendNotification=false
	/** Flag used to indicate if the task is visable to the average user */
	Boolean isPublished=true
	/** Flag that indicates if a task is auto generated by the system -  TODO : this will go away with the new Catalog functionality */
	Boolean autoGenerated = false
	/** The receipe id # that caused this task to be created - TODO : this will be replaced by the taskBatch property with Catalog functionality */
	Integer recipe
	Integer taskSpec				// The taskSpec id # within the receipe that caused this task to be created
	String instructionsLink
	
	static hasMany = [ 
		notes : CommentNote,
		taskDependencies : TaskDependency 
	]
	
	static belongsTo = [ assetEntity : AssetEntity ]
	
	// TODO : Add custom validator for role that checks that the role is legit for "Staff : *" of RoleType
	
	// Grouping of the various categories
	def static final preMoveCategories = AssetCommentCategory.getPreMoveCategories()
	def static final moveDayCategories = AssetCommentCategory.getMoveDayCategories()
	def static final postMoveCategories = AssetCommentCategory.getPostMoveCategories()
	def static final discoveryCategories = AssetCommentCategory.getDiscoveryCategories()
	def static final planningCategories = AssetCommentCategory.getPlanningCategories()
	def static final AUTOMATIC_ROLE = 'AUTO'

	static constraints = {	
		// comment(size:255)	// TODO: add constraint for comment size
		assetEntity(nullable:true )
		project(nullable:false)
		moveEvent(nullable: true)
		commentType(blank:false, inList: ['issue','instruction','comment'] )
		mustVerify(nullable:true )
		isResolved( nullable:true )
		createdBy( nullable:true  )
		lastUpdated( nullable:true )
		assignedTo( nullable:true  )
		resolvedBy( nullable:true  )
		resolution( blank:true, nullable:true  )
		dateCreated( nullable:true  )
		lastUpdated( nullable:true  )
		statusUpdated( nullable:true )
		dateResolved( nullable:true )
		dueDate( nullable:true)
		commentCode( blank:true, nullable:true  )
		category( blank:false, nullable:false ,	inList:preMoveCategories + moveDayCategories + postMoveCategories )
		displayOption( blank:false, inList: ['G','U'] ) // Generic or User
		attribute( blank:true, nullable:true  )
		commentKey( blank:true, nullable:true  )
		// TODO: remove the blank/nullable constraint for status after testing
		// status( blank:true, nullable:true , inList : ['Planned', 'Pending', 'Ready', 'Started', 'Hold', 'Completed'] )
		status( blank:true, nullable:true, inList : AssetCommentStatus.getList() )
			// validator:{ if (commentType=='issue' && ! status) return ['issue.blank'] } )
		// TODO: change duration to default to zero and min:1, need to coordinate with db update for existing data
		duration( )
		durationScale(nullable:false, inList:TimeScale.getKeys())
		// TODO : add constraint to priority
		// priority(range:1..5)
		priority( nullable:true )
		estStart( nullable:true  )
		actStart( nullable:true  )
		estFinish( nullable:true  )
		slack( nullable:true )
		workflowTransition( nullable:true  )
		constraintTime( nullable: true )
		constraintType( nullable: true )
		// TODO : add range to workflowOverride constraint
		workflowOverride( nullable:true)
		hardAssigned(nullable:true)
		sendNotification(nullable:true)
		role( blank:true, nullable:true  )
		taskNumber(nullable:true)	// Note - can't have nullable:true and min:1, the category+taskNumber should be unique
		autoGenerated(nullable:true)
		recipe(nullable:true)
		taskSpec(nullable:true)
		taskBatch(nullable:true)
		instructionsLink(blank:true, nullable:true, size:0..255)
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'asset_comment_id'
		isPublished column: 'is_published'
		sendNotification column: 'send_notification'
		instructionsLink column: 'instructions_link'
		resolvedBy column: 'resolved_by'
		createdBy column: 'created_by'
		columns {
			comment sqltype: 'text'
			mustVerify sqltype: 'tinyint'
			isResolved sqltype: 'tinyint'
			resolution sqltype: 'text'
			displayOption sqltype: 'char', length:1
			priority sqltype: 'tinyint'
			workflowOverride sqltype: 'tinyint'
			taskNumber sqltype: 'shortint unsigned'
			duration sqltype: 'mediumint'
			durationScale sqltype: 'char', length:1
		}
		/*
			NOTE THAT THIS LOGIC IS DUPLICATED IN THE TaskService.getUserTasks method SO IT NEEDS TO BE MAINTAINED TOGETHER


			The objectives are sort the list descending in this order:
				- HOLD 900
					+ last updated factor ASC
				- DONE recently (60 seconds), to allow undo 800
					+ actual finish factor DESC	
				- STARTED tasks     700
					- Hard assigned to user	+55
					- by the user	+50
					- + Est Start Factor to sort ASC
				- READY tasks		600
					- Hard assigned to user	+55
					- Assigned to user		+50
					- + Est Start factor to sort ASC
				- PENDING tasks		500
					- + Est Start factor to sort ASC
				- DONE tasks		200
					- Assigned to User	+50 
					- + actual finish factor DESC
					- DONE by others	+0 + actual finish factor DESC
				- All other statuses ?
				- Task # DESC (handled outside the score)
			
			The inverse of Priority will be added to any score * 5 so that Priority tasks bubble up above hard assigned to user
			
			DON'T THINK THIS APPLIES ANY MORE - Category of Startup, Physical, Moveday, or Shutdown +10
			- If duedate exists and is older than today +5
			- Priority - Six (6) - <priority value> (so a priority of 5 will add 1 to the score and 1 adds 5)
		*/
		score formula:	"CASE status \
			WHEN '${AssetCommentStatus.HOLD}' THEN 900 \
			WHEN '${AssetCommentStatus.DONE}' THEN IF(status_updated >= CONVERT_TZ(SUBTIME(NOW(),'00:01:00.0'),'-04:00','+00:00'), 800, 200) + status_updated/NOW() \
			WHEN '${AssetCommentStatus.STARTED}' THEN 700 + 1 - IFNULL(est_start,NOW())/NOW() \
			WHEN '${AssetCommentStatus.READY}' THEN 600 + 1 - IFNULL(est_start,NOW())/NOW() \
			WHEN '${AssetCommentStatus.PENDING}' THEN 500 + 1 - IFNULL(est_start,NOW())/NOW() \
			ELSE 0 END + \
			IF(role='${AssetComment.AUTOMATIC_ROLE}',-100,0) + \
			(6 - priority) * 5"			
	}

	// List of properties that should NOT be persisted
	static transients = ['actFinish', 'assignedToString', 'assetName', 'statusDuration']
	
	// TODO : need method to handle inserting new assetComment or updating so that the category+taskNumber is unique 
	
	def getAssignedToString(){
		return assignedTo.toString()
	}
	def getAssetName(){
		return assetEntity.assetName
	}

	// The actFinish value is stored in the dateResolved column	so need setter/getter
	def getActFinish() {
		return dateResolved
	}
	public void setActFinish(Date date) {
		setDateResolved( date )
	}

	/* 
	 * @return Boolean indicating if the tast is done
	 */
	def isDone() {
		return this.status == AssetCommentStatus.DONE
	}
	
	/*
	 * Used to determine if an object is a runbook task
	 * @return Boolean true if is runbook task
	 */
	def isRunbookTask() {
		return moveDayCategories.contains(this.category)
	}
	
	// Extend the dateResolved setter to also set the isResolved appropriately
	public void setDateResolved( Date date ) {
		this.dateResolved = date
		this.isResolved = date ? 1 : 0
	}
			
	/*
	 * Returns the duration in seconds that a task has been in at particular status
	 * @return Integer
	 */
	def statusDuration() {
		if (statusDate) {
			return groovy.time.TimeCategory.minus(new Date(), statusDate)			
		} else {
			return null
		}
	}

	/*
	 * Returns the remaining duration until the tasks will be completed. This tasks into account the start time
	 * if the task is in progress otherwise returns the total duration
	 * @return Integer duration in minutes
	 */
	def durationRemaining() {
		// TODO : implement durationRemaining
		return duration
	}

	// Returns the duration of the task in minutes
	def durationInMinutes() {
		def d
		if (durationScale)
			d = durationScale.toMinutes( duration )
		return d
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}
	
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		 (taskNumber ? "${taskNumber}:" : '') + org.apache.commons.lang.StringUtils.left(comment,25)
	}
	// Returns task Manager column header names and its labels
	static getTaskCustomizeFieldAndLabel(){
		def assetCommentFields = ['actStart':'Actual Start:', 'assignedTo':'Assigned To', 'category':'Category', 'commentType': 'Comment Type', 'createdBy':'Created By',
			 'dateCreated':'Date Created', 'dateResolved':'Date Resolved', 'displayOption':'Display Option', 'duration':'Duration', 'durationScale':'Duration Scale',
			 'estFinish':'Estimated Finish:', 'estStart':'Estimated Start', 'hardAssigned':'Hard Assignement', 'isPublished':'Is Published', 'sendNotification':'Send Notification',
			 'isResolved':'Is Resolved', 'priority':'Priority', 'resolution':'Resolution', 'resolvedBy':'Resolved By', 
			 'role':'Team', 'statusUpdated':'Status Updated', 'assetName':'Asset Name', 'assetType':'Asset Type','instructionsLink':'instructionsLink']
		return assetCommentFields
	}
}