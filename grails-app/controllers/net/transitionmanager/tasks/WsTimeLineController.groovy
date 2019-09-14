package net.transitionmanager.tasks

import com.tdsops.common.security.spring.HasPermission
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.security.Permission
import net.transitionmanager.task.RunbookService
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.CriticalPathRoute
import net.transitionmanager.task.timeline.TaskTimeLineGraph
import net.transitionmanager.task.timeline.TaskVertex
import net.transitionmanager.task.timeline.TimeLineService
import net.transitionmanager.task.timeline.TimelineSummary

@Secured('isAuthenticated()')
class WsTimeLineController implements ControllerMethods {

	TimeLineService timeLineService
	RunbookService runbookService

	@HasPermission(Permission.TaskTimelineView)
	def calculateCPA() {
		MoveEvent moveEvent = fetchDomain(MoveEvent, params)
		List<Task> tasks = runbookService.getEventTasks(moveEvent)
		List<TaskDependency> deps = runbookService.getTaskDependencies(tasks)

		def (TaskTimeLineGraph graph, TimelineSummary summary) = timeLineService.calculateCPA(moveEvent, tasks, deps)

		if (!summary.cycles.isEmpty()) {
			throw new RuntimeException('Can not calculate critical path analysis with cycles')
		}

		render(buildResponse(graph, summary) as JSON)
	}

	@HasPermission(Permission.TaskTimelineView)
	def baseline() {

		MoveEvent moveEvent = fetchDomain(MoveEvent, params)
		List<Task> tasks = runbookService.getEventTasks(moveEvent)
		List<TaskDependency> deps = runbookService.getTaskDependencies(tasks)

		def (TaskTimeLineGraph graph, TimelineSummary summary) = timeLineService.updateTaskFromCPA(moveEvent, tasks, deps)

		if (!summary.cycles.isEmpty()) {
			throw new RuntimeException('Can not calculate critical path analysis with cycles')
		}

		render(buildResponse(graph, summary) as JSON)
	}

	/**
	 * Builds a Map structure to be used in controller method response
	 *
	 * @param graph an instance of {@code TaskTimeLineGraph}
	 * @param summary an instance of {@code TimelineSummary}
	 * @return a{@code Map} used to build a respons in JSON format
	 */
	private Map<String, ?> buildResponse(TaskTimeLineGraph graph, TimelineSummary summary) {

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
}
