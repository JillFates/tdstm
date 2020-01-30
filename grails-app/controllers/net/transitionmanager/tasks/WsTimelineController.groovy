package net.transitionmanager.tasks

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeDuration
import net.transitionmanager.command.task.CalculateTimelineCommandObject
import net.transitionmanager.command.task.ExportTimelineCommand
import net.transitionmanager.command.task.ReadTimelineCommandObject
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.security.Permission
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.CPAResults
import net.transitionmanager.task.timeline.CriticalPathRoute
import net.transitionmanager.task.timeline.TaskTimeLineGraph
import net.transitionmanager.task.timeline.TaskVertex
import net.transitionmanager.task.timeline.TimelineService
import net.transitionmanager.task.timeline.TimelineSummary

import java.text.DateFormat

@Secured('isAuthenticated()')
class WsTimelineController implements ControllerMethods {

	PersonService personService
	TimelineService timelineService

	@HasPermission(Permission.TaskViewCriticalPath)
	def timeline() {

		CalculateTimelineCommandObject commandObject = populateCommandObject(CalculateTimelineCommandObject)

		MoveEvent moveEvent = fetchDomain(MoveEvent, commandObject.properties)
		userPreferenceService.setPreference(UserPreferenceEnum.MOVE_EVENT, moveEvent.id)

		Boolean recalculate = commandObject.isRecalculate()
		CPAResults cpaResults = timelineService.calculateCPA(moveEvent, commandObject.viewUnpublished)

		TaskTimeLineGraph graph = cpaResults.graph
		TimelineSummary summary = cpaResults.summary
		List<Task> tasks = cpaResults.tasks

		Date startDate
		Date endDate
		if (recalculate) {
			startDate = findEarliestStartVertex(graph.getStarts())?.earliestStartDate
			endDate = findLatestFinishVertex(graph.getSinks())?.latestFinishDate
		} else {
			List<Task> startTasks = tasks.findAll { it.taskNumber in graph.getStarts()*.taskNumber }
			List<Task> sinkTasks = tasks.findAll { it.taskNumber in graph.getSinks()*.taskNumber }
			startDate = findEarliestTaskStartTime(startTasks)
			endDate = findLatestTaskFinishTime(sinkTasks)
		}
		Integer zeroDurationAdjustment = 60

		renderAsJson(
			data: [
				sinks    : graph.sinks.collect { it.taskId },
				starts   : graph.starts.collect { it.taskId },
				startDate: startDate,
				endDate  : endDate,
				cycles   : summary.cycles.collect { it.collect { it.taskId } },
				tasks    : tasks.collect { Task task ->
					TaskVertex taskVertex = graph.getVertex(task.taskNumber)
					// When a task duration is zero, UI needs a different solution:
					// we need to add one minute on the estimatedFinish for tasks with zero duration
					Date estimatedFinish = recalculate ? taskVertex.earliestFinishDate : task.estFinish
					if (taskVertex.duration == 0 && estimatedFinish) {
						estimatedFinish = TimeUtil.adjustSeconds(estimatedFinish, zeroDurationAdjustment)
					}
					// and add one minute on the estimatedStart for all the task's successors.
					Date estimatedStart = recalculate ? taskVertex.earliestStartDate : task.estStart
					if (estimatedStart && taskVertex.predecessors.any { it.duration == 0 }) {
						estimatedStart = TimeUtil.adjustSeconds(estimatedStart, zeroDurationAdjustment)
						// If any subsequent task that has a duration of fewer than two minutes
						// will also need its estimated finish adjusted appropriately.
						if (taskVertex.duration < 2) {
							estimatedFinish = TimeUtil.adjustSeconds(estimatedFinish, zeroDurationAdjustment)
						}
					}

					Person currentPerson = securityService.userLoginPerson
					List<String> assignedTeams = personService.getPersonTeamCodes(currentPerson)

					[
						id            : task.id,
						number        : task.taskNumber,
						asset         : GormUtil.domainObjectToMap(task.assetEntity, ['id', 'assetName', 'assetType', 'assetClass']),
						name          : task.comment,
						criticalPath  : recalculate ? taskVertex.isCriticalPath() : task.isCriticalPath,
						duration      : task.duration,
						durationScale : task.durationScale?.name(),
						startInitial  : TimeUtil.elapsed(startDate, (recalculate ? taskVertex.earliestStartDate : task.estStart), TimeUtil.GRANULARITY_MINUTES),
						endInitial    : TimeUtil.elapsed(startDate, (recalculate ? taskVertex.earliestFinishDate : task.estFinish), TimeUtil.GRANULARITY_MINUTES),
						slack         : recalculate ? taskVertex.slack : task.slack,
						actualStart   : task.actStart,
						status        : task.status,
						actFinish     : task.actFinish,
						estStart      : estimatedStart,
						estFinish     : estimatedFinish,
						assignedTo    : task.assignedTo?.toString(),
						team          : task.role,
						isAutomatic   : task.isAutomatic(),
						myTask		  : task.assignedTo?.id == currentPerson.id || (task.assignedTo == null && task.role in assignedTeams),
						hasAction     : task.hasAction(),
						predecessorIds: task.taskDependencies?.findAll { it.successor.id == task.id }.collect {
							it.predecessor.id
						}
					]

				}
			]
		)
	}

