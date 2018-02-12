package com.tdsops.etl

import getl.data.Dataset
import getl.data.Field

class DataSetFacade {

	private Dataset dataSet

	DataSetFacade(Dataset dataSet) {
		this.dataSet = dataSet
	}

	/**
	 * Return rows from dataset
	 * @return
	 */
	List<Map> rows() {
		return dataSet.rows()
	}

	/**
	 * Count reading rows from dataset
	 */
	Long readRows() {
		return dataSet.readRows
	}

	/**
	 * Returns a lists with GETL fields associated to a GETL Dataset instance
	 * @return a list of GETL fields
	 */
	List<Field> fields() {
		return dataSet.connection.driver.fields(dataSet)
	}

	/**
	 * Returns the original file name from dataset instance.
	 * @return a String value with the filename
	 */
	String fileName(){
		return dataSet.params.fileName
	}

	Dataset getDataSet() {
		return dataSet
	}
}
