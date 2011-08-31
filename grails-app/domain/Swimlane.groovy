class Swimlane {
	
	String name
	String actorId
	String maxSource = "Unracked"
	String maxTarget = "Reracked"
	String minSource = "Release"
	String minTarget = "Staged"
	
	static belongsTo = [ workflow : Workflow ]
	static hasMany  = [ WorkflowTransitionMap ]
	
	static constraints = {
		actorId( blank:false, nullable:false )
		name( blank:false, nullable:false )
		workflow( blank:false, nullable:false )
		maxSource( blank:true, nullable:true )
		maxTarget( blank:false, nullable:false )
		minSource( blank:true, nullable:true )
		minTarget( blank:false, nullable:false )
	}
	
	static mapping = {
		version false
		id column:'swimlane_id'
	}

	String toString() {
		"${workflow} : ${name}"
	}
	
}
