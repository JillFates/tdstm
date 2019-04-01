package com.tdssrc.grails

import net.transitionmanager.exception.InvalidParamException;
import spock.lang.Specification;

class XmlUtilUnitSpec extends Specification {

	def 'Test the convertMapToXmlString method without custom root element'() {
		expect:
			expected == XmlUtil.convertMapToXmlString(value)

		where:
			value						| expected
			['a':'a-value']     		| '<root>\n  <a>a-value</a>\n</root>'
			['a':['b': 'b-value']]     	| '<root>\n  <a>\n    <b>b-value</b>\n  </a>\n</root>'
	}

	def 'Test the convertMapToXmlString method with custom root element'() {
		expect:
			expected == XmlUtil.convertMapToXmlString('test', value)

		where:
			value						| expected
			['a':'a-value']     		| '<test>\n  <a>a-value</a>\n</test>'
			['a':['b': 'b-value']]     	| '<test>\n  <a>\n    <b>b-value</b>\n  </a>\n</test>'
	}

	def 'Test the convertMapToXmlString method with invalid map throws exception'() {
		when:
			def subject = ['':null]
			println XmlUtil.convertMapToXmlString(subject)

		then:
			thrown InvalidParamException
	}
}
