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
	
	/** 
	 * Used to strip off one or more characters that may appear at the beginning of a string and return the remainder
	 * @param String chars - the characters that should be stripped off
	 * @param String str - the string to manipulate
	 * @return String - the string with the prefix characters removed
	 */
	static String stripOffPrefixChars(chars, str) {
		def r = ''
		for(int i=0; i < str.size(); i++) {
			if (! chars.contains(str[i]) ) {
				r=str.substring(i)
				break
			}
		}
		return r
	}

	/**
	 * Used to set a string to a default if the subject is blank
	 * @param subject - the string to check to see if it is blank
	 * @param defStr - the value to set the string to if it is blank
	 * @return The string value trimmed using default if blank
	 */
	static String defaultIfEmpty(String subject, String defStr) {
		def result = subject == null ? '' : subject.trim()
		return ( result.isEmpty()) ? defStr : result
	}
	
}