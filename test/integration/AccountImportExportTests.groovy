import com.tdssrc.grails.TimeUtil
import java.text.DateFormat
import java.sql.Timestamp
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Integrated test cases for the AccountImportExport class
 */
@TestFor(AdminController)
class AccountImportExportTests  extends Specification {
	
	// IOC
	def accountImportExportService 
	def securityService
	def AIES

	void setup() {
		AIES = accountImportExportService
	}

	def "Validate the checkMinusListForInvalidCodes method"() {
		when: 
			List valid = ['A','B','C','D','E','F']
		then:
			! AIES.checkMinusListForInvalidCodes(valid, ['A', '-B', '-C'])
			['X','Z'] == AIES.checkMinusListForInvalidCodes(valid, ['B','-X','C','Z','D'])
			['X'] == AIES.checkMinusListForInvalidCodes(valid, ['-X'])	
	}

	def "Test that isMinus is do it's job"(){
		expect:
			AIES.isMinus('-FU')
			! AIES.isMinus('BAR')
	}

	def "Checking to see if stripTheMinus really does strip"() {
		expect:
			'A' == AIES.stripTheMinus('-A')
			'B' == AIES.stripTheMinus('-B')
			'ABC' == AIES.stripTheMinus('-ABC')
			'ABC' == AIES.stripTheMinus('ABC')
			'' == AIES.stripTheMinus('-')
			'' == AIES.stripTheMinus('')
			null == AIES.stripTheMinus(null)
			'ManySpaces' == AIES.stripTheMinus('-   ManySpaces')

	}

	def "Running the determineTeamChanges method through it's paces"() {

		given: 'the perfect set of conditions'
			// Setup
			List allTeams = ['A','B','C','D','E']
			List currPersonTeams = ['A','E']
			List chgPersonTeams = ['A', 'C', '-E', '-D']
			List currProjectTeams = ['D', 'E']
			List chgProjectTeams = ['B']

			// Expected results
			List resultPerson = ['A', 'B', 'C']
			List resultProject = ['B']
			List addToPerson = ['B', 'C'], addToProject = ['B']
			List deleteFromPerson = ['E'], deleteFromProject = ['E', 'D']
		when:
			Map map = AIES.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'the results should be as expected without any errors'
			! map.errors
			map.hasChanges
			resultPerson == map.resultPerson
			resultProject == map.resultProject
			addToPerson == map.addToPerson
			deleteFromPerson == map.deleteFromPerson

		when: 'a bad code for the PERSON team CHANGES'
			chgPersonTeams = ['A', '-Z']
			map = AIES.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'it should report an error with a bad code and all the other values are empty'
			map.error.contains('Invalid team code')
			map.error.contains('Z')
			! map.hasChanges
			! map.resultPerson
			! map.resultProject
			! map.addToPerson
			! map.deleteFromPerson

		when: 'a bad code for the PROJECT teams CHANGES'
			chgPersonTeams = ['A']
			chgProjectTeams = ['A', '-Y']
			map = AIES.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'it should report an error with a bad code'
			map.error.contains('Invalid team code')
			map.error.contains('Y')

		when: 'a bad code for the CURRENT PERSON teams which is highly unlikely (A.K.A. orphaned record)'
			chgPersonTeams = ['A', 'B']
			chgProjectTeams = ['A', 'B']
			currPersonTeams = ['A','X']		
			map = AIES.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'it should report an error with a bad code'
			map.error.contains('System issue with invalid team role')
			map.error.contains('X')

		when: 'a bad code for the CURRENT PROJECT teams which is highly unlikely (A.K.A. orphaned record)'
			chgPersonTeams = ['A', 'B']
			chgProjectTeams = ['A', 'B']
			currPersonTeams = ['A','B']
			currProjectTeams = ['A','B','Q']
			map = AIES.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'it should report an error with a bad code'
			map.error.contains('System issue with invalid team role')
			map.error.contains('Q')
	}

