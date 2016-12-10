import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.SecurityService

import spock.lang.Specification
import org.apache.commons.lang.RandomStringUtils as RSU

class PersonServiceIntegrationTests extends Specification {

	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	SecurityService securityService
	private Person person
	private Project project
	private Person adminPerson
	private PersonTestHelper personHelper = new PersonTestHelper()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()

	void setup() {
		project = projectHelper.getProject()
		adminPerson = personHelper.getAdminPerson()
		securityService.assumeUserIdentity(adminPerson.userLogin.username)
		personService.addToProjectSecured(project, adminPerson)
	}

	def '1. Test the savePerson method with NO default project'() {
		setup:
			personService.addToProjectSecured(project, adminPerson)

		when: 'Creating a person they should not have access or be assigned to the project'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
		then:
			newPerson
			newPerson.company.id == project.client.id
			!personService.hasAccessToProject(newPerson, project)
			!personService.isAssignedToProject(project, newPerson)

		// Now try adding the person to the project so that they have access to the project
		when:
			personService.addToProject(adminPerson.userLogin, project.id.toString(), newPerson.id.toString())
		then:
			personService.hasAccessToProject(newPerson, project)
			personService.isAssignedToProject(project, newPerson)

	}

	def '2. Test the savePerson method with default project'() {
		when:
			Person newPerson = personHelper.createPerson(adminPerson, project.client, project)

		then:
			newPerson
			newPerson.company.id == project.client.id
			personService.hasAccessToProject(newPerson, project)
	}

	def '3. Test finding persons by their name using a string that must be parsed'() {
		// Know person for the project
		when:
			personHelper.createPerson(adminPerson, project.client, null, [lastName: 'Banks', firstName: 'Robin'])
			Map results = personService.findPerson("Robin Banks", project)
		then:
			!results.isAmbiguous
			results.person != null
			results.person.firstName == 'Robin'
			results.person.lastName == 'Banks'

		// Known person not on the project
		when:
			results = personService.findPerson(adminPerson.toString(), project)
		then:
			results.person == null

		// Fake person
		when:
			results = personService.findPerson("Robert E. Lee", project)
		then:
			results.person == null

		// Create a 2nd person that will cause ambiguous lookup
		when:
			Person secondPerson = personHelper.createPerson(adminPerson, project.client, null, [lastName: 'Banks', firstName: 'Robin', middleName: 'T'])
			results = personService.findPerson("Robin", project)
		then:
			results.isAmbiguous
			results.person == null
	}

	def '4. Test finding persons by their name using a mapped name'() {
		// Know person for the project
		when:
			personHelper.createPerson(adminPerson, project.client, null, [lastName:'Banks', firstName:'Robin'])
			Map results = personService.findPerson([first:'Robin', last:'Banks'], project)
		then:
			!results.isAmbiguous
			results.person != null
			results.person.firstName == 'Robin'
			results.person.lastName == 'Banks'

		// Known person not on the project
		when:
			results = personService.findPerson([first: adminPerson.firstName, last: adminPerson.lastName], project)
		then:
			results.person == null

		// Fake person
		when:
			results = personService.findPerson([first: 'Robert', middle: 'E.', last: 'Lee'], project)
		then:
			results.person == null

		// Create a 2nd person that will cause ambiguous lookup
		when:
			personHelper.createPerson(adminPerson, project.client, null, [lastName: 'Banks', firstName: 'Robin', middleName: 'T'])
			results = personService.findPerson("Robin", project)
		then:
			results.isAmbiguous
			results.person == null

		// Ambiguous search with middle initial where there are two persons (one with and one without middle initial)
		// TODO : JPM 3/2016 : TM-4756 Fix ambiguous person search
		//when:
		//	results = personService.findPerson("Robin Banks", project)
		//then:
		//	results.isAmbiguous
		//	results.person == null
	}

	def '5. Test project team assignment when creating person'() {
		when:
			Person person = personHelper.createPerson(adminPerson, project.client, null,
				[lastName:'Buster', firstName:'Brock'],
				['SYS_ADMIN', 'DB_ADMIN'],
				['editor'])
			List teams = personService.getPersonTeamCodes(person)
		then:
			teams.size() == 2
			teams.contains('SYS_ADMIN')
			teams.contains('DB_ADMIN')
			teams[0] == 'DB_ADMIN'   // Should be sorted alphabetical
			// Check individually
			personService.isAssignedToTeam(person, 'SYS_ADMIN')
			personService.isAssignedToTeam(person, 'DB_ADMIN')
			!personService.isAssignedToTeam(person, 'PROJ_MGR')

		// Add a new team to the person's repertoire
		when:
			Map results = [:]
			personService.addToProjectTeam(project.id.toString(), person.id.toString(), 'PROJ_MGR', results)
			teams = personService.getPersonTeamCodes(person)
		then:
			teams.size() == 3
			teams.contains('PROJ_MGR')
			personService.isAssignedToTeam(person, 'PROJ_MGR')
			personService.isAssignedToProjectTeam(project, person, 'PROJ_MGR')
			!personService.isAssignedToProjectTeam(project, person, 'SYS_ADMIN')
	}

