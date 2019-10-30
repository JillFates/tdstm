package com.tdsops.common.security.spring

import com.tdsops.common.security.SecurityUtil
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import net.transitionmanager.security.PasswordService
/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsPasswordEncoder {
	GrailsApplication grailsApplication
	PasswordService passwordService

	String encodePassword(String rawPass, salt) {
		SecurityUtil.encrypt rawPass, salt as String
	}

	boolean isPasswordValid(String encPass, String rawPass, salt) {

		// checkPassword
		if (encodePassword(rawPass, salt) == encPass) {
			return true
		}

		// check using the legacy method
		if (SecurityUtil.encryptLegacy(rawPass) == encPass) {
			return true
		}

		return false
	}
}
