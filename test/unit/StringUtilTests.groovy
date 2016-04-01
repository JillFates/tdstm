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

}
