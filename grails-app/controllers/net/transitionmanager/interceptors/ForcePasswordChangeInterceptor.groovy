package net.transitionmanager.interceptors

import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.SecurityService

class ForcePasswordChangeInterceptor {

	SecurityService securityService

	ForcePasswordChangeInterceptor() {
		matchAll()
	}


	boolean before() {
		UserLogin userLogin = securityService.userLogin

		if (userLogin?.forcePasswordChange == 'Y') {

			if ((controllerName == 'auth' && ['login', 'signIn', 'signOut'].contains(actionName)) ||
				(controllerName == 'userLogin' && ['changePassword', 'updatePassword'].contains(actionName))) {
				return true
			}

			flash.message = "Your password has expired and must be changed"
			redirect(controller: 'userLogin', action: 'changePassword', params: [userLoginInstance: userLogin])
			return false
		}

		return true
	}
}
