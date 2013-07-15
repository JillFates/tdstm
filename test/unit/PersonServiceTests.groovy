import groovy.mock.interceptor.*
import grails.test.GrailsUnitTestCase
import org.apache.log4j.* 

/**
 * Unit test cases for the TaskService class
 */
class PersonServiceTests extends GrailsUnitTestCase {
	
	def personService = new PersonService()
	def log
	
	void setUp() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		super.setUp() 

		// build a logger...
		BasicConfigurator.configure() 
		LogManager.rootLogger.level = Level.DEBUG
		log = LogManager.getLogger("TaskService")

		// use groovy metaClass to put the log into your class
		PersonService.class.metaClass.getLog << {-> log }
	}
	
	// @Test
	void testParseName() {

		def count = 0

		// Closure to use to call the assertions repeatedly
		def assertName = { name, first, middle='', last='', suffix = '' ->
			count++

			def map = personService.parseName(name)
			println "($count) $name = $map"
			assertEquals map.first, first
			assertEquals map.last, last
			assertEquals map.middle, middle
			assertEquals map.suffix, suffix
		}

		assertName('John', 'John')
		assertName('John Martin', 'John', '', 'Martin')
		assertName('John Van Zant', 'John', '', 'Van Zant')
		assertName('Martin, John', 'John', '', 'Martin')
		assertName('John P. Martin', 'John', 'P.', 'Martin')
		assertName('John P. Martin Sr', 'John', 'P.', 'Martin', 'Sr')
		assertName('John P. Van Zant Sr', 'John', 'P.', 'Van Zant', 'Sr')
		assertName('John P. Martin, Sr', 'John', 'P.', 'Martin', 'Sr')
		assertName('John P. T. Martin, Sr', 'John', 'P. T.', 'Martin', 'Sr')
		assertName('John P. T. Martin Sr', 'John', 'P. T.', 'Martin', 'Sr')
		assertName('Martin, John P.', 'John', 'P.', 'Martin')
		assertName('Martin, John P. T.', 'John', 'P. T.', 'Martin')
		assertName('Van Zant, John P. T.', 'John', 'P. T.', 'Van Zant')
	}

	void testLastNameWithSuffix() {
		assertEquals "Martin, Sr.", personService.lastNameWithSuffix( [last:'Martin', suffix:'Sr.'])
		assertEquals "Martin", personService.lastNameWithSuffix( [last:'Martin', suffix:''])
		assertEquals "Martin", personService.lastNameWithSuffix( [last:'Martin'])
		assertEquals "", personService.lastNameWithSuffix( [last:''])
		assertEquals "", personService.lastNameWithSuffix( [:] )
	}
	
}