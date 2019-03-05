package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 * A facade object to be used in ETL script using the following syntax:
 * <pre>
 *     find Application of id by id with DOMAIN.id
 * </pre>
 * Where id property is the value in the current row data for the column 'id'
 */
@CompileStatic
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

	/**
	 * Returns a {@code ETLProcessorResult#currentRow} to be used in {@code DependencyBuilder}
	 * @return
	 */
	RowResult currentRowMap(){
		return this.result.currentRow()
	}
}
