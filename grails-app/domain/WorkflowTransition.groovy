/*
 * WorkflowTransition is used to store the data found in the xml in the 'transitions' section 
 */
class WorkflowTransition {
	
	String process
	String code
	String name
	Integer transId
	String type
	String color
	
	static constraints = {
		process( blank:false, nullable:false )
		code( blank:false, nullable:false )
		name( blank:false, nullable:false )
		transId( nullable:false )
		type( blank:false, nullable:false )
		color( blank:true, nullable:true)
	}	
	
	static mapping  = {
		version false
		id column:'workflow_transition_id'
	}
}
