package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.Project

@Validateable
class NoticeCommand implements CommandObject {

	Long id
	String acknowledgeLabel = ''
	Date activationDate
	Boolean active = false
	Date expirationDate
	String htmlText
	Boolean locked = false
	Boolean needAcknowledgement = false
	Project project
	String rawText
	Integer sequence = 0
	String title
	Notice.NoticeType typeId

	static constraints = {
		id nullable: true
		rawText minSize: 1, maxSize: 65535
		htmlText minSize: 1, maxSize: 65535
		project nullable: true
		needAcknowledgement nullable: false
		acknowledgeLabel blank: true, nullable: false, size: 1..255
		activationDate nullable: true
		expirationDate nullable: true
	}

}
