package com.tdssrc.grails


import spock.lang.Specification
import spock.lang.Unroll

class NumberUtilTests extends Specification {

	void testToLong() {
		int four = 4

		expect:
		1L == NumberUtil.toLong('1')
		2L == NumberUtil.toLong("2")
		3L == NumberUtil.toLong(3L)
		4L == NumberUtil.toLong(four)
		99L == NumberUtil.toLong('', 99L)
		NumberUtil.toLong(12.34) == null
		NumberUtil.toLong("123.56") == null
		NumberUtil.toLong(false) == null
		NumberUtil.toLong('') == null
	}

	void testLimit() {
		expect:
		10 == NumberUtil.limit(12, 1, 10)
		10 == NumberUtil.limit(1, 10, 50)
		3 == NumberUtil.limit(3, 1, 10)
	}

	void testToPositiveLong() {
		expect:
		1L == NumberUtil.toPositiveLong('1')
		5L == NumberUtil.toPositiveLong(-3L, 5L)
		!NumberUtil.toPositiveLong('-1')
	}

	void testToInteger() {
		given:
		int four = 4

		expect:
		1 == NumberUtil.toInteger('1')
		2 == NumberUtil.toInteger("2")
		3 == NumberUtil.toInteger(3L)
		4 == NumberUtil.toInteger(four)
		99 == NumberUtil.toInteger('', 99)
		// real number
		NumberUtil.toInteger(12.34) == null
		// real # string'
		NumberUtil.toInteger("123.56") == null
		// boolean'
		NumberUtil.toInteger(false) == null
		// blank
		NumberUtil.toInteger('') == null
	}

	void 'Test toPositiveInteger'() {
		expect:
			// String
			1 == NumberUtil.toPositiveInteger('1')
			// GString
			1 == NumberUtil.toPositiveInteger("1")
			// Negative values no default
			null == NumberUtil.toPositiveInteger('-1')
			// Negative value with default
			2 == NumberUtil.toPositiveInteger('-4', 2)
			// non-numeric
			5 == NumberUtil.toPositiveInteger('abc', 5)
	}

	void testToTinyInt() {
		given:
		int four = 4

		expect:
		1 == NumberUtil.toTinyInt('1')
		2 == NumberUtil.toTinyInt("2")
		3 == NumberUtil.toTinyInt(3L)
		4 == NumberUtil.toTinyInt(four)
		99 == NumberUtil.toTinyInt('', 99)
		// real number
		NumberUtil.toTinyInt(12.34) == null
		// real # string'
		NumberUtil.toTinyInt("123.56") == null
		// boolean
		NumberUtil.toTinyInt(false) == null
		// blank
		NumberUtil.toTinyInt('') == null
		// to large of an int
		NumberUtil.toTinyInt('500') == null
	}

	void testIsLong() {
		expect:
		NumberUtil.isLong(50)
		NumberUtil.isLong(100L)
		NumberUtil.isLong('5')
		NumberUtil.isLong('12391023')
		NumberUtil.isLong('-1232135')
		!NumberUtil.isLong('abc')
		!NumberUtil.isLong(new Date())
		!NumberUtil.isLong('123.12')
		!NumberUtil.isLong(null)
	}

	void testIsDouble() {
		expect:
			!NumberUtil.isDouble(50)
			NumberUtil.isDouble(50d)
			!NumberUtil.isDouble(100L)
			!NumberUtil.isDouble(10.01)
			NumberUtil.isDouble('5')
			NumberUtil.isDouble('12391023')
			NumberUtil.isDouble('-1232135')
			!NumberUtil.isDouble('abc')
			!NumberUtil.isDouble(new Date())
			NumberUtil.isDouble('123.12')
			!NumberUtil.isDouble(null)
	}

	void testIsFloat() {
		expect:
			!NumberUtil.isFloat(50)
			NumberUtil.isFloat(50f)
			!NumberUtil.isFloat(100L)
			!NumberUtil.isFloat(10.01)
			NumberUtil.isFloat('5')
			NumberUtil.isFloat('12391023')
			NumberUtil.isFloat('-1232135')
			NumberUtil.isFloat('12391.023')
			NumberUtil.isFloat('-12321.35')
			!NumberUtil.isFloat('abc')
			!NumberUtil.isFloat(new Date())
			NumberUtil.isFloat('123.12')
			!NumberUtil.isFloat(null)
	}

	void testIsPostiveLong() {
		expect:
		NumberUtil.isPositiveLong('5')
		!NumberUtil.isPositiveLong('-5')
		NumberUtil.isPositiveLong(100)
		!NumberUtil.isPositiveLong(-100L)
	}

	void testMapToPositiveInteger(){
		def arr = ["1", "2", 3L, 4, "nada", null, "5"]
		expect:
			[1,2,3,4,5] == NumberUtil.mapToPositiveInteger(arr)
			[1,2,3,4,0,0,5] == NumberUtil.mapToPositiveInteger(arr, 0)
			NumberUtil.mapToPositiveInteger(arr).each {
				assert it instanceof Integer
			}
	}

