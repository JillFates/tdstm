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
}
