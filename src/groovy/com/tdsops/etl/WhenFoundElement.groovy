package com.tdsops.etl

/**
 * Concrete implementation for WhenFoundElement ETL commands.
 */
class WhenFoundElement extends FoundElement {

	WhenFoundElement(String domainPropertyName, ETLDomain domain, ETLProcessor processor) {
		super(domainPropertyName, domain, processor)
	}

	/**
	 * WhenNotFound update ETL command.
	 * It defines what should updated if find command found a result
	 * <pre>
	 *		whenFound asset update {
	 *			"TM Last Seen" NOW
	 *		}
	 * </pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement update(Closure closure) {
		return action(FoundElementType.update, closure)
	}
}
