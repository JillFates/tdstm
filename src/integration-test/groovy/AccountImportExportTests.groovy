import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.AccountImportExportService
import net.transitionmanager.exception.LogicException
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Specification

import java.sql.Timestamp

@Integration
@Rollback
class AccountImportExportTests extends Specification {

	// IOC
	AccountImportExportService accountImportExportService
	SecurityService securityService
	ProjectService projectService

	private Project project
	private Person adminPerson
	private Person person1
	private Person person2
	private Person person3
	private Person person4
	UserLogin adminUser
	UserLogin user1
	UserLogin user2
	UserLogin user3
	UserLogin user4
	private PersonTestHelper personHelper
	private ProjectTestHelper projectHelper

	def setup() {
		personHelper = new PersonTestHelper()
		projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()

		// Create a user/person with all role codes ['USER', 'SUPERVISOR', 'EDITOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
		adminPerson = personHelper.createStaff(projectService.getOwner(project))
		assert adminPerson
		List allRoleCodes = ['ROLE_USER', 'ROLE_SUPERVISOR', 'ROLE_EDITOR', 'ROLE_CLIENT_MGR', 'ROLE_CLIENT_ADMIN', 'ROLE_ADMIN']
		projectService.addTeamMember(project, adminPerson, allRoleCodes)
		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
		assert adminUser
		assert adminUser.username

		// Create a user1/person1 with role codes ['USER', 'SUPERVISOR', 'EDITOR']
		person1 = personHelper.createStaff(projectService.getOwner(project))
		assert person1
		List userSupervisorEditorRoleCodes = ['ROLE_USER', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
		projectService.addTeamMember(project, person1, userSupervisorEditorRoleCodes)
		user1 = personHelper.createUserLoginWithRoles(person1, ["${SecurityRole.ROLE_ADMIN}"])
		assert user1
		assert user1.username

		// Create a user2/person2 with role codes ['SUPERVISOR', 'CLIENT_ADMIN']
		person2 = personHelper.createStaff(projectService.getOwner(project))
		assert person2
		List supervisorClientAdminRoleCodes = ['ROLE_SUPERVISOR', 'ROLE_CLIENT_ADMIN']
		projectService.addTeamMember(project, person2, supervisorClientAdminRoleCodes)
		user2 = personHelper.createUserLoginWithRoles(person2, ["${SecurityRole.ROLE_ADMIN}"])
		assert user2
		assert user2.username

		// Create a user3/person3 with role codes ['SUPERVISOR', 'ADMIN']
		person3 = personHelper.createStaff(projectService.getOwner(project))
		assert person3
		List supervisorAdminRoleCodes = ['ROLE_SUPERVISOR', 'ROLE_ADMIN']
		projectService.addTeamMember(project, person3, supervisorAdminRoleCodes)
		user3 = personHelper.createUserLoginWithRoles(person3, ["${SecurityRole.ROLE_ADMIN}"])
		assert user3
		assert user3.username

		// Create a user4/person4 with role codes ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN']
		person4 = personHelper.createStaff(projectService.getOwner(project))
		assert person4
		List missingAdminRoleCodes = ['ROLE_USER', 'ROLE_EDITOR', 'ROLE_SUPERVISOR', 'ROLE_CLIENT_MGR', 'ROLE_CLIENT_ADMIN']
		projectService.addTeamMember(project, person4, missingAdminRoleCodes)
		user4 = personHelper.createUserLoginWithRoles(person4, ["${SecurityRole.ROLE_ADMIN}"])
		assert user4
		assert user4.username
	}

	void "01. Validate the checkMinusListForInvalidCodes method"() {
		when:
			List valid = ['A', 'B', 'C', 'D', 'E', 'F']

		then:
			!accountImportExportService.checkMinusListForInvalidCodes(valid, ['A', '-B', '-C'])
			['X', 'Z'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['B', '-X', 'C', 'Z', 'D'])
			['X'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['-X'])
	}

	void "02. Test that isMinus is do its job"() {
		expect:
			accountImportExportService.isMinus('-FU')
			!accountImportExportService.isMinus('BAR')
	}

	void "03. Checking to see if stripTheMinus really does strip"() {
		expect:
			'A' == accountImportExportService.stripTheMinus('-A')
			'B' == accountImportExportService.stripTheMinus('-B')
			'ABC' == accountImportExportService.stripTheMinus('-ABC')
			'ABC' == accountImportExportService.stripTheMinus('ABC')
			'' == accountImportExportService.stripTheMinus('-')
			'' == accountImportExportService.stripTheMinus('')
			null == accountImportExportService.stripTheMinus(null)
			'ManySpaces' == accountImportExportService.stripTheMinus('-   ManySpaces')
	}

	void "04. Running the determineTeamChanges method through it's paces"() {

		given: 'the perfect set of conditions'
			// Setup
			List allTeams = ['A', 'B', 'C', 'D', 'E']
			List currPersonTeams = ['A', 'E']
			List chgPersonTeams = ['A', 'C', '-E', '-D']
			List currProjectTeams = ['D', 'E']
			List chgProjectTeams = ['B']

			// Expected results
			List resultPerson = ['A', 'B', 'C']
			List resultProject = ['B']
			List addToPerson = ['B', 'C'], addToProject = ['B']
			List deleteFromPerson = ['E'], deleteFromProject = ['E', 'D']

		when:
			Map map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

		then: 'the results should be as expected without any errors'
			!map.errors
			map.hasChanges
			resultPerson == map.resultPerson
			resultProject == map.resultProject
			addToPerson == map.addToPerson
			deleteFromPerson == map.deleteFromPerson

		when: 'a bad code for the PERSON team CHANGES'
			chgPersonTeams = ['A', '-Z']
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

		then: 'it should report an error with a bad code and all the other values are empty'
			map.error.contains('Invalid team code')
			map.error.contains('Z')
			!map.hasChanges
			!map.resultPerson
			!map.resultProject
			!map.addToPerson
			!map.deleteFromPerson

		when: 'a bad code for the PROJECT teams CHANGES'
			chgPersonTeams = ['A']
			chgProjectTeams = ['A', '-Y']
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

		then: 'it should report an error with a bad code'
			map.error.contains('Invalid team code')
			map.error.contains('Y')

		when: 'a bad code for the CURRENT PERSON teams which is highly unlikely (A.K.A. orphaned record)'
			chgPersonTeams = ['A', 'B']
			chgProjectTeams = ['A', 'B']
			currPersonTeams = ['A', 'X']
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

		then: 'it should report an error with a bad code'
			map.error.contains('System issue with invalid team role')
			map.error.contains('X')

		when: 'a bad code for the CURRENT PROJECT teams which is highly unlikely (A.K.A. orphaned record)'
			chgPersonTeams = ['A', 'B']
			chgProjectTeams = ['A', 'B']
			currPersonTeams = ['A', 'B']
			currProjectTeams = ['A', 'B', 'Q']
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

			then: 'it should report an error with a bad code'
			map.error.contains('System issue with invalid team role')
			map.error.contains('Q')
	}

	void "05. Validate the various capabilities of the determineSecurityRoleChanges method"() {
		given: 'legitimate conditions we should not get any errors'
			// logs the admin user1 into the system
			securityService.assumeUserIdentity(adminUser.username, false) // ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
			println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
			assert securityService.isLoggedIn()

			List allRoles = securityService.getAllRoleCodes()
			List currentRoles = ['ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_USER']
			List changes = ['ROLE_EDITOR', '-ROLE_ADMIN', '-ROLE_CLIENT_MGR']
		when: 'calling determineSecurityRoleChanges'
			Map map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then: 'it should return with a valid results map'
			!map.error
			map.hasChanges
			map.add == ['ROLE_EDITOR']
			map.delete == ['ROLE_ADMIN']
			map.results == ['ROLE_EDITOR', 'ROLE_SUPERVISOR', 'ROLE_USER']
		when: 'attempting to remove all of the adminUser roles it should report an error'
			currentRoles = ['ROLE_EDITOR']
			changes = ['-ROLE_EDITOR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then:
			map.error
			map.error.contains('Deleting all security roles not permitted')

	//	when: 'the user1 does not have the ADMIN role and attempts to remove it for someone else the role a security \
	//		violation should occur'
	//	securityService.assumeUserIdentity(user1.username, false)  // ['USER', 'SUPERVISOR', 'EDITOR']
	//	changes = ['EDITOR', '-ADMIN', '-CLIENT_MGR']
	//	map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)

	//	then:
	//	map.error
	//	map.error.contains(' role')
	//	map.error.contains('ADMIN')
	//	!map.hasChanges

	//	when: 'the ADMIN role is not assigned to the user2 it should cause a security violation if trying to ADD it'
	//	securityService.assumeUserIdentity(user2.username, false)  // ['SUPERVISOR', 'CLIENT_ADMIN']
	//	changes = ['EDITOR', 'ADMIN', '-CLIENT_MGR']
	//	currentRoles = ['SUPERVISOR', 'USER']
	//	map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)

	//	then:
	//	map.error
	//	map.error.contains('sendUnauthorized security role')
	//	map.error.contains('ADMIN')

		when: 'calling the method without any changes should not error, should return the original results and flag no changes'
			securityService.logoutCurrentUser()
			securityService.assumeUserIdentity(user3.username, false) // ['SUPERVISOR', 'ADMIN']
			changes = []
			currentRoles = ['ROLE_EDITOR', 'ROLE_SUPERVISOR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then:
			!map.error
			!map.hasChanges
			map.results == currentRoles
		when: 'the ADMIN role not in the authorizedRoleCodes it should still be able to manage roles without affecting ADMIN'
			securityService.logoutCurrentUser()
			securityService.assumeUserIdentity(user4.username, false) // ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN']
			currentRoles = ['ROLE_SUPERVISOR', 'ROLE_USER', 'ROLE_ADMIN']
			changes = ['-ROLE_USER', 'ROLE_CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then:
			!map.error
			map.results == ['ROLE_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR']
		when: 'there is a bad code references in the currentRolls it should result in an error'
			securityService.assumeUserIdentity(user2.username, false)  // ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['BAD', 'ROLE_ADMIN']
			changes = ['-ROLE_USER', 'ROLE_CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then:
			map.error
			map.error.contains('invalid security role')
			map.error.contains('BAD')
		when: 'there is a bad code references in the changes it should result in an error'
			currentRoles = ['ROLE_ADMIN']
			changes = ['WTF', '-ROLE_USER', 'ROLE_CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes)
		then:
			map.error
			map.error.contains('Invalid security code')
			map.error.contains('WTF')
	}

	void "06. Test shouldUpdate methods"() {
		given:
			String PARAM_NAME = accountImportExportService.IMPORT_OPTION_PARAM_NAME
			String OPT_PERSON = accountImportExportService.IMPORT_OPTION_PERSON
			String OPT_USER = accountImportExportService.IMPORT_OPTION_USERLOGIN
			String OPT_BOTH = accountImportExportService.IMPORT_OPTION_BOTH
		expect:
			// Note this was tested as a where: but as it turns out, where is evaluated before
			// given: so it could not access the defined vars.
			accountImportExportService.shouldUpdatePerson([(PARAM_NAME): OPT_PERSON]) == true
			accountImportExportService.shouldUpdateUserLogin([(PARAM_NAME): OPT_PERSON]) == false
			accountImportExportService.shouldUpdatePerson([(PARAM_NAME): OPT_USER]) == false
			accountImportExportService.shouldUpdateUserLogin([(PARAM_NAME): OPT_USER]) == true
			accountImportExportService.shouldUpdatePerson([(PARAM_NAME): OPT_BOTH]) == true
			accountImportExportService.shouldUpdateUserLogin([(PARAM_NAME): OPT_BOTH]) == true
	}

	void "07. Test the shouldIdentifyUnplannedChanges method"() {
		given:
			String PARAM_NAME = accountImportExportService.IMPORT_OPTION_PARAM_NAME
			String OPT_PERSON = accountImportExportService.IMPORT_OPTION_PERSON
			String OPT_USER = accountImportExportService.IMPORT_OPTION_USERLOGIN
			String OPT_BOTH = accountImportExportService.IMPORT_OPTION_BOTH

			Map domains = [Person: [(OPT_PERSON): true, (OPT_USER): false, (OPT_BOTH): true],
		               UserLogin: [(OPT_PERSON): false, (OPT_USER): true, (OPT_BOTH): true]]
		expect:
			domains.each { domain, scenarios ->
				scenarios.each { opt, result ->
					accountImportExportService.shouldIdentifyUnplannedChanges([(PARAM_NAME): opt], domain) == result
				}
			}
	}

	void "08. Test the propertyHasError method"() {
		given:
			String prop = 'test'
			String errorProp = prop + accountImportExportService.ERROR_SUFFIX
		when:
			Map account = [(prop): 'currentValue']
		then:
			accountImportExportService.propertyHasError(account, prop) == false
		when:
			accountImportExportService.setErrorValue(account, prop, 'hasError')
		then:
			accountImportExportService.propertyHasError(account, prop) == true
			account[errorProp] == 'hasError'
	}

	void "09. Test the transformation closures"() {
		setup:
			Map options = [:]
		expect:
			accountImportExportService.xfrmToYN(true, options) == 'Y'
			accountImportExportService.xfrmToYN(false, options) == 'N'
			accountImportExportService.xfrmToYN(null, options) == ''
			accountImportExportService.xfrmToYN('notBoolean', options) == 'notBoolean'
			accountImportExportService.xfrmToYN(1, options) == '1'
			accountImportExportService.xfrmToYN(0, options) == '0'
			accountImportExportService.xfrmListToString(['a', 'b'], options) == 'a, b'
			accountImportExportService.xfrmListToString(['a'], options) == 'a'
			accountImportExportService.xfrmListToPipedString(['c', 'd'], options) == 'c|d'
			accountImportExportService.xfrmListToPipedString(['e'], options) == 'e'
		when:
			accountImportExportService.xfrmListToString('not a list for comma separated list', options)
		then:
			thrown LogicException
		when:
			accountImportExportService.xfrmListToPipedString('not a list for pipe separated list', options)
		then:
			thrown LogicException
	}

	def '10. Test the getUserPreferences'() {
		setup:
			String tzId = 'GMT'
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60 * 60 * 24 * 1000
			Timestamp timestamp = new Timestamp(oneDay)
			Map userPref = accountImportExportService.getUserPreferences()
		expect:
			userPref.userTzId == tzId
			userPref.userDateFormat == TimeUtil.MIDDLE_ENDIAN
			TimeUtil.formatDate(timestamp, userPref.dateFormatter) == '01/02/1970'
			TimeUtil.formatDateTimeWithTZ(userPref.userTzId, timestamp, userPref.dateTimeFormatter) == '01/02/1970 12:00:00 AM'
	}
}
