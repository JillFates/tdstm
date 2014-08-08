import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.TimeUtil

/**
 * Functioned used in conjuction with the Dashboards
 */
class DashboardService {

	def taskService

	/**
	 * Generates the model used by the Task Summary section of the Event Dashboard
	 * @param eventId - the event id to retrieve data for
	 * @return Map the data model
	 */
	Map getTaskSummaryModel(eventId, UserLogin user, Project project, maxTeamRows=6) {
		Map model = [:]
		def event

		//
		// Perform the security checks
		//
		if (eventId && (eventId instanceof Long) || (eventId instanceof String && eventId.isInteger() ) ) {
			event = MoveEvent.read(eventId)
			if (! event) {
				throw new EmptyResultException('Event not found')
			} else if (event.project != project) {
				throw new IllegalArgumentException('The event is not associated with the project')
			}
		} else {
			throw new EmptyResultException('Invalid event id')
		}

		// Process task summary data
		def results = taskService.getMoveEventTaskSummary(event)
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
		def effortRemaining = {status, defVal='' ->
			def time = defVal
			def sd = taskStatusMap[status]
			if (sd.taskCount) {
				time = TimeUtil.ago( sd.timeInMin * 60 )
			}
			return time
		}

		// Current counts
		model.countReady = taskStatusMap['Ready'].taskCount
		model.countStarted = taskStatusMap['Started'].taskCount
		model.countPending = taskStatusMap['Pending'].taskCount
		model.countDone = taskStatusMap['Completed'].taskCount

		// Task Percentage
		model.percTaskReady = percCalc(taskStatusMap['Ready'].taskCount, taskCountByEvent)
		model.percTaskStarted = percCalc(taskStatusMap['Started'].taskCount, taskCountByEvent)
		model.percTaskDone = percCalc(taskStatusMap['Completed'].taskCount, taskCountByEvent)

		// Duration Percentage
		model.percDurationReady = percCalc(taskStatusMap['Ready'].timeInMin, totalDuration)
		model.percDurationStarted = percCalc(taskStatusMap['Started'].timeInMin, totalDuration)
		model.percDurationDone = percCalc(taskStatusMap['Completed'].timeInMin, totalDuration)

		// Duration Remaining
		model.effortRemainPending = effortRemaining('Pending')
		model.effortRemainReady = effortRemaining('Ready')
		model.effortRemainStarted = effortRemaining('Started')
		model.effortRemainDone = effortRemaining('Completed')

		// Process Team information
		def teamTaskResults = taskService.getMoveEventTeamTaskSummary(event) //use the matrix instead once _taskSummary		
		model.roles = teamTaskResults.values()*.role
		// TODO : John 8/2014 : Shouldn't need the teamTaskMap but something in the view is still using it
		model.teamTaskMap = teamTaskResults

		// Contruct the team results as a matrix of rows and columns to layout the teams sorted serpentine 
		ArrayList teamTaskResultsMatrix = [] //use this in _taskSummary.gsp
		def i = 0
		teamTaskResults.each() { key, teamData ->
			def iMod = i % maxTeamRows
			teamData.percDone = percCalc(teamData.teamDoneCount, teamData.teamTaskCount)
			if(teamTaskResultsMatrix[iMod] == null)
				teamTaskResultsMatrix[iMod] = new ArrayList()
			teamTaskResultsMatrix[iMod] << teamData
			++i
		}
		model.teamTaskMatrix = teamTaskResultsMatrix

		return model
	}
	
}