	@HasPermission(Permission.TaskTimelineView)
	def calculateCPA() {

		ReadTimelineCommandObject commandObject = populateCommandObject(ReadTimelineCommandObject)

		MoveEvent moveEvent = fetchDomain(MoveEvent, commandObject.properties)
		CPAResults cpaResults = timelineService.calculateCPA(moveEvent, commandObject.viewUnpublished)

		if (!cpaResults.summary.cycles.isEmpty()) {
			throw new RuntimeException('Can not calculate critical path analysis with cycles')
		}

		renderAsJson(buildResponse(cpaResults))
	}

	@HasPermission(Permission.TaskTimelineView)
	def baseline() {

		ReadTimelineCommandObject commandObject = populateCommandObject(ReadTimelineCommandObject)

		MoveEvent moveEvent = fetchDomain(MoveEvent, commandObject.properties)
		CPAResults cpaResults = timelineService.updateTaskFromCPA(moveEvent, commandObject.viewUnpublished)

		if (!cpaResults.summary.cycles.isEmpty()) {
			throw new RuntimeException('Can not calculate critical path analysis with cycles')
		}

		renderAsJson(
			message: cpaResults.tasks.size() + ' tasks updated'
		)
	}

	@HasPermission(Permission.TaskViewCriticalPath)
	def exportCPA() {

		ExportTimelineCommand commandObject = populateCommandObject(ExportTimelineCommand)

		MoveEvent moveEvent = fetchDomain(MoveEvent, commandObject.properties)
		CPAResults cpaResults = timelineService.calculateCPA(moveEvent, commandObject.showAll)

		TaskTimeLineGraph graph = cpaResults.graph
		TimelineSummary summary = cpaResults.summary
		List<Task> tasks = cpaResults.tasks
		List<TaskDependency> deps = cpaResults.taskDependencies

		StringBuilder results = new StringBuilder("<h1>Timeline Data for Event $moveEvent</h1>")

		try {

			Closure buildList = { List<TaskVertex> vertices ->
				return vertices.collect { TaskVertex v ->
					"${v.taskNumber}:${v.taskComment}"
				}
			}

			results << "Found ${tasks.size()} tasks and ${deps.size()} dependencies<br/>"
			results << "Start Vertices: " << (graph.starts.size() > 0 ? buildList(graph.starts) : 'none') << '<br/>'
			results << "Sink Vertices: " << (graph.sinks.size() > 0 ? buildList(graph.sinks)  : 'none') << '<br/>'

			if (!summary.cycles.isEmpty()) {
				results << "Cyclical Maps: "

				results << '<ol>'
				summary.cycles.each { List<TaskVertex> cycle ->
					results << "<li> Circular Reference Stack: <ul>"
					cycle.each { TaskVertex taskVertex ->
						results << "<li>$taskVertex.taskNumber $taskVertex.taskComment"
					}
					results << '</ul>'
				}
				results << '</ol>'
			}

			results << '<br/>'
			results << "Pass Elapsed Time: $summary.elapsedTime<br/>"
			results << "<b>Estimated Runbook Duration: $summary.windowEndTime for Move Event: $moveEvent</b><br/>"

			String durationExtra = ''
			String timesExtra = ''
			String tailExtra = ''

			if (commandObject.showAll) {
				durationExtra = "<th>Act Duration</th><th>Deviation</th>"
				timesExtra = "<th>Act Start</th>"
				tailExtra = "<th>TaskSpec</th><th>Hard Assigned</th><th>Resolved By</th><th>Class</th>" +
					"<th>Asset Id</th><th>Asset Name</th>"
			}

			results << """<h1>Tasks Details</h1>
				<table align="center">
					<tr><th>Id</th><th>Task #</th><th>Action</th>
					<th>Est Duration</th>
					$durationExtra
					<th>Earliest Start</th><th>Latest Start</th><th>Slack</th><th>Constraint Time</th>
					$timesExtra
					<th>Act Finish</th><th>Priority</th><th>Critical Path</td><th>Team</th><th>Individual</th><th>Category</th>
					$tailExtra
					</tr>"""

			DateFormat dateTimeFormat = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)
			String userTzId = userPreferenceService.timeZone

			tasks.each { Task task ->
				task.refresh()

				def person = task.assignedTo ?: ''
				def team = task.role ?: ''
				def constraintTime = ''
				def actStart = ''
				def actFinish = ''
				TimeDuration actDuration
				def deviation = ''
				def actual = ''

				if (task.constraintTime) {
					constraintTime = TimeUtil.formatDateTimeWithTZ(userTzId, task.constraintTime, dateTimeFormat) + ' ' + task.constraintType
				}
				if (task.actStart) {
					actStart = TimeUtil.formatDateTimeWithTZ(userTzId, task.actStart, dateTimeFormat)
				}
				if (task.actFinish) {
					actFinish = TimeUtil.formatDateTimeWithTZ(userTzId, task.actFinish, dateTimeFormat)
				}

				if (task.actStart && task.actFinish) {
					actDuration = TimeUtil.elapsed(task.actStart, task.actFinish)
					TimeDuration estDuration = new TimeDuration(0, task.durationInMinutes(), 0, 0)
					TimeDuration delta = actDuration.minus(estDuration)
					deviation = TimeUtil.ago(delta)
					actual = TimeUtil.ago(actDuration)
				}

				durationExtra = ''
				timesExtra = ''
				tailExtra = ''
				if (commandObject.showAll) {
					durationExtra = "<td>$actual</td><td>$deviation</td>"
					timesExtra = "<td>$actStart</td>"
					tailExtra = "<td>${task.taskSpec ?: ''}</td>" +
						"<td>${task.hardAssigned == 1 ? 'Yes' : ''}</td>" +
						"<td>${task.resolvedBy ?: ''}</td>" +
						"<td>${task.assetEntity ? task.assetEntity.assetClass : ''}</td>" +
						"<td>${task.assetEntity ? task.assetEntity.id : ''}</td>" +
						"<td>${task.assetEntity ? task.assetEntity.assetName.encodeAsHTML() : ''}</td>"
				}

				def criticalPath = task.isCriticalPath

				results << """<tr>
					<td>$task.id</td><td>$task.taskNumber</td>
					<td>${task.comment.encodeAsHTML()}</td>
					<td align="right">${task.durationInMinutes()}</td>
					$durationExtra
					<td>${task.estStart?: ''}</td>
					<td>${task.estFinish?: ''}</td>
					<td align="right">${task.slack?: ''}</td>
					<td>$constraintTime</td>
					$timesExtra
					<td>${actFinish?: ''}</td>
					<td>$task.priority</td>
					<td>$criticalPath</td>
					<td>$team</td>
					<td>$person</td>
					<td>$task.category</td>
					$tailExtra
					</tr>"""
			}
			results << '</table>'
		}

