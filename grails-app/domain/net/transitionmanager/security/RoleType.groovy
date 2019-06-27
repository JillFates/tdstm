package net.transitionmanager.security

import net.transitionmanager.party.PartyRole

class RoleType {


	/* Role Types | Each of this works as a category that groups one or more Role Type Codes */
	/** Code that represent User Security Group types */
	public static final String SECURITY = 'ROLE_SECURITY'

	/** Code that represent Person Team types */
	public static final String TEAM = 'TEAM'

	/** Code that represents Project type roles */
	public static final String PROJECT = 'PROJECT'

	/** Code that represents Party roles */
	public static final String PARTY = 'PARTY'

	/** Code that represents Application roles - do not think this is utilized */
	public static final String APP = 'APP'


	/* Role Type Codes | Each of this is a role in the system, and is grouped by one Role Type */
	/** Code that represents STAFF in the system - Is grouped under TEAM */
	public static final String STAFF = 'STAFF'


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
		type nullable: false, inList: [SECURITY, TEAM, PROJECT, PARTY, APP]
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
		type == SECURITY
	}

	/**
	 * Determine if the current RoleType is a Team Role
	 */
	boolean isTeamRole() {
		type == TEAM
	}

	String toString() {
		description
	}

	def beforeValidate() {
		if (level == null && type != 'ROLE_SECURITY') {
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
