package com.tds.asset

import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentCategory

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
	String category = "general"
	String displayOption = "U"		// Used in dashboard to control display of user entered test (comment) or a generic message
	String attribute = 'default'	// TODO : What is attribute used for?  See if we're using and remove if not
	String commentKey				// TODO : What is commentKey used for?  See if we're using and remove if not
	
	String status
	Date dueDate
	
	Integer duration				// # of minutes to perform task
	String durationScale = 'm'		// Scale that duration represents m)inute, h)our, d)ay, w)eek
	Integer priority=3				// An additional option to score the order that like tasks should be processed where 1=highest and 5=lowest
	
	Date estStart
	Date estFinish	
	Date actStart
	// Date actFinish		// Alias of dateResolved
	
	Integer slack					// Indicated the original or recalculated slack time that this task has based on other predecessors of successors of this task
	WorkflowTransition workflowTransition	// The transition that this task was cloned from
	Integer workflowOverride = 0			// Flag that the Transition values (duration) has been overridden
	String role	// TODO : Determine proper name.
	Integer taskNumber	// TODO : constraint type short int min 1, max ?, nullable 
	Integer score		// Derived property that calculates the weighted score for sorting on priority
	
	static hasMany = [ 
		notes : CommentNote,
		taskDependencies : TaskDependency 
	]
	
	static belongsTo = [ assetEntity : AssetEntity ]
	
	// TODO : Add custom validator for role that checks that the role is legit for "Staff : *" of RoleType
	
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
		dateResolved( nullable:true  )
		dueDate( nullable:true)
		commentCode( blank:true, nullable:true  )
		category( blank:false, nullable:false ,inList:['general', 'discovery', 'planning','walkthru','premove','moveday','shutdown','physical','startup','postmove'])
		displayOption( blank:false, inList: ['G','U'] ) // Generic or User
		attribute( blank:true, nullable:true  )
		commentKey( blank:true, nullable:true  )
		// TODO: remove the blank/nullable constraint for status after testing
		// status( blank:true, nullable:true , inList : ['Planned', 'Pending', 'Ready', 'Started', 'Hold', 'Completed'] )
		status( blank:true, nullable:true, inList : AssetCommentStatus.getList() )
			// validator:{ if (commentType=='issue' && ! status) return ['issue.blank'] } )
		// TODO: change duration to default to zero and min:1, need to coordinate with db update for existing data
		duration(nullable:true)
		durationScale(nullable:true, blank:true, inList:['m','h','d','w'])
		// TODO : add constraint to priority
		// priority(range:1..5)
		priority( nullable:true )
		estStart( nullable:true  )
		actStart( nullable:true  )
		estFinish( nullable:true  )
		slack( nullable:true )
		workflowTransition( nullable:true  )
		// TODO : add range to workflowOverride constraint
		workflowOverride( nullable:true)
		hardAssigned(nullable:true)
		role( blank:true, nullable:true  )
		taskNumber(nullable:true)	// Note - can't have nullable:true and min:1, the category+taskNumber should be unique
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'asset_comment_id'
		resolvedBy column: 'resolved_by'
		createdBy column: 'created_by'
		score formula:  "( (CASE status \
			WHEN '${AssetCommentStatus.STARTED}' THEN 30 \
			WHEN '${AssetCommentStatus.READY}' THEN 15 \
			WHEN '${AssetCommentStatus.PENDING}' THEN 10 \
			ELSE 0 END) + \
			IF(category IN ('${AssetCommentCategory.SHUTDOWN}', '${AssetCommentCategory.PHYSICAL}', '${AssetCommentCategory.STARTUP}'), 10,0) + \
			6 - priority)"
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
		return [AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP].contains(this.category)
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
	// Returns the duration of the task in minutes
	def durationInMinutes() {
		def d = duration
		switch (durationScale) {
			case DurationScale.MINUTE:	d = duration; break
			case DurationScale.HOUR:	d = duration/60; break
			case DurationScale.DAY: 	d = duration/1440; break
			case DurationScale.WEEK:	d = duration/7200; break
		}
		return d
	}
	
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = dateCreated
	}
	
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

	String toString() {
		 (taskNumber ? "${taskNumber}:" : '') + org.apache.commons.lang.StringUtils.left(comment,25)
	}
}