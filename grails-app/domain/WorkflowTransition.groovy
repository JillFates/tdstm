/*
 * WorkflowTransition is used to store the data found in the xml in the 'transitions' section 
 */
class WorkflowTransition {
	
	String code
	String name
	Integer transId
	String type
	String color
	String dashboardLabel
	Integer predecessor
	String header
	Integer effort

	static belongsTo = [ workflow : Workflow ]
	static hasMany  = [ WorkflowTransitionMap ]
	
	static constraints = {
		workflow( blank:false, nullable:false )
		code( blank:false, nullable:false, unique:'workflow' )
		name( blank:false, nullable:false )
		transId( blank:false, nullable:false )
		type( blank:false, nullable:false, inList: ['process', 'boolean'] )
		color( blank:true, nullable:true)
		dashboardLabel( blank:true, nullable:true)
		predecessor( blank:true, nullable:true)
		header( blank:true, nullable:true)
		effort( blank:true, nullable:true)
	}	
	
	static mapping  = {
		version false
		id column:'workflow_transition_id'
	}
	
	String toString() {
		"${workflow} : ${code}"
	}
}
