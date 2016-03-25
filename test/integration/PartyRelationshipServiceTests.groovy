import grails.test.mixin.TestFor
// import spock.lang.Specification
import spock.lang.*

// @TestFor(PartyRelationshipService)
// @TestFor(PersonService)
class PartyRelationshipServiceTests  extends Specification {
	
	def partyRelationshipService
	def personService

	Person byWhom
	UserLogin userLogin
	Project project
	MoveEvent moveEvent
	Person person

/*
	@Shared byWhom = Person.get(100)	// John Martin
	@Shared userLogin = UserLogin.findByPerson(byWhom)
	@Shared project = Project.get(2445)	// Demo Project
	@Shared moveEvent = MoveEvent.findAllByProject(project, [max:1]) // Grab any one of the events
	@Shared person = personService.savePerson(personMap, byWhom, project.client.id, true)
	def setupSpec() {
		// Validate the shared vars are initialized correctly
		assert byWhom
		assert userLogin
		assert project
		assert moveEvent
		assert person
	}
*/

	def setup() {
		byWhom = Person.get(100)	// John Martin
		assert byWhom

		userLogin = UserLogin.findByPerson(byWhom)
		assert userLogin

		project = Project.get(2445)	// Demo Project
		assert project

		moveEvent = MoveEvent.findAllByProject(project, [max:1])[0] // Grab any one of the events
		assert moveEvent

		Map personMap = [firstName:"Test ${new Date()}", lastName: 'User', active:'Y', function: ['PROJ_MGR'] ]
		person = personService.savePerson(personMap, byWhom, project.client.id, true)
		assert person
	}

	def "Test the getTeamRoleTypes"() {
		when:
			List teams = partyRelationshipService.getTeamRoleTypes()

		then:
			teams != null
			teams?.size() > 1
			teams.find { it.id == 'SYS_ADMIN' }
			! teams.find { it.id == 'BOGUS_TEAM_CODE_THAT_WOULD_NOT_EXIST' }

	}

	def "Test the getTeamCodes"() {
		when:
			List teams = partyRelationshipService.getTeamCodes()

		then:
			teams != null
			teams?.size() > 1
			teams.contains('SYS_ADMIN')
			! teams.contains('BOGUS_TEAM_CODE_THAT_WOULD_NOT_EXIST')

	}

	def "Test team assignment to company staff"() {
		// Try assigning the person to two different teams
		when:
			partyRelationshipService.updateAssignedTeams(person, ['PROJ_MGR', 'SYS_ADMIN'])
			List teamAssignments = partyRelationshipService.getCompanyStaffFunctions(project.client.id, person.id)
		then:
			teamAssignments != null
			teamAssignments.size() == 2
			teamAssignments.find { it.id == 'PROJ_MGR' }
			teamAssignments.find { it.id == 'SYS_ADMIN' }

		// Assign the person to a different team and make sure that it removed them from the other teams
		when:
			partyRelationshipService.updateAssignedTeams(person, ['CLEANER'])
			teamAssignments = partyRelationshipService.getCompanyStaffFunctions(project.client.id, person.id)
		then:
			teamAssignments != null
			teamAssignments.size() == 1
			teamAssignments.find { it.id == 'CLEANER' }
	}

	def "Test Move Event Team Assignments"() {
		// Assign a person to a move event for the PROJ_MGR team
		when:
			// Make sure that the person has these teams
			partyRelationshipService.updateAssignedTeams(person, ['PROJ_MGR', 'SYS_ADMIN'])
			// And then assign the person with the PROJ_MGR team to the event
			personService.addToEvent(userLogin, project.id, moveEvent.id, person.id, 'PROJ_MGR')
			List moveEventAssignments = MoveEventStaff.findAllByPersonAndMoveEvent(person, moveEvent)
		then:
			moveEventAssignments != null
			moveEventAssignments.size() > 0
			moveEventAssignments.find { it.role.id == 'PROJ_MGR'}

		// Remove the PROJ_MGR team assignment from the person should also delete the moveEvent assignment
		when:
			partyRelationshipService.updateAssignedTeams(person, ['SYS_ADMIN'])		
			moveEventAssignments = MoveEventStaff.findAllByPersonAndMoveEvent(person, moveEvent)
		then: 
			! moveEventAssignments?.find { it.role.id == 'PROJ_MGR'}
	}

}