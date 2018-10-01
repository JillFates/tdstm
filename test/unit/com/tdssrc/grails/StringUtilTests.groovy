package com.tdssrc.grails

import spock.lang.Specification
import net.transitionmanager.service.InvalidParamException

/**
 * Unit test cases for the StringUtil class
 */
class StringUtilTests extends Specification {

	void testStripOffPrefixChars() {
		given:
			String match = '!<>='
			String criteria = ">=500"

		expect:
			// Stripped
			'Smith' == StringUtil.stripOffPrefixChars('Mr. ', 'Mr. Smith')
			// Skipped
			'Jones' == StringUtil.stripOffPrefixChars('xyz', 'Jones')

		and:
			// Test to see that it strips off the leading chars from the string
			'500' == StringUtil.stripOffPrefixChars(match, criteria)
			'5' == StringUtil.stripOffPrefixChars(match, '>=5')

			// Test to see that it doesn't modify a string where there are no matches
			'abc' == StringUtil.stripOffPrefixChars('X', 'abc')
			'a' == StringUtil.stripOffPrefixChars('X', 'a')
	}

	void testEllipsis() {
		given:
			String s = 'abcdefgh'

		expect:
			"abc..." == StringUtil.ellipsis(s, 6)
			s == StringUtil.ellipsis(s, 50)

		and:
			// No Ellipsis
			'abcdef' == StringUtil.ellipsis('abcdef', 10)
			// Has Ellipsis
			'abc...' == StringUtil.ellipsis('abcdefgh', 6)
	}

	void "Test toLongIfString"() {
		expect:
			StringUtil.toLongIfString('1') instanceof Long
			StringUtil.toLongIfString("1") instanceof Long
			!(StringUtil.toLongIfString(new Date()) instanceof Long)
	}

	void testDefaultIfEmpty() {
		expect:
			// Has Value
			'abc123' == StringUtil.defaultIfEmpty('abc123', 'foo')
			// Is Blank
			'abc123' == StringUtil.defaultIfEmpty('', 'abc123')
			// Is NULL
			'abc123' == StringUtil.defaultIfEmpty(null, 'abc123')
			// Is Zero(0)
			'0' == StringUtil.defaultIfEmpty('0', 'abc123')
	}

	void testSplit() {
		expect:
			// space
			['a', 'b', 'c'] == StringUtil.split(' a b c ')
			// spaces
			['a', 'b', 'c'] == StringUtil.split('a    b    c')
			// tabs
			['a', 'b', 'c'] == StringUtil.split(" a\tb\tc ")
			// comma
			['a', 'b', 'c'] == StringUtil.split(' a,b,c', ',')
			// commaWithSpace
			['a', 'b', 'c'] == StringUtil.split(' a, b, c ', ',')
			// regex
			['a', 'b', 'c'] == StringUtil.split('a.b.c', /\./)
			// Empty string
			[] == StringUtil.split('')
			// Null string
			[] == StringUtil.split(null)
	}

	void testContainsAny() {
		expect:
			// case insensitive
			StringUtil.containsAny('abcdefg', ['a', 'c'])
			// case sensitive
			StringUtil.containsAny('abCdeFg', ['C', 'Z'])
			// no match
			!StringUtil.containsAny('abcdefg', ['X', 'y'])
	}

	void testContainsAll() {
		expect:
			// case insensitive
			StringUtil.containsAll('abcdefg', ['a', 'c'])
			// case sensitive
			StringUtil.containsAll('abCdeFg', ['C', 'F'])
			// no match
			!StringUtil.containsAll('abcdefg', ['a', 'Z'])
	}

	void testConCat() {
		expect:
			"one" == StringUtil.concat('', 'one')
			"one-NULL" == StringUtil.concat(null, 'one-NULL')
			"one,two" == StringUtil.concat('one', 'two')
			"one : two" == StringUtil.concat('one', 'two', ' : ')
	}

	void testSplitter() {
		when:
			List list = ['one', 'two', 'three']
		then: 'Should properly split and trim the values using default or passed in delimiter'
			list == StringUtil.splitter('one, two, three  ')
			list == StringUtil.splitter('one; two; three  ', ';')
			list == StringUtil.splitter('  one| two: three  ', ';', [',', ':', '|'])

		then: 'Should properly split and trim the values using alternate delimiter'
			list == StringUtil.splitter('  one| two: three  ', ';', [',', ':', '|'])

		then: 'Should return empty list when null value is passed'
			StringUtil.splitter(null).size() == 0

		when:
			def result = StringUtil.splitter('   ')
		then: 'Should return empty list when a string of spaces is passed'
			result instanceof List
			result.size() == 0

		when:
			String str = 'this string should not be split'
			List singleItem = StringUtil.splitter(str)
		then: 'Should return single string in a list when delimeter is not found'
			singleItem.size() == 1
			singleItem[0] == str

		when:
			StringUtil.splitter('one, two, three  ', '.')
		then: 'Should throw an exception when the delimeter is a period (.)'
			RuntimeException ex = thrown()

		when:
			StringUtil.splitter('one, two, three  ', ';', ['f', '.', ';'])
		then: 'Should throw an exception when one of the alternate delimeters is a period (.)'
			ex = thrown()
	}

