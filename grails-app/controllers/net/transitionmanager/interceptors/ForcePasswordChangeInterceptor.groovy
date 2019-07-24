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
		if (userLogin?.forcePasswordChange == 'Y' && !WebUtil.isAjax(request)) {

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
