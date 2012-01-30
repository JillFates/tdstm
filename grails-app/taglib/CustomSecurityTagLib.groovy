import java.text.DateFormat
import java.text.SimpleDateFormat

import org.jsecurity.SecurityUtils

import com.tdssrc.grails.GormUtil;

class CustomSecurityTagLib {
	static namespace = 'tds'
	/**
	 * @param : roles list
	 * @param : permission
	 * @return : boolean true|false
	 */
	def hasPermissionToAnyRole = { attrs ->
		def returnVal = false
		def roles = attrs['roles'];
		def permissionItem = attrs['permission']
		def permission = Permissions.findByPermissionItem(permissionItem)
		
		def subject = SecurityUtils.subject
		def hasRoles = subject.hasRoles(roles)
		if(hasRoles.ontains(true)){
			def hasPermission = RolePermissions.hasPermissionToAnyRole(roles, permission)
			if(hasPermission)
				returnVal = true
		}
		
		return returnVal
	}
	/**
	 * @param : role
	 * @param : permission
	 * @return : boolean true|false
	 */
	def hasPermission = { attrs ->
		def returnVal = false
		def role = attrs['roles'];
		def permissionItem = attrs['permission']
		def permission = Permissions.findByPermissionItem(permissionItem)
		
		def subject = SecurityUtils.subject
		def hasRole = subject.hasRole(role)
		if(hasRole){
			def hasPermission = RolePermissions.hasPermission(role, permission)
			if(hasPermission)
				returnVal = true
		}
		
		return returnVal
	}
	/**
	 * @param : roles list
	 * @param : permission
	 * @return : boolean true|false
	 */
	def lacksPermissionToAllRole = { attrs ->
		def returnVal = true
		def roles = attrs['roles'];
		def permissionItem = attrs['permission']
		def permission = Permissions.findByPermissionItem(permissionItem)
		
		def subject = SecurityUtils.subject
		def hasRoles = subject.hasRoles(roles)
		if(hasRoles.ontains(true)){
			def hasPermission = RolePermissions.lacksPermissionToAllRole(roles, permission)
			if(!hasPermission)
				returnVal = false
		}
		
		return returnVal
	}
}
