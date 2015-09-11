package net.transitionmanager

import com.tdsops.tm.enums.domain.PasswordResetStatus
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdssrc.grails.TimeUtil

class PasswordReset {

	String token
	UserLogin userLogin
	PasswordResetStatus status = PasswordResetStatus.PENDING
	PasswordResetType type

	// IP address the originated the request
	String ipAddress 

	// The individual that originated the ForgotMyPassword, null if by USER
	Person createdBy 

	// Reference to the dispatch that will send the email to the person
	EmailDispatch emailDispatch 

	// Computed time in the future that token will expire
	Date expiresAfter 

	Date createdDate = TimeUtil.nowGMT()
	Date lastModified

	static constraints = {
		token ( blank: false, nullable:false )
		userLogin nullable:false
		ipAddress nullable:true
		createdBy nullable:true
		emailDispatch nullable:true
		expiresAfter nullable: false
		createdDate nullable:true
		lastModified nullable:true
	}

	static mapping  = {
		version false
		id column:'password_reset_id'
		expiresAfter sqltype: 'DateTime'
		createdDate sqltype: 'DateTime'
		lastModified sqltype: 'DateTime'
	}

	def beforeInsert = {
		createdDate = TimeUtil.nowGMT()
		lastModified = TimeUtil.nowGMT()
	}

	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

}