	void 'Test the sanitize function that should remove all control characters from a string'() {
		// while not touching the ASCII printible characters. This will remove the typical CR, LF, BS
		// along with Unicode Control characters, Line and Paragraph separators, etc {

		expect:
			StringUtil.sanitize(value) == result

		where:
			value               | result
			" abcdefghijklm "   | 'abcdefghijklm'
			" nopqrstuvwxyz "   | 'nopqrstuvwxyz'
			" ABCDEFGHIJKLM "   | 'ABCDEFGHIJKLM'
			" NOPQRSTUVWXYZ "   | 'NOPQRSTUVWXYZ'
			" 01234567890 "     | '01234567890'
			"!@#\$%^&*()-_=+`~" | '!@#$%^&*()-_=+`~'
			"',.<>/?\\"         | '\',.<>/?\\'
			" CR\r. "           | 'CR+.'
			" LF\n. "           | 'LF+.'
			" FF\f. "           | 'FF+.'
			" TAB\t. "          | 'TAB+.'
			" DQuote\". "       | 'DQuote".'
			' SQuote\'. '       | 'SQuote\'.'
			" \t White\t. \t "  | 'White+.'
			" .\bBACKSPACE. "   | '.~BACKSPACE.'
			" .\u2028LineSep"   | '.~LineSep'
			" .\u2029ParaSep"   | '.~ParaSep'
			" .\u00000000. "    | '.~0000.'
			" .\u00090009. "    | '.+0009.'
			" .\u00850085. "    | '.~0085.'
			" [\u007f007f] "    | '[~007f]'
			" [\u008f008f] "    | '[~008f]'

			/*
			" .\ u000A000A. "   | '.~000A.'
			" .\ u000D000D. "   | '.~000D.'
			" .\ uFFFFFFFF. "   | '.~FFFF.'
			" .\ uFFFEFFFE. "   | '.~FFFE.'
			" [\ u00A000A0] "   | '[~00A0]'
			" [\ u0A400A40] "   | '[~0A40]'
			*/
	}

	/**
	 * This method will test the function that strips
	 * spaces from strings in addition to the traditional
	 * sanitize method.
	 */
	void "Test sanitize string stripping white spaces too"(){
		expect:
			StringUtil.sanitizeAndStripSpaces(value) == result

		where:
			value 				| result
			" NOPQRSTUVWXYZ "	| 'NOPQRSTUVWXYZ'
			" 01234567890 "		| '01234567890'
			"!@#\$%^&*()-_=+`~"	| '!@#$%^&*()-_=+`~'
			"TEXTWITHNOSPACES"	| "TEXTWITHNOSPACES"
			"TEXT WITH SPACES"	| "TEXT+WITH+SPACES"
	}

	void "Test the toBoolean method that compares different strings for y/n|1/0|yes/no, etc "() {
		expect:
			StringUtil.toBoolean(value) == result

		where:
			value   | result
			'y'     | true
			'yes'   | true
			't'     | true
			'true'  | true
			'1'     | true
			'n'     | false
			'no'    | false
			'f'     | false
			'false' | false
			'0'     | false
			null    | null
			'a'     | null
			'2'     | null
			''      | null
			1		| true
			0		| false
			-1		| true
			false	| false
			true	| true
	}

	void "Test the containsPathTraversals for any possible hacks"() {
		expect:
			StringUtil.containsPathTraversals(value) == result

		where:
			value           | result
			'../asf'        | true
			'./asf'         | true
			'blah/../root'  | true
			'blah\\..\\root'| true      // Windoze backslashes
			'%2e%2e%2froot' | true      // Encoding of ../
			'%c0%afroot'    | true      // Double encoding /
			'%c1%9croot'    | true      // Double encoding \
			'foo?xyz'       | true      // Not necessarily a hack but we're not going to allow it anyways
			'bar&123'       | true      // Ditto
			null            | true      // A null let's fail it for good measure
			'file.ext'      | false     // Good filename
			'file'          | false
			''              | false
	}

	void "Test the listOfStringsAsMultiValueString"(){
		expect:
			StringUtil.listOfStringsAsMultiValueString(value) == result
		where:
			value           						| result
				[]									| ""
				null								| ""
				["one"]								| "\"one\""
				["one", "two", "three"]				| "\"one\", \"two\", \"three\""
				["\'one\'", "'two'", "\"three\""]	| "\"\'one\'\", \"'two'\", \"\"three\"\""

	}

