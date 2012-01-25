
class Permissions {
	static enum Roles{ADMIN,PROJECT_ADMIN,PROJECT_MANAGER,SUPERVISOR,MANAGER,OBSERVER}

	String permissionItem
	PermissionGroup permissionGroup
	//Roles roles
	static mapping = { 
		version false 
	}
	static constraints = {
		permissionItem( blank:false, nullable:false )
		permissionGroup( blank:false, nullable:false )
	}
}
