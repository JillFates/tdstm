import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Permissions
import net.transitionmanager.domain.RolePermissions
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PermissionsService
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PermissionsController implements ControllerMethods {

	static defaultAction = 'list'

	PermissionsService permissionsService

	@HasPermission(Permission.RolePermissionView)
	def show() {
		[permissions: permissionsService.findAll()]
	}

	@HasPermission(Permission.RolePermissionEdit)
	def edit() {
		[permissions: permissionsService.findAll()]
	}

	@HasPermission(Permission.RolePermissionEdit)
	def update() {
		permissionsService.update(params)
		redirect(action:"show")
	}
}
