package com.tdsops.etl

/**
 * Concrete implementation for WhenNotFound ETL commands.
 */
class WhenNotFoundElement extends FoundElement {

	WhenNotFoundElement(String dependentId, ETLProcessorResult result) {
		super(dependentId, result)
	}

	/**
	 * WhenFound create ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenNotFound asset create {
	 *			assetClass Application
	 *			assetName primaryName
	 *			assetType primaryType
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement create(Closure closure) {
		return action('create', closure)
	}
}
