package net.transitionmanager.reporting

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.dashboard.PlanningDashboardData
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectLogo
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.TaskService

/**
 * Dashboard methods.
 */
class DashboardService implements ServiceMethods {

	private static final Integer EVENT_DASHBOARD_MAX_TEAM_ROWS = 6

	static transactional = false

	MoveEventService moveEventService
	TaskService taskService
	UserPreferenceService userPreferenceService

	/**
	 * Generates the model used by the Task Summary section of the Event Dashboard
	 * @param eventId - the event id to retrieve data for
	 * @return Map the data model
	 */
	Map getTaskSummaryModel(eventId, int maxTeamRows = 6, boolean viewUnpublished = false) {
		Map model = [:]
		def event

		//
		// Perform the security checks
		//
		if (eventId && (eventId instanceof Long) || (eventId instanceof String && eventId.isInteger() ) ) {
			event = MoveEvent.read(eventId)
			if (! event) {
				throw new EmptyResultException('Event not found')
			}
			securityService.assertCurrentProject event.project
		} else {
			throw new EmptyResultException('Invalid event id')
		}

		// Process task summary data
		def results = taskService.getMoveEventTaskSummary(event, viewUnpublished)
		def taskStatusMap = results.taskStatusMap
		def taskCountByEvent = results.taskCountByEvent
		def totalDuration = results.totalDuration
		model.remainTaskCount = results.taskCountByEvent - taskStatusMap['Completed'].taskCount
		model.remainTaskCountFormat = String.format("%,d", model.remainTaskCount)
		model.taskCountByEvent = taskCountByEvent
		model.taskStatusMap = taskStatusMap
		model.totalDuration = totalDuration
		model.remainTaskCount = results.taskCountByEvent - taskStatusMap['Completed'].taskCount
		model.remainTaskCountFormat = String.format("%,d", model.remainTaskCount)

		// helper closure to compute the percentage completed
		def percCalc = { val, total, defVal=0 ->
			return total > 0  && val != null ? Math.round(val/total*100) : defVal
		}

		// helper closure to return the effort remaining or blank
		def effortRemaining = {status, defVal='0' ->
			def time = defVal
			def sd = taskStatusMap[status]
			if (sd.taskCount) {
				time = TimeUtil.ago( sd.timeInMin * 60 ) ?: '0'
			}
			return time
		}

		// Current counts
		model.countReady = taskStatusMap['Ready'].taskCount
		model.countStarted = taskStatusMap['Started'].taskCount
		model.countPending = taskStatusMap['Pending'].taskCount
		model.countHold = taskStatusMap['Hold'].taskCount
		model.countDone = taskStatusMap['Completed'].taskCount

		// Task Percentage
		model.percTaskReady = percCalc(taskStatusMap['Ready'].taskCount, taskCountByEvent)
		model.percTaskStarted = percCalc(taskStatusMap['Started'].taskCount, taskCountByEvent)
		//model.percTaskHold = percCalc(taskStatusMap['Hold'].taskCount, taskCountByEvent)
		model.percTaskDone = percCalc(taskStatusMap['Completed'].taskCount, taskCountByEvent)

		// Duration Percentage
		model.percDurationReady = percCalc(taskStatusMap['Ready'].timeInMin, totalDuration)
		model.percDurationStarted = percCalc(taskStatusMap['Started'].timeInMin, totalDuration)
		//model.percDurationHold = percCalc(taskStatusMap['Hold'].timeInMin, totalDuration)
		model.percDurationDone = percCalc(taskStatusMap['Completed'].timeInMin, totalDuration)

		// Duration Remaining
		model.effortRemainPending = effortRemaining('Pending')
		model.effortRemainReady = effortRemaining('Ready')
		model.effortRemainStarted = effortRemaining('Started')
		model.effortRemainHold = effortRemaining('Hold')
		model.effortRemainDone = effortRemaining('Completed')

		// Process Team information
		def teamTaskResults = taskService.getMoveEventTeamTaskSummary(event, viewUnpublished) //use the matrix instead once _taskSummary
		model.roles = teamTaskResults.values()*.role
		// TODO : John 8/2014 : Shouldn't need the teamTaskMap but something in the view is still using it
		model.teamTaskMap = teamTaskResults

		// Contruct the team results as a matrix of rows and columns to layout the teams sorted serpentine
		List teamTaskResultsMatrix = [] //use this in _taskSummary.gsp
		def i = 0
		teamTaskResults.each { key, teamData ->
			def iMod = i % maxTeamRows
			teamData.percDone = percCalc(teamData.teamDoneCount, teamData.teamTaskCount)
			if(teamTaskResultsMatrix[iMod] == null) {
				teamTaskResultsMatrix[iMod] = []
			}
			teamTaskResultsMatrix[iMod] << teamData
			++i
		}
		model.teamTaskMatrix = teamTaskResultsMatrix

		return model
	}

	/**
	 * Create and return a map with the model for the Event Dashboard.
	 * @param project - the user's current project.
	 * @param moveEvent - the event selected by the user.
	 * @param viewUnpublished - whether or not unpublished tasks should be included in the model.
	 * @return  A model with Event Dashboard info
	 */
	Map getEventDashboardModel(Project project, MoveEvent moveEvent, boolean viewUnpublished) {
		List<MoveEvent> moveEventsList = MoveEvent.findAllByProject(project,[sort:'name',order:'asc'])
		List<MoveBundle> moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = :moveEvent ORDER BY mb.startTime ", [moveEvent: moveEvent])

		Map model = [
			project                       : project,
			projectLogo                   : ProjectLogo.findByProject(project),
			moveEvent                     : moveEvent,
			moveEventsList                : moveEventsList,
			moveBundleList                : moveBundleList,
			timeToUpdate                  : userPreferenceService.getPreference(UserPreferenceEnum.DASHBOARD_REFRESH) ?: 'never',
			EventDashboardDialOverridePerm: securityService.hasPermission(Permission.EventDashboardDialOverride),
			viewUnpublished               : viewUnpublished ? '1' : '0'
		]

		Map taskSummaryMap = getTaskSummaryModel(moveEvent.id, EVENT_DASHBOARD_MAX_TEAM_ROWS, viewUnpublished)

		if (taskSummaryMap) {
			model.putAll(taskSummaryMap)
		}
		return model
	}

	/**
	 * Retrieve all the different metrics for populating the Planning Dashboard.
	 * @param project
	 * @return
	 */
	Map getDataForPlanningDashboard(Project project) {
		return new PlanningDashboardData(project).getDataForDashboard()
	}
}
