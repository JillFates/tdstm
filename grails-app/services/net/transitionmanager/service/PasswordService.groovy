package net.transitionmanager.service

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import net.transitionmanager.security.UserLogin

class PasswordService {
	SpringSecurityService springSecurityService

	@Transactional
	void forcePasswordChange(UserLogin user = null) {
		if (!user) {
			user = getUserLogin()
		}

		if (user.forcePasswordChange != 'Y') {
			user.forcePasswordChange = 'Y'
			user.save(flush:true )
		}
	}

	/**
	 * Get the UserLogin object of the currently logged in user or null if user is not logged in
	 */
	@Transactional(readOnly = true)
	UserLogin getUserLogin() {
		UserLogin.get springSecurityService.currentUserId
	}
}
