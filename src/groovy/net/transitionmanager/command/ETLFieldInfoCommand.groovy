package net.transitionmanager.command

import grails.validation.Validateable

/**
 * General purpose command object for updating an individual field
 */
@Validateable
class ETLFieldInfoCommand{

	// Field Name
	String fieldName

	// Value for the field
	Object value

	static constraints = {
		fieldName nullable: false, blank: false
		value nullable: false
	}
}
