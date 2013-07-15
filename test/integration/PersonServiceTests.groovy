import grails.test.*

import com.tdssrc.grails.GormUtil

class PersonServiceTests extends GrailsUnitTestCase {
	
	def personService

	def project
	def attributeSet
	def moveEvent
	def moveBundle
    

	void testFindPersonString() {
		def person

		// Know person for the project
		person = personService.findPerson("Robin Banks", project)
		assertNotNull person
		assertEqual 6, person.id

		// Known person not on the project
		person = personService.findPerson("John Martin", project)
		assertNull person

		// Fake person
		person = personService.findPerson("Robert E. Lee", project)
		assertNull person

	}

	void testFindPersonMap() {
		def person

		// Know person for the project
		person = personService.findPerson([first:'Robin', last:'Banks'], project)
		assertNotNull person
		assertEqual 6, person.id

		// Known person not on the project
		person = personService.findPerson([first:'John', last:'Martin'], project)
		assertNull person

		// Fake person
		person = personService.findPerson([first:'Robert', middle:'E.', last:'Lee'], project)
		assertNull person

	}

    protected void setUp() {
        super.setUp()
		personService = new PersonService()
		project = Project.read(457)
	}

    protected void tearDown() {
        super.tearDown()
    }

}
