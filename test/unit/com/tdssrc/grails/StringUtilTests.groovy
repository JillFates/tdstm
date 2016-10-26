package com.tdssrc.grails

import spock.lang.Specification

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
}
