package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Define all sort types.
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@CompileStatic
enum Color {

	Black('tag-black'),
	Brown('tag-brown'),
	Red('tag-red'),
	Orange('tag-orange'),
	Yellow('tag-yellow'),
	Green('tag-green'),
	Cyan('tag-cyan'),
	Blue('tag-blue'),
	Purple('tag-purple'),
	Pink('tag-pink'),
	Unselected('tag-unselected')

	final String css

	Color(String cssValue){
		css = cssValue
	}

	String toString() { name() }

	static Color valueOfParam(String param) {
		values().find { it.name() == param }
	}
}
