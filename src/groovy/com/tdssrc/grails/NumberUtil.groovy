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
	 * Convert various types into a Long value. If value param is a number with Decimal,
	 * it also returns the Long-part of the decimal value
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Long value if valid else null
	 */
	static Long toLongNumber(value, Long defVal = null) {

		if(value == null) {
			value = defVal
		} else {

			if (value instanceof CharSequence) {
				if( value.isDouble() ){
					value = value.toDouble()
				} else if( value.isBigDecimal() ) {
					value = value.toBigDecimal()
				} else if( value.isLong() ) {
					value = value.toLong()
				}
			}

			if (value.class in [Double, BigDecimal, BigInteger, Integer]) {
				value = value.longValue()
			}

			if (!(value instanceof Long)){
				value = defVal
			}

		}

		return value
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
	 * Convert various types (String/GString/CharSequence, or other Number type) into a Integer value
	 *
	 * @param value - the value to be converted to a Long
	 * @param defVal - the value to set to if it can't be converted (default null)
	 * @return the Integer value if valid else the default value or null
	 */
	static Integer toPositiveInteger(value, Integer defVal = null) {
		Long defLong = defVal ? new Long(defVal) : null
		toPositiveLong(value, defLong)?.toInteger()
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

	/**
	 * Convert a value to 0 or 1 .
	 *
	 * @param value
	 * @param defaultValue - value to be used when running into parsing issues or invalid inputs.
	 * @return 0, 1 or a default value (if original value is something other than 0 or 1).
	 */
	static Integer toZeroOrOne(Object value, Integer defaultValue = null) {
		// The result of the parsing, initialized with the default value in case of exceptions or invalid input.
		Integer result = defaultValue
		try {
			Integer parsedNumber = toPositiveInteger(value)
			// If value is 0 or 1, assign the result.
			if (parsedNumber <= 1 ) {
				result = parsedNumber
			}
		} catch (Exception e) {
			// Nothing to do. The default value has already been assigned.
		}
		return result
	}

	/**
	* Receive a list of string numbers and converts them to longs. Then it returns the list of long values.
	* If any value on the list is not positive, it will be just ignored and not returned/converted.
	* @param stringsList    The list of strings to be converted
	* @return   The resulting list of converted long values
	*/
	static List<Long> toPositiveLongList(List<String> stringsList) {
		List<Long> longsList = []
		stringsList.each { v ->
			Long id = toPositiveLong(v, null)
			if (id != null) {
			longsList << id
			}
		}
		return longsList
	}

	/**
	 * Used to determine if an object is a number (Integer or a Long)
	 * @param object - an object to inspect
	 * @return true if the object is a number
	 */
	static Boolean isaNumber(Object object) {
		return (object instanceof Integer) || (object instanceof Long)
	}

	/**
	 * Safely converts a String to a Double without an exception
	 * @param value
	 * @param precision - the number of decimal places to round the value to (default null / no rounding)
	 * @param defaultValue - the value to set if the string can not be converted (default null)
	 * @return the value converted to a Double
	 */
	static toDouble(CharSequence value, Integer precision = null, Double defaultValue=null) {
		Double result = defaultValue
		try {
			if (value?.size() > 0) {
				result = value.toBigDecimal()
				if (precision) {
					result = result.round(precision)
				}
			}
		} catch (java.lang.NumberFormatException e) {
		}
		return result
	}
}
