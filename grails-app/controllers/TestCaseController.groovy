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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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


	/**
	 * Simply a test page for the runbook optimization
	 */
	def testRBO = {

		def meId = params.containsKey('eventId') ? params.eventId : 280
		if (! meId.isNumber()) {
			render "Invalid event id was provided"
			return
		}

		def me = MoveEvent.get(meId)
		if (! me) {
			render "Unable to find event $meId"
			return
		}

		StringBuilder results = new StringBuilder()

		def startTime = 0
		def tasks, deps, dfsMap, durMap, graphs,estFinish

		try {
			tasks = runbookService.getEventTasks(me)
			deps = runbookService.getTaskDependencies(tasks)
			def tmp = runbookService.createTempObject(tasks, deps)

			dfsMap = runbookService.processDFS( tasks, deps, tmp )
			durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks, tmp) 
			graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)
			estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs, tmp)

			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

			results.append("Found ${tasks.size()} tasks and ${deps.size()} dependencies<br/>")
			results.append("Start Vertices: " + (dfsMap.starts.size() > 0 ? dfsMap.starts : 'none') + '<br/>')
			results.append("Sink Vertices: " + (dfsMap.sinks.size() > 0 ? dfsMap.sinks : 'none') + '<br/>')
			results.append("Cyclical Maps: ")
			
			def cyclicals = [:]
			dfsMap.cyclicals.each {
				cyclicals.put(it.key, it.value.stack)
			}
			
			// results.append(dfsMap.cyclicals)
			if (dfsMap.cyclicals?.size()) {
				results.append('<ol>')
				dfsMap.cyclicals.each { c ->
					def task = c.value.loopback
					results.append("<li> Circular Reference Stack: <ul>")
					// def marker = ''
					c.value.stack.each { cycTaskId ->
						// results.append('<li>')
						// def looper = cycTaskId == root
						//def cycTaskNum = tasks.find { it.id == cycTaskId }?.taskNumber
						task = tasks.find { it.id == cycTaskId }
						results.append("<li>$task.taskNumber $task.comment")
						// results.append("<li>${looper?'<b>':''}$task.taskNumber $task.comment ${looper?'</b>':''}")
						// if (!marker) marker = ' &gt; '
					}
					results.append(" >> $c.value.loopback.taskNumber $c.value.loopback.comment</li>")
					results.append('</ul>')
				}
				results.append('</ol>')
			} else {
				results.append('none')
			}
			results.append('<br/>')
			results.append("Pass 1 Elapsed Time: ${dfsMap.elapsed}<br/>")
			results.append("Pass 2 Elapsed Time: ${durMap.elapsed}<br/>")

			results.append("<b>Estimated Runbook Duration: ${estFinish} for Move Event: $me</b><br/>")

	/*
			results.append("<h1>Edges data</h1><table><tr><th>Id</th><th>Predecessor Task</th><th>Successor Task</th><th>DS Task Count</th><th>Path Duration</th></tr>")
			deps.each { dep ->
				results.append("<tr><td>${dep.id}</td><td>${dep.predecessor}</td><td>${dep.successor}</td><td>${dep.downstreamTaskCount}</td><td>${dep.pathDuration}</td></tr>")
			}
			results.append('</table>')

	*/
			results.append("<h1>Tasks Details</h1><table><tr><th>Id</th><th>Task #</th><th>Action</th>" + 
				"<th>Duration</th><th>Earliest Start</th><th>Latest Start</th>" +
				"<th>Constraint Time</th><th>Act Finish</th><th>Priority</th><th>Critical Path</td>" + 
				"<th>Team</th><th>Individual</th><th>Category</th></tr>")

			tasks.each { t ->

				def person = t.assignedTo ?: '&nbsp;'
				def team = t.role ?: '&nbsp;'
				def constraintTime = '&nbsp;'
				def actFinish = '&nbsp;'

				if (t.constraintTime) {
					constraintTime = formatter.format(TimeUtil.convertInToUserTZ(t.constraintTime, tzId)) + " ${t.constraintType}"
				}
				if (t.actFinish) {
					actFinish = formatter.format(TimeUtil.convertInToUserTZ(t.actFinish, tzId))
				}

				// TODO : add in computation for time differences if both constraint time est and/or actual
	 
	 			def criticalPath = (t.duration > 0 && tmp['tasks'][t.id].tmpEarliestStart == tmp['tasks'][t.id].tmpLatestStart ? 'Yes' : '&nbsp;')

				results.append( "<tr><td>${t.id}</td><td>${t.taskNumber}</td><td>${t.comment}</td><td>${t.duration}</td><td>${tmp['tasks'][t.id].tmpEarliestStart}</td>" + 
					"<td>${tmp['tasks'][t.id].tmpLatestStart}</td><td>$constraintTime</td><td>$actFinish</td><td>${t.priority}</td>" + 
					"<td>$criticalPath</td><td>${team}</td><td>$person</td><td>${t.category}</tr>")
			}
			results.append('</table>')
		} catch (e) {
			results.append("<h1>Unable to complete computation</h1>${e.getMessage()}")
		}
		
		render results.toString()

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