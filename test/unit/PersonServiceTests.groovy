import groovy.mock.interceptor.*
import org.apache.log4j.*

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the PersonService class
 */
@TestFor(PersonService)
class PersonServiceTests extends Specification {

	def personService = new PersonService()
	def log
	def count = 0

	void setup() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain

		// build a logger...
		BasicConfigurator.configure()
		LogManager.rootLogger.level = Level.DEBUG
		log = LogManager.getLogger("PersonService")

		// use groovy metaClass to put the log into your class
		PersonService.class.metaClass.getLog << {-> log }
	}

	// Closure to use to call the assertions repeatedly
	def validName = { name, first, middle='', last='', suffix = '' ->
		count++

		def map = personService.parseName(name)
		def valid = true
		println "($count) $name = $map"
		valid = valid && map.first.equals(first)
		valid = valid && map.last.equals(last)
		valid = valid && map.middle.equals(middle)
		valid = valid && map.suffix.equals(suffix)

		return valid
	}

	// @Test
	void testParseName() {

		expect:
			validName('John', 'John')
			validName('John Martin', 'John', '', 'Martin')
			validName('John Van Zant', 'John', '', 'Van Zant')
			validName('Martin, John', 'John', '', 'Martin')
			validName('John P. Martin', 'John', 'P.', 'Martin')
			validName('John P. Martin Sr', 'John', 'P.', 'Martin', 'Sr')
			validName('John P. Van Zant Sr', 'John', 'P.', 'Van Zant', 'Sr')
			validName('John P. Martin, Sr', 'John', 'P.', 'Martin', 'Sr')
			validName('John P. T. Martin, Sr', 'John', 'P. T.', 'Martin', 'Sr')
			validName('John P. T. Martin Sr', 'John', 'P. T.', 'Martin', 'Sr')
			validName('Martin, John P.', 'John', 'P.', 'Martin')
			validName('Martin, John P. T.', 'John', 'P. T.', 'Martin')
			validName('Van Zant, John P. T.', 'John', 'P. T.', 'Van Zant')
	}

	void testLastNameWithSuffix() {
		expect:
			"Martin, Sr.".equals(personService.lastNameWithSuffix( [last:'Martin', suffix:'Sr.']))
			"Martin".equals(personService.lastNameWithSuffix( [last:'Martin', suffix:'']))
			"Martin".equals(personService.lastNameWithSuffix( [last:'Martin']))
			"".equals(personService.lastNameWithSuffix( [last:'']))
			"".equals( personService.lastNameWithSuffix( [:] ))
	}

}
