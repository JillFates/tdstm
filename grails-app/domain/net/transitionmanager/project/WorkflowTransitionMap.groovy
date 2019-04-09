package net.transitionmanager.project

class WorkflowTransitionMap {

	Integer transId
	String flag

	static belongsTo = [swimlane: Swimlane, workflow: Workflow, workflowTransition: WorkflowTransition]

	static constraints = {
		flag nullable: true
	}

	static mapping = {
		version false
		id column: 'workflow_transition_map_id'
	}

	String toString() {
		"$workflowTransition : $swimlane"
	}
}
