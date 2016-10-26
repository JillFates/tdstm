package com.tdssrc.grails

/**
 * Enums manipulation methods
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
class EnumUtil {

	/**
	 * Looks in a list or array of Enums with a ivalue' for a value and return it
	 *
	 * @param values an array of values to look in
	 * @param param the value to search
	 */
	static <T extends Enum<T>> T searchfParam(Iterable<T> values, String param) {
		param ? values.find { it.value == param } : null
	}
}
