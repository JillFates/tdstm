package net.transitionmanager.security

import com.tdsops.tm.enums.domain.PasswordResetStatus
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.common.EmailDispatch
import net.transitionmanager.person.Person

import static com.tdsops.tm.enums.domain.PasswordResetStatus.PENDING

class PasswordReset {

	String token
	UserLogin userLogin
	PasswordResetStatus status = PENDING
	PasswordResetType type

	String        ipAddress                 // IP address the originated the request
	Person        createdBy                 // The individual that originated the ForgotMyPassword, null if by USER
	EmailDispatch emailDispatch      // Reference to the dispatch that will send the email to the person
	Date          expiresAfter                // Computed time in the future that token will expire

	Date createdDate = TimeUtil.nowGMT()
	Date lastModified

	static constraints = {
		createdBy nullable: true
		createdDate nullable: true
		emailDispatch nullable: true
		ipAddress nullable: true
		lastModified nullable: true
		token blank: false
	}

	static mapping = {
		version false
		id column: 'password_reset_id'
		expiresAfter sqltype: 'DateTime'
		createdDate sqltype: 'DateTime'
		lastModified sqltype: 'DateTime'
	}

	def beforeInsert = {
		createdDate = lastModified = TimeUtil.nowGMT()
	}

	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
