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
import net.transitionmanager.task.timeline.TaskVertex
import net.transitionmanager.task.timeline.TimeLineService
import net.transitionmanager.task.timeline.TimelineSummary

@Secured('isAuthenticated()')
class WsTimeLineController implements ControllerMethods {

	TimeLineService timeLineService
	RunbookService runbookService

	@HasPermission(Permission.TaskTimelineView)
	def baselining() {

		MoveEvent moveEvent = fetchDomain(MoveEvent, params)
		List<Task> tasks = runbookService.getEventTasks(moveEvent)
		List<TaskDependency> deps = runbookService.getTaskDependencies(tasks)

		TimelineSummary summary = timeLineService.executeCPA(moveEvent, tasks, deps)
		Map<String, ?> data = [
			windowStartTime   : summary.windowStartTime,
			windowEndTime     : summary.windowEndTime,
			cycles            : summary.cycles.collect { List<TaskVertex> cycle ->
				cycle.collect { [taskId: it.taskId, taskComment: it.taskComment] }
			},
			criticalPathRoutes: summary.criticalPathRoutes.collect { CriticalPathRoute route ->
				route.vertices.collect { [taskId: it.taskId, taskComment: it.taskComment] }
			}
		]
		render(data as JSON)
	}
}
