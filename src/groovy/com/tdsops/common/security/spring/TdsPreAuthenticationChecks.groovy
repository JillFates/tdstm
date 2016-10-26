package com.tdsops.common.security.spring

import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.eclipse.jdt.internal.core.Assert
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker

import java.text.DateFormat

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j(value='logger')
class TdsPreAuthenticationChecks implements UserDetailsChecker, InitializingBean {

	AuditService auditService
	SecurityService securityService
	UserPreferenceService userPreferenceService

	@Transactional
	void check(UserDetails user) {

		logger.debug '''start 'pre' checks for "{}" ''', user.username

		// checkExpirationDate

		if (!user.isAccountNonExpired()) {
			auditService.logMessage("User $user.username attempted access while account is expired")
			throw new LockedException('Your account has expired. Please contact support to have your account reactivated.')
		}

		// checkLockOutStatus

		UserLogin userLogin = UserLogin.findWhere(username: user.username)

		if (userLogin.lockedOutUntil) {
			if (userLogin.lockedOutUntil.time <= System.currentTimeMillis()) {
				// unlock the account since the expiration date of the lockout has passed
				userLogin.lockedOutUntil = null
				userLogin.failedLoginAttempts = 0
			}
			else {
				auditService.logMessage("User $user.username attempted access while account is locked")

				if (securityService.userLocalConfig.failedLoginLockoutPeriodMinutes == 0) {
					throw new LockedException('Your account is presently locked. Please contact support to unlock your account.')
				}

				String tzId = userPreferenceService.getTimeZone()

				String lockoutUntil = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME).format(
						TimeUtil.formatDateTimeWithTZ(tzId, TimeUtil.defaultFormatType,
						userLogin.lockedOutUntil, TimeUtil.FORMAT_DATE_TIME_2))
				throw new LockedException('Your account is presently locked until ' + lockoutUntil +
						'. You may wait or contact support to have your account unlocked.')
			}
		}

		// checkUserActive

		if (!user.isEnabled()) {
			auditService.logMessage("User $user.username attempted access while account is inactive")
			throw new DisabledException('Your account has been disabled. Please contact support to have your account reactivated.')
		}
	}

	void afterPropertiesSet() {
		Assert.isNotNull auditService, 'auditService is required'
		Assert.isNotNull securityService, 'securityService is required'
		Assert.isNotNull userPreferenceService, 'userPreferenceService is required'
	}
}
