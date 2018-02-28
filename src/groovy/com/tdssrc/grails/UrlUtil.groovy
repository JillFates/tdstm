package com.tdssrc.grails

import groovy.transform.CompileStatic

@CompileStatic
class UrlUtil {
	private static final String HTTPS_REGEX = '(?i)(^.*https:.*)'
	private static final String HTTP_PROTOCOL_REPLACE_REGEX = '(?i)(http:)'
	private static final String HTTPS_PROTOCOL_REPLACE_REGEX = '(?i)(https:)'
	private static final String CAMEL_HTTP4_PROTOCOL = 'http4:'
	private static final String CAMEL_HTTPS4_PROTOCOL = 'https4:'

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

}