		catch (e) {
			results << "<h1>Unable to complete computation</h1>" << e.message
		}

		render results.toString()
	}

	/**
	 * Builds a Map structure to be used in controller method response
	 *
	 * @param cpaResults an instance of {@code CPAResults}
	 * @return a{@code Map} used to build a respons in JSON format
	 */
	private Map<String, ?> buildResponse(CPAResults cpaResults) {

		TaskTimeLineGraph graph = cpaResults.graph
		TimelineSummary summary = cpaResults.summary

		return [
			windowStartTime   : summary.windowStartTime,
			windowEndTime     : summary.windowEndTime,
			cycles            : summary.cycles.collect { List<TaskVertex> cycle ->
				cycle.collect { [taskId: it.taskId, taskComment: it.taskComment] }
			},
			criticalPathRoutes: summary.criticalPathRoutes.collect { CriticalPathRoute route ->
				route.vertices.collect { [taskId: it.taskId, taskComment: it.taskComment] }
			},
			vertices          : graph.getVertices().collect { TaskVertex taskVertex ->
				[
					id                : taskVertex.taskId,
					number            : taskVertex.taskNumber,
					comment           : taskVertex.taskComment,
					criticalPath      : taskVertex.isCriticalPath(),
					duration          : taskVertex.duration,
					slack             : taskVertex.slack,
					actualStart       : taskVertex.actualStart,
					status            : taskVertex.status,
					earliestStartDate : taskVertex.earliestStartDate,
					earliestFinishDate: taskVertex.earliestFinishDate,
					latestStartDate   : taskVertex.latestStartDate,
					latestFinishDate  : taskVertex.latestFinishDate
				]
			}
		]
	}
	/**
	 * Given a list of {@code TaskVertex} it calculate the lowest earliestStartDate
	 *
	 * @param taskVertexList list of {@code TaskVertex}
	 * @return lowest earliestStartDate from a list of {@code TaskVertex}
	 */
	private TaskVertex findEarliestStartVertex(Collection<TaskVertex> taskVertexList) {
		return taskVertexList.min { it.earliestStartDate }
	}

	/**
	 * Given a list of {@code TaskVertex} it calculate the latest latestFinishDate
	 *
	 * @param taskVertexList list of {@code TaskVertex}
	 * @return latest latestFinishDate from a list of {@code TaskVertex}
	 */
	private TaskVertex findLatestFinishVertex(Collection<TaskVertex> taskVertexList) {
		return taskVertexList.max { it.latestFinishDate }
	}

	/**
	 * Given a list of {@code Task} it calculate the lowest estStart
	 *
	 * @param taskList list of {@code Task}
	 * @return lowest estStart from a list of {@code Task}
	 */
	private Date findEarliestTaskStartTime(List<Task> taskList) {
		return taskList.collect { Task task ->
			[task.actStart, task.estStart, task.actFinish].min()
		}.min()
	}

	/**
	 * Given a list of {@code Task} it calculate the latest estFinish
	 *
	 * @param taskList list of {@code Task}
	 * @return latest estFinish from a list of {@code Task}
	 */
	private Date findLatestTaskFinishTime(List<Task> taskList) {
		return taskList.collect { Task task ->
			[task.actStart, task.actFinish, task.latestFinish].max()
		}.max()
	}
}
