package com.tdsops.etl

import spock.lang.Specification
import spock.lang.Unroll

class ElementSpec extends Specification {

	void 'test Element String functions'() {
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
			new Element(value: strDateMMDDYYYY).toDate('yyyy-MM-dd', 'MM-dd-yyyy').value == new Date(2018 - 1900, 6 - 1, 25)
			new Element(value: strAbcDate).toDate('yyyy-MM-dd').value == 'abc-123'
	}

	void 'test Element String functions with null values'() {
		expect:
			new Element(value: null).left(4).value == ''
			new Element(value: null).lowercase().value == ''
			new Element(value: null).middle(3, 5).value == ''
			new Element(value: null).replaceAll('x', 'y').value == ''
			new Element(value: null).replaceFirst('x', 'y').value == ''
			new Element(value: null).replaceLast('x', 'y').value == ''
			new Element(value: null).sanitize().value == ''
			new Element(value: null).trim().value == ''
			new Element(value: null).uppercase().value == ''
			new Element(value: null).toDate('yyyy-MM-dd').value == null
	}

	void 'test Element methodMissing function, delegate to wrapped value'() {
		given:
			String strValue = "Test String"
			int intValue = 1974
			int intValue2 = 1000
			Date dateValue = new Date()

		expect:
			new Element(value: strValue).size() == new Element(value: strValue.size())
			new Element(value: strValue).substring(1, 4) == 'est'

			new Element(value: intValue).power(2) == new Element(value: intValue.power(2))
			new Element(value: intValue) - intValue2 == new Element(value: (intValue - intValue2))

			new Element(value: dateValue).getTime() == new Element(value: dateValue.getTime())
	}

	void 'test Exception if String function applied to Non String Element'() {
		given:
			int value = 100
			Element element = new Element(value: value)

		when:
			element.left(4)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "left function only supported for String values (${value} : ${value.class})"
	}

	@Unroll
	void 'test can use java.lang.Math.round transformation on Element.value=#value'() {

		expect:
			new Element(value: value).round().value == transformedValue

		where:
			value     || transformedValue
			1234      || 1234
			4321.56d  || 4322
			1111.56f  || 1112
			'2222.56' || 2223
			'FOO BAR' || 'FOO BAR'
			null      || null
	}

	@Unroll
	void 'test can use java.lang.Math.abs transformation on Element.value=#value'() {

		expect:
			new Element(value: value).abs().value == transformedValue

		where:
			value     || transformedValue
			1         || 1
			-1        || 1
			2l        || 2
			-2l       || 2
			3d        || 3
			-3d       || 3
			4f        || 4
			-4f       || 4
			'5'       || 5
			'-5'      || 5
			'FOO BAR' || 'FOO BAR'
			null      || null
	}

	@Unroll
	void 'test can use java.lang.Math.ceil transformation on Element.value=#value'() {

		expect:
			new Element(value: value).ceil().value == transformedValue

		where:
			value   || transformedValue
			10      || 10
			10d     || 10d
			10f     || 10f
			10.5    || 11
			10.6d   || 11
			10.7f   || 11
			10.5d   || 11
			10.1d   || 11.0d
			-20.18d || -20
			-20.68d || -20.0d
			-21.69f || -21.0f
			-4.9f   || -4f
			null    || null
	}

	@Unroll
	void 'test can use java.lang.Math.floor transformation on Element.value=#value'() {

		expect:
			new Element(value: value).floor().value == transformedValue

		where:
			value   || transformedValue
			10.5d   || 10
			10.1d   || 10
			10.1f   || 10
			10.1    || 10
			-20.18d || -21
			-20.68d || -21
			4.5f    || 4f
			-4.9f   || -5.0f
			null    || null
	}

	@Unroll
	void 'test can use java.lang.Math.min transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).min(otherValue).value == transformedValue

		where:
			value  | otherValue || transformedValue
			10     | 20         || 10
			10.11  | 20.11      || 10.11
			20     | 10         || 10
			20.12  | 10.12      || 10.12
			20.13f | 10.12      || 10.12
			20.14d | 10.12d     || 10.12d
			20.13f | 10.12f     || 10.12f
			20.14d | 10.12      || 10.12
			20l    | 10         || 10l
			-10l   | -20l       || -20l
			-20    | -10        || -20
			10     | 20.20d     || 10
			null   | 20.44      || null
			20.44  | null       || 20.44
	}

	@Unroll
	void 'test can use java.lang.Math.max transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).max(otherValue).value == transformedValue

		where:
			value  | otherValue || transformedValue
			10     | 20.1       || 20.1
			10     | 20.2f      || 20.2f
			10     | 20.3d      || 20.3d
			10     | 20l        || 20l
			21     | 10         || 21
			10.1d  | 20.10d     || 20.10d
			20.10d | 10.10d     || 20.10d
			10.1f  | 20.10f     || 20.10f
			20.10f | 10.10f     || 20.10f
			-10    | -20        || -10
			-20    | -10        || -10
			15.34  | null       || 15.34
			null   | 15.34      || null
	}

	void 'test can use java.lang.Math.random transformation'() {

		expect:
			new Element(value: null).random().value == null

			with(new Element(value: 123).random(), Element) {
				value != null
				value instanceof Double
			}
			with(new Element(value: 123.01d).random(), Element) {
				value != null
				value instanceof Double
			}
			with(new Element(value: 123.01f).random(), Element) {
				value != null
				value instanceof Double
			}

			with(new Element(value: 123.01f).random(), Element) {
				value != null
				value instanceof Double
			}

			with(new Element(value: 'FOO BAR').random(), Element) {
				value != null
				value instanceof Double
			}
	}

	@Unroll
	void 'test can org.apache.commons.lang3.StringUtils.appendIfMissing transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).appendIfMissing(otherValue).value == transformedValue

		where:
			value        | otherValue || transformedValue
			'grails.com' | '.com'     || 'grails.com'
			'grails.COM' | '.com'     || 'grails.COM.com'
			'grails'     | '.com'     || 'grails.com'
			'grails'     | null       || 'grails'
			null         | '.com'     || null
	}

	@Unroll
	void 'test can org.apache.commons.lang3.StringUtils.appendIfMissingIgnoreCase transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).appendIfMissingIgnoreCase(otherValue).value == transformedValue

		where:
			value        | otherValue || transformedValue
			'grails.com' | '.com'     || 'grails.com'
			3            | '.com'     || 3
			'grails.COM' | '.com'     || 'grails.COM'
			'grails'     | '.com'     || 'grails.com'
			'grails'     | null       || 'grails'
			null         | '.com'     || null
	}

	@Unroll
	void 'test can org.apache.commons.lang3.StringUtils.prependIfMissing transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).prependIfMissing(otherValue).value == transformedValue

		where:
			value            | otherValue || transformedValue
			'grails.com'     | 'www.'     || 'www.grails.com'
			3                | 'www.'     || 3
			'www.grails.com' | 'WWW.'     || 'WWW.www.grails.com'
			'grails.com'     | null       || 'grails.com'
			null             | 'www'      || null
	}

	@Unroll
	void 'test can org.apache.commons.lang3.StringUtils.prependIfMissingIgnoreCase transformation on Element.value=#value and other value=#otherValue'() {

		expect:
			new Element(value: value).prependIfMissingIgnoreCase(otherValue).value == transformedValue

		where:
			value            | otherValue || transformedValue
			'grails.com'     | 'www.'     || 'www.grails.com'
			3                | 'www.'     || 3
			'www.grails.com' | 'WWW.'     || 'www.grails.com'
			'grails.com'     | null       || 'grails.com'
			null             | 'www'      || null
	}
}
