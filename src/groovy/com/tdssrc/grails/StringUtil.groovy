package com.tdssrc.grails

import com.tdssrc.grails.NumberUtil

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
	 * Used to determine if a string is null, empty of just whitespace
	 * @param subject - the string to check
	 * @return true if blank otherwise false
	 */
	static boolean isBlank(String subject) {
		return (subject == null || subject.trim().size() == 0)
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

	/**
	 * Used to concat a string to a null, blank or existing string and using a separator appropriately
	 * @param existingString - the existing string (maybe)
	 * @param newString - the string to append to existingString
	 * @param separator - the seperator if existingString is not blank (default ',')
	 * @return the con
	 */
	static String concat(String existingString, String newString, String separator=',') {
		return (existingString?.size()>0) ? [existingString, newString].join(separator) : newString
	}

	/**
	 * Escapes characters in JSON output
	 *
	 * @param str - the string to escape
	 * @return the string encoded as JSON
	 */
	static String encodeAsJson(CharSequence str) {
		if (str == null || str.length() == 0) {
			return str;
		}

		StringBuilder sb = null;
		int n = str.length(), i;
		int startPos = -1;
		char prevChar = (char)0;
		for (i = 0; i < n; i++) {
			char ch = str.charAt(i);
			if (startPos == -1) {
				startPos = i;
			}
			String escaped = escapeCharacter(ch, prevChar);
			if (escaped != null) {
				if (sb == null) {
					sb = new StringBuilder();
				}
				if (i - startPos > 0) {
					sb.append(str, startPos, i);
				}
				if (escaped.length() > 0) {
					sb.append(escaped);
				}
				startPos = -1;
			}
			prevChar = ch;
		}
		if (sb != null) {
			if (startPos > -1 && i - startPos > 0) {
				sb.append(str, startPos, i);
			}
			return sb.toString();
		}
		else {
			return str;
		}
	}

	/**
	 * Escape the character, return null if no replacement has to be made
	 *
	 * @param ch the character to escape
	 * @param previousChar  the previous char
	 * @return the replacement string, null if no replacement has to be made
	 */
	static String escapeCharacter(char ch, char previousChar) {
		switch (ch) {
			case '"':
				return "\\\"";
			case '\\':
				return "\\\\";
			case '\t':
				return "\\t";
			case '\n':
				return "\\n";
			case '\r':
				return "\\r";
			case '\f':
				return "\\f";
			case '\b':
				return "\\b";
			case '\u000B': // vertical tab: http://bclary.com/2004/11/07/#a-7.8.4
				return "\\v";
			case '\u2028':
				return "\\u2028"; // Line separator
			case '\u2029':
				return "\\u2029"; // Paragraph separator
			case '/':
				// preserve special handling that exists in JSONObject.quote to improve security if JSON is embedded in HTML document
				// prevents outputting "</" gets outputted with unicode escaping for the slash
				if (previousChar == '<') {
					return "\\u002f"; 
				}
				break;
		}
		if(ch < ' ') {
			// escape all other control characters
			return ch
			//return "\\u" + StringGroovyMethods.padLeft(Integer.toHexString(ch), 4, "0");
		}
		return null;
	}

	/**
	 * Used to determine if a variable is an instance of String or Groovy GString
	 * @param v - the variable to examine
	 * @return true if variable is one of the two types otherwise false
	 */
	static boolean instanceOfString( v ) {
		return (v instanceof String) || (v instanceof org.codehaus.groovy.runtime.GStringImpl)
	}

	/**
	 * Used to convert a variable to a long IF the variable is a String otherwise it returns the original variable
	 * @param v - a variable to examine
	 * @return the original variable or the Long value if the variable was a String
	 */
	static def toLongIfString( v ) {
		if (instanceOfString(v)) {
			return NumberUtil.toLong(v)
		}
		return v
	}

	/**
	 * Used to split strings using one or more delimiters and trimming the resulting elements in the list
	 * @param str - the string to be split
	 * @param delim - the delimiter to split the string on (default ',')
	 * @param alterDelim - a list of alternate delimiters
	 * @return the list of individual elements from the list
	 */
	static List splitter(String str, String delim=',', List alterDelims=[]) {
		if (delim == '.' || alterDelims?.contains('.')) {
			throw RuntimeException("The split() method does not support the period (.) character as a delimiter")
		}

		// Replace all alternate delimiters with the specified delimiter
		alterDelims.each {
			str = str.replace(it, delim)
		}

		List list = str.split(delim)
		list = list.collect { it.trim() }
		return list
	}
}