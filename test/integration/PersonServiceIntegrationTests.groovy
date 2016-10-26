import net.transitionmanager.domain.Person
import net.transitionmanager.service.PersonService
import spock.lang.Specification

class PersonServiceIntegrationTests extends Specification {

	PersonService personService

	private Project project
	private Person adminPerson
	private PersonTestHelper personHelper = new PersonTestHelper()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()

	void setup() {
		project = projectHelper.getProject()
		adminPerson = personHelper.getAdminPerson()
	}

	void "1. Test the savePerson method with NO default project "() {
		when:
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

	void "2. Test the savePerson method with default project "() {
		when:
		Person newPerson = personHelper.createPerson(adminPerson, project.client, project)

		then:
		newPerson
		newPerson.company.id == project.client.id
		personService.hasAccessToProject(newPerson, project)
	}

	void "3. Test finding persons by their name using a string that must be parsed "() {
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

	void "4. Test finding persons by their name using a mapped name "() {
		// Know person for the project
		when:
		personHelper.createPerson(adminPerson, project.client, null, [lastName: 'Banks', firstName: 'Robin'])
		Map results = personService.findPerson([first: 'Robin', last: 'Banks'], project)

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
		/*
		when:
			results = personService.findPerson("Robin Banks", project)
		then:
			results.isAmbiguous
			results.person == null
		*/
	}

	void "5. Test project team assignment when creating person "() {
		when:
		Person person = personHelper.createPerson(adminPerson, project.client, null,
				[lastName: 'Buster', firstName: 'Block'],
				['SYS_ADMIN', 'DB_ADMIN'])
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
		personService.addToProjectTeam(adminPerson.userLogin, project.id.toString(), person.id.toString(), 'PROJ_MGR', results)
		teams = personService.getPersonTeamCodes(person)

		then:
		teams.size() == 3
		teams.contains('PROJ_MGR')
		personService.isAssignedToTeam(person, 'PROJ_MGR')
		personService.isAssignedToProjectTeam(project, person, 'PROJ_MGR')
		!personService.isAssignedToProjectTeam(project, person, 'SYS_ADMIN')
	}

	void "6. Test assigning a person to a move event team directly by an admin user"() {
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
		personService.assignToProjectEvent(adminPerson.userLogin, personId, meId, 'SYS_ADMIN', '1') == ''

		then: 'validate that the person is assigned at the project and event level'
		personService.isAssignedToProjectTeam(project, person, 'SYS_ADMIN')
		personService.isAssignedToEventTeam(moveEvent, person, 'SYS_ADMIN')

		then: 'Do a negative check for a team that were not assigned'
		!personService.isAssignedToProjectTeam(project, person, 'DB_ADMIN')
		!personService.isAssignedToEventTeam(moveEvent, person, 'DB_ADMIN')
	}

	/*
		TODO : JPM 3/2016 : personService.hasAccessToPerson() does not work correctly
	void "7. Test if a person has access to another person based on who they are "() {
		when:
		// Make sure that the adminPerson is assigned to the project
		Map results = [:]
		personService.addToProjectTeam(adminPerson.userLogin, project.id.toString(), adminPerson.id.toString(), 'PROJ_MGR', results)
		Person person = personHelper.createPerson(adminPerson, project.client)

		then:
		// Admin should be able to access the person
		personService.hasAccessToPerson(adminPerson, person, true, false)

		// client person should not have access to admin
		!personService.hasAccessToPerson(person, adminPerson, true, false)
		!personService.hasAccessToPerson(person, adminPerson, false, false)
	}
	*/
}
