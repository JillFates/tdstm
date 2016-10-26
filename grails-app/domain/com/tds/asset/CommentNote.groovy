package com.tds.asset

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.Person

/**
 * Represents a note created by users and associated to AssetComment (aka Task).
 */
class CommentNote {

	Person createdBy
	String note
	Integer isAudit = 0   // Flag if the note is created as an audit note created by the system

	Date dateCreated
	Date lastUpdated

	static belongsTo = [assetComment: AssetComment]

	static constraints = {
		dateCreated nullable: true
		note blank: false
	}

	static mapping = {
		version false
		autoTimestamp false
		note sqlType: 'text'
		isAudit sqlType: 'tinyint'
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		note
	}
}
