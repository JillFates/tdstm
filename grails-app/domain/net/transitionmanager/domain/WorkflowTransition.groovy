package net.transitionmanager.domain

/**
 * Stores the data found in the xml in the 'transitions' section
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
	Integer duration              // The duration to assign to tasks when building runbooks
	String durationScale = 'm'    // Scale that duration represents m)inute, h)our, d)ay, w)eek
	String category               // Identifies which task category that a transition will assigned to when building a runbook
	RoleType role

	static belongsTo = [workflow: Workflow]

	static hasMany = [WorkflowTransitionMap]

	static constraints = {
		// TODO : set to blank:false and nullable:false once we update category for existing data.
		category nullable: true
		code blank: false, unique: 'workflow'
		color nullable: true
		dashboardLabel nullable: true
		duration nullable: true
		durationScale nullable: true, inList: ['m', 'h', 'd', 'w']
		header nullable: true
		name blank: false
		predecessor nullable: true
		type blank: false, inList: ['process', 'boolean']
	}

	static mapping = {
		version false
		id column: 'workflow_transition_id'
		duration sqltype: 'mediumint'
		durationScale sqltype: 'char', length: 1
	}

	def beforeInsert = {
		if (!role) {
			role = RoleType.load('ROLE_PROJ_MGR')
		}
	}

	String toString() {
		"$workflow : $code"
	}
}
