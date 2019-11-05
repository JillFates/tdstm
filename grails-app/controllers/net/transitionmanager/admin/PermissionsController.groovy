package net.transitionmanager.admin

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.security.PermissionsService

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
			try {
				permissionsService.update(params)
			}catch(ValidationException v){
				flash.message = errorsInValidation(v.errors)
			}catch(Exception e){
				flash.message = e.message
			}
		}.invalidToken {
			flash.message = message(code: 'invalid.csrf.token')
		}

		redirect(action: "show")
	}
}
