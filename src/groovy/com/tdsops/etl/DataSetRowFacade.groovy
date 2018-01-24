package com.tdsops.etl

/**
 *
 *
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
