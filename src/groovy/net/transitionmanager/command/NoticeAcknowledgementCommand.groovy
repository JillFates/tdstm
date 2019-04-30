package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class NoticeAcknowledgementCommand implements CommandObject {
	String ipAddress = ''
	String browserType = ''

	static constraints = {
		ipAddress blank: true, nullable: false
		browserType blank: true, nullable: false
	}
}
