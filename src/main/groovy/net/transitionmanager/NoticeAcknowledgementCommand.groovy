package net.transitionmanager


import net.transitionmanager.command.CommandObject

class NoticeAcknowledgementCommand implements CommandObject {
	String ipAddress = ''
	String browserType = ''

	static constraints = {
		ipAddress blank: true, nullable: false
		browserType blank: true, nullable: false
	}
}
