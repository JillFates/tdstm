package net.transitionmanager.command.metricdefinition

import grails.validation.Validateable

/**
 * A Command object to bind, and validate the Query JSON.
 */
@Validateable
class QueryCommand {
	String             domain
	String             aggregation
	List<WhereCommand> where
	List<String>       groupBy

	static constraints = {
		where nullable: true, cascade: true
		groupBy nullable: true
	}

	/**
	 * Converts the Command object to a map, so that it can be serialized back to JSON.
	 *
	 * @return A Map of the Query fields, without the properties introduced by @Validateable.
	 */
	Map toMap() {
		return [
				domain     : domain,
				aggregation: aggregation,
				where      : where ? where*.toMap() : null,
				groupBy    : groupBy ?: null
		]
	}
}
