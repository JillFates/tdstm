package net.transitionmanager.reporting

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.reporting.DashboardService
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class WsDashboardController implements ControllerMethods {

	DashboardService 	dashboardService
	MoveEventService    moveEventService

	/**
	 * Returns data used to render the Event Dashboard
	 * based on the following json structure:
	 * <code>
	 * 		snapshot: [
	 * 			"revisedComp"  The Event revisedCompletionTime
	 * 			"moveBundleId"  The Bundle ID
	 * 			"calcMethod"  The Event calc method: Is one of METHOD_LINEAR = 'L' or METHOD_MANUAL = 'M'
	 * 			"systime"  The actual system time
	 * 			"eventStartDate  The estStartTime of the Event
	 * 			"planSum": [
	 * 					"dialInd"  The dial indicator percentage of the MoveEventSnapshot
	 * 					"compTime"  The maximum completionTime of the bundles associated to the Event
	 * 					"dayTime"  The difference between now and the eventStartTime
	 * 					"eventDescription"  The event description
	 * 					"eventString"  One of "Countdown Until Event" or "Elapsed Event Time".
	 * 					"eventRunbook"  The runbookStatus property of the Event
	 * 			]
	 * 		]
	 * </code>
	 *
	 * @param id - The MoveEvent id
	 * @return JSON map
	 */
	@HasPermission(Permission.DashboardMenuView)
	def eventData(Long id) {
		MoveEvent moveEvent = fetchDomain(MoveEvent, params)
		renderAsJson(moveEventService.eventData(moveEvent))
	}

	/**
	 * Calculates percentage of Filtered Apps on Total Planned apps.
	 * @param totalAppCount : Total count of Application that is in Planned Bundle
	 * @param filteredAppCount : This is filtered app based on PlanStatus
	 * @return : Percentage of Calculated app
	 */
	@HasPermission(Permission.AssetView)
	def countAppPercentage(int totalAppCount, int filteredAppCount) {
		return totalAppCount ? Math.round((filteredAppCount /  totalAppCount) * 100) : 0
	}

	@HasPermission(Permission.DashboardMenuView)
	def getDataForPlanningDashboard() {
		Project project = getProjectForWs()
		renderSuccessJson(dashboardService.getDataForPlanningDashboard(project))
	}
}
