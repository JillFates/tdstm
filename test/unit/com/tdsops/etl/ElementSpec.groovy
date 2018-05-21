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
			new Element(value: strValue).uppercase().value == 'TEST STRING'
			new Element(value: strValue).lowercase().value == 'test string'
			new Element(value: strValue).replace('Test', 'Prueba').value == 'Prueba String'
			new Element(value: strValue).replaceAll('Test ').value == 'String'
			new Element(value: strValue).replaceFirst('t').value == 'Tes String'
			new Element(value: strValue).replaceLast('t').value == 'Test Sring'
			new Element(value: trimableStr).trim().value == 'TRIM ME'


	}

	void 'test Exception if String function applied to Non String Element' () {
		given:
			Element element = new Element(value: 100)

		when:
			element.left(4)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "left function only supported for String values"
	}


}
