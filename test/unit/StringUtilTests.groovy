import grails.test.*
import com.tdssrc.grails.StringUtil as SU

class StringUtilTests extends GrailsUnitTestCase {
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDefaultIfEmpty() {
		assertEquals 'Has Value', 'abc123', SU.defaultIfEmpty('abc123', 'foo')
		assertEquals 'Is Blank', 'abc123', SU.defaultIfEmpty('', 'abc123')
		assertEquals 'Is NULL', 'abc123', SU.defaultIfEmpty(null, 'abc123')
		assertEquals 'Is Zero(0)', '0', SU.defaultIfEmpty('0', 'abc123')		
    }

    void testEllipsis() {
    	assertEquals 'No Ellipsis', 'abcdef', SU.ellipsis('abcdef', 10)
    	assertEquals 'Has Ellipsis', 'abc...', SU.ellipsis('abcdefgh', 6)
    	// assertEquals 'Less than 3', 'ab', SU.ellipsis('abcdef', 2)
    }

    void testStripOffPrefixChars() {

    	assertEquals 'Stripped', 'Smith', SU.stripOffPrefixChars('Mr. ', 'Mr. Smith')    	
    	assertEquals 'Skipped', 'Jones', SU.stripOffPrefixChars('xyz', 'Jones')
    }

    void testSplit() {
        assertTrue 'space', ['a','b','c'] == SU.split(' a b c ')
        assertTrue 'spaces', ['a','b','c'] == SU.split('a    b    c')
        assertTrue 'tabs', ['a','b','c'] == SU.split(" a\tb\tc ")
        assertTrue 'comma', ['a','b','c'] == SU.split(' a,b,c', ',')
        assertTrue 'commaWithSpace', ['a','b','c'] == SU.split(' a, b, c ', ',')
        assertTrue 'regex', ['a','b','c'] == SU.split('a.b.c', /\./)
        assertTrue 'Empty string', [] == SU.split('')
        assertTrue 'Null string', [] == SU.split(null)

    }

    void testContainsAny() {
        assertTrue 'case insensitive', SU.containsAny('abcdefg', ['a','c'])
        assertTrue 'case sensitive', SU.containsAny('abCdeFg', ['C','Z'])
        assertFalse 'no match', SU.containsAny('abcdefg', ['X','y'])
    }

    void testContainsAll() {
        assertTrue 'case insensitive', SU.containsAll('abcdefg', ['a','c'])
        assertTrue 'case sensitive', SU.containsAll('abCdeFg', ['C','F'])
        assertFalse 'no match', SU.containsAll('abcdefg', ['a','Z'])
    }

    void testConCat() {
        assertEquals "one", SU.concat('', 'one')
        assertEquals "one-NULL", SU.concat(null, 'one-NULL')
        assertEquals "one,two", SU.concat('one', 'two')
        assertEquals "one : two", SU.concat('one', 'two', ' : ')
    }
}