	def '6. Test assigning a person to a move event team directly by an admin user'() {
		setup:
			String errMsg
			personService.addToProjectSecured(project, adminPerson)

		when: 'setting up the new person and getting an event to work with'
			Person person = personHelper.createPerson(adminPerson, project.client)
			MoveEvent moveEvent = projectHelper.getFirstMoveEvent(project)
		then: 'validate that we got the person and moveEvent we were expecting'
			assert person
			assert moveEvent
			moveEvent.project.id == project.id

		when: 'getting the params needed'
			String meId = moveEvent.id.toString()
			String personId = person.id.toString()
		then: """attempt to assign a person as a SYS_ADMIN to the first MoveEvent of the project before the person
			is assigned to the project"""
			personService.assignToProjectEvent(personId, meId, 'SYS_ADMIN', '1') == ''

		then: 'validate that the person is assigned at the project and event level'
			personService.isAssignedToProjectTeam(project, person, 'SYS_ADMIN')
			personService.isAssignedToEventTeam(moveEvent, person, 'SYS_ADMIN')

		then: 'Do a negative check for a team that were not assigned'
			! personService.isAssignedToProjectTeam(project, person, 'DB_ADMIN')
			! personService.isAssignedToEventTeam(moveEvent, person, 'DB_ADMIN')
	}

	/*
		TODO : JPM 3/2016 : personService.hasAccessToPerson() does not work correctly
	def '7. Test if a person has access to another person based on who they are'() {
		when:
			// Make sure that the adminPerson is assigned to the project
			Map results = [:]
			personService.addToProjectTeam(adminPerson.userLogin, project.id.toString(), adminPerson.id.toString(), 'PROJ_MGR', results)
			Person person = personHelper.createPerson(adminPerson, project.client)
		then:
			// Admin should be able to access the person
			personService.hasAccessToPerson(adminPerson, person, true, false)
			// client person should not have access to admin
			! personService.hasAccessToPerson(person, adminPerson, true, false)
			! personService.hasAccessToPerson(person, adminPerson, false, false)

		// client person should not have access to admin
		!personService.hasAccessToPerson(person, adminPerson, true, false)
		!personService.hasAccessToPerson(person, adminPerson, false, false)
	}
	*/

	def '8. Test findByCompanyAndEmail method'() {
		when:
			def company = projectHelper.createCompany()
			Person p1 = personHelper.createStaff(company, null, null, null, null)
			Person p2 = personHelper.createStaff(company, null, null, null, null)
			List lp1 = personService.findByCompanyAndEmail(company, p1.email)
			List lp2 = personService.findByCompanyAndEmail(company, p2.email)
		then:
			lp1.size() == 1
			lp1[0].id == p1.id
			lp1[0].email
			lp2.size() == 1
			lp2[0].id == p2.id
			personService.findByCompanyAndEmail(company, null) == []
			personService.findByCompanyAndEmail(company, 'bogus@email_address.test') == []

		when: 'Check for same email across companies which probably would never happen anyways'
			def c2 = projectHelper.createCompany()
			Person p3 = personHelper.createStaff(c2, null, null, null, p1.email)
			lp1 = personService.findByCompanyAndEmail(c2, p1.email)
		then:
			lp1.size() == 1
			lp1[0].id == p3.id

		expect: 'calling findByCompanyAndEmail with blank or null should return empty list'
			personService.findByCompanyAndEmail(company, '') == []
			personService.findByCompanyAndEmail(company, null) == []
	}

	def '9. Test findByCompanyAndName method'() {
		when: 'Try finding a single exact match on full name'
			def company = projectHelper.createCompany()
			Person p1 = personHelper.createStaff(company)
			Person p2 = personHelper.createStaff(company)
			Person p3 = personHelper.createStaff(company, p1.firstName, '')
			Map nm = personToNameMap(p1)
			List lp1 = personService.findByCompanyAndName(company, nm)
		then:
			lp1.size() == 1
			lp1[0].id == p1.id

		when: 'Try finding a person that does not exist'
			nm = emptyNameMap()
			nm.first=RSU.randomAlphabetic(10)
			nm.middle=RSU.randomAlphabetic(10)
			nm.last=RSU.randomAlphabetic(10)
			lp1 = personService.findByCompanyAndName(company, nm)
		then:
			lp1.size() == 0

		when: 'Try searching where the middle name is missing'
			nm = personToNameMap(p1)
			nm.middle = ''
			lp1 = personService.findByCompanyAndName(company, nm)
		then:
			lp1.size() == 1
			lp1[0].id == p1.id

		when: 'Find just by first name should result in multiple hits'
			nm = emptyNameMap()
			nm.first = p1.firstName
			lp1 = personService.findByCompanyAndName(company, nm)
		then:
			lp1.size() == 2
			lp1.find { it.id == p1.id }
			lp1.find { it.id == p3.id }

		when: 'Called with an invalid NameMap it should throw an exception'
			personService.findByCompanyAndName(company, [bogusMap:true])
		then:
			// TODO : Change to InvalidParamException when updating 4.x
			// thrown InvalidParamException
			thrown RuntimeException

	}

	// Used to convert a person object into a map used by the PersonService
	private Map personToNameMap(Person p) {
		[first:p.firstName, middle:p.middleName, last:p.lastName]
	}
	private Map emptyNameMap() {
		[first:'', middle:'', last:'']
	}

}
