package com.tdsops.etl

/**
 * Defines an stackable Component that need to be validated by the ETL processor
 */
interface ETLStackableCommand {
	/**
	 * Evaluates the required properties of the command object and
	 * return an error string if there are any missing
	 * or an empty string if no problem was found
	 * @return string with errors or empty
	 */
	String stackableErrorMessage()
}
