package com.tdssrc.grails

import com.tdsops.common.lang.CollectionUtils
import groovy.json.StringEscapeUtils
import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory

import java.util.regex.Matcher

/**
 * String manipulation methods.
 */
class StringUtil {

	private static final List<String> trueList = ['y', 'yes', 't', 'true', '1'].asImmutable()
	private static final List<String> falseList = ['n', 'no', 'f', 'false', '0'].asImmutable()
	static final String PLACEHOLDER_REGEXP = /\{([^\}]*)\}/

	/**
	 * Truncates a string to a specified length and adds ellipsis (...) if the string was longer
	 * than the specified length. The total length including the ellipsis will be the length specified.
	 * @param s  The string to ellipsis if necessary
	 * @param length  The length to truncate the string to
	 * @return The ellipsised string
	 */
	static String ellipsis(String s, int length) {
		String ellipsised = s
		if (s.size() > length) {
			ellipsised = s.substring(0, length - 3) + '...'
		}
		return ellipsised
	}

	/**
	 * Strip off one or more characters that may appear at the beginning of a string and return the remainder
	 * @param String chars - the characters that should be stripped off
	 * @param String str - the string to manipulate
	 * @return String - the string with the prefix characters removed
	 */
	static String stripOffPrefixChars(chars, str) {
		def r = ''
		for(int i = 0; i < str.size(); i++) {
			if (!chars.contains(str[i]) ) {
				r = str.substring(i)
				break
			}
		}
		return r
	}

	/**
	 * Determine if a string is null, empty of just whitespace
	 * @param subject - the string to check
	 * @return true if blank
	 */
	static boolean isBlank(CharSequence subject) {
		return StringUtils.isBlank(subject)
	}

	/**
	 * Determine if a string is not null, empty of just whitespace
	 * @param subject - the string to check
	 * @return true if not blank
	 */
	static boolean isNotBlank(CharSequence subject) {
		return StringUtils.isNotBlank(subject)
	}

	/**
	 * Set a string to a default if the subject is blank
	 * @param subject - the string to check to see if it is blank
	 * @param defStr - the value to set the string to if it is blank
	 * @return The string value trimmed using default if blank
	 */
	static String defaultIfEmpty(String subject, String defStr) {
		subject?.trim() ?: defStr
	}

	/**
	 * Split a string that is guaranteed to be a list and all elements are trimed
	 * @param words - the string to be split
	 * @param delimiter - the delimiter to split on which can be a string or regex (default whitespace)
	 * @return the split list of words
	 */
	static List split(String words, String delimiter=/\s++/) {
		List list = []
		String trimmedWords = words?.trim() ?: ''
		if (trimmedWords) {
			list = CollectionUtils.asList(trimmedWords.split(delimiter))*.trim()
		}
		return list
	}


	/**
	 * Determine if a string contains a list of strings
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
		for (String word in searchList) {
			matched = searchStr.contains(word)
			if (mustMatchAll) {
				if (!matched) {
					break
				}
			} else if (matched) {
				break
			}
		}
		return matched
	}

	/**
	 * Determine if a string contains ALL of list a strings
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
	 * Validate that any string/file name reference that is passed via the browser
	 * does not contain any directory tranversal notations (e.g. /root, ../../etc/passwd, etc). This
	 * will not allow the following strings in the name:  '..', '/', '\', '&', '?' and the '%' which can be used
	 * to encode other characters. It will also fail if a null is passed as this shouldn't be considered safe...
	 * @reference https://www.owasp.org/index.php/Path_Traversal
	 * @param str - the string to inspect
	 * @return true if there were violations detected
	 */
	static boolean containsPathTraversals(String str) {
		boolean someoneIsTryingToHack = true

		if (str != null) {
			// Make sure that the user can NOT perform any PATH tranversal
			List pathTraversals = ['..', '/', '\\', '%', '&', '?']

			someoneIsTryingToHack = ( str.startsWith('.') || containsAny(str, pathTraversals) )
		}

		return someoneIsTryingToHack
	}

	/**
	 * Concat a string to a null, blank or existing string and using a separator appropriately
	 * @param existingString - the existing string (maybe)
	 * @param newString - the string to append to existingString
	 * @param separator - the seperator if existingString is not blank (default ',')
	 * @return the con
	 */
	static String concat(String existingString, String newString, String separator=',') {
		return existingString ? [existingString, newString].join(separator) : newString
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
				return "\\\""
			case '\\':
				return "\\\\"
			case '\t':
				return "\\t"
			case '\n':
				return "\\n"
			case '\r':
				return "\\r"
			case '\f':
				return "\\f"
			case '\b':
				return "\\b"
			case '\u000B': // vertical tab: http://bclary.com/2004/11/07/#a-7.8.4
				return "\\v"
			case '\u2028':
				return "\\u2028" // Line separator
			case '\u2029':
				return "\\u2029" // Paragraph separator
			case '/':
				// preserve special handling that exists in JSONObject.quote to improve security if JSON is embedded in HTML document
				// prevents outputting "</" gets outputted with unicode escaping for the slash
				if (previousChar == '<') {
					return "\\u002f"
				}
				break
		}
		if(ch < ' ') {
			// escape all other control characters
			return ch
			//return "\\u" + StringGroovyMethods.padLeft(Integer.toHexString(ch), 4, "0")
		}
		return null
	}

	/**
	 * Convert a variable to a long IF the variable is a String/GString otherwise it returns the original variable
	 * @param v - a variable to examine
	 * @return the original variable or the Long value if the variable was a String
	 */
	static toLongIfString( v ) {
		if (v instanceof CharSequence) {
			NumberUtil.toLong(v)
		}

		else {
			v
		}
	}

	/**
	 * Splits strings using one or more delimiters and trims the resulting elements in the list
	 * @param str - the string to be split
	 * @param delim - the delimiter to split the string on (default ',')
	 * @param alterDelim - a list of alternate delimiters
	 * @return the list of individual elements from the list
	 */
	static List splitter(String str, String delim=',', List<String> alterDelims = []) {
		if (isBlank(str)) {
			return []
		}

		if (delim == '.' || alterDelims?.contains('.')) {
			throw new RuntimeException("The split() method does not support the period (.) character as a delimiter")
		}

		// Replace all alternate delimiters with the specified delimiter
		alterDelims.each {
			str = str.replace(it, delim)
		}

		str.split(delim)*.trim()
	}

	/**
	 * Replace all of the escape characters (CR|LF|TAB|Backspace|FormFeed|single/double quote) with plus(+) and replaces
	 * any non-printable, control and special unicode character with a tilda (~). The method will also remove any leading and trailing whitespaces.
	 *
	 * TODO : JPM 4/2016 : sanitize() - need to review for better Unicode support and what the deal is with the \b that flakes out.
	 *
	 * References:
	 *    http://stackoverflow.com/questions/1367322/what-are-all-the-escape-characters-in-java
	 *    http://www.regular-expressions.info/unicode.html
	 *    http://unicode.org/charts/
	 * @param str - the string to be sanitized
	 * @return the freshly sanitized string
	 */
	static String sanitize(String str) {
		if (!str) return str

		String result = str.trim()
		// NOTE stripping out the \b causes + to be added to beginning and end of string for some strange reason
		result = result.replaceAll(/\r|\n|\t|\f/, '+')
		// invisible control characters and unused code points; Line (u2028) and Paragraph (u2029) separators .
		result.replaceAll(/\p{C}|\p{Zl}|\p{Zp}/, '~')
	}

	/**
	 * Sanitize the String with StringUtil.sanitize
	 * and then replace all white spaces with a '+'
	 */
	 static String sanitizeAndStripSpaces(String str){
		 if (str) {
			 str = sanitize(str).replaceAll(/\s/, "+")
		 }
		 return str
	 }

	/**
	 * Escape string being used in Dot graphs to avoid unterminated strings
	 * @param str
	 * @return
	 */
	static String sanitizeDotString(String str) {
		return StringEscapeUtils.escapeJava(str)
	}

	/**
	 * Escape string being used in Javascript to avoid unterminated strings
	 * @param str
	 * @return
	 */
	static String sanitizeJavaScript(String str) {
		return StringEscapeUtils.escapeJavaScript(str)
	}

	/**
	 * Capitalizes a String changing the first letter uppercase
	 * @param text
	 * @return
	 */
	static String capitalize( String text) {
		return StringUtils.capitalize(text)
	}

	/**
	 * Compare various String, numeric and boolean values and returns Boolean or null if undeterminable
	 * @param value - the value to compare
	 * @return true/false if it matches either list otherwize null for undeterminable
	 */
	static Boolean toBoolean(Object value) {
		if (value == null) {
			return null
		} else if (value instanceof Boolean) {
			return value
		} else if (value instanceof CharSequence) {
			String match = value.toLowerCase()
			if (trueList.contains(match)) {
				return true
			}
			if (falseList.contains(match)) {
				return false
 			}
		} else if  (NumberUtil.isaNumber(value)) {
			return value == 0 ? false : true
		}
		return null
	}

	/**
	 * Strip a message enclosed by two tags
	 * ex.
	 * -- OPEN --
	 * da Message
	 * -- CLOSE --
	 */
	static openEnvelop(String openTag, String closeTag, String message){
		def idxB = message.indexOf(openTag)
		if(idxB >= 0){
			def idxE = message.indexOf(closeTag)
			if(idxE < 0){
				throw new RuntimeException("Malformed Message", "Missing ${closeTag} tag for request")
			}
			message = message.substring(idxB + openTag.length(), idxE)
		}
		message.trim()
	}

	/**
	 * This method joins a list of objects into a single string using a given
	 * delimiter and surrounding each element with double quotes.
	 *
	 * @param list: List of strings. Elements can contain escape characters.
	 * @param baseDelimiter: String to be added between each item.
	 *
	 * @return a String containing all the items joined using the provided delimiter.
	 */
	static String listOfStringsAsMultiValueString(Iterable list, String baseDelimiter = ", ") {
		String result = ""
		if( list?.size() > 0){
			String itemsDelimiter = "\"" + baseDelimiter + "\""
			result = "\"" + StringUtils.join(list , itemsDelimiter) + "\""
		}
		return result
	}

	/**
	 * Decode a Base64 encoded string
	 * @param str
	 * @return
	 */
	static String base64DecodeToString(String str) {
		return new String(Base64.decodeBase64(str))
	}

	/**
	 * Returns a copy of the original string showing a number of characters in the edges and the rest of it
	 * is changed to the given one
	 *
	 * "anita lava la tina" mask with 4 characters in each of the edges using '*' becomes:
	 * "anit**********tina"
	 *
	 * in case that the string is shorter than the sum of the visible edges we return the original string
	 * @param numOfCharsInEdges
	 * @return
	 */
	static String maskStringCenter(String original, int numOfCharsInEdges, String character = '*') {

		if ( original == null || original.size() < (numOfCharsInEdges * 2) ){
			return original
		}

		String left = StringUtils.left(original, numOfCharsInEdges)
		String right = StringUtils.right(original, numOfCharsInEdges)

		left = StringUtils.rightPad(left, original.size() - numOfCharsInEdges, character)

		return left + right
	}

	/**
	 * Performs a check to determine if the string compareTo is similar to the given string str.
	 * For instance for compareTo='hello', will return true if str is 'h', 'he', 'hel', 'elo' etc.
	 * Also checks for nullity and blank of str (returns false).
	 *
	 * @param str - the string to compare
	 * @param compareTo - the string to be compared to
	 * @param caseInsensitive - flag if true will perform the comparison case insensitive (default true)
	 * @return True if str is like compareTo.
	 */
	static boolean isLike( String str, String compareTo, boolean caseInsensitive=true) {
		if (isBlank(str) || isBlank(compareTo)) {
			return false
		}
		if (caseInsensitive) {
			str = str.toUpperCase()
			compareTo = compareTo.toUpperCase()
		}
		return compareTo.contains(str)
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 32 character hex string
	 * @param text - the text to convert
	 * @return the MD5 equivalent hash
	 */
	static String md5Hex(String text) {
		return DigestUtils.md5Hex(text)
	}

	/**
	 * Used to replace in a String mustache like placeholders (e.g. {{ KEY }} ) with the name value map provided
	 * @param text - String with Mustache like placeholders to be replaced
	 * @param params Parameters Dictionary
	 * @return the String with the parameters replaced with the values
	 * @throws InvalidParamException if isn't a parameter defined for one or more placeholders
	 *
	 * @usage
	 * <pre>
	 * 'My favorite color is Red' == StringUtil.replacePlaceholders('My favorite color is {COLOR}', [COLOR:'Red'] )
	 * </pre>
	 */
	static String replacePlaceholders(String text, Map params) throws InvalidParamException {
		if (params == null) {
			throw new InvalidParamException('Parameters map for placeholder replacement is null')
		}

		StringBuffer sb = new StringBuffer()
		Matcher m = text =~ PLACEHOLDER_REGEXP
		Set<String> missing = []

		while (m.find()) {
			String paramName = m.group(1).trim()
			if (params.containsKey(paramName)) {
				m.appendReplacement(sb, params[paramName])
			} else {
				missing << paramName
			}
		}
		m.appendTail(sb)

		if (missing) {
			if (missing.size() == 1 ) {
				throw new InvalidParamException("Missing parameter for placeholder ${missing[0]}")
			} else {
				throw new InvalidParamException("Missing parameters for placeholders ${missing.join(', ')}")
			}
		}

		return sb.toString()
	}

	/**
	 * Extract a Set of String mustache like placeholders from another string (avoiding duplicated entries)
	 * @param text String with the placeholders
	 * @return Set of placeholders, empty set if null
	 */
	static Set<String> extractPlaceholders(String text) {
		HashSet<String> placeholders = []

		if (text) {
			Matcher m = text =~ PLACEHOLDER_REGEXP

			while (m.find()) {
				placeholders << m.group(1).trim()
			}
		}

		return placeholders
	}

	/**
	 * Used to determine if a string contains any placeholders
	 * @param text - the string to inspect
	 * @return true if the string contains a placeholder otherwise false
	 */
	static Boolean containsPlaceholders(String text) {
		return extractPlaceholders(text).size() > 0
	}

	/**
	 * If the value parameter size is bigger than the limit, truncate value to limit.
	 * Otherwise just return value.
	 * @param value  The String value
	 * @param limit  The size limit in chars (If limit == 0, just returns value)
	 * @throws InvalidParamException  if any parameter is null
	 */
	static String truncateIfBigger(String value, int limit) {
		if (value == null || limit < 0) {
			throw new InvalidParamException('null parameters not allowed for truncateIfBigger method')
		}

		return (value.size() < limit) ? value : value.substring(0, limit)
	}

	/**
	 * Generate a random GUID.
	 * @return
	 */
	static String generateGuid() {
		return UUID.randomUUID()
	}

	/**
	 * check Unsafe HTML String, returns a list of HTML elements that contains non-compliance code
	 * @param unsafeHtmlString
	 * @return
	 */
	static List<String> checkHTML(String unsafeHtmlString) {

		List<String> results = []
		HTML_POLICY_DEFINITION.sanitize(unsafeHtmlString, new HtmlChangeListener(), results)
		return results
	}

	/**
	 * Class that collects the discarded elements when sanitizing HTML
	 */
	@CompileStatic
	private static class HtmlChangeListener implements org.owasp.html.HtmlChangeListener<List<String>> {

		void discardedTag(List<String> context, String elementName) {
			context << elementName
		}

		void discardedAttributes(List<String> context, String tagName, String... attributeNames) {
			context << tagName
		}
	}

	/**
	 * A policy that can be used to produce policies that sanitize to HTML sinks
	 * via {@link PolicyFactory#apply}.
	 */
	public static final PolicyFactory HTML_POLICY_DEFINITION = new HtmlPolicyBuilder()
			.allowCommonBlockElements()
			.allowCommonInlineFormattingElements()
			.allowStyling()
			.toFactory()

}
