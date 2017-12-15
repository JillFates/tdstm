import net.transitionmanager.domain.Party
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService

import spock.lang.Specification

class PartyRelationshipServiceTests extends Specification {

	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	SecurityService securityService

	private Person byWhom

	private UserLogin userLogin
	private Project project
	private MoveEvent moveEvent
	private Person person

	static ProjectTestHelper projectHelper = new ProjectTestHelper()

	void setup() {
		def projectHelper = new ProjectTestHelper()
		project = projectHelper.getProject()
		moveEvent = projectHelper.getFirstMoveEvent(project)

		def personHelper = new PersonTestHelper()
		byWhom = personHelper.getAdminPerson()
		securityService.assumeUserIdentity(byWhom.userLogin.username, false)

		userLogin = byWhom.userLogin
		assert userLogin

		person = personHelper.createPerson(byWhom, project.client)
	}

	void "Test the getTeamRoleTypes"() {
		when:
			List teams = partyRelationshipService.getTeamRoleTypes()
		then:
			teams != null
			teams?.size() > 1
			teams.find { it.id == 'SYS_ADMIN' }
			!teams.find { it.id == 'BOGUS_TEAM_CODE_THAT_WOULD_NOT_EXIST' }
	}

	void "Test the getTeamCodes"() {
		when:
			List teams = partyRelationshipService.getTeamCodes()
		then:
			teams != null
			teams?.size() > 1
			teams.contains('SYS_ADMIN')
			!teams.contains('BOGUS_TEAM_CODE_THAT_WOULD_NOT_EXIST')

		then: 'the AUTO team should not appear by default'
			!teams.contains('AUTO')

		when:
			teams = partyRelationshipService.getTeamCodes(true)
		then:
			teams != null
			teams?.size() > 1

		then: 'the AUTO team should now appear'
			teams.contains('AUTO')
	}

	void "Test the getStaffingRoles method"() {
		when:
			List roles = partyRelationshipService.getStaffingRoles()
		then:
			roles != null
			roles?.size() > 1
			roles.find { it.id == 'SYS_ADMIN' }
			!roles.find { it.id == 'BOGUS_TEAM_CODE_THAT_WOULD_NOT_EXIST' }

		then: 'the AUTO team should appear by default'
			roles.find { it.id == 'AUTO' }

		when:
			roles = partyRelationshipService.getStaffingRoles(false)
		then: 'the AUTO team should appear by default so this is a test to see if it does not when passed false'
			roles.size > 1
			!roles.find { it.id == 'AUTO' }

		then: 'test that the list is sorted by the description'
			for (int i = 0; i < roles.size() - 1; i++) {
				roles[i].description < roles[i + 1].description
			}
	}

	void "Test team assignment to company staff"() {
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

	void "Test Move Event Team Assignments"() {
		// Assign a person to a move event for the PROJ_MGR team
		when:
			// Make sure that the person has these teams
			partyRelationshipService.updateAssignedTeams(person, ['PROJ_MGR', 'SYS_ADMIN'])
			// And then assign the person with the PROJ_MGR team to the event
			personService.addToEvent(project.id, moveEvent.id, person.id, 'PROJ_MGR')
			List moveEventAssignments = MoveEventStaff.findAllByPersonAndMoveEvent(person, moveEvent)
		then:
			moveEventAssignments != null
			moveEventAssignments.size() > 0
			moveEventAssignments.find { it.role.id == 'PROJ_MGR' }

		// Remove the PROJ_MGR team assignment from the person should also delete the moveEvent assignment
		when:
			partyRelationshipService.updateAssignedTeams(person, ['SYS_ADMIN'])
			moveEventAssignments = MoveEventStaff.findAllByPersonAndMoveEvent(person, moveEvent)
		then:
			! moveEventAssignments?.find { it.role.id == 'PROJ_MGR' }
	}

	void "Test getCompanyOfStaff"() {
		// Get company by Person object
		when:
			def company = partyRelationshipService.getCompanyOfStaff(byWhom)
		then:
			company

		// Get company by id number
		when:
			company = partyRelationshipService.getCompanyOfStaff(byWhom.id)
		then:
			company

		// Get company by string of number
		when:
			company = partyRelationshipService.getCompanyOfStaff("${byWhom.id}")
		then:
			company
	}

	void "Test getCompanyStaff"() {
		// Get the list of staff for the company whom byWhom is assigned (TDS)
		when:
			def company = byWhom.company
			List staffList = partyRelationshipService.getCompanyStaff(company)
		then:
			staffList

		// Disable one of the staff and make sure that the list size drops by one
		when:
			int staffSize = staffList.size()
			def staff = staffList[0]
			staff.disable()
			assert staff.save()
		then:
			partyRelationshipService.getCompanyStaff(company).size() == (staffSize - 1)
			// Include the disabled accounts
			partyRelationshipService.getCompanyStaff(company, true)
	}

	void "Test getProjectCompanies with a client, owner and partner" () {
		setup: "Create a project with different owner, client and partner"
			Project project = projectHelper.createProject(null, true)
		when: "Asking for the companies for the project "
			List<Party> companies = partyRelationshipService.getProjectCompanies(project)
		then: "The List has three objects"
			companies.size() == 3
		and: "There are no duplicated companies"
			companies.unique {it.id}.size() == 3
		and: "It contains the owner"
			companies.find {it.id == project.owner.id}
		and: "It contains the client"
			companies.find {it.id == project.client.id}

		when: "Retrieving the partners"
			List<Party> partners = projectService.getPartners(project)
		then: "There's only one partner"
			partners.size() == 1
		and: "The partner is in the list of companies"
			Party partner = partners.get(0)
			companies.find {it.id == partner.id}
	}

	void "Test getProjectCompanies for a project with no partner" () {
		setup: "Create a project with different owner, client"
			Project project = projectHelper.createProject()
		when: "Asking for the companies for the project "
			List<Party> companies = partyRelationshipService.getProjectCompanies(project)
		then: "The List has three objects"
			companies.size() == 2
		and: "There are no duplicated companies"
			companies.unique {it.id}.size() == 2
		and: "It contains the owner"
			companies.find {it.id == project.owner.id}
		and: "It contains the client"
			companies.find {it.id == project.client.id}
	}

	void "Test getProjectCompanies with a null project" () {

		when: "Asking for the companies with a null project "
			List<Party> companies = partyRelationshipService.getProjectCompanies(null)
		then: "The result is null."
			companies == null
	}
}
