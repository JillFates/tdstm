package com.tdssrc.grails

import grails.test.*
import spock.lang.Specification

/**
 * Unit test cases for the HtmlUtil class
 * author: @tavo_luna
 *
*/
class HtmlUtilTests extends Specification {

	/**
	* task: <a href="https://support.transitionmanager.com/browse/TM-4706">TM-4706</a>
	* test to avoid regression bug
	*/
	public void testIsURL() {
		def url = "http://google.com"
		def malformedUrls = [
			"http//google.com",
			"  ",
			"",
			null
		]

		expect:
			// test for validation
			true == HtmlUtil.isURL(url)

			malformedUrl.each {
				false == HtmlUtil.isURL(it)	
			}			
	}
}