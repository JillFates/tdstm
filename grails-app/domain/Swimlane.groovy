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
		workflow( nullable:false )
		maxSource( blank:true, nullable:true )
		maxTarget( blank:true, nullable:true )
		minSource( blank:true, nullable:true )
		minTarget( blank:true, nullable:true )
	}
	
	static mapping = {
		version false
		id column:'swimlane_id'
	}

	String toString() {
		"${workflow} : ${name}"
	}
	
}
