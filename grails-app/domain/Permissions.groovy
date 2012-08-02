
class Permissions {
	static enum Roles{ADMIN,PROJECT_ADMIN,PROJECT_MANAGER,SUPERVISOR,MANAGER,OBSERVER}

	String permissionItem
	PermissionGroup permissionGroup
	String description
	//Roles roles
	static mapping = { 
		version false 
	}
	static constraints = {
		permissionItem( blank:false, nullable:false,unique:true )
		permissionGroup( nullable:false )
		description( blank:true , nullable:true)
	}
}
