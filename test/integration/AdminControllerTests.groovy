import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the TimeUtil class
 * Note that in order to test with the HttpSession that this test spec is using the AdminController not for any thing in particular
 * but it allows the tests to access the session and manipulate it appropriately.
*/
@TestFor(AdminController)
class AdminControllerTests extends Specification {

	def personHelper
	def projectHelper
	Project project
	def privPerson, privUser
	def unPrivPerson, unPrivUser


	def setup() {
		personHelper = new PersonTestHelper()
		projectHelper = new ProjectTestHelper()
		privUser = personHelper.getAdminPerson()
	}

	def 'Test the AccountImportExport controller methods for permissions'() {
		setup:
			project = projectHelper.createProject(privUser.company)
			unPrivPerson = personHelper.createPerson(privUser, project.company, project, null, null, 'USER')

		when:
			def x=1
			// Need to mock the session to return the user appropriately
			//controller.importAccountsTemplate()
		then:
			x == 1
	}
}
