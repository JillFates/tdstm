import grails.test.mixin.TestFor
import spock.lang.*

class PartyRelationshipServiceTests  extends Specification {
	
	def partyRelationshipService
	def personService

	Person byWhom
	UserLogin userLogin
	Project project
	MoveEvent moveEvent
	Person person

	static Long byWhomId=100
	static Long projectId=2445

	def setup() {
		byWhom = Person.get(byWhomId)	// John Martin
		assert byWhom

		userLogin = UserLogin.findByPerson(byWhom)
		assert userLogin

		project = Project.get(projectId)	// Demo Project
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

	def "Test getCompanyOfStaff"() {
		// Get company by Person object
		when:
			def company = partyRelationshipService.getCompanyOfStaff(byWhom)
		then:
			company != null

		// Get company by id number
		when:
			company = partyRelationshipService.getCompanyOfStaff(byWhomId)
		then:
			company != null

		// Get company by string of number
		when:
			company = partyRelationshipService.getCompanyOfStaff("$byWhomId")
		then:
			company != null
	}

	def "Test getCompanyStaff"() {
		// Get the list of staff for the company whom byWhom is assigned (TDS)
		when:
			def company = byWhom.company
			List staffList = partyRelationshipService.getCompanyStaff(company)
		then:
			staffList != null
			def staffSize = staffList.size()
			staffSize > 0

		// Disable one of the staff and make sure that the list size drops by one
		when: 
			def staff = staffList[0]
			staff.disable()
			assert staff.save()
		then:
			partyRelationshipService.getCompanyStaff(company).size() == (staffSize - 1)
			// Include the disabled accounts
			partyRelationshipService.getCompanyStaff(company, true)
	}

}