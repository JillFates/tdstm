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
    private final static String DEFAULT_SEPARATOR = ', '

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
        def toJoin = values.findAll{ StringUtil.isNotBlank(it as String) && (it as String) != '[]'  }

        return toJoin.collect {
            if (it.getClass().isArray()) {
                return concat(separator, it)
            } else {
                ETLValueHelper.valueOf(it)
            }
        }.join(StringUtil.defaultIfEmpty(separator, DEFAULT_SEPARATOR))
    }

    /**
     *
     * @param separator
     * @param values
     * @return
     */
    static String append(String separator, Object...values) {
        return concat(separator, values)
    }
}