	void "Test the maskStringCenter"() {
		expect:
			StringUtil.maskStringCenter(value, 5, '*') == result

		where:
			value 						 | result
			"NOPQRSTUVWXYZNOPQRSTUVWXYZ" | 'NOPQR****************VWXYZ'
			"0123456789001234567890"	 | '01234************67890'
			"!@#\$%^&*()-_=+`~!@#\\\$%^" | "!@#\$%*************#\\\$%^"
			"SMALLTEXT"					 | "SMALLTEXT"
	}

	void 'Test isBlank with different values'() {
		expect:
			result == StringUtil.isBlank(value)

		where:
			value					| result
			''						| true
			' '						| true
			'a'						| false
			""						| true
			" "						| true
			"a"						| false
			null					| true
			'' as CharSequence		| true
			'a'	as CharSequence		| false
			null as CharSequence	| true
	}

	void 'Test isNotBlank with different values'() {
		expect:
			result == StringUtil.isNotBlank(value)

		where:
			value					| result
			''						| false
			' '						| false
			'a'						| true
			""						| false
			" "						| false
			"a"						| true
			null					| false
			'' as CharSequence		| false
			'a'	as CharSequence		| true
			null as CharSequence	| false
	}

	void 'Test the md5hex method'() {
		expect:
			result == StringUtil.md5Hex(value)
		where:
			value			| result
			'Some text'	| '9db5682a4d778ca2cb79580bdb67083f'
			''				| 'd41d8cd98f00b204e9800998ecf8427e'
	}

	void 'Test extractPlaceholders'() {
		given:
			Set set

		when: 'called with multiple placeholders'
			set = StringUtil.extractPlaceholders('My favorite color is {COLOR} and my favorite icecream is { FLAVOR }. What is yours?')
		then: 'results should have to values'
			2 == set.size()
		and: 'it contains the expected value color'
			set.contains('COLOR')
		and: 'it contains FLAVOR with spacing removed'
			set.contains('FLAVOR')

		when: 'called with repeated placeholders'
			set = StringUtil.extractPlaceholders('My favorite beer is {BEER}. Do you like {BEER}?')
		then: 'the results should only have one'
			1 == set.size()
		and: 'contain the expected value'
			'BEER' == set[0]
	}

	void 'Test replacePlaceholders method for safe calls'() {
		expect:
			result == StringUtil.replacePlaceholders(text, map)

		where:
			text							| map					      | result
			'Color { COLOR }'			| [COLOR:'red']			| 'Color red'
			'Letters {1}, {2}, {1}'	| ['1':'A', '2': 'Z']	| 'Letters A, Z, A'
			'{first.name}'				| ['first.name': 'Tom']	| 'Tom'
	}

	void 'Test replacePlaceholders method for bad cases'() {
		given:
			String message = 'Expecting {KEY} and {VALUE} in map'
		when: 'called with a single missing parameter in map'
			StringUtil.replacePlaceholders(message, [KEY:'lowercase', value:'is wrong'])
		then: 'an exception is thrown'
			InvalidParamException ex = thrown()
		and: 'the message is what is expecting'
			'Missing parameter for placeholder VALUE' == ex.message

		when: 'called with missing parameters in map'
			StringUtil.replacePlaceholders(message, [key:'lowercase', value:'is wrong'])
		then: 'an exception is thrown'
			InvalidParamException ex2 = thrown()
		and: 'the message contains the list of missing parameters'
			ex2.message.contains('KEY, VALUE')

		when: 'called with a null map'
			StringUtil.replacePlaceholders(message, null)
		then: 'an exception is thrown'
			InvalidParamException ex3 = thrown()
		and: 'the message contains the list of both missing parameters'
			'Parameters map for placeholder replacement is null' == ex3.message

	}

	def 'test containsPlaceholders method'() {
		expect:
			expected == StringUtil.containsPlaceholders(text)
		where:
			text		| expected
			'abcdefg'| false
			''			| false
			'a{b}c'	| true
			null		| false
	}

	def 'test the truncateIfBigger method under some valid scenarios'() {
		expect:
			expected == StringUtil.truncateIfBigger(text, limit)
		where:
			text		|   limit       | expected
			''          |   10          |   ''
			'abc'       |   0           |   ''
			'abc'       |   3           |   'abc'
			'abcd'      |   3           |   'abc'
			'abc'       |   10          |   'abc'
	}

	def 'test the truncateIfBigger under invalid scenarios' () {
		when: 'Trying to truncate a null String'
			StringUtil.truncateIfBigger(null, 10)
		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
		when: 'Trying to trucante using a negative limit'
			StringUtil.truncateIfBigger('abc', -1)
		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
	}


	def 'test generateGuid' () {
		when: 'Generating a GUID'
			String guid = StringUtil.generateGuid()
		then: 'No exception'
			noExceptionThrown()
		and: 'The length of the GUID is 36'
			guid.length() == 36
		when: 'Generating a second guid'
			String guid2 = StringUtil.generateGuid()
		then: "The guids don't match."
			guid != guid2
	}

}
