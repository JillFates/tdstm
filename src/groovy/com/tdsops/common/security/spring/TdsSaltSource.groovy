package com.tdsops.common.security.spring

import groovy.transform.CompileStatic
import org.springframework.security.authentication.dao.SaltSource
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsSaltSource implements SaltSource {

	def getSalt(UserDetails user) {
		Assert.isInstanceOf TdsUserDetails, user, 'Only TdsUserDetails is supported'
		((TdsUserDetails) user).salt
	}
}
