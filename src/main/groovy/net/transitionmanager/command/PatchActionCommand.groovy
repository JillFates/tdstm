package net.transitionmanager.command


import net.transitionmanager.service.InvalidParamException

/**
 * Command class that will be used typically for PATCH activities that accept an "action" name
 * and a list of one or more IDs for the action to be performed on. The Action and Ids should be passed
 * as part of the request body.
 *
 * There will be validation on the Action and the Ids. The list of ids must be of the correct data types (Long)
 * and that the ids are all greater than zero (0).
 *
 *
 */

class PatchActionCommand implements CommandObject{
	/**
	 * The list of ids that the action should be performed against
	 */
	List<Long> ids

	/**
	 * The string representation of the action to be performed
	 */
	String action

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

		action nullable:false, size:1..255
	}

	/**
	 * Used to convert the action into an Enum
	 * @param enumClass - the enum class that should be used to perform the lookup
	 * @return the enum instance if found otherwise an InvalidParamException is thrown
	 */
	Object actionLookup(Class enumClass) {
		try {
			return enumClass.lookup(action)
		} catch (e) {
			throw new InvalidParamException("Unsupported action $action")
		}
	}

}