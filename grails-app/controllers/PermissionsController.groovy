import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Permissions
import net.transitionmanager.domain.RolePermissions
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PermissionsController implements ControllerMethods {

	static defaultAction = 'list'

	JdbcTemplate jdbcTemplate

	@HasPermission('RolePermissionView')
	def show() {
		def permissions = Permissions.withCriteria {
			and {
			   order('permissionGroup','asc')
			   order('permissionItem','asc')
			}
		}
		[permissions:permissions]
	}

	@HasPermission('RolePermissionView')
	def edit() {
		def permissions = Permissions.withCriteria {
			and {
			   order('permissionGroup','asc')
		   order('permission    Item','asc')
			}
		}
		[permissions:permissions]
	}

	def update() {
		def paramList = params.column
		jdbcTemplate.update("delete from role_permissions")
		for (Permissions permission in Permissions.list()) {
			for (String role in Permissions.Roles.NAMES) {
				def param = params['role_' + permission.id + '_' + role]
				if (param == "on") {
					def rolePermissions = new RolePermissions(role: role, permission: permission)
					if (!rolePermissions.save(flush: true)) {
						println "Error while updating rolePermissions : $rolePermissions"
						rolePermissions.errors.each { println it }
					}
				}
			}
		}
		for(String id in paramList){
			Permissions permissions = Permissions.get(id)
			if(permissions){
				permissions.description = params["description_"+id]
				if(!permissions.save(flush:true)){
					permissions.errors.allErrors.each {
						println it
				    }
			    }
			}
		}
		redirect(action:"show")
	}
}
