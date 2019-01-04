import net.transitionmanager.command.PersonCO
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.*
import com.tds.asset.Application
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.RoleType
import net.transitionmanager.security.Permission
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.SecurityService
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.PartyGroup

import org.apache.commons.lang.RandomStringUtils as RSU
import groovy.time.TimeCategory
import spock.lang.Specification

class PersonServiceIntegrationTests extends Specification {

	UserLogin adminUser

	MoveEventService moveEventService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	SecurityService securityService
	private Person person
	private Project project
	private Person adminPerson
	private PersonTestHelper personHelper = new PersonTestHelper()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private AssetTestHelper assetHelper = new AssetTestHelper()

	final Map personMap = [lastName:'Bullafarht']

	def setup() {
		projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()

		adminPerson = personHelper.createStaff(project.owner)
		assert adminPerson

		projectService.addTeamMember(project, adminPerson, ['PROJ_MGR'])

		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
		// adminUser = UserLogin.findByUsername('tdsadmin')
		assert adminUser
		assert adminUser.username
		//adminUser = UserLogin.findByUsername('tdsadmin')

		// logs the admin user into the system
		securityService.assumeUserIdentity(adminUser.username, false)
		println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
		assert securityService.isLoggedIn()

		personService.addToProjectSecured(project, adminPerson)
	}

	def '01. Test the AdminPerson and AdminUser are setup correctly'() {
		expect: 'admin person is setup correctly'
			adminPerson
			adminPerson.userLogin
			securityService.hasPermission(adminUser, Permission.AdminUtilitiesAccess)
			personService.hasAccessToProject(adminPerson, project)
	}

	def '02. Test owner staff access to a project'() {
		setup: 'add the adminPerson to the project so that'
			personService.addToProjectSecured(project, adminPerson)

		when: 'creating a person and user as staff for the project owner with USER role'
			Person newPerson = personHelper.createPerson(adminPerson, project.owner)
			personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.USER}"])
		then: 'the person and the accompaning userlogin are created and associated to the project owner'
			newPerson
			newPerson.id
			newPerson.userLogin
			newPerson.userLogin.id
			newPerson.company == project.owner
		and: 'the person is staff of the client'
			newPerson.company.id == project.owner.id
		and: 'the person does not have access to the project'
			!personService.hasAccessToProject(newPerson, project)
		and: 'the person is not assigned to the project'
			!personService.isAssignedToProject(project, newPerson)

		when: 'adding the person to the project'
			personService.addToProject(adminUser, project.id.toString(), newPerson.id.toString())
		then: 'the person should be able to access the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the person is assigned to the project'
			personService.isAssignedToProject(project, newPerson)

		when: 'a new project for the newPerson.company as the owner is created'
			Project newProject = projectHelper.createProject()
			newProject.owner = newPerson.company
		then: 'the newPerson should not have access to the project'
			!personService.hasAccessToProject(newPerson, newProject)
		and: 'the newPerson is not assigned to the project'
			!personService.isAssignedToProject(newProject, newPerson)

