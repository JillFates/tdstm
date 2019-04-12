package net.transitionmanager.manufacturer

import com.tdssrc.grails.TimeUtil

/**
 * Represents individual alias names used to reference the same manufacturer.
 */
class ManufacturerAlias {

	String name
	Manufacturer manufacturer
	Date dateCreated

	static constraints = {
		name unique: true
	}

	static mapping = {
		autoTimestamp false
		version false
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
}