	void testMapToPositiveLong(){
		def arr = ["1", "2", 3L, 4, "nada", null, "5"]
		expect:
			[1L,2L,3L,4L,5L] == NumberUtil.mapToPositiveLong(arr)
			[1L,2L,3L,4L,0L,0L,5L] == NumberUtil.mapToPositiveLong(arr, 0)

			NumberUtil.mapToPositiveLong(arr).each {
				assert it instanceof Long
			}
	}

   void testToPositiveLongList(){
      List<String> listOfStrings = ["1", "2", "nada", null, "3", "-4"]
      expect:
      [1L,2L,3L] == NumberUtil.toPositiveLongList(listOfStrings)

      NumberUtil.toPositiveLongList(listOfStrings).each {
         assert it instanceof Long
      }
   }

   def 'Test the isNumber method'() {
	   	expect:
	   		result == NumberUtil.isaNumber( object )
		where:
			object		| result
			5			| true
			5L			| true
			'abc'		| false
			3.142		| false
   }

	def 'Test toLongNumber method'() {
		expect:
			NumberUtil.toLongNumber(object, defValue) == result
		where:
			object                  | defValue || result
			0                       | null     || 0 as Long
			5                       | null     || 5 as Long
			5L                      | null     || 5 as Long
			'abc'                   | null     || null
			'abc'                   | 123456l  || 123456l
			3                       | null     || 3 as Long
			3.142                   | null     || 3 as Long
			'3.142'                 | null     || 3 as Long
			new BigDecimal('3.142') | null     || 3 as Long
	}

	def 'Test toDouble method'() {
		expect:
			result == NumberUtil.toDouble(value, precision, defValue)
		where:
			value      | precision | defValue | result
			'123.1236' | null      | null     | 123.1236
			'123.1236' | 3         | null     | 123.124
			null       | 2         | 5.0      | 5.0
			'foo'      | 2         | 5.0      | 5.0
	}

	@Unroll
	def 'test toLongNumber transformation with value #value and default value #defaultValue to #result'() {

		expect:
			result == NumberUtil.toLongNumber(value, defaultValue)

		where:
			value                   | defaultValue || result
			12                      | 12l          || 12l
			12                      | null         || 12l
			'12'                    | 12l          || 12l
			'12'                    | null         || 12l
			12l                     | 12l          || 12l
			12l                     | null         || 12l
			12f                     | 12l          || 12l
			12f                     | null         || 12l
			12d                     | 12l          || 12l
			12d                     | null         || 12l
			null                    | 12l          || 12l
			null                    | null         || null
			new BigDecimal('12.12') | 12l          || 12l
			new BigDecimal('12.12') | null         || 12l
	}

	@Unroll
	def 'test toDoubleNumber transformation with value #value and default value #defaultValue to #result'() {

		expect:
			result == NumberUtil.toDoubleNumber(value, defaultValue)

		where:
			value                   | defaultValue || result
			12                      | 12d          || 12d
			12                      | null         || 12d
			'12'                    | 12d          || 12d
			'12'                    | null         || 12d
			12l                     | 12d          || 12d
			12l                     | null         || 12d
			12f                     | 12d          || 12d
			12f                     | null         || 12d
			12d                     | 12d          || 12d
			12d                     | null         || 12d
			null                    | 12d          || 12d
			null                    | null         || null
			new BigDecimal('12.12') | 12d          || 12.12d
			new BigDecimal('12.12') | null         || 12.12d

	}

	@Unroll
	def 'test toPositiveLong transformation with value #value and default value #defaultValue to #result'() {

		expect:
			result == NumberUtil.toPositiveLong(value, defaultValue)

		where:
			value                   | defaultValue || result
			12                      | 12l          || 12l
			12                      | null         || 12l
			-12                     | 12l          || 12l
			-12                     | null         || null
			'12'                    | 12l          || 12l
			'12'                    | null         || 12l
			'-12'                   | 12l          || 12l
			'-12'                   | null         || null
			12l                     | 12l          || 12l
			12l                     | null         || 12l
			-12l                    | 12l          || 12l
			-12l                    | null         || null
			12f                     | 12l          || 12l
			12f                     | null         || 12l
			-12f                    | 12l          || 12l
			-12f                    | null         || null
			12d                     | 12l          || 12l
			12d                     | null         || 12l
			-12d                    | 12l          || 12l
			-12d                    | null         || null
			null                    | 12l          || 12l
			null                    | null         || null
			new BigDecimal('12.12') | 12l          || 12l
			new BigDecimal('12.12') | null         || 12l
	}

	def 'Test toFloat method'() {
		expect:
			result == NumberUtil.toFloat(value, defValue)
		where:
			value       | defValue || result
			'123.1236f' | 5.0f     || 123.1236f
			'123.1236f' | null     || 123.1236f
			null        | 5.0f     || 5.0f
			123.23f     | 5.0f     || 123.23f
	}

	def 'Test percentage'() {
		expect:
			result == NumberUtil.percentage(total, value)
		where:
			total	|	value	||	result
			1000	|	500		||	50
			1000	|	0		||	0
			0		|	500		||	0
			1000	|	1		||	1
			1000	|	999		||	99
			1000	|	473		||	48
			1000	|	463		||	47
	}
}
