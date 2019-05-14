package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.Project

@Validateable
class NoticeCommand implements CommandObject {

	Long id
	String title
	String rawText
	String htmlText
	Notice.NoticeType typeId
	Boolean active = false
	Project project
	Boolean needAcknowledgement = false
	String acknowledgeLabel = ''
	Integer sequence = 0
	Date activationDate
	Date expirationDate

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
