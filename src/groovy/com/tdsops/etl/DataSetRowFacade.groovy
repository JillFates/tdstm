package com.tdsops.etl

/**
 * Defines a Row Facade used in commands like the following:
 * <pre>
 *		find Application 'for' id by id with SOURCE.'application id'
 * </pre>
 * Source application will recover the Dataset 'application id' over each row
 */
class DataSetRowFacade {

	private Map row

	DataSetRowFacade(Map row) {
		this.row = row
	}

	Object getProperty(String name) {
		// TODO - remove toLowerCase once GETL library is fixed - see TM-9268
		if(!row.containsKey(name.toLowerCase())) {
			throw ETLProcessorException.unknownDataSetProperty(name)
		}
		return new SourceField(row[name.toLowerCase()])
	}
}
