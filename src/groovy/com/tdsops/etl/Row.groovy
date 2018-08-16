package com.tdsops.etl

import com.tdssrc.grails.JsonUtil

/**
 * <p>Row from GETL DataSet.<p>
 * <p>Every iteration in an ETLScript will use this structure to take values from the source data set.<p>
 * It collects also Element instances created during an ETL script iterate execution.
 * That collection is used internally or as a validation of the internal status of an ETL command.
 */
class Row {

	/**
	 * List of values recovered from GETL Dataset mapped by column index
	 */
	List dataSetValues
	/**
	 * Maps with elements created for the current row instance.
	 * <pre>
	 *
	 * </pre>
	 */
	Map<Integer, Element> elementsMap
	Integer rowIndex
	ETLProcessor processor

	Row(List<?> dataSetValues, ETLProcessor processor) {
		this.processor = processor
		this.dataSetValues = dataSetValues
		elementsMap = [:]
	}

	Element addNewElement(Object value, ETLFieldDefinition fieldDefinition, ETLProcessor processor) {
		dataSetValues.add(value)
		Integer columnIndex = dataSetValues.size()
		Element newElement = new Element(originalValue: value,
			value: value,
			fieldDefinition: fieldDefinition,
			processor: processor
		)
		elementsMap[columnIndex] = newElement
		newElement
	}

	/**
	 * <p>Creates an instance of Element with column index content as a value. </p>
	 * In case of path parameter different than nul, then it creates the value
	 * using the GPATH over the original value.
	 * @param columnIndex colum index position
	 * @param path gpath used for calculate dataset value
	 * @return an instance of Element class
	 */
	Element getDataSetElement(Integer columnIndex, String path = null) {
		Object value = dataSetValues[columnIndex]

		if(path){
			value = JsonUtil.gpathAt(value, path)
		}

		Element element = new Element(
			originalValue: value,
			value: value,
			processor: processor)

		elementsMap[columnIndex] = element
		return element
	}

	Object getDataSetValue(Integer columnIndex) {
		return dataSetValues[columnIndex]
	}

	Element getElement(Integer columnIndex) {
		return elementsMap[columnIndex]
	}

	int size() {
		dataSetValues.size()
	}
}