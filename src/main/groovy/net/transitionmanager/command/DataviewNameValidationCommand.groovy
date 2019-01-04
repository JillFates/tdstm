package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class DataviewNameValidationCommand {

	Long dataViewId
	String name

	static constraints = {
		dataViewId nullable: true
		name nullable: false, blank: false
	}
}
