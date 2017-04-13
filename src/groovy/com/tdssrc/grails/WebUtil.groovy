package com.tdssrc.grails

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

import javax.servlet.http.HttpServletRequest

@CompileStatic
class WebUtil {

	/**
	 * Returns a list of checkbox values as a comma separated string
	 */
	static String checkboxParamAsString(param) {
		listAsMultiValueString(param.collect { id -> "'" + (id?.toString()?.trim() ?: '') + "'" })
	}

	/**
	 * Returns multi-value String of a List
	 */
	static String listAsMultiValueString(Iterable list) {
		list?.join(', ') ?: ''
	}

	/**
	 * Concatenates a list of strings in <li> HTML tag to display.
	 * @param strings  the list of warning strings
	 */
	static String getListAsli(List strings) {
		strings.collect { '<li>' + it + '</li>' }.join('')
	}

	/**
	 * Splits a camel-case String with '_'.
	 */
	static String splitCamelCase(String s) {
		return s.replaceAll(
		   String.format('%s|%s|%s',
			  '(?<=[A-Z])(?=[A-Z][a-z])',
			  '(?<=[^A-Z])(?=[A-Z])',
			  '(?<=[A-Za-z])(?=[^A-Za-z])'
		   ),
		   '_'
		).toLowerCase()
	 }

	/**
	 * Used to determine if a request was issued by a Javascript AJAX call
	 * @param request - the HttpRequest object
	 * @return true if the request is from an Ajax client
	 */
	static boolean isAjax(final HttpServletRequest request) {
		boolean isAjax = SpringSecurityUtils.isAjax(request)
		if (!isAjax) {
			// Angular in particular doesn't set the X-Requested-With header so we check for Accept allowing json
			String accept = request.getHeader(HttpHeaders.ACCEPT)
			if (accept) {
				isAjax = StringUtils.containsIgnoreCase(accept, MediaType.APPLICATION_JSON_VALUE)
			}
		}
		return isAjax
	}
}
