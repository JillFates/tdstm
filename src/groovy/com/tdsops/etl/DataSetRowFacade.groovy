package com.tdsops.etl

/**
 * Defines a Row Facade used in commands like the following:
 * <pre>
 *		find Application 'for' id by id with SOURCE.'application id'
 * </pre>
 * Source application will recover the Dataset 'application id'
 */
class DataSetRowFacade {

	Map row

	DataSetRowFacade(Map row) {
		this.row = row
	}

	Object getProperty(String name) {

		if(!row.containsKey(name)) {
			throw ETLProcessorException.unknownDataSetProperty(name)
		}
		return new SourceField(row[name])
	}
}
