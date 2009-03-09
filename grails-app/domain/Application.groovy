/**
 * The Application domain represents the various applications that operate on the servers.  This
 * helps in managing contacts and groupings of servers by purpose.
 */
class Application extends PartyGroup {

	String 		appCode		// 
	PartyGroup	owner		// The company that owns the application
	String 		environment	// Indicates what environment that the application runs in (potential Attribute)

	static constraints = {
		owner(blank:false, nullable:false)	
		name(blank:false, nullable:false) // related party Group
		appCode(blank:false, nullable:false	)
		environment (
			blank:false, nullable:false, 
			inList:["Production", "Test", "Staging", "Development", "Other"]
		)
		dateCreated( ) // related to party
		lastUpdated( ) // related to party
	}

	static mapping  = {
		version true
		id column: 'app_id'
		columns {
			trackChanges sqlType: 'char(1)'
			code sqlType: 'varchar(20)', index: 'project_projectcode_idx', unique: true
		}
	}
	
	// TODO : Add unique validation on CompanyId + appCode
	
	String toString() {
		"$name ($projectCode)"
	}
}
