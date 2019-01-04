package com.tdssrc.grails

import groovy.transform.CompileStatic
import java.net.URLDecoder
import java.net.URLEncoder

@CompileStatic
class UrlUtil {
	private static final String HTTPS_REGEX = '(?i)(^.*https:.*)'
	private static final String HTTP_PROTOCOL_REPLACE_REGEX = '(?i)(http:)'
	private static final String HTTPS_PROTOCOL_REPLACE_REGEX = '(?i)(https:)'
	private static final String CAMEL_HTTP4_PROTOCOL = 'http4:'
	private static final String CAMEL_HTTPS4_PROTOCOL = 'https4:'
	private static final String DEFAULT_CHARACTER_ENCODING = 'UTF-8'

	/**
	 * Easy check if the url starts with HTTPS
	 * @param url - the url to check
	 * @return
	 */
	static boolean isSecure(String url) {
		return url ==~ HTTPS_REGEX
	}

	/**
	 * Replaces HTTP, HTTPS protocols from the hostname by Camel HTTP4
	 * @param hostname
	 * @return
	 */
	static String sanitizeUrlForCamel(String url) {
		// do this for http4 camel component
		if (isSecure(url)) {
			return url.replaceAll(HTTPS_PROTOCOL_REPLACE_REGEX, CAMEL_HTTPS4_PROTOCOL).trim()
		} else {
			return url.replaceAll(HTTP_PROTOCOL_REPLACE_REGEX, CAMEL_HTTP4_PROTOCOL).trim()
		}
	}

	/**
	 * Used to decode a value using the URLEncoder with UTF-8
	 * @param text - the text to be decoded
	 * @return the text decoded
	 */
	static String decode(String text) {
		return URLDecoder.decode(text, DEFAULT_CHARACTER_ENCODING)
	}

	/**
	 * Used to encode a value using the URLEncoder with UTF-8
	 * @param text - the text to be encoded
	 * @return the text encoded
	 */
	static String encode(String text) {
		return URLEncoder.encode(text, DEFAULT_CHARACTER_ENCODING)
	}

	/**
	 * Used to parse a set of query string name value parameters into a map and optionally
	 * decoding the values.
	 * @param qs - the query string portion of a URI excluding the question mark (?)
	 * @param decodeValue - a flag if the value should be decoded in the results (default true)
	 * @return the map of the name/value pairs
	 */
	static Map<String, String> queryStringToMap(String qs, Boolean decodeValue=true) {
		decodeValue = (decodeValue==null ? true : decodeValue)
		Map<String, String> map = [:]
		qs.split('&').each { String param ->
			String[] nameAndValue = param.split('=')
			String value = null
			if (nameAndValue.size() == 2) {
				value = (decodeValue ? decode(nameAndValue[1]) : nameAndValue[1])
			}
			map.put( nameAndValue[0], value)
		}
		return map
	}

}
