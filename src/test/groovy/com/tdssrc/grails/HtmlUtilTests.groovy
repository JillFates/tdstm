package com.tdssrc.grails

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

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

	/**
	 * Test out the escape functionality that will encode text appropriately
	 */
	@Unroll
	void 'test out escape HTML'() {
		expect:
			result == HtmlUtil.escape(value)

		where:
			value 			| result
			'hello'			| 'hello'
			null			| ''
			'abc<p>123'		| 'abc&lt;p&gt;123'
			10				| '10'
			true			| 'true'
			'&lt;'			| '&amp;lt;'
	}

	void 'test the underlying method used for parsing instruction links with valid input'() {
		expect:
			List<String> instructionsLinkList = HtmlUtil.parseMarkupURL(instructionsLink, defaultLabel)
			label == instructionsLinkList[0]
			url == instructionsLinkList[1]
		where:
			label		|	url						|	instructionsLink						|	defaultLabel
			'label'		|	'http://foo.com'		|	'label|http://foo.com'				|	''
			'dummy'		|	'http://foo.com'		|	'http://foo.com'					|	'dummy'
			'label'		|	'http://foo.com'		|	' label | http://foo.com '			|	''
	}

	void 'test the underlying method used for parsing instruction links with invalid input'() {
		expect:
			HtmlUtil.parseMarkupURL(instructionsLink, defaultLabel) == null
		where:
			instructionsLink	|	defaultLabel
			null			|	'label'
			''				|	'label'
			'label'			|	''
	}
}
