package net.transitionmanager.security

import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.TdsPasswordEncoder
import com.tdsops.common.security.spring.TdsSaltSource
import com.tdsops.common.security.spring.TdsUserDetails
import grails.compiler.GrailsCompileStatic
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

@GrailsCompileStatic
class CustomSecurityProvider extends DaoAuthenticationProvider {
	PasswordService passwordService
	TdsPasswordEncoder tdsPasswordEncoder
	TdsSaltSource tdsSaltSource

	@Value('${tdstm.security.localUser.forceUseNewEncryption}')
	boolean forceUseNewEncryption

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		Object salt = null;

		if (this.tdsSaltSource != null) {
			salt = this.tdsSaltSource.getSalt(userDetails);
		}

		if (authentication.getCredentials() == null) {
			logger.debug("Authentication failed: no credentials provided");

			throw new BadCredentialsException(messages.getMessage(
				"AbstractUserDetailsAuthenticationProvider.badCredentials",
				"Bad credentials"));
		}

		String presentedPassword = authentication.getCredentials().toString();



		if (!tdsPasswordEncoder.isPasswordValid(userDetails.getPassword(), presentedPassword, salt)) {
			logger.debug("Authentication failed: password does not match stored value");

			throw new BadCredentialsException(messages.getMessage(
				"AbstractUserDetailsAuthenticationProvider.badCredentials",
				"Bad credentials"));
		}

		if (SecurityUtil.encryptLegacy(presentedPassword) == userDetails.getPassword() && forceUseNewEncryption) {
			UserLogin.withNewSession {
				TdsUserDetails tdsUser = (TdsUserDetails)userDetails
				passwordService.forcePasswordChange(UserLogin.get((Long)tdsUser.id))
			}
		}
	}
}
