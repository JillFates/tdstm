import grails.test.*
import com.tdssrc.grails.StringUtil as SU

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the StringUtil class
 */
class StringUtilTests extends Specification {

	protected void setup() {
	}

	protected void cleanup() {
	}

	void testDefaultIfEmpty() {
		expect:
			// Has Value
			'abc123'.equals(SU.defaultIfEmpty('abc123', 'foo'))
			// Is Blank
			'abc123'.equals(SU.defaultIfEmpty('', 'abc123'))
			// Is NULL
			'abc123'.equals(SU.defaultIfEmpty(null, 'abc123'))
			// Is Zero(0)
			'0'.equals(SU.defaultIfEmpty('0', 'abc123'))
	}

	void testEllipsis() {
		expect:
			// No Ellipsis
			'abcdef'.equals(SU.ellipsis('abcdef', 10))
			// Has Ellipsis
			'abc...'.equals(SU.ellipsis('abcdefgh', 6))
	}

	void testStripOffPrefixChars() {
		expect:
			// Stripped
			'Smith'.equals(SU.stripOffPrefixChars('Mr. ', 'Mr. Smith'))
			// Skipped
			'Jones'.equals(SU.stripOffPrefixChars('xyz', 'Jones'))
	}

	void testSplit() {
		expect:
			// space
			['a','b','c'] == SU.split(' a b c ')
			// spaces
			['a','b','c'] == SU.split('a    b    c')
			// tabs
			['a','b','c'] == SU.split(" a\tb\tc ")
			// comma
			['a','b','c'] == SU.split(' a,b,c', ',')
			// commaWithSpace
			['a','b','c'] == SU.split(' a, b, c ', ',')
			// regex
			['a','b','c'] == SU.split('a.b.c', /\./)
			// Empty string
			[] == SU.split('')
			// Null string
			[] == SU.split(null)
	}

	void testContainsAny() {
		expect:
			// case insensitive
			SU.containsAny('abcdefg', ['a','c'])
			// case sensitive
			SU.containsAny('abCdeFg', ['C','Z'])
			// no match
			!SU.containsAny('abcdefg', ['X','y'])
	}

	void testContainsAll() {
		expect:
			// case insensitive
			SU.containsAll('abcdefg', ['a','c'])
			// case sensitive
			SU.containsAll('abCdeFg', ['C','F'])
			// no match
			!SU.containsAll('abcdefg', ['a','Z'])
	}

	void testConCat() {
		expect:
			"one".equals(SU.concat('', 'one'))
			"one-NULL".equals(SU.concat(null, 'one-NULL'))
			"one,two".equals(SU.concat('one', 'two'))
			"one : two".equals(SU.concat('one', 'two', ' : '))
	}

	void testSplitter() {
		when:
			List list = ['one','two','three']
		then: 'Should properly split and trim the values using default or passed in delimiter'
			list == SU.splitter('one, two, three  ')
			list == SU.splitter('one; two; three  ', ';')
			list == SU.splitter('  one| two: three  ', ';', [',', ':', '|'])

		then: 'Should properly split and trim the values using alternate delimiter'
			list == SU.splitter('  one| two: three  ', ';', [',', ':', '|'])

		then: 'Should return empty list when null value is passed'
			SU.splitter(null).size() == 0

		when:
			def result = SU.splitter('   ')
		then: 'Should return empty list when a string of spaces is passed'
			(result instanceof List)
			result.size() == 0

		when:
			String str = 'this string should not be split'
			List singleItem = SU.splitter(str)
		then: 'Should return single string in a list when delimeter is not found'
			singleItem.size() == 1
			singleItem[0].equals(str)

		when:
			SU.splitter('one, two, three  ', '.')
		then: 'Should throw an exception when the delimeter is a period (.)'
			RuntimeException ex = thrown()

		when:
			SU.splitter('one, two, three  ', ';', ['f', '.', ';'])
		then: 'Should throw an exception when one of the alternate delimeters is a period (.)'
			ex = thrown()

	}

	def 'Test the sanitize function that should remove all control characters from a string'() {
		// while not touching the ASCII printible characters. This will remove the typical CR, LF, BS
		// along with Unicode Control characters, Line and Paragraph separators, etc {

		expect:
			SU.sanitize(value) == result

		where:
			value 				| result
			" abcdefghijklm "	| 'abcdefghijklm'
			" nopqrstuvwxyz "	| 'nopqrstuvwxyz'
			" ABCDEFGHIJKLM "	| 'ABCDEFGHIJKLM'
			" NOPQRSTUVWXYZ "	| 'NOPQRSTUVWXYZ'
			" 01234567890 "		| '01234567890'
			"!@#\$%^&*()-_=+`~"	| '!@#$%^&*()-_=+`~'
			"',.<>/?\\"			| '\',.<>/?\\'
			" CR\r. "			| 'CR+.'
			" LF\n. "			| 'LF+.'
			" FF\f. "			| 'FF+.'
			" TAB\t. "			| 'TAB+.'
			" DQuote\". "		| 'DQuote".'
			' SQuote\'. '		| 'SQuote\'.'
			" \t White\t. \t "	| 'White+.'
			" .\bBACKSPACE. "	| '.~BACKSPACE.'
			" .\u2028LineSep"	| '.~LineSep'
			" .\u2029ParaSep"	| '.~ParaSep'
			" .\u00000000. "	| '.~0000.'
			" .\u00090009. "	| '.+0009.'
			" .\u00850085. "	| '.~0085.'
			" [\u007f007f] "	| '[~007f]'
			" [\u008f008f] "	| '[~008f]'

			/*
			" .\ u000A000A. "	| '.~000A.'
			" .\ u000D000D. "	| '.~000D.'
			" .\ uFFFFFFFF. "	| '.~FFFF.'
			" .\ uFFFEFFFE. "	| '.~FFFE.'
			" [\ u00A000A0] "	| '[~00A0]'
			" [\ u0A400A40] "	| '[~0A40]'
			*/
	}

	/**
	 * This method will test the function that strips
	 * spaces from strings in addition to the traditional
	 * sanitize method.
	 */
	def "Test sanitize string stripping white spaces too"(){
		expect:
			SU.sanitizeAndStripSpaces(value) == result
		where:
			value 				| result
			" NOPQRSTUVWXYZ "	| 'NOPQRSTUVWXYZ'
			" 01234567890 "		| '01234567890'
			"!@#\$%^&*()-_=+`~"	| '!@#$%^&*()-_=+`~'
			"TEXTWITHNOSPACES"	| "TEXTWITHNOSPACES"
			"TEXT WITH SPACES"	| "TEXT+WITH+SPACES"
	}

	def "Test the toBoolean method that compares different strings for y/n|1/0|yes/no, etc "() {
		expect:
			SU.toBoolean(value) == result
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
			null	| null
			'a'		| null
			'2'		| null
			''		| null

	}

	def "Test the containsPathTraversals for any possible hacks"() {
		expect:
			SU.containsPathTraversals(value) == result
		where:
			value 			| result
			'../asf'		| true
			'./asf'			| true
			'blah/../root'	| true
			'blah\\..\\root'| true		// Windoze backslashes
			'%2e%2e%2froot'	| true		// Encoding of ../
			'%c0%afroot'	| true		// Double encoding /
			'%c1%9croot'	| true		// Double encoding \
			'foo?xyz'		| true		// Not necessarily a hack but we're not going to allow it anyways
			'bar&123'		| true		// Ditto
			null 			| true		// A null let's fail it for good measure
			'file.ext'		| false		// Good filename
			'file'			| false
			''				| false

	}
}
