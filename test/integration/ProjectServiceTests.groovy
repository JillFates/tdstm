import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.*
import net.transitionmanager.service.*
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

		// Assign the admin to the project
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

	void '5. Testing the getUserProjects for project owner staff to determine user access to projects'() {
		when: 'creating a new person'
			Person person = personHelper.createPerson(adminPerson, project.owner, project)

		then: 'a person should be created'
			person
		and: 'the person does not have access to the project because the person has no login'
			! personService.hasAccessToProject(person, project)

		when: 'creating a user login for the person'
			personHelper.createUserLogin(person)
		then: 'the person should have access to the project'
			personService.hasAccessToProject(person, project)
		and: 'filting for ANY status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ANY, [personId: person.id]).size()
		and: 'filting for ACTIVE status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: person.id]).size()
		and: 'filting for COMPLETED status should return zero projects'
			0 == projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: person.id]).size()
	}

	void '6. Testing the getUserProjects for project partner staff to determine user access to projects'() {
		when: 'creating a partner company and staff for the partner'
			PartyGroup partner = projectHelper.createPartner(project.owner, project)
			Person person = personHelper.createPerson(adminPerson, partner, project)
		then: 'a partner company and  person should be created'
			partner
			person
		and: 'the partner should be associated to the project'
			partner.id == partyRelationshipService.getProjectPartners(project)[0].id
		and: 'the person does not have access to the project because the person has no login'
			! personService.hasAccessToProject(person, project)

		when: 'creating a user login for the person'
			personHelper.createUserLogin(person)
		then: 'the person should have access to the project'
			personService.hasAccessToProject(person, project)
		and: 'filting for ANY status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ANY, [personId: person.id]).size()
		and: 'filting for ACTIVE status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: person.id]).size()
		and: 'filting for COMPLETED status should return zero projects'
			0 == projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: person.id]).size()
	}

	void '7. Testing the getUserProjects for client staff to determine user access to projects'() {
		when: 'creating a new person'
			Person person = personHelper.createPerson(adminPerson, project.client, project)

		then: 'a person should be created'
			person
		and: 'the person does not have access to the project because the person has no login'
			! personService.hasAccessToProject(person, project)
		when: 'creating a user login for the person'
			personHelper.createUserLogin(person)
		then: 'the person should have access to the project'
			personService.hasAccessToProject(person, project)
		and: 'the person has access to the default project because he is an admin'
			securityService.hasPermission(adminPerson, Permission.ProjectManageDefaults)
		and: 'filting for ANY status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ANY, [personId: person.id]).size()
		and: 'filting for ACTIVE status should return one project, plus the default project.'
			2 == projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: person.id]).size()
		and: 'filting for COMPLETED status should return zero projects'
			0 == projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: person.id]).size()
	}

	void '8. Test defaultAccountExpirationDate'() {
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

	def "9. Test companyIsAssociated"() {
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

	void '10. Test the getStaff method'() {
		when: 'calling getStaff for the project'
			List staff = projectService.getStaff(project)
		then: 'by default the admin person should be a member'
			1 == staff?.size()
			adminPerson.id == staff[0].id

		when: 'adding a new person to the project'
			Person person = personHelper.createPerson(adminPerson, project.client, project)
			staff = projectService.getStaff(project)
		then: 'the getStaff count should jump to two'
			2 == staff?.size()
		and: 'the new person should be in the list'
			staff.find { person.id == it.id }

	}

	void '11. Test the cloneDefaultSettings method'() {
		when: 'a new project is created directly'
			Project p = projectHelper.createProject()
		then: 'there should be no Asset Field Settings for the project'
			0 == Setting.findAllByProjectAndType(p, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()

		when: 'the cloneDefaultSettings method is called for the new project'
			projectService.cloneDefaultSettings(p)
		then: 'the project should have 4 Asset Field Settings'
			4 == Setting.findAllByProjectAndType(p, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()

		when: 'the cloneDefaultSettings method is called a second time'
			projectService.cloneDefaultSettings(p)
		then: 'an InvalidRequestException exception should be thrown'
			thrown InvalidRequestException

		when: 'the cloneDefaultSettings method is called for the default project'
			Project dp = Project.get(Project.DEFAULT_PROJECT_ID)
			projectService.cloneDefaultSettings(dp)
		then: 'an InvalidParamException exception should be thrown'
			thrown InvalidParamException

		when: 'the Default project is missing the Asset Field Settings'
			Setting.executeUpdate(
				'delete Setting s where s.project=:p and type=:t',
				[p:dp, t:SettingType.CUSTOM_DOMAIN_FIELD_SPEC] )
		and: 'the cloneDefaultSettings method is called for a new project'
			Project p2 = projectHelper.createProject()
			projectService.cloneDefaultSettings(p2)
		then: 'the ConfigurationException should be thrown'
			thrown ConfigurationException
	}

	void '12. Testing the getUserProjects for users without the permission for accessing the default project'() {
		when: 'creating a new person'
			Person userPerson = personHelper.createStaff(project.owner)
			projectService.addTeamMember(project, userPerson, ['PROJ_MGR'])
			UserLogin userLogin = personHelper.createUserLoginWithRoles(userPerson, ["${SecurityRole.USER}"])
			securityService.assumeUserIdentity(userLogin.username, false)
		then: 'a person should have been created'
			userPerson
		and: 'a user login should have been created'
			userLogin
		then: 'the person does not access to the default project because he is not an admin'
			! securityService.hasPermission(userPerson, Permission.ProjectManageDefaults)
		and: 'filting for ANY status should return one project.'
			1 == projectService.getUserProjects(false, ProjectStatus.ANY, [personId: userPerson.id]).size()
		and: 'filting for ACTIVE status should return one project.'
			1 == projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: userPerson.id]).size()
		and: 'filting for COMPLETED status should return zero projects'
			0 == projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: userPerson.id]).size()
	}
}
