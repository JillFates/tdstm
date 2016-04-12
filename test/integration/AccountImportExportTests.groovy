import spock.lang.*
// import com.tdsops.common.grails.ApplicationContextHolder

class AccountImportExportTests  extends Specification {
	
	// IOC
	def accountImportExportService 
	def securityService

	def "Validate the checkMinusListForInvalidCodes method"() {
		when: 
			List valid = ['A','B','C','D','E','F']
		then:
			! accountImportExportService.checkMinusListForInvalidCodes(valid, ['A', '-B', '-C'])
			['X','Z'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['B','-X','C','Z','D'])
			['X'] == accountImportExportService.checkMinusListForInvalidCodes(valid, ['-X'])	
	}

	def "Test that isMinus is do it's job"(){
		expect:
			accountImportExportService.isMinus('-FU')
			! accountImportExportService.isMinus('BAR')
	}

	def "Checking to see if stripTheMinus really does strip"() {
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
			Map map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'the results should be as expected without any errors'
			! map.errors
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
			! map.hasChanges
			! map.resultPerson
			! map.resultProject
			! map.addToPerson
			! map.deleteFromPerson

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
			currPersonTeams = ['A','X']		
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		then: 'it should report an error with a bad code'
			map.error.contains('System issue with invalid team role')
			map.error.contains('X')

		when: 'a bad code for the CURRENT PROJECT teams which is highly unlikely (A.K.A. orphaned record)'
			chgPersonTeams = ['A', 'B']
			chgProjectTeams = ['A', 'B']
			currPersonTeams = ['A','B']
			currProjectTeams = ['A','B','Q']
			map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
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
			Map map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
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
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			map.error
			map.error.contains(' role')
			map.error.contains('ADMIN')
			! map.hasChanges


		when: 'the ADMIN role is not assigned to the user it should cause a security violation if trying to ADD it'
			changes = ['EDITOR', 'ADMIN', '-CLIENT_MGR']
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['SUPERVISOR', 'USER']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			map.error
			map.error.contains('unauthorized security role')
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
			! map.error
			! map.hasChanges
			map.results == currentRoles


		when: 'the ADMIN role not in the authorizedRoleCodes it should still be able to manage roles without affecting ADMIN'
			authorizedRoleCodes = ['USER', 'EDITOR', 'SUPERVISOR', 'CLIENT_MGR', 'CLIENT_ADMIN']
			currentRoles = ['SUPERVISOR', 'USER', 'ADMIN']
			changes = ['-USER', 'CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)
		then: 
			! map.error
			map.results == ['ADMIN', 'CLIENT_MGR', 'SUPERVISOR']


		when: 'there is a bad code references in the currentRolls it should result in an error'
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['BAD','ADMIN']
			changes = ['-USER', 'CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)	
		then:
			map.error
			map.error.contains('invalid security role')
			map.error.contains('BAD')


		when: 'there is a bad code references in the changes it should result in an error'
			authorizedRoleCodes = ['SUPERVISOR', 'CLIENT_ADMIN']
			currentRoles = ['ADMIN']
			changes = ['WTF','-USER', 'CLIENT_MGR']
			map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoleCodes)	
		then:
			map.error
			map.error.contains('Invalid security code')
			map.error.contains('WTF')
	}

}