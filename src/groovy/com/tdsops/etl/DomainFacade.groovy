package com.tdsops.etl

/**
 * A facade object to be used in ETL script using the following syntax:
 * <pre>
 *     find Application of id by id with DOMAIN.id
 * </pre>
 * Where id property is the value in the current row data for the column 'id'
 */
class DomainFacade {

	private ETLProcessorResult result

	DomainFacade(ETLProcessorResult result) {
		this.result = result
	}

	/**
	 * Return property value
	 * @param name a property name
	 * @return 
	 */
	Object getProperty(String name) {
		Object value = result.getFieldValue(name)
		return value
	}
}
