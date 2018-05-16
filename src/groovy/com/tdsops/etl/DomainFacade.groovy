package com.tdsops.etl

/**
 * A facade object to be used in ETL script using the following syntax:
 * <pre>
 *     find Application of id by id with DOMAIN.id
 * </pre>
 * Where id property is the value in the current row data for the column 'id'
 * The facade can contain the data row index reference of the fetching data
 * this is used in LOOKUP operations
 */
class DomainFacade {

	private ETLProcessorResult result
	private int domainRow = -1

	DomainFacade(ETLProcessorResult result, int domainRow = -1) {
		this.result = result
		this.domainRow = domainRow
	}

	Object getProperty(String name) {

		Map<String,?> currentDataFields

		if ( domainRow >=0 ) {
			currentDataFields = result.reference.data[domainRow].fields
		} else {
			currentDataFields = result.reference.data.last().fields
		}

		if(!currentDataFields.containsKey(name)) {
			throw ETLProcessorException.unknownDomainProperty(name)
		}
		return new DomainField(currentDataFields[name].value)
	}

}
