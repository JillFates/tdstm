package com.tdsops.common.builder

import net.transitionmanager.UserAudit
import com.tdsops.tm.enums.domain.UserAuditSeverity
import com.tdsops.tm.enums.domain.UserAuditClassification
import org.springframework.web.context.request.RequestContextHolder

/**
 * This class defines the different UserAudit instances that can be created
 */
class UserAuditBuilder {

	/**
	 * Creates an UserAudit to indicate that the user login in to the system
	 */
	public static login(userLogin) {
		UserAudit userAudit = new UserAudit()

		userAudit.userLogin = userLogin
		userAudit.project = null
		userAudit.ipAddress = RequestContextHolder.currentRequestAttributes().request.remoteAddr
		userAudit.severity = UserAuditSeverity.INFO
		userAudit.securityRelevant = false
		userAudit.classification = UserAuditClassification.LOGIN
		userAudit.message = "User login"

		return userAudit
	}

	/**
	 * Creates an UserAudit to indicate that the user logout in to the system
	 */
	public static logout(userLogin) {
		UserAudit userAudit = new UserAudit()

		userAudit.userLogin = userLogin
		userAudit.project = null
		userAudit.ipAddress = RequestContextHolder.currentRequestAttributes().request.remoteAddr
		userAudit.severity = UserAuditSeverity.INFO
		userAudit.securityRelevant = false
		userAudit.classification = UserAuditClassification.LOGIN
		userAudit.message = "User logout"

		return userAudit
	}

	/**
	 * Creates an UserAudit to indicate that a new user was created
	 */
	public static newUserLogin(userLogin, newUserName) {
		UserAudit userAudit = new UserAudit()

		userAudit.userLogin = userLogin
		userAudit.project = null
		userAudit.ipAddress = RequestContextHolder.currentRequestAttributes().request.remoteAddr
		userAudit.severity = UserAuditSeverity.INFO
		userAudit.securityRelevant = false
		userAudit.classification = UserAuditClassification.USER_MGMT
		userAudit.message = "New user login created: " + newUserName

		return userAudit
	}

	/**
	 * Creates an UserAudit to indicate that the user changed his password
	 */
	public static userLoginPasswordChanged(userLogin) {
		UserAudit userAudit = new UserAudit()

		userAudit.userLogin = userLogin
		userAudit.project = null
		userAudit.ipAddress = RequestContextHolder.currentRequestAttributes().request.remoteAddr
		userAudit.severity = UserAuditSeverity.INFO
		userAudit.securityRelevant = false
		userAudit.classification = UserAuditClassification.USER_MGMT
		userAudit.message = "User password changed"

		return userAudit
	}

	/**
	 * Creates an UserAudit to indicate that the user changed a project configuration
	 */
	public static projectConfig(userLogin, project) {
		UserAudit userAudit = new UserAudit()

		userAudit.userLogin = userLogin
		userAudit.project = project
		userAudit.ipAddress = RequestContextHolder.currentRequestAttributes().request.remoteAddr
		userAudit.severity = UserAuditSeverity.INFO
		userAudit.securityRelevant = false
		userAudit.classification = UserAuditClassification.ADMIN
		userAudit.message = "Project configuration changed"

		return userAudit
	}

}
