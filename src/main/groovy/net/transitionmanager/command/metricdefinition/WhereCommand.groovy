package net.transitionmanager.command.metricdefinition

import net.transitionmanager.command.CommandObject


/**
 * A Command object to bind and validate the where JSON.
 */

class WhereCommand implements CommandObject{
	String column
	String expression

	static constraints = {

	}

	/**
	 * Converts the Command object to a map, so that it can be serialized back to JSON.
	 *
	 * @return A Map of the Where fields, without the properties introduced by Validateable.
	 */
	Map toMap() {
		[
				column    : column,
				expression: expression
		]
	}
}
