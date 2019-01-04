package net.transitionmanager.interceptors


import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserService

class LastPageLoadInterceptor {
	SecurityService securityService
	UserService     userService

	LastPageLoadInterceptor() {
		matchAll()
	}

	boolean after() {
		if (request.getAttribute('tds_initialRequest') != 'auth/signIn') {
			// We don't want to update lastPageLoad when logging in
			if (securityService.loggedIn) {
				userService.updateLastPageLoad()
			}
		}

		return true
	}
}
