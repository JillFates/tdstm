package com.tdsops.etl

import getl.data.Dataset
import getl.data.Field

class DataSetFacade {

	Dataset dataSet

	DataSetFacade(Dataset dataSet) {
		this.dataSet = dataSet
	}

	Object getProperty(String name) {


	}

	/**
	 * Return rows from dataset
	 * @return
	 */
	List<Map> rows() {
		dataSet.rows()
	}

	/**
	 * Count reading rows from dataset
	 */
	Long readRows() {
		dataSet.readRows
	}

	/**
	 * Returns a lists with GETL fields associated to a GETL Dataset instance
	 * @return a list of GETL fields
	 */
	List<Field> fields() {
		dataSet.connection.driver.fields(dataSet)
	}
}
