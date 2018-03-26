package net.transitionmanager.command

import grails.validation.Validateable

/**
 * This class reflects the structure of the ETL JSON results for the data section
 *
 *		"data": [
 *			{
 *				"op": "I",
 *				"warn": false,
 *				"duplicate": false,
 *				"errors": [],
 *				"fields": {
 *					"id": {
 *						"value": "114052",
 *						"originalValue": "114052",
 *						"error": false,
 *						"warn": false,
 *						"find": {
 *							"query": []
 *						}
 *					},
 *					...
 *				}
 *			}
 *
 */
@Validateable
class ETLDataRecordCommand {

	// The type of operation to perform
	String op

	// A flag that a warning was raised on the row to prevent automatically processing the record
	Boolean warn = false

	// A flag that indicates that one or more reference fields in the record could not be matched to
	// a singluar domain entity but instead the query criteria found multiple matches. This requires
	// human intervention to match properly.
	Boolean duplicate = false

	// A list of errors recorded on the individual record
	List<String> errors = []

	// A Map that contains all of the values for each of the fields in a map structure where the key
	// of the map is the domain property name and the data is all the juicy stuff that the ETL processor
	// conjured up.

	Map<String, ETLDataRecordFieldsCommand> fields

	static constraints = {
		op inList: ['I', 'U', 'D']		// Insert, Update, Delete, ? for Undetermined
	}
}

