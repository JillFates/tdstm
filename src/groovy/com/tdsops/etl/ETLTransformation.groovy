package com.tdsops.etl

/**
 *
 * Creates a transformation for an element withing the ETL Script.
 *
 * It can modifies element value, for example, with an uppercase/lowercase string function
 *
 */
trait ETLTransformation {

    Closure<ETLProcessor.Element> closure
    /**
     *
     * Applies a transformation on an Element modifying its current value
     *
     * @param element and ETL Processor element to be modified
     */
    void apply (ETLProcessor.Element element) {
        closure(element)
        element.transformations.add(this)
    }


}