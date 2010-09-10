class Swimlane {
	
	String name
	String actorId
	
	static belongsTo = [ workflow : Workflow ]
	static hasMany  = [ WorkflowTransitionMap ]
	
	static constraints = {
		actorId( blank:false, nullable:false )
		name( blank:false, nullable:false )
		workflow( blank:false, nullable:false )
	}
	
	static mapping = {
		version false
		id column:'swimlane_id'
	}

	String toString() {
		"${workflow} : ${name}"
	}
	
}
