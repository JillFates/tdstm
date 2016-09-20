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
}
