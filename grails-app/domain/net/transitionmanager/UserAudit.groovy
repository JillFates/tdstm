package net.transitionmanager

import com.tdsops.tm.enums.domain.UserAuditSeverity
import com.tdsops.tm.enums.domain.UserAuditClassification
import com.tdssrc.grails.TimeUtil

class UserAudit {

	UserLogin userLogin
	Project project
	Date createdDate
	String ipAddress
	UserAuditSeverity severity
	Boolean securityRelevant = false
	UserAuditClassification classification
	String message

	static constraints = {
		userLogin nullable:false
		createdDate nullable:true
		project nullable:true
		message nullable:false
		ipAddress nullable:true
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'user_audit_id'
		createdDate sqltype: 'DateTime'
		message sqlType: 'varchar(255)'
		ipAddress sqlType: 'varchar(15)'
	}

	def beforeInsert = {
		createdDate = TimeUtil.nowGMT()
	}

} 