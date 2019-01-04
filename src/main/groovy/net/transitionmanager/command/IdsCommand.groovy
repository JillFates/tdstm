package net.transitionmanager.command


/**
 * Command class that can be used in requests receiving a single id
 * or a list of ids, that ensures the correct data types (Long) of the given ids and that the 
 * ids are all greater than zero (0).
 */

class IdsCommand implements CommandObject{
	/**
	 * The list of ids.
	 */
	List<Long> ids

	static constraints = {
		/**
		 * The validation for the ids
		 */
		ids nullable: false, validator: { List ids ->
			// List must contain one or more ids
			boolean isOk = ids && ids.size() > 0

			// Check for an id below 1 which would be invalid
			if (isOk && ids.find { it < 1 }) {
				isOk = false
			}

			return isOk
		}
	}
}