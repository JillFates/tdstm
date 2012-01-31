
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
	
	// Helper methods
	static Boolean hasPermission(role, permission){
		def returnVal = false
		def rolePermissions = RolePermissions.findByPermissionAndRole(permission, role)
		if(rolePermissions){
			returnVal = true
		}
		return returnVal
	}
	
	static Boolean hasAnyPermissionToRole(role, permissions){
		def returnVal = false
		def rolePermissions = RolePermissions.createCriteria().list {
			and {
				eq ('role', role)
				'in'('permission', permissions )
			}
		}
		if(rolePermissions.size()>0){
			returnVal = true
		}
		return returnVal
	}
	
	static Boolean hasPermissionToAnyRole(roles, permission){
		def returnVal = false
		def rolePermissions = RolePermissions.findAllByPermissionAndRoleInList(permission,roles)
		if(rolePermissions.size()>0){
			returnVal = true
		}
		return returnVal
	}
	
	static Boolean lacksPermissionToAllRole(role, permission){
		def returnVal = false
		def rolePermissions = RolePermissions.createCriteria().list {
			and {
				'in' ('role', role)
				eq('permission', permission )
			}
		}
		if(rolePermissions.size() == 0){
			returnVal = true
		}
		return returnVal
	}
	
}
