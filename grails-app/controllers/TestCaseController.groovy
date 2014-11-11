/*
 * This controller just allows us to do some testing of things until we can move them into an integrated testcase
 */

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files

import com.tdssrc.grails.DateUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.GormUtil

import org.codehaus.groovy.grails.commons.GrailsClassUtils
import java.text.SimpleDateFormat

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.ConnectorActiveDirectory

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class TestCaseController {

	// IoC
	def partyRelationshipService
	def personService
	def runbookService
	def taskService	
	def securityService
	def userService

	// def messageSource
	
	def testPerms = {
		def user = securityService.getUserLogin()

		def hasPermGood = securityService.hasPermission(user, 'ShowCartTracker')
		def hasPermBad = securityService.hasPermission(user, 'xyzzy')

		render "hasPermGood=$hasPermGood, hasPermBad=$hasPermBad"

	}

	def checkADConfig = {
		def out = "<h1>Testing AD Configuration</h1><pre>" 
		out += securityService.getActiveDirectorySettings().toString() + "</pre>"

		render out.toString()
	}

	def findPerson = {
		def nameMap = [first:'John', last:'Martin']

		def client = PartyGroup.read(18)
		def fullname = personService.findByClientAndName(client, nameMap) 
		def firstname = personService.findByClientAndName(client, [first:'John'])
		def email = personService.findByClientAndEmail(client, 'jmartin@transitionaldata.com')
		def login = UserLogin.findAllByPersonInList(firstname)

		render "Found by:<br>fullname: $fullname<br>firstname: $firstname<br>email: $email<br>logins: $login".toString()
	}

	// Used to test the JQuery one method
	def testOne = {
		render(view: '../dashboard/testOne', model:[a:1])
	}

	def finduser = {
		def userLogin = UserLogin.findByUsername( 'jmtest@transitionaldata.com' )
		render (userLogin ? userLogin.toString() : 'Not found')
	}
	
	def provisioning = {
		def conf = securityService.getActiveDirectoryConfig()
		conf.defaultProject = 2468

		def userInfo=[:]
		userInfo.company = 2467
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

	def adIntegration = {
		def ctx = ApplicationContextHolder.getApplicationContext()
		def conf = ApplicationContextHolder.getConfig()
		def adConf = conf?.tdstm?.security?.ad

		//render ctx.securityService.class
		//return

		def username='jmtest'
		def pswd='tryT0Gu3ss1t'
		def userInfo = ConnectorActiveDirectory.getUserInfo(username, pswd, adConf)

		render userInfo.toString()

	}


	def testGormUtilGetDPWC = {
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

	def testStaffingRoles = {
		def list = partyRelationshipService.getStaffingRoles(false)
		def s = '<table>'
		list.each {
			s += "<tr><td>${it.id}</td><td>${it.description}</td></tr>"
		}
		s += '</table>'
		render s
	}

	def testPersonServiceFindPerson = {
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

	def testFindPerson = {

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
			s.append("$msg</td></tr>")
		}
		s.append("</table>")
		
		render s
	}

	def indirectTest = {

		def p = new Person(firstName:'Robin', lastName:'Banks', id:123)

		def property = 'testingBy'
		def asset = new Application(id:1, name:'test app', shutdownBy:'Martin, John', testingBy:'#sme', sme:p)
		def asset2 = new AssetEntity()


		//def type = GrailsClassUtils.getPropertyType(Application, property)?.getName()
		//def type = GrailsClassUtils.getPropertyType(asset.getClass(), property)?.getName()

		//render "type=$type, ${asset.testingBy}, asset2=${asset2.getClass().getName()}"

		def obj = taskService.getIndirectPropertyRef(asset, property)

		render obj.toString()

	}

	/**
	 * Testing the GORM error handler to see if we can get human readable messages
	 */
	def gormErrorsTest = {

		def app = new AssetEntity(assetName:'Test app', validation:'Foo', )
		if (app.validate())
			render "No errors with validation"
		else {
			def s = "We got errors<br/>${GormUtil.errorsAsUL(app)}"
			render s
		}

	}

}