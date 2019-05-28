package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class ETLDataRecordFieldsPropertyCommand {

	// The value that will ultimately saved to the domain entity when created or updated
	// For the time being this is a String but in the future could change
	String value = ''

	// The original value of the property when extracted from the datasource before any
	// transformations were performed on the value
	String originalValue = ''

	// A flag that a warning was raised on the row to prevent automatically processing the record
	Boolean warn = false

	Boolean error = false

	ETLDataRecordFieldsFindCommand find

	static constraints = {
		value nullable: false, blank: true
		originalValue nullable: false, blank: true
	}
}


@Validateable
class ETLDataRecordFieldsFindCommand {

	Integer size = 0
	List results = []
	Integer matchOn = null

	ETLDataRecordFieldsFindQueryCommand query

}

@Validateable
class ETLDataRecordFieldsFindQueryCommand {

	String domain
	Map kv

	static constraints = {
		domain nullable: false, blank: false
	}
}