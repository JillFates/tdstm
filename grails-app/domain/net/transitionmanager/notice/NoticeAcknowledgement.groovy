package net.transitionmanager.notice

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.person.Person

/**
 * @author octavio
 */
class NoticeAcknowledgement {
	// The IP address that the user acknowledged the Notice from
	String ipAddress = ''

	// The browser that the user used when the acknowledged the Notice.
	String browserType = ''

	Date dateCreated

	static belongsTo = [notice: Notice, person: Person]

	static constraints = {
		ipAddress blank: true, nullable: false
		browserType blank: true, nullable: false
	}

	static mapping = {
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column: 'notice_acknowledgement_id'
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}

}
