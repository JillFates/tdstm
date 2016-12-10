import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.common.security.ConnectorActiveDirectory
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AccountImportExportService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

import static net.transitionmanager.domain.Permissions.Roles.ADMIN
import static net.transitionmanager.domain.Permissions.Roles.CLIENT_ADMIN
import static net.transitionmanager.domain.Permissions.Roles.CLIENT_MGR
import static net.transitionmanager.domain.Permissions.Roles.SUPERVISOR
import static net.transitionmanager.domain.Permissions.Roles.USER

/*
 * This controller just allows us to do some testing of things until we can move them into an integrated testcase
 */
import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class TestCaseController implements ControllerMethods {

	AccountImportExportService accountImportExportService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	SecurityService securityService
	TaskService taskService
	UserPreferenceService userPreferenceService
	UserService userService

	def remoteAddr() {
		render "Your address is ${HtmlUtil.getRemoteIp()}"
	}

	def shouldUpdatePerson() {
		Map opt = [(accountImportExportService.IMPORT_OPTION_PARAM_NAME): accountImportExportService.IMPORT_OPTION_PERSON ]
		render (accountImportExportService.shouldUpdatePerson(opt) ? 'shouldUpdatePerson=TRUE' : 'shouldUpdatePerson=FALSE FAILED!!!!')
	}

	def cleanString() {
		def a = " a\tstring\nwith\rcharacters \u000bA\u007cB\u008fC that\r\ncan\fhave funcky\'character\"in it"
		def b = StringUtil.clean(a)
		render '[' + b + ']'
	}

	def elapsed() {
		StringBuffer sb = new StringBuffer("<h1>Testing the Elapsed Method</h1>")
		List now = [new Date()]

		sb.append("elapsed now=$now <br>")

		sleep(3000)
		sb.append("elapsed time was ${TimeUtil.elapsed(now)} now=$now <br>")
		sleep(1000)
		sb.append("elapsed time was ${TimeUtil.elapsed(now)} now=$now <br>")

		render sb.toString()
	}

	def tz() {
		String tz = userPreferenceService.timeZone
		String dateFormat = userPreferenceService.dateFormat
		String now = TimeUtil.formatDateTime(new Date())
		render "session isa ${session.getClass().name}, TZ=$tz<br>dateFormat=$dateFormat<br>now=$now".toString()
	}

	def securityRoleChanges() {
		List<String> allRoles = securityService.getAllRoleCodes()
		// List currentRoles = [ADMIN.name(), SUPERVISOR.name(), USER.name()]
		List<String> currentRoles = ['WHAT', USER.name(), ADMIN.name()]
		List<String> authorizedRoles = [SUPERVISOR.name(), CLIENT_ADMIN.name()]
		List<String> changes = [CLIENT_MGR.name()]

		Map map = accountImportExportService.determineSecurityRoleChanges(allRoles, currentRoles, changes, authorizedRoles)
		String out = "Results were:<br> <pre>current: $currentRoles\nchanges:$changes\nauthorized:$authorizedRoles\n$map</pre>"
		render out
	}

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
		StringBuffer out = new StringBuffer("Results were:<br> <pre>")
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

	def checkMinusList() {
		List valid   = ['A','B','C','D','E']
		List toCheck = ['A','-B','Q','-Z']
		List results = accountImportExportService.checkMinusListForInvalidCodes(valid, toCheck)
		String out="Checking of <pre>valid: $valid\ntoCheck: $toCheck\nresults: $results"
		render out
	}

	def testPersonGetAssignedProjects() {
		List projects = personService.getAssignedProjects(securityService.userLoginPerson)
		render 'Assigned to projects ' + projects*.id
	}

	def testPerms() {
		boolean hasPermGood = securityService.hasPermission('ShowCartTracker')
		boolean hasPermBad = securityService.hasPermission('xyzzy')

		render "hasPermGood=$hasPermGood, hasPermBad=$hasPermBad"
	}

	def checkADConfig() {
		String out = "<h1>Testing AD Configuration</h1><pre>" + securityService.getActiveDirectorySettings() + "</pre>"
		render out
	}

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
	def testOne() {
		render(view: '../dashboard/testOne', model:[a:1])
	}

	def finduser() {
		def userLogin = UserLogin.findByUsername( 'jmtest@transitionaldata.com' )
		render (userLogin ? userLogin.toString() : 'Not found')
	}

	def provisioning() {
		def conf = securityService.getActiveDirectoryConfig()
		conf.defaultProject = 2468

		def userInfo=[:]
		userInfo.companyId = 2467
		userInfo.username = 'jmtest'
		userInfo.firstName = 'John'
		userInfo.lastName = 'Martin'
		userInfo.fullName = 'John Martin'
		userInfo.email = 'jmtest@transitionaldata.com'
		userInfo.telephone = '555-111-3333'
		userInfo.mobile = '555-222-3333'
		userInfo.guid = 'kjlj23l4kj2l3jl23423lkj'
		userInfo.roles = ['editor']

		def user = userService.findOrProvisionUser(userInfo, conf)
		render (user ? user.toString() : 'Nothing')
	}

	def adIntegration() {
		def adConf = grailsApplication.config.tdstm?.security?.ad

		def username='jmtest'
		def pswd='tryT0Gu3ss1t'
		def userInfo = ConnectorActiveDirectory.getUserInfo(username, pswd, adConf)

		render userInfo.toString()

	}


	def testGormUtilGetDPWC() {
		def sb = new StringBuilder()

		def list = []

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'nullable', true)
		sb.append("<h2>MoveEvent nullable:true properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'nullable', false)
		sb.append("<h2>MoveEvent nullable:false properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'nullable')
		sb.append("<h2>MoveEvent nullable:ANY properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'blank', true)
		sb.append("<h2>MoveEvent blank:true properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'blank', false)
		sb.append("<h2>MoveEvent blank:false properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		list = GormUtil.getDomainPropertiesWithConstraint(MoveEvent, 'blank')
		sb.append("<h2>MoveEvent blank:ANY properties<h2><ul>")
		list.each { sb.append("<li>$it")}
		sb.append("</ul>")

		render sb.toString()
	}

	def testStaffingRoles() {
		def list = partyRelationshipService.getStaffingRoles(false)
		def s = '<table>'
		list.each {
			s += "<tr><td>$it.id</td><td>$it.description</td></tr>"
		}
		s += '</table>'
		render s
	}

	def testPersonServiceFindPerson() {
		def person
		def isa
		def project = Project.read(457)

		// Known person not on the project
		(person, isa) = personService.findPerson("John Martin", project)
		log.info "person = $person"
		assert person == null

		// Know person for the project
		(person, isa) = personService.findPerson("Robin Banks", project)
		log.info "person = $person"
		assert person != null
		assert 6 == person.id

		// Fake person
		(person, isa) = personService.findPerson("Robert E. Lee", project)
		log.info "person = $person"
		assert person == null

		// Know person for the project
		person = personService.findPerson([first:'Robin', middle:'', last:'Banks'], project)
		log.info "person = $person"
		assert person != null
		assert 6 == person.id

		// Known person not on the project
		person = personService.findPerson([first:'John', last:'Martin'], project)
		log.info "person = $person"
		assert person == null

		// Fake person
		person = personService.findPerson([first:'Robert', middle:'E.', last:'Lee'], project)
		assert person == null

		render "Tests were successful"

	}

	def testFindPerson() {

		// The dataset consists of [searchString, clientStaffOnly, shouldFind, ambiguous]
		def data = [
			['John Martin', true, false, false],
			['John Martin', false, true, false],
			['jmartin@transitionaldata.com', false, true, false],
			['Andy Adrian', true, true, false],
			['Andy Adrian', false, true, false],
			['Eric', true, true, true],
		]

		StringBuilder s = new StringBuilder()
		def project = Project.findByProjectCode('SuddenLink')

		s.append("<h2>Searching for Staff of project $project</h2><table><tr><th>Search String</th><th>clientStaffOnly</th><th>Success</th></tr>")
		data.each { d ->
			def map = personService.findPerson(d[0], project, null, d[1])
			s.append("<tr><td>${d[0]}</td><td>${d[1]}</td><td>")
			def msg = 'SUCCESSFUL'
			if (d[3]) {
				if ( map.person ) {
					if ( d[3] != map.isAmbiguous ) {
						msg = "FAILED - Ambiguity should be ${d[3]} - $map"
					}
				} else {
					msg = 'FAILED - Not Found'
				}
			}
			s.append(msg).append('</td></tr>')
		}
		s.append("</table>")

		render s
	}

	def indirectTest() {

		def p = new Person(firstName:'Robin', lastName:'Banks', id:123)

		def property = 'testingBy'
		def asset = new Application(id:1, name:'test app', shutdownBy:'Martin, John', testingBy:'#sme', sme:p)
		def asset2 = new AssetEntity()


		//def type = GrailsClassUtils.getPropertyType(Application, property)?.name
		//def type = GrailsClassUtils.getPropertyType(asset.getClass(), property)?.name

		//render "type=$type, $asset.testingBy, asset2=${asset2.getClass().name}"

		def obj = taskService.getIndirectPropertyRef(asset, property)

		render obj.toString()

	}

	/**
	 * Testing the GORM error handler to see if we can get human readable messages
	 */
	def gormErrorsTest() {

		def app = new AssetEntity(assetName:'Test app', validation:'Foo', )
		if (app.validate())
			render "No errors with validation"
		else {
			def s = "We got errors<br/>${GormUtil.errorsAsUL(app)}"
			render s
		}

	}

}
