package net.transitionmanager.project

class Swimlane {

	String name
	String actorId
	String maxSource = 'Unracked'
	String maxTarget = 'Reracked'
	String minSource = 'Release'
	String minTarget = 'Staged'

	static belongsTo = [workflow: Workflow]
	static hasMany = [WorkflowTransitionMap]

	static constraints = {
		actorId blank: false
		name blank: false
		maxSource nullable: true
		maxTarget nullable: true
		minSource nullable: true
		minTarget nullable: true
	}

	static mapping = {
		version false
		id column: 'swimlane_id'
	}

	String toString() {
		"$workflow : $name"
	}
}
