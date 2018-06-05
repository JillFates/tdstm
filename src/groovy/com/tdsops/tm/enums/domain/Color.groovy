package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Define all sort types.
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@CompileStatic
enum Color {

	Black('black-tag'),
	Brown('brown-tag'),
	Red('red-tag'),
	Orange('orange-tag'),
	Yellow('yellow-tag'),
	Green('green-tag'),
	Cyan('cyan-tag'),
	Blue('blue-tag'),
	Purple('purple-tag'),
	Pink('pink-tag'),
	Unselected('unselected-tag')

	final String css

	Color(String cssValue){
		css = cssValue
	}

	String toString() { name() }

	static Color valueOfParam(String param) {
		values().find { it.name() == param }
	}
}
