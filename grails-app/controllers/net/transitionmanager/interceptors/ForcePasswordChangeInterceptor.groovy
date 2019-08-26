package net.transitionmanager.interceptors

import com.tdssrc.grails.WebUtil
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.SecurityService

class ForcePasswordChangeInterceptor {

	SecurityService securityService

	// controllerNames in this list must be excluded. Otherwise they may cause weird behavior like multiple redirects.
	static final List EXCLUDED_CONTROLLER_NAMES = [null, 'css', 'dist', 'images'].asImmutable()

	ForcePasswordChangeInterceptor() {
		matchAll()
	}


	boolean before() {
		UserLogin userLogin = securityService.userLogin

		// Check if the user is being forced to update their password (and the request is not AJAX).
		if (userLogin?.forcePasswordChange == 'Y' && !WebUtil.isAjax(request) && !EXCLUDED_CONTROLLER_NAMES.contains(controllerName)) {

			if ((controllerName == 'auth' && ['login', 'signIn', 'signOut'].contains(actionName)) ||
				(controllerName == 'userLogin' && ['changePassword', 'updatePassword'].contains(actionName))) {
				return true
			}

			flash.message = "Your password has expired and must be changed"
			redirect(uri: '/module/auth/changePassword')
			return false
		}

		return true
	}
}
