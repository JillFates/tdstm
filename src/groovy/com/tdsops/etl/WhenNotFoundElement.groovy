package com.tdsops.etl

/**
 * Concrete implementation for WhenNotFound ETL commands.
 */
class WhenNotFoundElement extends FoundElement {

	WhenNotFoundElement(String domainPropertyName, ETLDomain domain, ETLProcessor processor) {
		super(domainPropertyName, domain, processor)
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
		return action(FoundElementType.create, closure)
	}
}
