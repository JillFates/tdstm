package com.tdsops.etl

import spock.lang.Specification

class ElementSpec extends Specification {

	void 'test Element String functions' () {
		given:
			String strValue = "Test String"
			String trimableStr = """
				TRIM ME
			"""
			String strBlankDate = ''
			String strDateValue = 'Date Value'
			String strDateYYYYMMDD = '2018-06-25'
			String strDateMMDDYYYY = '06-25-2018'
			String strAbcDate = 'abc-123'

		expect:
			new Element(value: strValue).sanitize().value == 'Test String'
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
			new Element(value: strBlankDate).toDate('yyyy-MM-dd').value == ''
			new Element(value: strDateValue).toDate('yyyy-MM-dd').value == 'Date Value'
			new Element(value: strDateYYYYMMDD).toDate('yyyy-MM-dd').value == new Date(2018 - 1900, 6 - 1, 25)
			new Element(value: strDateMMDDYYYY).toDate('yyyy-MM-dd','MM-dd-yyyy').value == new Date(2018 - 1900, 6 - 1, 25)
			new Element(value: strAbcDate).toDate('yyyy-MM-dd').value == 'abc-123'
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
			new Element(value: null).toDate('yyyy-MM-dd').value == null
	}

	void 'test Element methodMissing function, delegate to wrapped value' () {
		given:
			String strValue = "Test String"
			int intValue = 1974
			int intValue2 = 1000
			Date dateValue = new Date()

		expect:
			new Element(value: strValue).size() == new Element(value: strValue.size())
			new Element(value: strValue).substring(1, 4) == 'est'

			new Element(value: intValue).power(2) == new Element(value: intValue.power(2))
			new Element(value: intValue) - intValue2 == new Element(value: (intValue - intValue2) )

			new Element(value: dateValue).getTime() == new Element(value: dateValue.getTime())
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
