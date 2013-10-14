/*
 * This controller just allows us to do some testing of things until we can move them into an integrated testcase
 */

class TestCaseController {

	// IoC
	def partyRelationshipService
	def personService
	


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

		def tasks = runbookService.getEventTasks(me)
		def deps = runbookService.getTaskDependencies(tasks)
		def startTime = 0

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)
		def estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs)

		StringBuilder results = new StringBuilder("Found ${tasks.size()} tasks and ${deps.size()} dependencies<br/>")

		results.append("Start Vertices: " + (dfsMap.starts.size() > 0 ? dfsMap.starts : 'none') + '<br/>')
		results.append("Sink Vertices: " + (dfsMap.sinks.size() > 0 ? dfsMap.sinks : 'none') + '<br/>')
		results.append("Cyclical Maps: " + (dfsMap.cyclicals?.size() ? dfsMap.cyclicals : 'none') + '<br/>')
		results.append("Pass 1 Elapsed Time: ${dfsMap.elapsed}<br/>")
		results.append("Pass 2 Elapsed Time: ${durMap.elapsed}<br/>")

		results.append("<b>Estimated Runbook Duration: ${estFinish}</b><br/>")

		results.append("<h1>Edges data</h1><table><tr><th>Id</th><th>Predecessor Task</th><th>Successor Task</th><th>DS Task Count</th><th>Path Duration</th></tr>")
		deps.each { dep ->
			results.append("<tr><td>${dep.id}</td><td>${dep.predecessor}</td><td>${dep.successor}</td><td>${dep.downstreamTaskCount}</td><td>${dep.pathDuration}</td></tr>")
		}
		results.append('</table>')


		results.append("<h1>Tasks Details</h1><table><tr><th>Id</th><th>Task</th><th>Duration</th><th>Earliest Start</th><th>Latest Start</th><th>Critical Path</td></tr>")
		tasks.each { t ->
			results.append("<tr><td>${t.id}</td><td>${t.taskNumber} ${t.comment}</td><td>${t.duration}</td><td>${t.tmpEarliestStart}</td><td>${t.tmpLatestStart}</td><td>${t.tmpCriticalPath ? 'Yes' : 'No'}</td></tr>")
		}
		results.append('</table>')


		render results.toString()

	}


}