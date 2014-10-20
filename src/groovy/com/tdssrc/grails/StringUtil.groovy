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

	/**
	 * Used to split a string that is guaranteed to be a list and all elements are trimed
	 * @param words - the string to be split
	 * @param delimiter - the delimiter to split on which can be a string or regex (default whitespace)
	 * @return the split list of words
	 */
	static List split(String words, delimiter=/\s++/) {
		List list = []
		String trimmedWords = (words != null ? words.trim() : '')
		if (trimmedWords.size()) {
			list = trimmedWords.split(delimiter)
			if (list instanceof String)
				list = [ list ]
			list = list.collect { it.trim() }
		}
		return list
	}


	/**
	 * Used to determine if a string contains a list of strings
	 * @param str - the string to search
	 * @param list - the list of words to check for
	 * @param caseInsensitive - flag if true will perform the search case insensitive (default true)
	 * @param mustMatchAll - flag if true all words in list must match (default true)
	 * @return true if the search was successful
	 */
	private static boolean containsThese(String str, List words, boolean caseInsensitive=true, mustMatchAll=true) {
		String searchStr = str
		List searchList = words
		if (caseInsensitive) {
			searchStr = str.toLowerCase()
			searchList = words.collect { it.toLowerCase() }
		}

		boolean matched
		for (int i=0; i < searchList.size(); i++) {
			matched = searchStr.contains(searchList[i])
			if (mustMatchAll) {
				if (! matched) {
					break
				}
			} else if (matched) {
				break
			}
		}
		return matched
	}

	/**
	 * Used to determine if a string contains ALL of list a strings
	 * @param str - the string to search
	 * @param list - the list of words to check for
	 * @param caseInsensitive - flag if true will perform the search case insensitive (default true)
	 * @return true if the search was successful
	 */
	static boolean containsAll(String str, List words, boolean caseInsensitive=true) {
		return containsThese(str, words, caseInsensitive, true)
	}

	/**
	 * Used to determine if a string contains ANY of list a strings
	 * @param str - the string to search
	 * @param list - the list of words to check for
	 * @param caseInsensitive - flag if true will perform the search case insensitive (default true)
	 * @return true if the search was successful
	 */
	static boolean containsAny(String str, List words, boolean caseInsensitive=true) {
		return containsThese(str, words, caseInsensitive, false)
	}

}