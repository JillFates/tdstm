package com.tdsops.etl

import com.tds.asset.AssetEntity

class Row {

    List<Element> elements
    Integer index
    AssetEntity instance

    Row () {
        elements = []
    }

    Row (Integer index, List<?> values, ETLProcessor processor) {
        this.index = index
        this.elements = values.withIndex().collect { def value, int i ->
            new Element(
                    originalValue: value,
                    value: "${value}".toString(),
                    rowIndex: index,
                    columnIndex: i,
                    processor: processor)
        }
    }

    Element addNewElement (String value, ETLProcessor processor) {
        Element newElement = new Element(originalValue: value,
                value: value,
                rowIndex: index,
                columnIndex: elements.size(),
                processor: processor)
        elements.add(newElement)
        newElement
    }

    Element getElement (Integer index) {
        elements[index]
    }

    int size () {
        elements.size()
    }
}
