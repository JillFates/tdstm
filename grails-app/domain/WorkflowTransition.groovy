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
	// Integer effort	// TODO : Copy effort data into duration column and drop effort column
	Integer duration	// The duration to assign to tasks when building runbooks
	String durationScale = 'm'		// Scale that duration represents m)inute, h)our, d)ay, w)eek
	String category		// Identifies which task category that a transition will assigned to when building a runbook
	RoleType role 

	static belongsTo = [ workflow : Workflow ]
	static hasMany  = [ WorkflowTransitionMap ]
	
	static constraints = {
		workflow( nullable:false )
		code( blank:false, nullable:false, unique:'workflow' )
		name( blank:false, nullable:false )
		transId( nullable:false )
		type( blank:false, nullable:false, inList: ['process', 'boolean'] )
		color( blank:true, nullable:true)
		dashboardLabel( blank:true, nullable:true)
		predecessor( nullable:true)
		// TODO : set to blank:false and nullable:false once we update category for existing data.
		category( blank:true, nullable:true )
		header( blank:true, nullable:true)
		//effort( blank:true, nullable:true)
		duration( nullable:true)
		durationScale(nullable:true, blank:true, inList:['m','h','d','w'])
		role( nullable:false)
}	
	
	static mapping  = {
		version false
		id column:'workflow_transition_id'
		duration sqltype: 'mediumint'
		durationScale sqltype: 'char', length:1
	}
	def beforeInsert = {
		role = this.role ? this.role : RoleType.get('PROJ_MGR')
	}
	
	String toString() {
		"${workflow} : ${code}"
	}
}
