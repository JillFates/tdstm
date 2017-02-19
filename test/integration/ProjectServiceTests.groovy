import com.tdsops.tm.enums.domain.ProjectStatus
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import com.tdsops.tm.enums.domain.SecurityRole

import spock.lang.Specification

class ProjectServiceTests extends Specification {

	// IOC
	ProjectService projectService
	PersonService personService
	SecurityService securityService
	PartyRelationshipService partyRelationshipService

	// Initialized by setup()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private Project project
	private Person adminPerson
	private UserLogin adminUser

	void setup() {
		project = projectHelper.createProject()
		adminPerson = personHelper.createStaff(project.owner)
		assert adminPerson
		projectService.addTeamMember(project, adminPerson, ['PROJ_MGR'])

		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
		assert adminUser
		assert adminUser.username
		// setup the Admin User as though they're logged in
		securityService.assumeUserIdentity(adminUser.username, false)
	}

	void "1. Test the getStaff "() {
		setup:
			List staff

		when: 'getting a list of staff for a project'
			staff = projectService.getStaff(project)
		then: 'then there should be one staff member'
			1 == staff?.size()

		when: 'getting a subset of staff for SYS_ADMIN'
			staff = projectService.getStaff(project, 'SYS_ADMIN')
		then: 'then the list should be empty'
			!staff

		when: 'adding the SYS_ADMIN team to the person and the getting the list'
			projectService.addTeamMember(project, adminPerson, ['SYS_ADMIN'])
			staff = projectService.getStaff(project, 'SYS_ADMIN')
		then: 'the list it should be have the staff member'
			1 == staff?.size()

		when: 'getting a subset of staff for PROJ_MGR'
			staff = projectService.getStaff(project, 'PROJ_MGR')
		then: 'there should be one staff member'
			1 == staff?.size()
	}

	void "2. Test the getProjectManagers "() {
		setup:
			List pms

		when: 'getting a list of project managers for a new project'
			pms = projectService.getProjectManagers(project)
		then: 'the list should contain one person`'
			1 == pms?.size()

		when: 'disabling the one PM on the project'
			def staff = pms[0]
			staff.disable()
			assert staff.save()
			pms = projectService.getProjectManagers(project)
		then:
			!pms
	}

	void "3. Test getProjectsWherePersonIsStaff "() {
		setup:
			List projects

		when: 'getting a list of active projects'
			// Default is ProjectStatus.ACTIVE
			projects = projectService.getProjectsWherePersonIsStaff(adminPerson)
			//int activeCount = projects?.size()
		then: 'one project should be returned'
			1 == projects?.size()

		when: 'getting a list of Completed projects'
			projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.COMPLETED)
		then: 'no projects should be returned'
			! projects

		when: 'getting a list of ANY projects'
			projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.ANY)
		then: 'the list should contain one project'
			1 == projects?.size()

		when: 'creating a person for the project.client'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			projects = projectService.getProjectsWherePersonIsStaff(newPerson, ProjectStatus.ANY)
		then: 'getting the list of their projects it should return none'
			! projects

		// Now assign the person to the project with a team
	}

	void "4. Test getProjectsWhereClient "() {
		when: 'getting a list of ALL projects for the client'
			def company = project.client
			assert company
			List projectList = projectService.getProjectsWhereClient(company, ProjectStatus.ANY)
			int allProjects = projectList?.size()
		then: 'one project should be returned'
			1 == projectList?.size()
		and: 'it should be the project created by the test'
			project.id == projectList[0].id

		// The total of the ACTIVE and COMPLETED projects should equal that of ANY
		when:
			projectList = projectService.getProjectsWhereClient(company, ProjectStatus.ACTIVE)
			int activeProjects = projectList?.size()
			projectList = projectService.getProjectsWhereClient(company, ProjectStatus.COMPLETED)
			int completedProjects = projectList?.size()

		then:
			allProjects == (activeProjects + completedProjects)
	}

	void "5. Test access by users to projects "() {
		when:
		Person adminPerson = personHelper.getAdminPerson()
		Person person = personHelper.createPerson(adminPerson, project.client, project)

		then:
		person
		personService.hasAccessToProject(person, project)
		projectService.getUserProjects(false, ProjectStatus.ANY, [personId: person.id]).size() == 1
		projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: person.id]).size() == 1
		projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: person.id]).size() == 0
	}

	void "6. Test defaultAccountExpirationDate"() {
		when:
		Date compDate = new Date() + 45
		Project project = new Project()

		then:
		projectService.defaultAccountExpirationDate(project) > compDate

		when:
		project.completionDate = compDate

		then:
		projectService.defaultAccountExpirationDate(project) == compDate
	}

	def "7. Test companyIsAssociated"() {
		when:
			Project p = projectHelper.createProject()
			PartyGroup partner = projectHelper.createCompany()
			partyRelationshipService.assignPartnerToCompany(partner, p.owner)
			projectService.updateProjectPartners(p, partner.id)
			PartyGroup unrelatedCompany = projectHelper.createCompany()
		then:
			projectService.companyIsAssociated(p, p.owner.id)
			projectService.companyIsAssociated(p, p.client)
			projectService.companyIsAssociated(p, partner)
			! projectService.companyIsAssociated(p, unrelatedCompany)
	}
}
