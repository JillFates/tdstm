import com.tdsops.tm.enums.domain.SecurityRole
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PartyGroupService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Shared
import spock.lang.Specification

class PartyGroupServiceIntegrationSpec extends Specification {

	PartyGroupService partyGroupService

	@Shared
	PartyType company

	// IOC
	ProjectService           projectService
	SecurityService          securityService

	// Initialized by setup()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper  personHelper  = new PersonTestHelper()
	private Project           project
	private Person            adminPerson
	private UserLogin         adminUser

	void setup() {
		company = PartyType.get('COMPANY')
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
			testPartyGroup.partyType == company
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
			testPartyGroup.partyType == company
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
			testPartyGroup.partyType == company
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
			testPartyGroup.partyType == company
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
			def results = partyGroupService.list([companyName: 'TestCompany$'], 'companyName', 'asc', 2, 0, 0)
		then: 'the results had the first two companies is ascending order.'
			results.page == 0
			results.records == 4
			results.total == 2

			results.rows[0].cell[0].split('\\$')[1] == 'Alpha'
			results.rows[1].cell[0].split('\\$')[1] == 'Delta'
	}

	void '07. Test list page 2'() {
		setup: 'given a set of companies'
			partyGroupService.save('TestCompany$Alpha', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Omega', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Delta', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Zed', 'a comment', 'N', company)
		when: 'getting a listing, offsetting by 2 by the company name, and given a max rows of 2 in descending order'
			def results = partyGroupService.list([companyName: 'TestCompany$'], 'companyName', 'desc', 2, 1, 2)
		then: 'the results have the last two companies in descending order.'
			results.page == 1
			results.records == 4
			results.total == 2

			results.rows[0].cell[0].split('\\$')[1] == 'Delta'
			results.rows[1].cell[0].split('\\$')[1] == 'Alpha'
	}

	void '08. Test list filter to partners'() {
		setup: 'given a set of companies'
			partyGroupService.save('TestCompany$Alpha', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Omega', 'a comment', 'N', company)
			partyGroupService.save('TestCompany$Delta', 'a comment', 'Y', company)
			partyGroupService.save('TestCompany$Zed', 'a comment', 'N', company)
		when: 'getting a listing, filtering by partner'
			def results = partyGroupService.list([companyName: 'TestCompany$', partner: 'Y'], 'companyName', 'asc', 2, 0, 0)
		then: 'only the company that is a partner is returned.'
			results.page == 0
			results.records == 1
			results.total == 1

			results.rows[0].cell[0].split('\\$')[1] == 'Delta'
	}
}