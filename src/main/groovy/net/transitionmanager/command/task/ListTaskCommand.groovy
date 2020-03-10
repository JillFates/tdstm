package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ListTaskCommand implements CommandObject {

	String filter
	Integer rows            = 25
	Integer page            = 0
	Integer justRemaining   = 1
	Integer viewUnpublished = 0
	Integer justMyTasks     = 0
	Long    moveEvent       = 0
	String  sortColumn
	String  sortOrder       = 'ASC'

	// Filterable columns
	String actFinish
	String actStart
	String apiAction
	String assetName
	String assetType
	String assignedTo
	String bundle
	String category
	String comment
	String createdBy
	String dateCreated
	String dueDate
	String duration
	String durationScale
	String estFinish
	String estStart
	String event
	String hardAssigned
	String instructionsLink
	String isCriticalPath
	String isPublished
	String lastUpdated
	String latestFinish
	String latestStart
	String percentageComplete
	String priority
	String role
	String sendNotification
	String slack
	String status
	String statusUpdated
	String taskNumber
	String taskSpec

	static constraints = {
		filter nullable: true
		justMyTasks range: 0..1, nullable: true
		justRemaining range: 0..1, nullable: true
		viewUnpublished range: 0..1, nullable: true
		sortOrder inList: ['ASC', 'DESC']
		moveEvent nullable: true

		// filterable columns
		actFinish nullable: true
		actStart nullable: true
		apiAction nullable: true
		assetName nullable: true
		assetType nullable: true
		assignedTo nullable: true
		bundle nullable: true
		category nullable: true
		comment nullable: true
		createdBy nullable: true
		dateCreated nullable: true
		dueDate nullable: true
		duration nullable: true
		durationScale nullable: true
		estFinish nullable: true
		estStart nullable: true
		event nullable: true
		hardAssigned nullable: true
		instructionsLink nullable: true
		isCriticalPath nullable: true
		isPublished nullable: true
		lastUpdated nullable: true
		latestFinish nullable: true
		latestStart nullable: true
		percentageComplete nullable: true
		priority nullable: true
		role nullable: true
		sendNotification nullable: true
		slack nullable: true
		status nullable: true
		statusUpdated nullable: true
		taskNumber nullable: true
		taskSpec nullable: true
	}
}
