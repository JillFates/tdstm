package com.tdssrc.grails

/**
 * The NumberUtil class contains a collection of useful number manipulation methods 
 */
class NumberUtil {
	
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
				if (value.isLong()) {
					result = value.toLong()
				} else {
					// log.debug "asLong() received invalid value ($moveEventId)"
					return defVal
				}
				break
			case Integer:
				result = value.toLong()
				break
			case Long:
				result = value
				break
		} 
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