		when: 'the newPerson is given the ADMIN security role'
			personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.ADMIN}"])
		then: 'the newPerson should have access to the new project'
			personService.hasAccessToProject(newPerson, newProject)
		and: 'the newPerson should still NOT be assigned to the project'
			!personService.isAssignedToProject(newProject, newPerson)

		when: 'an unrelatedProject is created with the default project.client as the owner'
			Project unrelatedProject = projectHelper.createProject()
			unrelatedProject.owner = project.client
		then: 'the newPerson should not have access to the unrelatedProject'
			!personService.hasAccessToProject(newPerson, unrelatedProject)
		and: 'the newPerson is not assigned to the unrelatedProject'
			!personService.isAssignedToProject(unrelatedProject, newPerson)
	}

	def '03. Test client staff access to a project'() {
		setup: 'add the adminPerson to the project so that'
			personService.addToProjectSecured(project, adminPerson)

		when: 'creating a person and their userlogin as staff for the project client with USER role'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.USER}"])
		then: 'the person and userlogin are created'
			newPerson
			newPerson.userLogin
		and: 'the person is staff of the client'
			newPerson.company.id == project.client.id
		and: 'the person does not have access to the project'
			!personService.hasAccessToProject(newPerson, project)
		and: 'the person is not assigned to the project'
			!personService.isAssignedToProject(project, newPerson)

		when: 'adding the person to the project'
			personService.addToProject(adminUser, project.id.toString(), newPerson.id.toString())
		then: 'the person should be able to access the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the person is assigned to the project'
			personService.isAssignedToProject(project, newPerson)

		when: 'a new project for the newPerson.company as the owner is created'
			Project newProject = projectHelper.createProject()
			newProject.owner = newPerson.company
		then: 'the newPerson should not have access to the project'
			!personService.hasAccessToProject(newPerson, newProject)
		and: 'the newPerson is not assigned to the project'
			!personService.isAssignedToProject(newProject, newPerson)

		when: 'the newPerson is given the ADMIN security role'
			personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.ADMIN}"])
		then: 'the newPerson should have access to the new project'
			personService.hasAccessToProject(newPerson, newProject)
		and: 'the newPerson should still NOT be assigned to the project'
			!personService.isAssignedToProject(newProject, newPerson)

		when: 'an unrelatedProject is created with the default project.owner as the owner'
			Project unrelatedProject = projectHelper.createProject()
			unrelatedProject.owner = project.owner
		then: 'the newPerson should not have access to the unrelatedProject'
			!personService.hasAccessToProject(newPerson, unrelatedProject)
		and: 'the newPerson is not assigned to the unrelatedProject'
			!personService.isAssignedToProject(unrelatedProject, newPerson)
	}

	def '04. Test partner staff access to a project'() {

		when: 'a partner and partnerStaff with USER role are created '
			PartyGroup partner = projectHelper.createPartner(project.owner, project)
			Person newPerson = personHelper.createPerson(adminPerson, partner)
			UserLogin user = personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.USER}"])
		then: 'the partnerStaff should NOT have access to any projects'
			!personService.hasAccessToProject(newPerson, project)
		when: 'the partner is added to the default project'
			partyRelationshipService.assignPartnerToProject(partner, project)
		then: 'the partnerStaff should still NOT have access to any projects'
			!personService.hasAccessToProject(newPerson, project)

		when: 'the partnerStaff is added to the default project'
			personService.addToProject(adminUser, project.id.toString(), newPerson.id.toString())
		then: 'the partnerStaff should have access to the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the partnerStaff should be assigned to the project'
			personService.isAssignedToProject(project, newPerson)

		when: 'an unrelatedProject is created where the owner is the same as the default project.owner'
			Project unrelatedProject = projectHelper.createProject(project.owner)
		then: 'the partnerStaff should NOT have access to the unrelatedProject'
			!personService.hasAccessToProject(newPerson, unrelatedProject)

		when: 'a partnerProject is created where the owner is the the default project.owner and the partner company is a partner'
			Project partnerProject = projectHelper.createProject(project.owner)
			partyRelationshipService.assignPartnerToProject(partner, partnerProject)
		then: 'the partnerStaff should NOT have access to the partnerProject'
			!personService.hasAccessToProject(newPerson, partnerProject)
		when: 'the partnerStaff is given the ADMIN security role'
			securityService.assignRoleCode(newPerson, "${SecurityRole.ADMIN}")
		then: 'the partnerStaff should still NOT have access to the unrelatedProject'
			!personService.hasAccessToProject(newPerson, unrelatedProject)
		//and: 'the partnerStaff should now have access to the partnerProject'
		//	personService.hasAccessToProject(newPerson, partnerProject)
	}

	def '05. Test finding persons by their name using a string that must be parsed'() {
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

	def '06. Test finding persons by their name using a mapped name'() {
		// Know person for the project
		when:
			personHelper.createPerson(adminPerson, project.client, null, [lastName:'Banks', firstName:'Robin', middleName: ''])
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
		//	results = personService.findPerson('Robin Banks', project)
		//then:
		//	results.isAmbiguous
		//	results.person == null
	}

	def '07. Test project team assignment when creating person'() {
		when:
			Person person = personHelper.createPerson(adminPerson, project.client, null,
				[lastName:'Buster', firstName:'Brock'],
				['SYS_ADMIN', 'DB_ADMIN'], ['editor'])

			personService.addToTeam(person, 'SYS_ADMIN')
			personService.addToTeam(person, 'DB_ADMIN')
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

	def '08. Test assigning a person to a move event team directly by an admin user'() {
		setup: 'create an event for the project'
			String errMsg
			// personService.addToProjectSecured(project, adminPerson)
			moveEventService.create(project, 'MoveEvent 1')

		when: 'a new person/userlogin and event are created'
			Person person = personHelper.createPerson(adminPerson, project.client, project)
			personHelper.createUserLogin(person)
			MoveEvent moveEvent = projectHelper.getFirstMoveEvent(project)
		then: 'we should have person, userlogin and moveEvent we were expecting'
			person
			person.userLogin
			moveEvent
		and: 'the move event is associated to the project'
			moveEvent.project.id == project.id

		when: '''assigning a person as a SYS_ADMIN to the first MoveEvent of the
			project before the person is assigned to the project'''
			String meId = moveEvent.id.toString()
			String personId = person.id.toString()
		then: 'no error message should be returned'
			personService.assignToProjectEvent(personId, meId, 'SYS_ADMIN', '1') == ''
		and: 'the person should be assigned to the project'
			personService.isAssignedToProjectTeam(project, person, 'SYS_ADMIN')
		and: 'the person should be assigned to the event'
			personService.isAssignedToEventTeam(moveEvent, person, 'SYS_ADMIN')
		and: 'the person DB_ADMIN role was not assigned to the project'
			! personService.isAssignedToProjectTeam(project, person, 'DB_ADMIN')
		and: 'the person DB_ADMIN role was not assigned to the event'
			! personService.isAssignedToEventTeam(moveEvent, person, 'DB_ADMIN')
	}

	/*
		TODO : JPM 3/2016 : personService.hasAccessToPerson() does not work correctly
	def '08. Test if a person has access to another person based on who they are'() {
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

	def '10. Test findByCompanyAndEmail method'() {
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

	def '11. Test findByCompanyAndName method'() {
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

	def "12. Delete Person Tests"() {
		when: 'creating a new person with a user account'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			Long newPID = newPerson.id
			UserLogin user = personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.ADMIN}"])
			Long userId = user.id
			Application app = assetHelper.createApplication(newPerson, project)
			app.sme = newPerson
			app.save(flush:true)

		then: 'make sure they are associated with the client company'
			personService.isAssociatedTo(newPerson, project.client)
		and: 'have valid ids'
			userId
			newPID
		and: 'there should be PartyRelationship references'
			PartyRelationship.findAllWhere(partyIdTo: newPerson)
		and: 'is associated as a SME on an application'
			app.sme.id == newPID

		when: 'the person is deleted and all associations with the individual are removed'
			Map result = personService.deletePerson(adminPerson, newPerson, true, true)
			app.refresh()
		then: 'deleted flag should be true'
			result['deleted'] == true
		and: 'the person and user references should be deleted'
			// Shouldn't be able to lookup the person
			! Person.get(newPID)
			! UserLogin.get(userId)
		and: 'there should be no PartyRelationship references'
			! PartyRelationship.findAllWhere(partyIdTo: newPerson)
		and: 'is associated as a SME on an application has been cleared'
			! app.sme

	}

	def "13. Merge Person with no user login accounts"() {
		// Note that there maybe some overlap of this test and GormUtilIntegrationSpec.mergeDomainReferences
		when: 'Setup the initial data for the test cases'
			Map results = [:]
			String extraTeam = 'DB_ADMIN'

			Person fromPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
			personService.addToProjectTeam(project.id.toString(), fromPerson.id.toString(), extraTeam, results)

			Person toPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
			Application app = assetHelper.createApplication(fromPerson, project)

			// Closure used a few times for testing
			def findTestPersons = { return GormUtil.findAllByProperties(Person, personMap) }
			List persons = findTestPersons()
		then: 'we should expect the following'
			// We should have two people
			persons.size() == 2

			// The from person should have a number of PartyRelationship records
			GormUtil.findAllByProperties(PartyRelationship, [partyIdFrom:fromPerson, partyIdTo:fromPerson], GormUtil.Operator.OR).size() > 0

			app.id > 0
			app.appOwner.id == fromPerson.id

			// The From Person should have the extraTeam reference
			personService.getPersonTeamCodes(fromPerson).contains(extraTeam)

		when: 'When the persons are merged together the various domain references should be updated appropriately'
			personService.mergePerson(adminUser, fromPerson, toPerson)

			persons = findTestPersons()

			// Reload the application so we can check any changed reference
			app = Application.get(app.id)
			String toRefId = toPerson.id.toString()
		then:
			persons.size() == 1
			persons[0].id == toPerson.id

			// All PartyRelationship records for the From person should now be gone
			GormUtil.findAllByProperties(PartyRelationship, [partyIdFrom:fromPerson, partyIdTo:fromPerson], GormUtil.Operator.OR).size() == 0

			// The To person should now have the extraTeam reference
			personService.getPersonTeamCodes(toPerson).contains(extraTeam)

			// Validate that direct references have been properly migrated over to the To person
			// Check that all of the application references were switched
			app != null
			app.appOwner.id == toPerson.id
			app.sme.id == toPerson.id
			app.sme2.id == toPerson.id
			app.modifiedBy.id == toPerson.id
			app.shutdownBy == toRefId
			app.startupBy == toRefId
			app.testingBy == toRefId

	}

	def "14. Merge Person with login account only associated with From person"() {
		// Note that there maybe some overlap of this test and GormUtilIntegrationSpec.mergeDomainReferences
		when: 'Setup the initial data for the test cases'
			Map results = [:]

			// Setup the From Person with a UserLogin
			Person fromPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
			UserLogin fromUser = personHelper.createUserLogin(fromPerson)
			Long fromUID = fromUser.id

			// Setup the To Person with NO UserLogin
			Person toPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)

			// Closure used a few times for testing
			def findTestPersons = { return GormUtil.findAllByProperties(Person, personMap) }
			List persons = findTestPersons()

		then: 'we should find two people'
			// We should have two people
			persons.size() == 2
		and: 'fromPerson has a userLogin'
			fromPerson.userLogin
		and: 'the toPerson does not have a userLogin'
			! toPerson.userLogin

		when: 'the persons are merged together'
			// Perform the merge of the accounts
			securityService.mergePersonsUserLogin(adminUser, fromPerson, toPerson)
			toPerson = Person.get(toPerson.id)
			UserLogin toUserLogin = toPerson.userLogin
		then: 'the From user should be switched to the To person'
			toUserLogin.id == fromUID
/*
		when: 'the persons are merged together'
			// Perform the merge of the accounts
			personService.mergePerson(adminUser, fromPerson, toPerson)
			toPerson = Person.get(toPerson.id)
			UserLogin toUserLogin = toPerson.userLogin
		then: 'the From user should be switched to the To person'
			toUserLogin.id == fromUID
*/
	}

	def "15. Merge Person with login accounts associated with both persons"() {
		// Note that there maybe some overlap of this test and GormUtilIntegrationSpec.mergeDomainReferences

		when: 'getting the Person userLogin property'
			def domainProp = GormUtil.getDomainProperty(Person, 'userLogin')
		then: 'it should have an association and the owner of relationship'
			domainProp.isAssociation()
			domainProp.isOwningSide()
			domainProp.isOneToOne()
			domainProp.isHasOne()

		when: 'getting the UserLogin person property'
			domainProp = GormUtil.getDomainProperty(UserLogin, 'person')
		then: 'it should have an Association relationship'
			domainProp.isAssociation()
			! domainProp.isOwningSide()

		when: 'Setup the initial data for the test cases'
			Map results = [:]

			Date earlyDate, laterDate
			use(TimeCategory) {
				earlyDate = new Date() + 30.days
				laterDate = earlyDate + 18.days
			}

			// Setup the From Person with a UserLogin
			Person fromPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
			Long fromPID = fromPerson.id
			UserLogin fromUser = personHelper.createUserLogin(fromPerson, [expiryDate: earlyDate, lastLogin: new Date()])
			Long fromUID = fromUser.id

			// Setup the To Person with a UserLogin that has later expiry date
			Person toPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
			UserLogin toUser = personHelper.createUserLogin(toPerson, [expiryDate: laterDate])
			Long toUID = toUser.id
		then:
			fromUser != null
			toUser != null

		when: 'the persons are merged together'
			// Perform the merge of the accounts
			personService.mergePerson(adminUser, fromPerson, toPerson)
			toPerson.refresh()
			toUser = toPerson.getUserLogin()
		then: 'the From UserLogin should be switched to the To Person'
			toUser.id != toUID
			toUser.id == fromUID
		and:'the expiry date from the original To account should have been copied over'
			// need to format the dates to compare because of Java vs Sql dates
			toUser.expiryDate.format("d MMMM, yyyy") == laterDate.format("d MMMM, yyyy")

		when: 'searching for From Person'
			List fromPersons = GormUtil.findAllByProperties(Person, ['id':fromPID])
		then: 'it should no longer exist'
			fromPersons.size() == 0

		when: 'searching for From UserLogin'
			List fromUsers = GormUtil.findAllByProperties(UserLogin, ['person.id':fromPID])
		then: 'it should no longer exist'
			fromUsers.size() == 0

	}

	def "16. Bulk Delete Persons Test"() {
		when: 'creating a new person with a user account'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			Long newPID = newPerson.id
			Application app = assetHelper.createApplication(newPerson, project)
			app.save(flush: true)

		then: 'have valid ids'
			newPID != null

		when: 'the person is deleted and all associations with the individual are removed'
			Map result = personService.bulkDelete(adminPerson, [newPID], true)
			app.refresh()
		then: 'deleted count should greater than zero'
			result['deleted'] == 1
	}






	def "17. Test for TM-6141 - Add an admin new person to a project as client staff, log in with that user, remove and re-attach itself to the project"() {

		setup: 'create a person and user as staff with ADMIN role for the project client so that'
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			UserLogin newUser = personHelper.createUserLoginWithRoles(newPerson, ["${SecurityRole.ADMIN}"])

		when: 'the newUser logs into the system'
			securityService.assumeUserIdentity(newUser.username, false)

		then: 'the newUser is correctly logged into the system and has an Admin permission'
			assert securityService.isLoggedIn()
			securityService.hasPermission(newUser, Permission.AdminUtilitiesAccess)
		and: 'the newPerson should be able to access the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the newPerson is NOT assigned to the project'
			!personService.isAssignedToProject(project, newPerson)

		when: 'adding the person to the project'
		personService.addToProject(adminUser, project.id.toString(), newPerson.id.toString())
		then: 'the person should be able to access the project'
		personService.hasAccessToProject(newPerson, project)
		and: 'the person is assigned to the project'
		personService.isAssignedToProject(project, newPerson)

		when: 'removing the newUser from the project'
			personService.removeFromProject(project.id.toString(), newPerson.id.toString())
		then: 'the person should be able to access the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the person is NOT assigned to the project'
			!personService.isAssignedToProject(project, newPerson)

		when: 'the person re-attach himself to the project'
			personService.addToProject(adminUser, project.id.toString(), newPerson.id.toString())
		then: 'the person should be able to access the project'
			personService.hasAccessToProject(newPerson, project)
		and: 'the person is assigned to the project'
			personService.isAssignedToProject(project, newPerson)
	}

	def "18. Merge Person using ProcessMergePersonRequest"() {
		when: 'Setup the initial data for the test'
		MockHttpServletRequest mockRequest = new MockHttpServletRequest()

		// Setup the From Person with a UserLogin
		Person fromPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
		fromPerson.firstName = "John"
		fromPerson.middleName = "Jeffrey"
		fromPerson.lastName = "Doe"
		String[] fromIDs = [Long.toString(fromPerson.id)]

		// Setup the To Person
		Person toPerson = personHelper.createPerson(adminPerson, project.client, project, personMap)
		UserLogin toUser = personHelper.createUserLogin(toPerson)
		toPerson.firstName = "Jane"
		toPerson.middleName = "Mary"
		toPerson.lastName = "Doe"
		mockRequest.addParameter("fromId[]", fromIDs)
		mockRequest.addParameter("toId", Long.toString(toPerson.id))

		def params = new GrailsParameterMap(mockRequest)
		// Perform the merge of the accounts
		def test = personService.processMergePersonRequest(toUser, new PersonCO(), params)
		then: 'the From UserLogin should be switched to the To Person'
        test == "John Jeffrey Doe was merged to Jane Mary Doe"
	}

	// Used to convert a person object into a map used by the PersonService
	private Map personToNameMap(Person p) {
		[first: p.firstName, middle: p.middleName, last: p.lastName]
	}

	private Map emptyNameMap() {
		[first: '', middle: '', last: '']
	}
}
