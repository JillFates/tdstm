package net.transitionmanager.domain

import com.tdsops.etl.ETLDomain
import com.tdssrc.grails.TimeUtil

class TagLink {
	Long      domainId
	ETLDomain domain
	Date      dateCreated
	Date      lastUpdated

	static belongsTo = [tag: Tag]

	static constraints = {
		domainId unique: ['domain', 'tag']
	}

	static mapping = {
		id column: 'tag_link_id'
		autoTimestamp false
		domainClass enumType: "string"
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT().clearTime()
	}

	def beforeUpdSate = {
		lastUpdated = TimeUtil.nowGMT().clearTime()
	}
}
