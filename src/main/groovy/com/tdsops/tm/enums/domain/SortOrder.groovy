package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Define all sort types.
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@CompileStatic
enum SortOrder {

	ASC('asc'),
	DESC('desc')

	final String value

	private SortOrder(String label) {
		value = label
	}

	String toString() { value }

	static SortOrder valueOfParam(String param) {
		values().find { it.value == param }
	}
}
