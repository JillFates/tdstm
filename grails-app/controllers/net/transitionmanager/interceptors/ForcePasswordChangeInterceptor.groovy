package net.transitionmanager.interceptors

import com.tdssrc.grails.WebUtil
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.SecurityService

class ForcePasswordChangeInterceptor {

	SecurityService securityService

	ForcePasswordChangeInterceptor() {
		matchAll()
	}


	boolean before() {
		UserLogin userLogin = securityService.userLogin
		// controllerNames in this list shouldn't be excluded. Otherwise they may cause weird behavior like multiple redirects.
		List excludedControllerNames = [null, 'css', 'dist', 'images']
		// Check if the user is being forced to update their password (and the request is not AJAX).
		if (userLogin?.forcePasswordChange == 'Y' && !WebUtil.isAjax(request) && !excludedControllerNames.contains(controllerName)) {

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
