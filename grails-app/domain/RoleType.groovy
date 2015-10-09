class RoleType {
	
	/** Code that represent User Security Group types */
	static String SECURITY = "SECURITY"
	
	/** Code that represent Person Team types */
	static String TEAM = "TEAM"
	
	/** Code that represents Project type roles */
	static String PROJECT = "PROJECT"
	
	/** Code that represents Party roles */
	static String PARTY = "PARTY"
	
	/** Code that represents Application roles - do not think this is utilized */
	static String APP = "APP"

	String id
	String description
	String help
	String type
	Integer level

	static hasMany = [
		partyRoles: PartyRole
	]

	static constraints = {
		id (blank:false, nullable:false, size: 1..32)
		description (blank:true, nullable:true, size: 0..255)
		help (blank:true, nullable:true, size: 0..255)
		type (nullable:false, inList:[SECURITY, TEAM, PROJECT, PARTY, APP])
		level (nullable:false, range:0..100)
	}

	static mapping  = {
		version false
		id column: 'role_type_code', generator: 'assigned'
	}

	static transients = ['securityRole']

	/** 
	 * Used to determine if the given type is a security role type
	 */
	boolean isSecurityRole() {
		return ( type.equals(SECURITY) )
	}

	/**
	 * Used to determine if the current RoleType is a Team Role
	 */
	boolean isTeamRole() {
		return (type.equals(TEAM))
	}

	String toString(){
		description
	}
}