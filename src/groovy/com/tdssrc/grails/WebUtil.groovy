package com.tdssrc.grails

import groovy.transform.CompileStatic

@CompileStatic
class WebUtil {

	/**
	 * Returns a list of checkbox values as a comma separated string
	 */
	static String checkboxParamAsString(param) {
		listAsMultiValueString(param.collect { id -> "'" + (id?.toString()?.trim() ?: '') + "'" })
	}

	/**
	 * Returns multi-value String of a List
	 */
	static String listAsMultiValueString(Iterable list) {
		list?.join(', ') ?: ''
	}

	/**
	 * Concatenates a list of strings in <li> HTML tag to display.
	 * @param strings  the list of warning strings
	 */
	static String getListAsli(List strings) {
		strings.collect { '<li>' + it + '</li>' }.join('')
	}

	/**
	 * Splits a camel-case String with '_'.
	 */
	static String splitCamelCase(String s) {
		return s.replaceAll(
		   String.format('%s|%s|%s',
			  '(?<=[A-Z])(?=[A-Z][a-z])',
			  '(?<=[^A-Z])(?=[A-Z])',
			  '(?<=[A-Za-z])(?=[^A-Za-z])'
		   ),
		   '_'
		).toLowerCase()
	 }
}
