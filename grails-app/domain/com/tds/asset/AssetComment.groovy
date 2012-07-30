package com.tds.asset

import com.tdssrc.grails.GormUtil

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
	Integer priority=4				// An additional option to score the order that like tasks should be processed where 1=highest and 5=lowest
	
	Date estStart
	Date estFinish	
	Date actStart
	// Date actFinish		// Alias of dateResolved
	
	WorkflowTransition workflowTransition	// The transition that this task was cloned from
	Integer workflowOverride = 0			// Flag that the Transition values (duration) has been overridden
	String role	// TODO : Determine proper name.
	Integer taskNumber	// TODO : constraint type short int min 1, max ?, nullable 
	
	static hasMany = [ 
		notes : CommentNote,
		taskDependency : TaskDependency 
	]
	
	static belongsTo = [ assetEntity : AssetEntity ]
	
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
		assignedTo( blank:true, nullable:true  )
		resolvedBy( nullable:true  )
		resolution( blank:true, nullable:true  )
		dateCreated( nullable:true  )
		lastUpdated( blank:true, nullable:true  )
		dateResolved( nullable:true  )
		dueDate( blank:true, nullable:true)
		commentCode( blank:true, nullable:true  )
		category( blank:false, nullable:false ,inList:['general', 'discovery', 'planning','walkthru','premove','moveday','shutdown','physical','startup','postmove'])
		displayOption( blank:false, inList: ['G','U'] ) // Generic or User
		attribute( blank:true, nullable:true  )
		commentKey( blank:true, nullable:true  )
		// TODO: remove the blank/nullable constraint for status after testing
		status( blank:true, nullable:true , inList : ['Planned', 'Pending', 'Ready', 'Started', 'Hold', 'Completed'] )
		// TODO: change duration to default to zero and min:1, need to coordinate with db update for existing data
		duration(nullable:true)
		durationScale(nullable:true, blank:true, inList:['m','h','d','w'])
		// TODO : add constraint to priority
		// priority(range:1..5)
		priority( nullable:true )
		estStart( nullable:true  )
		actStart( nullable:true  )
		estFinish( nullable:true  )
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

	// TODO : need method to handle inserting new assetComment or updating so that the category+taskNumber is unique 
	
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

	def getAssignedToString(){
		return assignedTo.toString()
	}
	def getAssetName(){
		return assetEntity.assetName
	}
	def getActFinish() {
		return dateResolved
	}
	def setActFinish(def date) {
		dateResolved = date
	}
	// Get the duration in minutes
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
	
	// Force the created and updated dates to be GMT
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = dateCreated
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

	String toString() {
		 comment
	}

}