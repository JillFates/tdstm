
class RolePermissions {
	String role
	Permissions permission
	
	static mapping = {
		version false
	}
	
    static constraints = {
		role( blank:false, nullable:false )
		permission( blank:false, nullable:false )
    }
	
}
