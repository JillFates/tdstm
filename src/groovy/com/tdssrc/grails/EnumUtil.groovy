package com.tdssrc.grails

import org.apache.commons.lang3.EnumUtils

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

	/**
	 * Checks if the specified name is a valid enum for the class.
	 * @param enumClass - the class of the enum to query, not null
	 * @param enumName - the enum name, null returns false
	 * @return
	 */
	static boolean isValidEnum(Class enumClass, String enumName) {
		return EnumUtils.isValidEnum(enumClass, enumName)
	}
}
