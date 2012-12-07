
class Permissions {
	static enum Roles{ADMIN,CLIENT_ADMIN,CLIENT_MGR,SUPERVISOR,EDITOR,USER}

	String permissionItem
	PermissionGroup permissionGroup
	String description

	static mapping = { 
		version false 
	}
	
	static constraints = {
		permissionItem( blank:false, nullable:false,unique:true )
		permissionGroup( nullable:false )
		description( blank:true , nullable:true)
	}
}
