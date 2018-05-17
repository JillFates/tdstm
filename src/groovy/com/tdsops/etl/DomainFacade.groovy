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

	Object getProperty(String name) {

		Map<String,?> currentDataFields = result.currentRowData().fields

		if(!currentDataFields.containsKey(name)) {
			throw ETLProcessorException.unknownDomainProperty(name)
		}
		return new DomainField(currentDataFields[name].value)
	}

}
