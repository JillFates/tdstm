class RoleType {
	
	/* ********************************************************************* */

	/** Default type for description beginning with: System. */
	static String SECURITY = "SECURITY"
	
	/** Default type for description beginning with: Staff. */
	static String TEAM = "TEAM"
	
	/** Default type for description beginning with: Project. */
	static String PROJECT = "PROJECT"
	
	/** Default type for description beginning with: Party. */
	static String PARTY = "PARTY"
	
	/** Default type for description beginning with: App. */
	static String APP = "APP"

	/* ********************************************************************* */

	String id
	String description
	String help
	String type
	Integer level


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
		return (description ==~ /^System.:.*/)
	}

	String toString(){
		description
	}
}