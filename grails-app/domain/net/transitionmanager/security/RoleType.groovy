package net.transitionmanager.security

import net.transitionmanager.party.PartyRole

class RoleType {


	/* Role Types | Each of this works as a category that groups one or more Role Type Codes */
	/* Code that represent User Security Group types */
	public static final String SECURITY = 'ROLE_SECURITY'
	/* Code that represent Person Team types */
	public static final String TEAM = 'TEAM'
	/* Code that represents Project type roles */
	public static final String PROJECT = 'PROJECT'
	/* Code that represents Party roles */
	public static final String PARTY = 'PARTY'
	/* Code that represents Application roles - do not think this is utilized */
	public static final String APP = 'APP'


	/* Role Type Codes for Role Type ROLE_SECURITY */
	public static final String CODE_ROLE_ADMIN = 'ROLE_ADMIN'
	public static final String CODE_ROLE_CLIENT_ADMIN = 'ROLE_CLIENT_ADMIN'
	public static final String CODE_ROLE_CLIENT_MGR = 'ROLE_CLIENT_MGR'
	public static final String CODE_ROLE_EDITOR = 'ROLE_EDITOR'
	public static final String CODE_ROLE_SUPERVISOR = 'ROLE_SUPERVISOR'
	public static final String CODE_ROLE_USER = 'ROLE_USER'

	/* Role Type Codes for Role Type TEAM */
	public static final String ODE_ACCT_MGR = 'ACCT_MGR'
	public static final String CODE_APP_COORD = 'APP_COORD'
	public static final String CODE_AUTO = 'AUTO'
	public static final String CODE_BACKUP_ADMIN = 'BACKUP_ADMIN'
	public static final String CODE_CLEANER = 'CLEANER'
	public static final String CODE_DBA_DB2 = 'DBA_DB2'
	public static final String CODE_DB_ADMIN = 'DB_ADMIN'
	public static final String CODE_DB_ADMIN_MS = 'DB_ADMIN_MS'
	public static final String CODE_DB_ADMIN_ORA = 'DB_ADMIN_ORA'
	public static final String CODE_MIG_ANALYST = 'MIG_ANALYST'
	public static final String CODE_MIG_LEAD = 'MIG_LEAD'
	public static final String CODE_MOVE_MGR = 'MOVE_MGR'
	public static final String CODE_MOVE_TECH = 'MOVE_TECH'
	public static final String CODE_MOVE_TECH_SR = 'MOVE_TECH_SR'
	public static final String CODE_NETWORK_ADMIN = 'NETWORK_ADMIN'
	public static final String CODE_PROJ_ADMIN = 'PROJ_ADMIN'
	public static final String CODE_PROJ_MGR = 'PROJ_MGR'
	public static final String CODE_STOR_ADMIN = 'STOR_ADMIN'
	public static final String CODE_SYS_ADMIN = 'SYS_ADMIN'
	public static final String CODE_SYS_ADMIN_AIX = 'SYS_ADMIN_AIX'
	public static final String CODE_SYS_ADMIN_LNX = 'SYS_ADMIN_LNX'
	public static final String CODE_SYS_ADMIN_UNIX = 'SYS_ADMIN_UNIX'
	public static final String CODE_SYS_ADMIN_WIN = 'SYS_ADMIN_WIN'
	public static final String CODE_TECH = 'TECH'
	public static final String CODE_VM_ADMIN = 'VM_ADMIN'
	public static final String CODE_VM_ADMIN_AWS = 'VM_ADMIN_AWS'
	public static final String CODE_VM_ADMIN_EC2 = 'VM_ADMIN_EC2'
	public static final String CODE_VM_ADMIN_HYPERV = 'VM_ADMIN_HYPERV'
	public static final String CODE_VM_ADMIN_UCS = 'VM_ADMIN_UCS'
	public static final String CODE_VM_ADMIN_VMWARE = 'VM_ADMIN_VMWARE'
	public static final String CODE_VM_ADMIN_XEN = 'VM_ADMIN_XEN'

	/* Role Type Codes for Role Type APP */
	public static final String CODE_APP_1ST_CONTACT = 'APP_1ST_CONTACT'
	public static final String CODE_APP_2ND_CONTACT = 'APP_2ND_CONTACT'
	public static final String CODE_APP_OWNER = 'APP_OWNER'
	public static final String CODE_APP_SME = 'APP_SME'

	/* Role Type Codes for Role Type PROJECT */
	public static final String CODE_MOVE_BUNDLE = 'MOVE_BUNDLE'

	/* Role Type Codes for Role Type Party */
	public static final String CODE_APP_ROLE = 'APP_ROLE'
	public static final String CODE_CLIENT = 'CLIENT'
	public static final String CODE_COMPANY = 'COMPANY'
	public static final String CODE_PARTNER = 'PARTNER'
	public static final String CODE_PROJECT = 'PROJECT'
	public static final String CODE_STAFF = 'STAFF'
	public static final String CODE_TEAM = 'TEAM'
	public static final String CODE_TEAM_MEMBER = 'TEAM_MEMBER'
	public static final String CODE_VENDOR = 'VENDOR'

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
