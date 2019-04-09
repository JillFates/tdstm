package net.transitionmanager.command


import net.transitionmanager.project.MoveEvent
import net.transitionmanager.asset.Room


class MoveBundleCommand implements CommandObject {

 	Long id
	String name
	String description
	Date startTime                   // Time that the MoveBundle Tasks will begin
	Date completionTime              // Planned Completion Time of the MoveBundle
	Integer operationalOrder = 1     // Order that the bundles are performed in (NOT BEING USED)
	MoveEvent moveEvent
	String workflowCode
	Boolean useForPlanning = true
	Room sourceRoom
	Room targetRoom
	Boolean tasksCreated = false

	static constraints = {
		id nullable: true
		description nullable: true
		completionTime nullable: true, blank: true
		description nullable: true
		moveEvent nullable: true
		name blank: false, unique: ['project']
		operationalOrder range: 1..25
		sourceRoom nullable: true
		startTime nullable: true
		targetRoom nullable: true
		tasksCreated nullable: true
		workflowCode blank: false
	}

}


