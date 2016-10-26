package com.tdsops.common.security.spring

import com.tdsops.common.security.SecurityUtil
import groovy.transform.CompileStatic
import net.transitionmanager.service.SecurityService
import org.springframework.security.authentication.encoding.PasswordEncoder

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsPasswordEncoder implements PasswordEncoder {

	SecurityService securityService

	String encodePassword(String rawPass, salt) {
		SecurityUtil.encrypt rawPass, salt as String
	}

	boolean isPasswordValid(String encPass, String rawPass, salt) {

		// checkPassword

		if (encodePassword(rawPass, salt) == encPass) {
			return true
		}

		if (SecurityUtil.encryptLegacy(rawPass) != encPass) {
			return false
		}

		// checkLegacyPassword

		if (securityService.userLocalConfig.forceUseNewEncryption) {
			securityService.forcePasswordChange()
		}

		true
	}
}
