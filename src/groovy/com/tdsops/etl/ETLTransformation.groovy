package com.tdsops.etl

/**
 *
 * Creates a transformation for an element withing the ETL Script.
 *
 * It can modifies element value, for example, with an uppercase/lowercase string function
 *
 */
abstract class ETLTransformation {

    Closure<Element> closure
    /**
     *
     * Applies a transformation on an Element modifying its current value
     *
     * @param element and ETL Processor element to be modified
     */
    void apply (Element element) {
        closure(element)
        //TODO: Diego. Add every transformation to a list of applied transformation over an element
    }

    void apply (Element element, def args) {
        closure(element, args)
    }

}