/*
 * WorkflowTransition is used to store the data found in the xml in the 'transitions' section 
 */
class WorkflowTransition {
	
	String code
	String name
	String type
	String color
	
	static constraints = {
		code( blank:false, nullable:false )
		name( blank:false, nullable:false )
		type( blank:false, nullable:false )
		color( blank:true, nullable:true)
	}	
	
	static mapping  = {
		version false
		id column:'workflow_transition_id'
	}
}
