package net.transitionmanager.security

import com.tdsops.tm.enums.domain.UserAuditClassification
import com.tdsops.tm.enums.domain.UserAuditSeverity
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin

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
		createdDate nullable: true
		ipAddress nullable: true
		project nullable: true
	}

	static mapping = {
		version false
		autoTimestamp false
		id column: 'user_audit_id'
		createdDate sqltype: 'DateTime'
		ipAddress sqlType: 'varchar(15)'
	}

	def beforeInsert = {
		createdDate = TimeUtil.nowGMT()
	}

	String toString() {
		"UserAudit user (${userLogin?.username ?: 'Unknown'}) '$message'"
	}
}
