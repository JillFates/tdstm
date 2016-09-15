package com.tdssrc.grails

/**
 * The NumberUtil class contains a collection of useful number manipulation methods
 */
class NumberUtil {

	/**
	 * Nifty little test to validate that a value is a Long
	 * @param a string representing a long value
	 * @return true if the value is a Long otherwise false
	 */
	static boolean isLong(val) {
		switch (val) {
			case String:
			case org.codehaus.groovy.runtime.GStringImpl:
				return (val?.isNumber() && val.isLong())
			case Long:
			case Integer:
				return true
		}
		return false
	}

	/**
	 * Nifty little test to validate that a value is a Long and Postive
	 * @param a string representing a long value
	 * @return true if the value is a Long otherwise false
	 */
	static boolean isPositiveLong(val) {
		return (isLong(val) && toLong(val) > 0)
	}

	/**
	 * Used to convert various types into a Long value
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Long value if valid else null
	 */
	static Long toLong(value, defVal=null) {
		Long result
		switch (value) {
			case String:
			case org.codehaus.groovy.runtime.GStringImpl:
				result = isLong(value) ? value.toLong() : defVal
				break
			case Integer:
				result = value.toLong()
				break
			case Long:
				result = value
				break
			default:
				result = defVal
		}
		return result
	}

	/**
	 * Used to convert various types into a Long value
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Long value if valid else null
	 */
	static Long toPositiveLong(value, defVal=null) {
		Long result = toLong(value, defVal)
		if (result != null && result < 0)
			result = defVal
		return result
	}

	/**
	 * Used to convert various types to Integer
	 * @param value - the value to convert
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return a tinyint value or the default value if unable to convert
	 */
	static Integer toInteger(value, defVal=null) {
		Long result = toLong(value, defVal)
		if (result && result != defVal)
			result = result.toInteger()

		return result
	}

	/**
	 * Used to convert various types to TINYINT (0-255)
	 * @param value - the value to convert
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return a tinyint value or the default value if unable to convert
	 */
	static Integer toTinyInt(value, defVal=null) {
		Long result = toLong(value, defVal)
		if (result != defVal && result > 255)
			result = defVal

		return result
	}
}
