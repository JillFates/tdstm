package net.transitionmanager.command.bulk

import net.transitionmanager.command.CommandObject


/**
 * Represents an individual bulk change edit.
 *
 * @param fieldName This  is the field that is being edited, and will be mapped to a service, to do the bulk edits.
 * @param action  This is the bulk action that will be executed on the service mapped from the fieldName.
 * @param value This is the value to use to the bulk change, this could be anything from a list of number, a boolean, a number, or a string.
 * For the initial implementation  this is a json string that can be parsed into any value that we need.
 */

class EditCommand implements CommandObject{

	String fieldName
	String action
	def value

	static constraints = {
		value nullable: true, blank:true
	}
}
