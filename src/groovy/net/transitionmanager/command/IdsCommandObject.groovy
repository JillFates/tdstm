package net.transitionmanager.command

import grails.validation.Validateable

/**
 * Command object that can be used in requests receiving a single id
 * or a list of ids, that ensures the correct data types (Long) of the given ids.
 */
@Validateable
class IdsCommandObject {
	/**
	 * The list of ids.
	 */
	List<Long> ids

	static constraints = {
		/**
		 * The
		 */
		ids nullable: false, validator: { List ids ->
			boolean isOk = ids && ids.size() > 0
			return isOk
		}
	}
}