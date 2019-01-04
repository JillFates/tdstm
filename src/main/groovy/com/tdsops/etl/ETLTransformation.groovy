package com.tdsops.etl

import com.tdsops.common.lang.CollectionUtils
import com.tdssrc.grails.StringUtil

import java.lang.reflect.Array

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
    def apply (Element element) {
        closure(element)
        element
        //TODO: Diego. Add every transformation to a list of applied transformation over an element
    }

    def apply (Element element, def args) {
        closure(element, args)
        element
    }

    /**
     * Perform the concatenate process over all values separated by <code>separator</code> provided
     * @param separator - value separator
     * @param values - list of values to concatenate
     * @return the joined string
     */
    static String concat(String separator, Object...values) {
        // first flatten values array to unify nested arrays
        values = values.flatten()

        // determine if we should include blank or null values in the resultant joined string
        Boolean includeEmpty = false
        def shouldInclueEmpty = values[-1]
        if ((shouldInclueEmpty instanceof Boolean) && Boolean.valueOf(shouldInclueEmpty.toString())) {
            includeEmpty = true
            values = values.dropRight(1)
        }

        // find the values to be joined by filtering blank or nulls when required
        def toJoin = values.findAll {
            includeEmpty || (StringUtil.isNotBlank(it as String) && (it as String) != '[]')
        }

        // return a joined string
        return toJoin.collect{ it == null ? '' : ETLValueHelper.valueOf(it) }.join(separator)
    }

    /**
     * Perform the append process, but for that we just call the concat with the actual values
     * @param separator - value separator
     * @param values - list of values to concatenate
     * @return the joined string
     */
    static String append(String separator, Object...values) {
        return concat(separator, values)
    }
}
