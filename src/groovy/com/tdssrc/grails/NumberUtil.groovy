package com.tdssrc.grails

/**
 * The NumberUtil class contains a collection of useful number manipulation methods
 */
class NumberUtil {

	/**
	 * Nifty little test to validate that a value is a Long
	 * @param a string representing a long value
	 * @return true if the value is a Long
	 */
	static boolean isLong(value) {
		if (value instanceof Long || value instanceof Integer) {
			true
		}
		else if (value instanceof CharSequence) {
			value.isLong()
		}
		else {
			false
		}
	}

	/**
	 * Nifty little test to validate that a value is a Long and Postive
	 * @param value  a number in string format
	 * @return true if the value is a Long and >= 0
	 */
	static boolean isPositiveLong(value) {
		toLong(value, -1) > 0
	}

	/**
	 * Convert various types into a Long value
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Long value if valid else null
	 */
	static Long toLong(value, Long defVal = null) {
		if (value instanceof Long) {
			value
		}
		else if (value instanceof Integer) {
			value.longValue()
		}
		else if (value instanceof CharSequence) {
			value.isLong() ? value.toLong() : defVal
		}
		else {
			defVal
		}
	}

	/**
	 * Constrain a number to be between a minimum and maximum value.
	 * @param value  the value
	 * @param min  the minimum allowed
	 * @param max  the maximum allowed
	 * @return the original value or the min or max if the value was out of range
	 */
	static int limit(int value, int min, int max) {
		if (value < min) {
			min
		}
		else if (value > max) {
			max
		}
		else {
			value
		}
	}

	/**
	 * Convert various types (String/GString/CharSequence, or other Number type) into a Long value.
	 *
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Long value if valid else null
	 */
	static Long toPositiveLong(value, Long defVal = null) {
		Long result = toLong(value, defVal)
		if (result != null && result < 0) {
			defVal
		}
		else {
			result
		}
	}

	/**
	 * Convert various types (String/GString/CharSequence, or other Number type) to Integer.
	 *
	 * @param value  the value to convert
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return a tinyint value or the default value if unable to convert
	 */
	static Integer toInteger(value, Integer defVal = null) {
		Long result = toLong(value, defVal)
		if (result != null && result != defVal) {
			result.toInteger()
		}
		else {
			result
		}
	}

	/**
	 * Convert various types to TINYINT (0-255)
	 * @param value - the value to convert
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return a tinyint value or the default value if unable to convert
	 */
	static Integer toTinyInt(value, Integer defVal = null) {
		Long result = toLong(value, defVal)
		if (result != defVal && result > 255) {
			defVal
		}
		else {
			result
		}
	}

	/**
	 * receive an heterogeneous collection and pick only those that can be converted to a Positive Long
	 * using a default value for those that can't be converted or rejecting them when the default is null
	 * @param values  heterogeneous collection
	 * @param defValue	default value to use when a value can't be mapped, if null we reject those that can't be mapped
	 * @return new Collection of Long Values
	 */
	static Collection<Long> mapToPositiveLong(Collection values, Long defValue = null){
		if(!values){
			values = []
		}
		values.collectMany {
			def v = toPositiveLong(it, defValue)
			if(v != null){
				[v]
			}else{
				[]
			}
		}
	}

	/**
	 * receive an heterogeneous collection and pick only those that can be converted to a Positive Integer
	 * using a default value for those that can't be converted or rejecting them when the default is null
	 * @param values  heterogeneous collection
	 * @param defValue	default value to use when a value can't be mapped, if null we reject those that can't be mapped
	 * @return new Collection of Integer Values
 	 */
	static Collection<Integer> mapToPositiveInteger(Collection values, Integer defValue = null){
		values = mapToPositiveLong(values, defValue)
		values*.toInteger()
	}
}
