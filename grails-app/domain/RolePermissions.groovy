import org.apache.shiro.SecurityUtils

class RolePermissions {
	String role
	Permissions permission
	
	static mapping = {
		version false
	}
	
    static constraints = {
		role( blank:false, nullable:false )
		permission( nullable:false )
    }
	
	// Helper methods
	static Boolean hasPermission( permissionItems){
		def returnVal = false
		def permissionItem = permissionItems
		def permission = Permissions.findByPermissionItem(permissionItem)
		
		List roles = []
		def subject = SecurityUtils.subject
		Permissions.Roles.values().each{
			if(subject.hasRole(it.toString())){
				roles << it.toString()
			}
		}
		if(roles.size()>0){
			def hasPermission = RolePermissions.hasPermissionToAnyRole(roles, permission)
			if(hasPermission)
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
