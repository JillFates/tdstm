
class WorkflowTransitionMap {

	Integer transId
	String flag

	static belongsTo = [ workflow : Workflow, workflowTransition : WorkflowTransition, swimlane : Swimlane  ]
	
	static constraints = {
		transId( nullable:false )
		flag( blank:true, nullable:true )
		workflow( nullable:false )
		workflowTransition( nullable:false )
		swimlane( nullable:false )
	}
	
	static mapping = {
		version false
		id column:'workflow_transition_map_id'
	}

	String toString() {
		"${workflowTransition} : ${swimlane}"
	}
	
}
