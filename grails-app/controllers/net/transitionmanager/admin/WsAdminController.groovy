package net.transitionmanager.admin

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class WsAdminController implements ControllerMethods {

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then
	 * it returns all unassigned applications for the user's current project
	 */
	@HasPermission(Permission.UserUnlock)
	def unlockAccount() {
		securityService.unlockAccount(UserLogin.get(params.id))
		renderSuccessJson()
	}
}
