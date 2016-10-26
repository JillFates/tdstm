package com.tdsops.common.security.spring

import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.SecurityService
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j(value='logger')
class TdsPostAuthenticationChecks implements UserDetailsChecker {

	private static final int MILLIS_IN_ONE_DAY = 24 * 60 * 60 * 1000

	SecurityService securityService

	@Transactional
	void check(UserDetails user) {

		logger.debug '''start 'post' checks for "{}" ''', user.username

		UserLogin userLogin = UserLogin.findWhere(username: user.username)

		// checkPasswordExpirationDate

		boolean passwordExpired = !userLogin.passwordNeverExpires && userLogin.passwordExpirationDate &&
				userLogin.passwordExpirationDate.time <= System.currentTimeMillis()
		if (passwordExpired) {
			// no exception, just set the flag to force a password update
			securityService.forcePasswordChange()
		}

		// checkPasswordAge

		int maxAgeDays = (int) securityService.userLocalConfig.maxPasswordAgeDays
		if (maxAgeDays && userLogin.passwordChangedDate &&
				userLogin.passwordChangedDate.time + maxAgeDays * MILLIS_IN_ONE_DAY < System.currentTimeMillis()) {

			// no exception, just set the flag to force a password update
			securityService.forcePasswordChange()
		}

		// checkIfUserHasRoles

		if (!user.authorities) {
			throw new DisabledException('Your account has no assigned security role. ' +
					'Please contact support to have your account updated.')
		}
	}
}
