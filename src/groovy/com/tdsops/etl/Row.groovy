package com.tdsops.etl

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
			processor: processor)
		elementsMap[columnIndex] = newElement
		newElement
	}

	Element getDataSetElement(Integer columnIndex) {
		Object value = dataSetValues[columnIndex]
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