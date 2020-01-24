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

	@Unroll
	void 'test can use java.lang.Math.round transformation on Element.value=#value'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.round().value == transformedValue
			element.errors == errors

		where:
			value     || transformedValue | errors
			1234      || 1234d            | null
			4321.56d  || 4322d            | null
			1111.56f  || 1112d            | null
			77.777    || 78d              | null
			'2222.56' || '2222.56'        | ['Unable to apply round transformation on non numeric value']
			'FOO BAR' || 'FOO BAR'        | ['Unable to apply round transformation on non numeric value']
			null      || null             | null
	}

	@Unroll
	void 'test can use java.lang.Math.abs transformation on Element.value=#value'() {

		setup:
			Element element = new Element(value: value)
			Object result = element.abs().value

		expect:
			result == transformedValue
			result?.class == value?.class
			element.errors == errors

		where:
			value     || transformedValue | errors
			1         || 1                | null
			-1        || 1                | null
			2l        || 2l               | null
			-2l       || 2l               | null
			3d        || 3d               | null
			-3d       || 3d               | null
			4f        || 4f               | null
			-4f       || 4f               | null
			'5'       || '5'              | ['Unable to apply abs transformation on non numeric value']
			'-5'      || '-5'             | ['Unable to apply abs transformation on non numeric value']
			'FOO BAR' || 'FOO BAR'        | ['Unable to apply abs transformation on non numeric value']
			null      || null             | null
	}

	@Unroll
	void 'test can use java.lang.Math.ceil transformation on Element.value=#value'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.ceil().value == transformedValue
			element.errors == errors

		where:
			value     || transformedValue | errors
			10        || 10               | null
			10d       || 10d              | null
			10f       || 10f              | null
			10.5      || 11               | null
			10.6d     || 11               | null
			10.7f     || 11               | null
			10.5d     || 11               | null
			10.1d     || 11.0d            | null
			-20.18d   || -20              | null
			-20.68d   || -20.0d           | null
			-21.69f   || -21.0f           | null
			-4.9f     || -4f              | null
			null      || null             | null
			'7.9'     || '7.9'            | ['Unable to apply ceil transformation on non numeric value']
			'FOO BAR' || 'FOO BAR'        | ['Unable to apply ceil transformation on non numeric value']
	}

	@Unroll
	void 'test can use java.lang.Math.floor transformation on Element.value=#value'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.floor().value == transformedValue
			element.errors == errors

		where:
			value   || transformedValue | errors
			10.5d   || 10               | null
			10.1d   || 10               | null
			10.1f   || 10               | null
			10.1    || 10               | null
			-20.18d || -21              | null
			-20.68d || -21              | null
			4.5f    || 4f               | null
			-4.9f   || -5.0f            | null
			null    || null             | null
			'8.0'   || '8.0'            | ['Unable to apply floor transformation on non numeric value']
			'FOO'   || 'FOO'            | ['Unable to apply floor transformation on non numeric value']
	}

	@Unroll
	void 'test can use java.lang.Math.min transformation on Element.value=#value and other value=#otherValue'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.min(otherValue).value == transformedValue
			element.errors == errors

		where:
			value   | otherValue || transformedValue | errors
			10      | 20         || 10               | null
			10.11   | 20.11      || 10.11            | null
			20      | 10         || 10               | null
			20.12   | 10.12      || 10.12            | null
			20.13f  | 10.12      || 10.12            | null
			20.14d  | 10.12d     || 10.12d           | null
			20.13f  | 10.12f     || 10.12f           | null
			20.14d  | 10.12      || 10.12            | null
			20l     | 10         || 10l              | null
			-10l    | -20l       || -20l             | null
			-20     | -10        || -20              | null
			10      | 20.20d     || 10               | null
			null    | 20.44      || null             | null
			20.44   | null       || 20.44            | null
			'25.44' | null       || '25.44'          | null
			'FOO'   | null       || 'FOO'            | null
			'25.44' | 22         || '25.44'          | ['Unable to apply min transformation on non numeric value']
			'FOO'   | 44         || 'FOO'            | ['Unable to apply min transformation on non numeric value']
	}

	@Unroll
	void 'test can use java.lang.Math.max transformation on Element.value=#value and other value=#otherValue'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.max(otherValue).value == transformedValue
			element.errors == errors

		where:
			value   | otherValue || transformedValue | errors
			10      | 20.1       || 20.1             | null
			10      | 20.2f      || 20.2f            | null
			10      | 20.3d      || 20.3d            | null
			10      | 20l        || 20l              | null
			21      | 10         || 21               | null
			10.1d   | 20.10d     || 20.10d           | null
			20.10d  | 10.10d     || 20.10d           | null
			10.1f   | 20.10f     || 20.10f           | null
			20.10f  | 10.10f     || 20.10f           | null
			-10     | -20        || -10              | null
			-20     | -10        || -10              | null
			15.34   | null       || 15.34            | null
			null    | 15.34      || null             | null
			'25.44' | 22         || '25.44'          | ['Unable to apply min transformation on non numeric value']
			'FOO'   | 44         || 'FOO'            | ['Unable to apply min transformation on non numeric value']
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

	@Unroll
	void 'test can apply toBoolean transformation on Element.value=#value with default value=#defaultValue'() {

		setup:
			Element element = new Element(value: value)
		expect:
			element.toBoolean(defaultValue).value == transformedValue
			element.errors == errors

		where:
			value   | defaultValue || transformedValue | errors
			true    | null         || true             | null
			true    | false        || true             | null
			true    | true         || true             | null
			null    | false        || false            | null
			null    | null         || null             | null
			'true'  | null         || true             | null
			'true'  | false        || true             | null
			'true'  | true         || true             | null
			'false' | null         || false            | null
			'false' | false        || false            | null
			'false' | true         || false            | null
			'Yes'   | null         || true             | null
			'Yes'   | false        || true             | null
			'Yes'   | true         || true             | null
			'No'    | null         || false            | null
			'No'    | false        || false            | null
			'No'    | true         || false            | null
			'1'     | null         || true             | null
			'1'     | false        || true             | null
			'1'     | true         || true             | null
			'0'     | null         || false            | null
			'0'     | false        || false            | null
			'0'     | true         || false            | null
			'FOO'   | true         || 'FOO'            | ['Unable to transform value to Boolean']
	}

	@Unroll
	void 'test can apply toNumber transformation on Element.value=#value with default value=#defaultValue'() {

		setup:
			Element element = new Element(value: value)
		expect:
			element.toNumber(defaultValue).value == transformedValue
			element.errors == errors

		where:
			value | defaultValue || transformedValue | errors
			1     | null         || 1l               | null
			null  | 2            || 2l               | null
			3.0d  | null         || 3l               | ['Unable to transform value to Number']
			5.0f  | null         || 5l               | ['Unable to transform value to Number']
			6l    | null         || 6l               | null
			'FOO' | null         || 'FOO'            | ['Unable to transform value to Number']
			'FOO' | 23           || 'FOO'            | ['Unable to transform value to Number']

	}

	@Unroll
	void 'test can apply toDecimal transformation on Element.value=#value with default value=#defaultValue'() {

		setup:
			Element element = new Element(value: value)

		expect:
			element.toDecimal(precision, defaultValue).value == transformedValue
			element.errors == errors

		where:
			value     | precision | defaultValue || transformedValue | errors
			1         | 2         | null         || 1.00d            | null
			2.02      | 2         | null         || 2.02d            | null
			null      | 2         | 3.0d         || 3.00d            | null
			null      | 2         | null         || null             | null
			''        | 2         | null         || ''               | null
			null      | 2         | 4.0f         || 4.00d            | null
			null      | 2         | 5.55         || 5.55d            | null
			6.1234d   | 3         | null         || 6.123d           | null
			null      | 3         | 7.1234d      || 7.123d           | null
			8l        | 3         | null         || 8.000d           | null
			9.999d    | 3         | null         || 9.999d           | null
			10.1111d  | 3         | null         || 10.111d          | null
			'10.1111' | 3         | null         || 10.111d          | null
			'FOO'     | 3         | null         || 'FOO'            | ['Unable to transform value to Decimal']
			'Yes'     | 3         | null         || 'Yes'            | ['Unable to transform value to Decimal']
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

}
