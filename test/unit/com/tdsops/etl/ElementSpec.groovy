package com.tdsops.etl

import spock.lang.Specification

class ElementSpec extends Specification {
	void 'test Element String functions' () {
		given:
			String strValue = "Test String"
			String trimableStr = """
				TRIM ME
			"""

		expect:
			new Element(value: strValue).sanitize().value == 'Test+String'
			new Element(value: strValue).left(4).value == 'Test'
			new Element(value: strValue).right(4).value == 'ring'
			new Element(value: strValue).middle(6, 3).value == 'Str'
			// new Element(value: strValue).middle(16, 3).value == ''
			new Element(value: strValue).uppercase().value == 'TEST STRING'
			new Element(value: strValue).lowercase().value == 'test string'
			new Element(value: strValue).replace('Test', 'Prueba').value == 'Prueba String'
			new Element(value: strValue).replaceAll('t', 'X').value == 'TesX SXring'
			new Element(value: strValue).replaceFirst('t', 'X').value == 'TesX String'
			new Element(value: strValue).replaceLast('t', 'X').value == 'Test SXring'
			new Element(value: trimableStr).trim().value == 'TRIM ME'
	}

	void 'test Element String functions with null values' () {
		expect:
			new Element(value: null).left(4).value == ''
			new Element(value: null).lowercase().value == ''
			new Element(value: null).middle(3,5).value == ''
			new Element(value: null).replaceAll('x','y').value == ''
			new Element(value: null).replaceFirst('x','y').value == ''
			new Element(value: null).replaceLast('x','y').value == ''
			new Element(value: null).sanitize().value == ''
			new Element(value: null).trim().value == ''
			new Element(value: null).uppercase().value == ''
	}

	void 'test Exception if String function applied to Non String Element' () {
		given:
			int value = 100
			Element element = new Element(value: value)

		when:
			element.left(4)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "left function only supported for String values (${value} : ${value.class})"
	}


}
