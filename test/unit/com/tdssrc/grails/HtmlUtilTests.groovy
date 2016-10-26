package com.tdssrc.grails

import spock.lang.Issue
import spock.lang.Specification

/**
 * @author tavo_luna
 */
class HtmlUtilTests extends Specification {

	@Issue('https://support.transitionmanager.com/browse/TM-4706')
	void testIsURL() {
		given:
		String url = "http://google.com"
		def malformedUrls = ['http//google.com', '  ', '', null]

		expect:
		HtmlUtil.isURL(url)

		malformedUrls.each {
			assert !HtmlUtil.isURL(it)
		}
	}

	/**
	 * Admittedly a somewhat silly test, but an earlier implementation of the method would trigger an
	 * IllegalStateException when there was no active request. This test validates that the lack of a
	 * current request is handled correctly.
	 */
	void 'check IP address without active request'() {
		expect:
		HtmlUtil.getRemoteIp() == 'Unknown'
	}
}
