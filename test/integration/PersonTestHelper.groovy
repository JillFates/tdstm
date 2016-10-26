/**
 * ProjectTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.domain.Person
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService

class PersonTestHelper {
	def personService
	def securityService

	Long adminPersonId = 100

	PersonTestHelper() {
		personService = ApplicationContextHolder.getService('personService')
		securityService = ApplicationContextHolder.getService('securityService')
		assert (personService instanceof PersonService)
		assert (securityService instanceof SecurityService)
	}

	/**
	 * Creates test person assigning them to the company and team(s) specified
	 * @param currentPerson - the person that is attempting to create the person
	 * @param company - the company that the person should belong to
	 * @param project - the project to assign the person to (optional default null)
	 * @param personProps - a map of person property settings to override the defaults (optional)
	 * @param teams - a list of teams to add the person to (default ['PROJ_MGR'])
	 * @return the newly created person
	 */
	Person createPerson(Person currentPerson, PartyGroup company, Project project=null, Map personProps=null, List teams=['PROJ_MGR'], roles=null) {
		Map personMap = [firstName:"Test ${new Date()}", lastName: 'User', active:'Y', function: teams ]

		// Apply any changes passed into the method
		if (personProps) {
			personMap << personProps
		}

		Person person = personService.savePerson(personMap, currentPerson, company.id, project, true)
		assert person

		if (roles) {
			if (roles instanceof String) [
				roles = [roles]
			]
			securityService.assignRoleCodes(person, roles)
		}

		return person
	}

	/**
	 * Used to get the Admin person to use for tests
	 * @return a Person that has Administration privileges
	 */
	Person getAdminPerson() {
		Person admin = Person.get(adminPersonId)
		assert admin

		// Make certain that the user has the permission we're expecting
		assert securityService.assignRoleCode(admin, 'ADMIN')

		return admin
	}

}
