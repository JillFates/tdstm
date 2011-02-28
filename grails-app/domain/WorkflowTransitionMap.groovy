
class WorkflowTransitionMap {

	Integer transId
	String flag

	static belongsTo = [ workflow : Workflow, workflowTransition : WorkflowTransition, swimlane : Swimlane  ]
	
	static constraints = {
		transId( blank:false, nullable:false )
		flag( blank:true, nullable:true, inList: [ "busy", "comment", "idle", "issue", "skipped" ])
		workflow( blank:false, nullable:false )
		workflowTransition( blank:false, nullable:false )
		swimlane( blank:false, nullable:false )
	}
	
	static mapping = {
		version false
		id column:'workflow_transition_map_id'
	}

	String toString() {
		"${workflowTransition} : ${swimlane}"
	}
	
}
