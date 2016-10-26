package com.tdsops.common.security.spring

import grails.plugin.springsecurity.web.access.intercept.AbstractFilterInvocationDefinition
import grails.plugin.springsecurity.web.access.intercept.AnnotationFilterInvocationDefinition
import groovy.transform.CompileStatic
import org.springframework.security.web.FilterInvocation

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsAnnotationFilterInvocationDefinition extends AnnotationFilterInvocationDefinition {

	static final String RESOLVED_URL_KEY = AbstractFilterInvocationDefinition.name + '.resolvedUrl'

	protected String determineUrl(FilterInvocation filterInvocation) {
		String url = super.determineUrl(filterInvocation)
		filterInvocation.request.setAttribute RESOLVED_URL_KEY, url
		url
	}
}
