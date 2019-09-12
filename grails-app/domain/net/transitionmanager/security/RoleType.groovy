package net.transitionmanager.security

import net.transitionmanager.party.PartyRole

class RoleType {

	static final String ROLE_PREFIX = 'ROLE_'

	/* Role Types | Each of this works as a category that groups one or more Role Type Codes */
	/* Code that represent User Security Group types */
	public static final String TYPE_SECURITY = 'SECURITY'
	/* Code that represent Person Team types */
	public static final String TYPE_TEAM     = 'TEAM'
	/* Code that represents Project type roles */
	public static final String TYPE_PROJECT  = 'PROJECT'
	/* Code that represents Party roles */
	public static final String TYPE_PARTY    = 'PARTY'
	/* Code that represents Application roles - do not think this is utilized */
	public static final String TYPE_APP      = 'APP'

	/* Role Type Codes for Role Type TEAM */
	public static final String CODE_TEAM_AUTO      = 'AUTO'
	public static final String CODE_TEAM_CLEANER   = 'CLEANER'
	public static final String CODE_TEAM_DB_ADMIN  = 'DB_ADMIN'
	public static final String CODE_TEAM_MOVE_MGR  = 'MOVE_MGR'
	public static final String CODE_TEAM_MOVE_TECH = 'MOVE_TECH'
	public static final String CODE_TEAM_PROJ_MGR  = 'PROJ_MGR'
	public static final String CODE_TEAM_SYS_ADMIN = 'SYS_ADMIN'

	/* Role Type Codes for Role Type PROJECT */
	public static final String CODE_PROJECT_MOVE_BUNDLE = 'MOVE_BUNDLE'

	/* Role Type Codes for Role Type Party */
	public static final String CODE_PARTY_CLIENT  = 'CLIENT'
	public static final String CODE_PARTY_COMPANY = 'COMPANY'
	public static final String CODE_PARTY_PARTNER = 'PARTNER'
	public static final String CODE_PARTY_PROJECT = 'PROJECT'
	public static final String CODE_PARTY_STAFF   = 'STAFF'
	public static final String CODE_PARTY_TEAM    = 'TEAM'
	public static final String CODE_TEAM_MEMBER   = 'TEAM_MEMBER'

	/* Just represents something without a role associated */
	public static final String NO_ROLE = "NO_ROLE"

	String id
	String description
	String help
	String type
	Integer level

	static hasMany = [partyRoles: PartyRole]

	static constraints = {
		id blank: false, size: 1..32
		description nullable: true
		help nullable: true
		type nullable: false, inList: [TYPE_SECURITY, TYPE_TEAM, TYPE_PROJECT, TYPE_PARTY, TYPE_APP]
		level range: 0..100
	}

	static mapping = {
		version false
		id column: 'role_type_code', generator: 'assigned'
	}
	static transients = ['securityRole']

	/**
	 * Determine if the given type is a security role type
	 */
	boolean isSecurityRole() {
		type == TYPE_SECURITY
	}

	/**
	 * Determine if the current RoleType is a Team Role
	 */
	boolean isTeamRole() {
		type == TYPE_TEAM
	}

	String toString() {
		description
	}

	def beforeValidate() {
		if (level == null && type != TYPE_SECURITY) {
			level = 0
		}
	}

	/**
	 * Uses get() and does a client-side check that the type matches assuming that the get() call
	 * will be very similar in performance to the dynamic finder but cached much more efficiently.
	 * @param id the id````
	 * @param type the type
	 * @return the RoleType if found
	 */
	static RoleType findByIdAndType(String id, String type) {
		RoleType roleType = get(id)
		roleType.type == type ? roleType : null
	}
}
