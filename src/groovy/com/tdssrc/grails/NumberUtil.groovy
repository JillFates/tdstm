package com.tdssrc.grails

/**
 * The NumberUtil class contains a collection of useful number manipulation methods 
 */
class NumberUtil {
	
	/**
	 * Used to convert various types into a Long value
	 * @param value - the value to be converted to a Long
	 * @return the Long value if valid else null
	 */
	static Long toLong(value) {
		Long result
		switch (value) {
			case String:
				if (value.isLong()) {
					result = value.toLong()
				} else {
					// log.debug "asLong() received invalid value ($moveEventId)"
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

}