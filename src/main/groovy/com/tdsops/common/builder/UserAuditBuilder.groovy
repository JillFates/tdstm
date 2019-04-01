package com.tdsops.common.builder

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.UserAuditClassification
import com.tdsops.tm.enums.domain.UserAuditSeverity
import net.transitionmanager.security.UserAudit
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.SecurityService
import org.springframework.web.context.request.RequestContextHolder

import static com.tdsops.tm.enums.domain.UserAuditClassification.ADMIN
import static com.tdsops.tm.enums.domain.UserAuditClassification.LOGIN
import static com.tdsops.tm.enums.domain.UserAuditClassification.USER_MGMT

/**
 * Defines the different UserAudit instances that can be created
 */
class UserAuditBuilder {

	@Lazy
	private static SecurityService securityService = { -> ApplicationContextHolder.getBean('securityService', SecurityService) }()

	/**
	 * Creates an UserAudit to indicate that the user login in to the system
	 */
	static UserAudit login() {
		create LOGIN, 'User login'
	}

	/**
	 * Creates an UserAudit to indicate that the user logout in to the system
	 */
	static UserAudit logout() {
		create LOGIN, 'User logout'
	}

	static UserAudit restartedApplication() {
		create ADMIN, 'Initiated an application restart'
	}
	
	/**
	 * Creates an UserAudit to indicate that a new user was created
	 */
	static UserAudit newUserLogin(String newUserName) {
		create USER_MGMT, 'New user login created: ' + newUserName
	}

	/**
	 * Creates an UserAudit to indicate that the user changed his password
	 */
	static UserAudit userLoginPasswordChanged(UserLogin userLogin = null) {
		create USER_MGMT, 'User password changed', null, userLogin
	}

	/**
	 * Creates an UserAudit to indicate that the user changed a project configuration
	 */
	static UserAudit projectConfig(Project project) {
		create ADMIN, 'Project configuration changed', project
	}

	/**
	 * Creates an UserAudit to indicate that the user account was locked out due to inactivity
	 */
	static UserAudit userAccountWasLockedOutDueToInactivity(UserLogin userLogin) {
		create LOGIN, 'User\'s account was locked out due to inactivity', null, userLogin
	}

	/**
	 * Creates an UserAudit to indicate that the user account was unlocked by someone
	 */
	static UserAudit userAccountWasUnlockedBy(String unlockedBy, UserLogin userLogin) {
		create USER_MGMT, 'User\'s account was unlocked by (' + unlockedBy + ')', null, userLogin
	}

	private static UserAudit create(UserAuditClassification classification, 
			String message,
	        Project project = null, UserLogin userLogin = null) {

		new UserAudit(
			userLogin: userLogin ?: loadCurrentUserLogin(),
			project: project,
			ipAddress: RequestContextHolder.requestAttributes ? RequestContextHolder.currentRequestAttributes().request.remoteAddr: null,
			severity: UserAuditSeverity.INFO,
			securityRelevant: false,
			classification: classification,
			message: message)
	}

	private static UserLogin loadCurrentUserLogin() {
		securityService.loadCurrentUserLogin()
	}
}
