package com.tdssrc.grails

/**
 * The StringUtil class contains a collection of useful string manipulation methods 
 */
class StringUtil {
	
	/**
	 * Truncates a string to a specified length and adds ellipsis (...) if the string was longer than the specified length. The total length 
	 * including the ellipsis will be the length specified.
	 * @param String	The string to ellipsis if necessary
	 * @param Integer	The length to truncate the string to
	 * @return String	The ellipsised string
	 */
	static String ellipsis(s, length) {
		def r = s
		if (s.size() > length && length) {
			r = s.substring(0, length - 3) + '...'
		}
		return r
	}
}