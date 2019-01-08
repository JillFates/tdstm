import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.SettingType
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.ProjectDailyMetric
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Specification
import test.helper.DataviewTestHelper

@Integration
@Rollback
class ProjectServiceIntegrationSpec extends Specification {

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

		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
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
		// <SL> Commenting this out since projectHelper.createProject() is already calling
		// cloneDefaultSettings(), then because of that this part of the test fails
		//then: 'there should be no Asset Field Settings for the project'
		//	0 == Setting.findAllByProjectAndType(p, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()
		//then: 'the cloneDefaultSettings method is called for the new project'
		//	projectService.cloneDefaultSettings(p)
		then: 'the project should have 4 Asset Field Settings'
			4 == Setting.findAllByProjectAndType(p, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()

		// The new project service for creating a project now prepopulates the project from the default so this test
		// no longer makes sense.
		//when: 'the cloneDefaultSettings method is called for the new project'
		//	projectService.cloneDefaultSettings(p)
		//then: 'the project should have 4 Asset Field Settings'
		//	4 == Setting.findAllByProjectAndType(p, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()

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
			UserLogin userLogin = personHelper.createUserLoginWithRoles(userPerson, ["${SecurityRole.ROLE_USER}"])
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

	void '13. Test deleting a project will delete Dataviews belonging to the project'() {
		setup: 'Given a project is created'
			Person userPerson = personHelper.createStaff(project.owner)
			projectService.addTeamMember(project, userPerson, ['PROJ_MGR'])
			UserLogin userLogin = personHelper.createUserLoginWithRoles(userPerson, ["${SecurityRole.ROLE_USER}"])
			securityService.assumeUserIdentity(userLogin.username, false)
		and: 'Dataview is created for the project'

			DataviewTestHelper dataviewTestHelper = new DataviewTestHelper()
			Dataview dataview = dataviewTestHelper.createDataview(project)

		when: 'project is deleted with the ProjectService.deleteProject method'
			projectService.deleteProject(project.id, true)

		then: 'Then the dataview record should be deleted'
			null == Dataview.createCriteria().get {
				eq('id', dataview.id)
				eq('project', project)
			}
	}

	def '14. Test getPartners and getPartnerIds methods together'() {
		expect: 'No partners for the project'
			[] == projectService.getPartnerIds(project)
			[] == projectService.getPartners(project)

		when: 'a partner is added to the project'
			PartyGroup partner = projectHelper.createPartner(project.owner, project)
		then: 'the partner id should be returned'
			[ partner.id ] == projectService.getPartnerIds(project)
		and: 'the partner object should be returned'
			[ partner ] == projectService.getPartners(project)
	}

	def '15. Test getAssociatedStaffIds method'() {
		setup: 'Get the list of owner staff'
			List ownerStaffIds = projectService.getCompanyStaffIds(project.owner)
		expect: 'Project owner staff should have one person'
			1 == ownerStaffIds.size()
		and: 'Project Associated Staff should be the owner staff person'
			ownerStaffIds == projectService.getAssociatedStaffIds(project)
		and: 'No partners for the project'
			[] == projectService.getPartnerIds(project)
		and: 'No staff for client of project'
			[] == projectService.getCompanyStaffIds(project.client)

		when: 'a partner is added to the project'
			PartyGroup partner = projectHelper.createPartner(project.owner, project)
		and: 'partner staff is added to the project'
			def partnerPerson = personHelper.createStaff(partner)
			projectService.addTeamMember(project, partnerPerson, ['PROJ_MGR'])
			List assocStaff = projectService.getAssociatedStaffIds(project)
		then: 'the list of associated staff should have increased by one'
			2 == assocStaff.size()
		and: 'the partner staff should be in the list'
			assocStaff.contains(partnerPerson.id)

		when: 'a client staff is created'
			Person clientPerson = personHelper.createStaff(project.client)
		then: 'the client staffing should have one person'
			1 == projectService.getCompanyStaffIds(project.client).size()
		and: 'the list of associated staff should have increased one more'
			3 == projectService.getAssociatedStaffIds(project).size()

		when: 'a client staff is assigned to the project'
			projectService.addTeamMember(project, clientPerson, ['PROJ_MGR'])
		then: 'the list of associated staff should remain the same size'
			3 == projectService.getAssociatedStaffIds(project).size()

		when: 'a owner staff is created'
			Person ownerPerson = personHelper.createStaff(project.owner)
		then: 'the list of associated staff should remain the same size'
			3 == projectService.getAssociatedStaffIds(project).size()

		when: 'a owner staff is assigned to the project'
			projectService.addTeamMember(project, ownerPerson, ['PROJ_MGR'])
		then: 'the list of associated staff should have increased by one'
			4 == projectService.getAssociatedStaffIds(project).size()
	}

	def '16. Test activitySnapshot method'() {
		setup: 'Delete the metrics ran for today'
			Date today = new Date().clearTime()
			List metrics = ProjectDailyMetric.findAllByMetricDate(today)
			metrics*.delete(flush: true)
		expect: 'There are no ProjectDailyMetrics for today'
			ProjectDailyMetric.findAllByMetricDate(today).isEmpty()

		when: 'a new project is created'
			Project newProject = projectHelper.createProject()
		and: 'the project completion date is a date greater than today'
			newProject.setCompletionDate(today + 1)
		and: 'the activitySnapshot method is run in order to collect metrics'
			projectService.activitySnapshot()
		then: 'we have a ProjectDailyMetric associated to the new Project for today'
			1 == ProjectDailyMetric.countByMetricDateAndProject(today, newProject)
	}
}
