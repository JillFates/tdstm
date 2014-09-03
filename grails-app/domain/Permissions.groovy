
class Permissions {
	static enum Roles{ADMIN,CLIENT_ADMIN,CLIENT_MGR,SUPERVISOR,EDITOR,USER}

	String permissionItem
	PermissionGroup permissionGroup
	String description

	static mapping = { 
		version false 
	}
	
	static hasMany = [ rolePermissions: RolePermissions ]

	static constraints = {
		permissionItem( blank:false, nullable:false,unique:true )
		permissionGroup( nullable:false )
		description( blank:true , nullable:true)
		rolePermissions cascade: "all-delete-orphan"
	}

	String toString() {
		return permissionItem
	}
}
