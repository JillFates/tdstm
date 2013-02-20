import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.shiro.SecurityUtils

import com.tdssrc.grails.GormUtil;

class CustomSecurityTagLib {
	static namespace = 'tds'
	/**
	 * @param : role
	 * @param : permission
	 * @return : boolean true|false
	 */
	def hasPermission = { attrs, body ->
		def returnVal = false
		def permissionItem = attrs['permission']
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
		
		if(returnVal)
		  out << body()
	}
}
