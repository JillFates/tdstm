package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ListTaskCommand implements CommandObject {

	Integer rows            = 25
	Integer page            = 0
	Integer justRemaining   = 1
	Integer viewUnpublished = 0
	Integer justMyTasks     = 0
	Long    moveEvent       = 0
	String  sortColumn
	String  sortOrder       = 'ASC'

	//Filter columns
	String comment
	String assetName
	String assetType
	String taskNumber
	String dueDate
	String status
	String assignedTo
	String apiAction
	String role
	String actFinish
	String actStart
	String category
	String commentType
	String percentageComplete
	String createdBy
	String dateCreated
	String dateResolved
	String displayOption
	String duration
	String durationScale
	String estFinish
	String estStart
	String hardAssigned
	String instructionsLink
	String isPublished
	String lastUpdated
	String bundle
	String event
	String priority
	String resolution
	String resolvedBy
	String sendNotification
	String statusUpdated
	String taskSpec

	static constraints = {
		justMyTasks range: 0..1, nullable: true
		justRemaining range: 0..1, nullable: true
		viewUnpublished range: 0..1, nullable: true
		sortOrder inList: ['ASC', 'DESC']
		moveEvent nullable: true

		//filter columns
		comment nullable: true
		assetName nullable: true
		assetType nullable: true
		taskNumber nullable: true
		dueDate nullable: true
		status nullable: true
		assignedTo nullable: true
		apiAction nullable: true
		role nullable: true
		actFinish nullable: true
		actStart nullable: true
		category nullable: true
		commentType nullable: true
		percentageComplete nullable: true
		createdBy nullable: true
		dateCreated nullable: true
		dateResolved nullable: true
		displayOption nullable: true
		duration nullable: true
		durationScale nullable: true
		estFinish nullable: true
		estStart nullable: true
		hardAssigned nullable: true
		instructionsLink nullable: true
		isPublished nullable: true
		lastUpdated nullable: true
		bundle nullable: true
		event nullable: true
		priority nullable: true
		resolution nullable: true
		resolvedBy nullable: true
		sendNotification nullable: true
		statusUpdated nullable: true
		taskSpec nullable: true
	}
}
