package com.tds.asset

import com.tdssrc.grails.GormUtil

class AssetComment {

	

	String comment

	String commentType

	Integer mustVerify = 0

	AssetEntity assetEntity
	MoveEvent moveEvent

	Date dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
	Date lastUpdated 

	Integer isResolved = 0

	Date dateResolved 

	String resolution

	Person resolvedBy

	Person createdBy
	Person assignedTo			// FKA owner

	String commentCode 

	String category = "general"

	String displayOption = "U"	// Used in dashboard to control display of user entered test (comment) or a generic message
	String attribute = 'default'
	String commentKey
	String status
	Date dueDate
	String predecessor 
	Integer duration
	String type = 'ASAP'
	Integer priority
	Date startedTime
	Date estStart
	Date estFinsh
	String wbsParent
	String wbsSequence
	String workflow
	String workflowItem
	String workflowOverride
	String role
	Project project
	

	static hasMany = [notes : CommentNote]
	

	static constraints = {

		

		comment( blank:true, nullable:true  )
		assetEntity( blank:true, nullable:true )
		moveEvent(nullable: true)
		// TODO - Need to verify if commentType can be blank/nullable

		commentType( blank:false, nullable:false, inList: ['issue','instruction','comment'] )

		mustVerify( nullable:true )

		isResolved( nullable:true )

		createdBy( nullable:true  )
		assignedTo( blank:true, nullable:true  )
		resolvedBy( nullable:true  )

		resolution( blank:true, nullable:true  )
		dateCreated( nullable:true  )
		lastUpdated( blank:true, nullable:true  )

		dateResolved( nullable:true  )

		commentCode( blank:true, nullable:true  )

		category( blank:false, nullable:false ,inList:['general', 'discovery', 'planning','walkthru','premove','shutdown','moveday','startup','postmove'])

		displayOption( blank:false, inList: ['G','U'] ) // Generic or User
		attribute( blank:true, nullable:true  )
		commentKey( blank:true, nullable:true  )
		status( blank:true, nullable:true , inList : ['Ready','Planned', 'Pending', 'Started', 'Hold', 'Completed'] )
		predecessor( blank:true, nullable:true  )
		duration( blank:true, nullable:true  )
		type( blank:true, nullable:true , inList :['ASAP', 'SS', 'SF']  )
		priority( blank:true, nullable:true  )
		startedTime( blank:true, nullable:true  )
		estStart( blank:true, nullable:true  )
		estFinsh( blank:true, nullable:true  )
		wbsParent( blank:true, nullable:true  )
		wbsSequence( blank:true, nullable:true  )
		workflow( blank:true, nullable:true  )
		workflowItem( blank:true, nullable:true  )
		workflowOverride( blank:true, nullable:true  )
		role( blank:true, nullable:true  )
		dueDate( blank:true, nullable:true  )
		project( blank:false, nullable:false  )

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

			displayOption sqltype: 'char(1)'

		}

	}
	def getAssignedToString(){
		return assignedTo.toString()
	}
	def getAssetName(){
		return assetEntity.assetName
	}
	
	def beforeInsert = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

	String toString() {

		 comment

	}

	

}