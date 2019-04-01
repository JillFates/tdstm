import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyType
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.PartyGroupService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class PartyGroupServiceIntegrationSpec extends Specification {

	PartyGroupService partyGroupService

	@Shared
	PartyType company

	// IOC
	ProjectService           projectService
	SecurityService          securityService

	// Initialized by setup()
	@Shared
	ProjectTestHelper projectHelper

	@Shared
	PersonTestHelper personHelper

	@Shared
	Project project

	@Shared
	Person adminPerson

	@Shared
	UserLogin adminUser

	@Shared
	boolean initialized

	void setup() {
		if(!initialized) {
			projectHelper = new ProjectTestHelper()
			personHelper = new PersonTestHelper()
			company = PartyType.get('COMPANY')
			project = projectHelper.createProject()
			adminPerson = personHelper.createStaff(projectService.getOwner(project))
			assert adminPerson

			// Assign the admin to the project
			projectService.addTeamMember(project, adminPerson, ['ROLE_PROJ_MGR'])

			adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
			assert adminUser
			assert adminUser.username

			// setup the Admin User as though they're logged in
			securityService.assumeUserIdentity(adminUser.username, false)
			initialized = true
		}

	}


	void '01. Test save not partner'() {
		when: 'Saving a new partyGroup, that is not a partner'
			PartyGroup partyGroup = partyGroupService.save('TestCompanyZed', 'a comment', 'N', company)
			PartyGroup testPartyGroup = PartyGroup.get(partyGroup.id)
			boolean isPartner = partyGroupService.isAPartner(testPartyGroup)
			boolean isAProjectPartner = partyGroupService.isAProjectPartner(partyGroup)
		then: 'the party group is saved to the db'
			testPartyGroup
			testPartyGroup.id
			testPartyGroup.name == 'TestCompanyZed'
			testPartyGroup.partyType.id == company.id
			testPartyGroup.comment == 'a comment'
			!isPartner
			!isAProjectPartner

	}

	void '02. Test save partner'() {
		when: 'Saving a new partyGroup that is a partner'
			PartyGroup partyGroup = partyGroupService.save('TestCompanyZed', 'a comment', 'Y', company)
			PartyGroup testPartyGroup = PartyGroup.get(partyGroup.id)
			boolean isPartner = partyGroupService.isAPartner(testPartyGroup)
			boolean isAProjectPartner = partyGroupService.isAProjectPartner(partyGroup)
		then: 'the party group is saved to the db'
			testPartyGroup
			testPartyGroup.id
			testPartyGroup.name == 'TestCompanyZed'
			testPartyGroup.partyType.id == company.id
			testPartyGroup.comment == 'a comment'
			isPartner
			!isAProjectPartner

	}


	void '03. Test update partner'() {
		when: 'Updating a partyGroup with a new name, comment, and making it partner'
			PartyGroup partyGroup = partyGroupService.save('TestCompanyZed', 'a comment', 'N', company)
			partyGroupService.update(partyGroup.id, [name: 'TestCompanyOmega', comment: 'a new comment', partner: 'Y'])
			PartyGroup testPartyGroup = PartyGroup.get(partyGroup.id)
			boolean isPartner = partyGroupService.isAPartner(testPartyGroup)
			boolean isAProjectPartner = partyGroupService.isAProjectPartner(partyGroup)
		then: 'the changes are saved to the db'
			testPartyGroup
			testPartyGroup.id
			testPartyGroup.name == 'TestCompanyOmega'
			testPartyGroup.partyType.id == company.id
			testPartyGroup.comment == 'a new comment'
			isPartner
			!isAProjectPartner

	}

	void '04. Test update no partner'() {
		when: 'updating a company just changing the name and the comment'
			PartyGroup partyGroup = partyGroupService.save('TestCompanyZed', 'a comment', 'N', company)
			partyGroupService.update(partyGroup.id, [name: 'TestCompanyOmega', comment: 'a new comment'])
			PartyGroup testPartyGroup = PartyGroup.get(partyGroup.id)
			boolean isPartner = partyGroupService.isAPartner(testPartyGroup)
			boolean isAProjectPartner = partyGroupService.isAProjectPartner(partyGroup)
		then: 'the changes are saved to the db'
			testPartyGroup
			testPartyGroup.id
			testPartyGroup.name == 'TestCompanyOmega'
			testPartyGroup.partyType.id == company.id
			testPartyGroup.comment == 'a new comment'
			!isPartner
			!isAProjectPartner

	}

	void '05. Test delete'() {
		when: 'deleting a party group'
			PartyGroup partyGroup = partyGroupService.save('TestCompanyZed', 'a comment', 'N', company)
			partyGroupService.delete(partyGroup.id)
			PartyGroup testPartyGroup = PartyGroup.get(partyGroup.id)
		then: 'the group is deleted from the db'
			!testPartyGroup
	}

	void '06. Test list'() {
		setup: 'given a set of companies'
			partyGroupService.save('TestCompany$Alpha', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Omega', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Delta', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Zed', 'a comment', 'N', company)
		when: 'getting a listing, by the company name, and given a max rows of 2 in ascending order'
			def results = partyGroupService.list(['name': 'TestCompany$'], 'companyName', 'asc', 2, 0, 0)
		then: 'the results had the first two companies is ascending order.'
			results.page == 0
			results.records == 4
			results.total == 2

			results.rows[0].cell[0].split('\\$')[1] == 'Alpha</a>'
			results.rows[1].cell[0].split('\\$')[1] == 'Delta</a>'
	}

	void '07. Test list page 2'() {
		setup: 'given a set of companies'
			partyGroupService.save('TestCompany$Alpha', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Omega', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Delta', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Zed', 'a comment', 'N', company)
		when: 'getting a listing, offsetting by 2 by the company name, and given a max rows of 2 in descending order'
			def results = partyGroupService.list(['name': 'TestCompany$'], 'companyName', 'desc', 2, 1, 2)
		then: 'the results have the last two companies in descending order.'
			results.page == 1
			results.records == 4
			results.total == 2

			results.rows[0].cell[0].split('\\$')[1] == 'Delta</a>'
			results.rows[1].cell[0].split('\\$')[1] == 'Alpha</a>'
	}

	void '08. Test list filter to partners'() {
		setup: 'given a set of companies'
			partyGroupService.save('TestCompany$Alpha', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Omega', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Delta', 'a comment', 'Y', company)
			partyGroupService.save('TestCompany$Zed', 'a comment', 'N', company)
		when: 'getting a listing, filtering by partner'
			def results = partyGroupService.list(['name': 'TestCompany$', partner: 'Y'], 'companyName', 'asc', 2, 0, 0)
		then: 'only the company that is a partner is returned.'
			results.page == 0
			results.records == 1
			results.total == 1

			results.rows[0].cell[0].split('\\$')[1] == 'Delta</a>'
	}
}