	def "1. Validate the various capabilities of the determineSecurityRoleChanges method"() {

		given: 'legitimate conditions we should not get any errors'
			List allRoles = securityService.getAllRoleCodes()
			List currentRoles = ['ADMIN', 'SUPERVISOR', 'USER']
			List authorizedRoleCodes = ['USER', 'SUPERVISOR', 'EDITOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
			List changes = ['EDITOR', '-ADMIN', '-CLIENT_MGR']
		when: 'calling determineSecurityRoleChanges'
			Map map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 'it should return with a valid results map'
			! map.error
			map.hasChanges
			map.add == ['EDITOR']
			map.delete == ['ADMIN']
			map.results == ['EDITOR', 'SUPERVISOR', 'USER']


		when: 'the user does not have the ADMIN role and attempts to remove it for someone else the role a security \
			violation should occur'
			changes = ['EDITOR', '-ADMIN', '-CLIENT_MGR']
			authorizedRoleCodes = ['USER', 'SUPERVISOR', 'EDITOR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			map.error
			map.error.contains(' role')
			map.error.contains('ADMIN')
			! map.hasChanges


		when: 'the ADMIN role is not assigned to the user it should cause a security violation if trying to ADD it'
			changes = ['EDITOR', 'ADMIN', '-CLIENT_MGR']
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['SUPERVISOR', 'USER']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			map.error
			map.error.contains('unauthorized security role')
			map.error.contains('ADMIN')


		when: 'attempting to remove all of the user roles it should report an error'
			authorizedRoleCodes = ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN', 'ADMIN']
			currentRoles = ['EDITOR']
			changes = ['-EDITOR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			map.error
			map.error.contains('Deleting all security roles not permitted')


		when: 'calling the method without any changes should not error, should return the original results and flag no changes'
			authorizedRoleCodes = ['SUPERVISOR', 'ADMIN']
			changes = []
			currentRoles = ['EDITOR', 'SUPERVISOR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			! map.error
			! map.hasChanges
			map.results == currentRoles


		when: 'the ADMIN role not in the authorizedRoleCodes it should still be able to manage roles without affecting ADMIN'
			authorizedRoleCodes = ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN']
			currentRoles = ['SUPERVISOR', 'USER', 'ADMIN']
			changes = ['-USER', 'CLIENT_MGR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			! map.error
			map.results == ['ADMIN', 'CLIENT_MGR', 'SUPERVISOR']


		when: 'there is a bad code references in the currentRolls it should result in an error'
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['BAD','ADMIN']
			changes = ['-USER', 'CLIENT_MGR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)	
		then:
			map.error
			map.error.contains('invalid security role')
			map.error.contains('BAD')


		when: 'there is a bad code references in the changes it should result in an error'
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['ADMIN']
			changes = ['WTF','-USER', 'CLIENT_MGR']
			map = AIES.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)	
		then:
			map.error
			map.error.contains('Invalid security code')
			map.error.contains('WTF')
	}


	def "Test shouldUpdate methods"() {
		given: 
			String PARAM_NAME = AIES.IMPORT_OPTION_PARAM_NAME
			String OPT_PERSON = AIES.IMPORT_OPTION_PERSON
			String OPT_USER = AIES.IMPORT_OPTION_USERLOGIN
			String OPT_BOTH = AIES.IMPORT_OPTION_BOTH

		expect:
			// Note this was tested as a where: but as it turns out, where is evaluated before
			// given: so it could not access the defined vars.
			AIES.shouldUpdatePerson([(PARAM_NAME): OPT_PERSON]) == true
			AIES.shouldUpdateUserLogin([(PARAM_NAME): OPT_PERSON]) == false
			AIES.shouldUpdatePerson([(PARAM_NAME): OPT_USER]) == false
			AIES.shouldUpdateUserLogin([(PARAM_NAME): OPT_USER]) == true
			AIES.shouldUpdatePerson([(PARAM_NAME): OPT_BOTH]) == true
			AIES.shouldUpdateUserLogin([(PARAM_NAME): OPT_BOTH]) == true
	}

	def "Test the shouldIdentifyUnplannedChanges method"() {
		given: 
			String PARAM_NAME = AIES.IMPORT_OPTION_PARAM_NAME
			String OPT_PERSON = AIES.IMPORT_OPTION_PERSON
			String OPT_USER = AIES.IMPORT_OPTION_USERLOGIN
			String OPT_BOTH = AIES.IMPORT_OPTION_BOTH

			Map domains = [
				'Person': [ (OPT_PERSON):true, (OPT_USER):false, (OPT_BOTH):true],
				'UserLogin': [ (OPT_PERSON):false, (OPT_USER):true, (OPT_BOTH):true]
			]

		expect:
			domains.each {domain, scenarios ->
				scenarios.each { opt, result ->
					AIES.shouldIdentifyUnplannedChanges([(PARAM_NAME):opt], domain) == result
				}
			}

	}

	def "Test the propertyHasError method"() {
		given:
			String prop = 'test'
			String errorProp = prop+AIES.ERROR_SUFFIX
		when:
			Map account = [(prop): 'currentValue' ]
		then:
			AIES.propertyHasError(account, prop) == false

		when:
			AIES.setErrorValue(account, prop, 'hasError')
		then:
			AIES.propertyHasError(account, prop) == true
			account[errorProp] == 'hasError'
	}

	def "Test the transformation closures"() {
		setup:
			Map options = [:]
		expect:
			AIES.xfrmToYN(true, options) == 'Y'
			AIES.xfrmToYN(false, options) == 'N'
			AIES.xfrmToYN(null, options) == ''
			AIES.xfrmToYN('notBoolean', options) == 'notBoolean'
			AIES.xfrmToYN(1, options) == '1'
			AIES.xfrmToYN(0, options) == '0'

			AIES.xfrmListToString(['a','b'], options) == 'a, b'
			AIES.xfrmListToString(['a'], options) == 'a'

			AIES.xfrmListToPipedString(['c','d'], options) == 'c|d'
			AIES.xfrmListToPipedString(['e'], options) == 'e'
			
		when:	
			AIES.xfrmListToString('not a list for comma separated list', options)
		then: 
			thrown LogicException

		when:	
			AIES.xfrmListToPipedString('not a list for pipe separated list', options)
		then: 
			thrown LogicException

	}

	def 'Test the getUserPreferences'() {
		setup:
			String tzId='GMT'

			// Mock the bullshit format of session attributes ...
			def mockSession = new GrailsHttpSession(request)
			mockSession.setAttribute('CURR_DT_FORMAT', [ 'CURR_DT_FORMAT': TimeUtil.MIDDLE_ENDIAN ] )
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': tzId ] )

			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60*60*24*1000
			Timestamp timestamp = new Timestamp(oneDay)
			Map userPref = AIES.getUserPreferences(mockSession)

		expect:
			userPref.userTzId == tzId
			userPref.userDateFormat == TimeUtil.MIDDLE_ENDIAN
			TimeUtil.formatDate(timestamp, userPref.dateFormatter) == '01/02/1970'
			TimeUtil.formatDateTimeWithTZ(userPref.userTzId, timestamp, userPref.dateTimeFormatter) == '01/02/1970 12:00:00 AM'

	}
}