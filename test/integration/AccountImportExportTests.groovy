import com.tdssrc.grails.TimeUtil
import net.transitionmanager.service.AccountImportExportService
import net.transitionmanager.service.LogicException
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import spock.lang.Specification
import org.springframework.mock.web.MockHttpServletRequest
import java.sql.Timestamp

class AccountImportExportTests extends Specification {

	// IOC
	AccountImportExportService accountImportExportService
	SecurityService securityService

	void "Validate the checkMinusListForInvalidCodes method"() {
		when:
		List valid = ['A', 'B', 'C', 'D', 'E', 'F']

		then:
		!accountImportExportService.checkMinusListForInvalidCodes(valid, ['A', '-B', '-C'])
		['X', 'Z'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['B', '-X', 'C', 'Z', 'D'])
		['X'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['-X'])
	}

	void "Test that isMinus is do its job"() {
		expect:
		accountImportExportService.isMinus('-FU')
		!accountImportExportService.isMinus('BAR')
	}

	void "Checking to see if stripTheMinus really does strip"() {
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

	void "Running the determineTeamChanges method through it's paces"() {

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

	void "1. Validate the various capabilities of the determineSecurityRoleChanges method"() {

		given: 'legitimate conditions we should not get any errors'
		List allRoles = securityService.getAllRoleCodes()
		List currentRoles = ['ADMIN', 'SUPERVISOR', 'USER']
		List authorizedRoleCodes = ['USER', 'SUPERVISOR', 'EDITOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
		List changes = ['EDITOR', '-ADMIN', '-CLIENT_MGR']

		when: 'calling determineSecurityRoleChanges'
		Map map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 'it should return with a valid results map'

		!map.error
		map.hasChanges
		map.add == ['EDITOR']
		map.delete == ['ADMIN']
		map.results == ['EDITOR', 'SUPERVISOR', 'USER']

		when: 'the user does not have the ADMIN role and attempts to remove it for someone else the role a security \
			violation should occur'
		changes = ['EDITOR', '-ADMIN', '-CLIENT_MGR']
		authorizedRoleCodes = ['USER', 'SUPERVISOR', 'EDITOR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		map.error
		map.error.contains(' role')
		map.error.contains('ADMIN')
		!map.hasChanges

		when: 'the ADMIN role is not assigned to the user it should cause a security violation if trying to ADD it'
		changes = ['EDITOR', 'ADMIN', '-CLIENT_MGR']
		authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
		currentRoles = ['SUPERVISOR', 'USER']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		map.error
		map.error.contains('sendUnauthorized security role')
		map.error.contains('ADMIN')

		when: 'attempting to remove all of the user roles it should report an error'
		authorizedRoleCodes = ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
		currentRoles = ['EDITOR']
		changes = ['-EDITOR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		map.error
		map.error.contains('Deleting all security roles not permitted')

		when: 'calling the method without any changes should not error, should return the original results and flag no changes'
		authorizedRoleCodes = ['SUPERVISOR', 'ADMIN']
		changes = []
		currentRoles = ['EDITOR', 'SUPERVISOR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		!map.error
		!map.hasChanges
		map.results == currentRoles

		when: 'the ADMIN role not in the authorizedRoleCodes it should still be able to manage roles without affecting ADMIN'
		authorizedRoleCodes = ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN']
		currentRoles = ['SUPERVISOR', 'USER', 'ADMIN']
		changes = ['-USER', 'CLIENT_MGR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		!map.error
		map.results == ['ADMIN', 'CLIENT_MGR', 'SUPERVISOR']

		when: 'there is a bad code references in the currentRolls it should result in an error'
		authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
		currentRoles = ['BAD', 'ADMIN']
		changes = ['-USER', 'CLIENT_MGR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		map.error
		map.error.contains('invalid security role')
		map.error.contains('BAD')

		when: 'there is a bad code references in the changes it should result in an error'
		authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
		currentRoles = ['ADMIN']
		changes = ['WTF', '-USER', 'CLIENT_MGR']
		map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)

		then:
		map.error
		map.error.contains('Invalid security code')
		map.error.contains('WTF')
	}

	void "Test shouldUpdate methods"() {
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

	void "Test the shouldIdentifyUnplannedChanges method"() {
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

	void "Test the propertyHasError method"() {
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

	void "Test the transformation closures"() {
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

	def 'Test the getUserPreferences'() {
		setup:
		String tzId = 'GMT'

		// Mock the bullshit format of session attributes ...
		def mockSession = new GrailsHttpSession(new MockHttpServletRequest())
		mockSession.setAttribute('CURR_DT_FORMAT', [CURR_DT_FORMAT: TimeUtil.MIDDLE_ENDIAN])
		mockSession.setAttribute('CURR_TZ', [CURR_TZ: tzId])

		// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
		long oneDay = 60 * 60 * 24 * 1000
		Timestamp timestamp = new Timestamp(oneDay)
		Map userPref = accountImportExportService.getUserPreferences(mockSession)

		expect:
		userPref.userTzId == tzId
		userPref.userDateFormat == TimeUtil.MIDDLE_ENDIAN
		TimeUtil.formatDate(timestamp, userPref.dateFormatter) == '01/02/1970'
		TimeUtil.formatDateTimeWithTZ(userPref.userTzId, timestamp, userPref.dateTimeFormatter) == '01/02/1970 12:00:00 AM'
	}
}
