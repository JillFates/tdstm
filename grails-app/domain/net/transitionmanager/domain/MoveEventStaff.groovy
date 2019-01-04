package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil

/**
 * Provides a mapping between moveEvent and assigned person to that Event with role
 */
class MoveEventStaff {

	Person person
	MoveEvent moveEvent
	RoleType role
	Date dateCreated
	Date lastUpdated

	static mapping = {
		autoTimestamp false
		person unique:['moveEvent', 'role']
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}
