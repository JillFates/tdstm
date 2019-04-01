import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.AccountImportExportService
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.ApiCatalogService
import net.transitionmanager.service.AwsService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

import static net.transitionmanager.security.Permissions.Roles.ROLE_ADMIN
import static net.transitionmanager.security.Permissions.Roles.ROLE_CLIENT_ADMIN
import static net.transitionmanager.security.Permissions.Roles.ROLE_CLIENT_MGR
import static net.transitionmanager.security.Permissions.Roles.ROLE_SUPERVISOR
import static net.transitionmanager.security.Permissions.Roles.ROLE_USER

/*
 * This controller just allows us to do some testing of things until we can move them into an integrated testcase
 */
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class TestCaseController implements ControllerMethods {

	AccountImportExportService accountImportExportService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	TaskService taskService
	UserPreferenceService userPreferenceService
	UserService userService
	ApiActionService apiActionService
	ApiCatalogService apiCatalogService

	AwsService awsService

	def ex() {
		throw new EmptyResultException('could not find what you are looking for')
	}

	def agents() {
		renderAsJson(apiCatalogService.listCatalogNames())
	}

	@HasPermission('ViewAdminTools')
	def sendSedaMessage() {
		String queue = 'seda:alert.queue'
		sendMessage(queue, 'We need a pizza ASAP!!!')
		render "Message sent to $queue"
	}

	@HasPermission('ViewAdminTools')
	def sendSqsMessage(String option) {
		String queue = awsService.responseQueueName

		Map message
		switch (option) {
			case 'missingMethod':
				message = [method:'noSuchMethod', foo:'bar', meaningOfLife:42, when:new Date()]
				break

			case 'noMethod':
				message = [foo:'bar', meaningOfLife:42, when:new Date()]
				break

			default:
				message = [method:'updateTransportStatus', serverName:'server001', taskId:1242, when:new Date()]
				break
		}
		awsService.sendSqsMessage(queue, message)
		render "Message was sent"
	}

	@HasPermission('ViewAdminTools')
	def sendSnsMessage() {
		String queue = 'RiverMeadow_lambda_getTransportStatus'
		Map message = [serverName:'server001', taskId:123, date:new Date()]
		awsService.sendSnsMessage(queue, message)
		render "Message was sent to $queue"
	}

	@HasPermission('ViewAdminTools')
	def remoteAddr() {
		render "Your address is ${HtmlUtil.getRemoteIp()}"
	}

	@HasPermission('ViewAdminTools')
	def shouldUpdatePerson() {
		Map opt = [(accountImportExportService.IMPORT_OPTION_PARAM_NAME): accountImportExportService.IMPORT_OPTION_PERSON ]
		render (accountImportExportService.shouldUpdatePerson(opt) ? 'shouldUpdatePerson=TRUE' : 'shouldUpdatePerson=FALSE FAILED!!!!')
	}

	@HasPermission('ViewAdminTools')
	def elapsed() {
		StringBuilder sb = new StringBuilder("<h1>Testing the Elapsed Method</h1>")
		List now = [new Date()]

		sb.append("elapsed now=$now <br>")

		sleep(3000)
		sb.append("elapsed time was ${TimeUtil.elapsed(now)} now=$now <br>")
		sleep(1000)
		sb.append("elapsed time was ${TimeUtil.elapsed(now)} now=$now <br>")

		render sb.toString()
	}

	@HasPermission('ViewAdminTools')
	def tz() {
		String tz = userPreferenceService.timeZone
		String dateFormat = userPreferenceService.dateFormat
		String now = TimeUtil.formatDateTime(new Date())
		render "session isa ${session.getClass().name}, TZ=$tz<br>dateFormat=$dateFormat<br>now=$now".toString()
	}

	@HasPermission('ViewAdminTools')
	def securityRoleChanges() {
		List<String> allRoles = securityService.getAllRoleCodes()
		// List currentRoles = [ADMIN.name(), SUPERVISOR.name(), USER.name()]
		List<String> currentRoles = ['WHAT', ROLE_USER.name(), ROLE_ADMIN.name()]
		List<String> authorizedRoles = [ROLE_SUPERVISOR.name(), ROLE_CLIENT_ADMIN.name()]
		List<String> changes = [ROLE_CLIENT_MGR.name()]

		Map map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoles)
		String out = "Results were:<br> <pre>current: $currentRoles\nchanges:$changes\nauthorized:$authorizedRoles\n$map</pre>"
		render out
	}

	@HasPermission('ViewAdminTools')
	def teamCodeChanges() {
		List allTeams = ['A','B','C','D','E']
		List currPersonTeams = ['A','E']
		List chgPersonTeams = ['A', '-E', '-D']
		List currProjectTeams = ['D', 'E']
		List chgProjectTeams = ['B']

		List resultPerson = ['A', 'B']
		List resultProject = ['B']
		List addToPerson = ['B'], addToProject = ['B']
		List deleteFromPerson = ['E'], deleteFromProject = ['E', 'D']

		Map map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		StringBuilder out = new StringBuilder("Results were:<br> <pre>")
		out.append("\n\t        allTeams: $allTeams")
		out.append("\n\t currPersonTeams: $currPersonTeams")
		out.append("\n\t  chgPersonTeams: $chgPersonTeams")
		out.append("\n\tcurrProjectTeams: $currProjectTeams")
		out.append("\n\t chgProjectTeams: $chgProjectTeams")
		out.append("\n\t         results: $map")
		out.append("</pre>")

		out.append("<br>Testing for Invalid codes in chgPersonTeams:")
		map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, ['Z'], currProjectTeams, chgProjectTeams)
		out.append("<br>\tResults: <pre>$map</pre>")

		out.append("<br>Testing for Invalid codes in chgPersonTeams:")
		map = accountImportExportService.determineTeamChanges(allTeams, currPersonTeams, chgPersonTeams, currProjectTeams, ['Z'])
		out.append("<br>\tResults: <pre>$map</pre>")

		render out.toString()
	}

	//determineTeamChanges(List allTeams, List currPersonTeams, List chgPersonTeams, List currProjectTeams, List chgProjectTeams)

	@HasPermission('ViewAdminTools')
	def checkMinusList() {
		List valid   = ['A','B','C','D','E']
		List toCheck = ['A','-B','Q','-Z']
		List results = accountImportExportService.checkMinusListForInvalidCodes(valid, toCheck)
		String out="Checking of <pre>valid: $valid\ntoCheck: $toCheck\nresults: $results"
		render out
	}

	@HasPermission('ViewAdminTools')
	def testPersonGetAssignedProjects() {
		List projects = personService.getAssignedProjects(securityService.userLoginPerson)
		render 'Assigned to projects ' + projects*.id
	}

	@HasPermission('ViewAdminTools')
	def testPerms() {
		boolean hasPermGood = securityService.hasPermission('ShowCartTracker')
		boolean hasPermBad = securityService.hasPermission('xyzzy')

		render "hasPermGood=$hasPermGood, hasPermBad=$hasPermBad"
	}

	@HasPermission('ViewAdminTools')
	def checkADConfig() {
		String out = "<h1>Testing AD Configuration</h1><pre>" + securityService.getActiveDirectorySettings() + "</pre>"
		render out
	}

	@HasPermission('ViewAdminTools')
	def findPerson() {
		def nameMap = [first:'John', last:'Martin']

		def company = PartyGroup.read(18)
		def fullname = personService.findByCompanyAndName(company, nameMap)
		def firstname = personService.findByCompanyAndName(company, [first:'John'])
		def email = personService.findByCompanyAndEmail(company, 'jmartin@transitionaldata.com')
		def login = UserLogin.findAllByPersonInList(firstname)

		render "Found by:<br>fullname: $fullname<br>firstname: $firstname<br>email: $email<br>logins: $login".toString()
	}

	// Used to test the JQuery one method
	@HasPermission('ViewAdminTools')
	def testOne() {
		render(view: '../dashboard/testOne', model:[a:1])
	}

	@HasPermission('ViewAdminTools')
	def finduser() {
		def userLogin = UserLogin.findByUsername( 'jmtest@transitionaldata.com' )
		render (userLogin ? userLogin.toString() : 'Not found')
	}

}
