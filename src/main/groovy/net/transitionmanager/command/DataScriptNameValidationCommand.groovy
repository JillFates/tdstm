package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class DataScriptNameValidationCommand {

	Long providerId
	Long dataScriptId
	String name

	static constraints = {
		providerId nullable:false
		dataScriptId nullable: true
		name nullable: false, blank: false
	}
}
