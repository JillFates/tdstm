package com.tdsops.etl

import com.tds.asset.AssetEntity

class Row {

	/**
	 * List of values recovered from GETL Dataset mapped by column index
	 */
	List values
    //List<Element> elements
    Integer index
    AssetEntity instance
	ETLProcessor processor

    Row () {
	    values = []
    }

    Row (Integer index, List<?> values, ETLProcessor processor) {
        this.index = index
	    this.processor = processor
	    this.values = values
    }

	Element addNewElement (Object value, ETLFieldSpec fieldSpec, ETLProcessor processor) {
		Element newElement = new Element(originalValue: value,
			value: value,
			rowIndex: index,
			columnIndex: values.size(),
			fieldSpec: fieldSpec,
			processor: processor)
		//elements.add(newElement)
		newElement
	}
	Element getElement (Integer index) {
		Object value = values[index]
		return new Element(
			originalValue: value,
			value: value?:'',
			rowIndex: index,
			columnIndex: index,
			processor: processor)
    }

    int size () {
	    values.size()
    }
}