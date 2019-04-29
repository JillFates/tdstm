import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PermissionsService

@Secured('isAuthenticated()')
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
		withForm {
			permissionsService.update(params)
		}.invalidToken {
			flash.message = INVALID_CSRF_TOKEN
		}

		redirect(action: "show")
	}
}
