package net.transitionmanager.reporting

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files

import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.MoveEventSnapshot
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WsDashboardController implements ControllerMethods {

	JdbcTemplate jdbcTemplate
	CustomDomainService customDomainService
	DashboardService dashboardService
	MoveEventService moveEventService
	TaskService taskService

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
		Project project = securityService.userCurrentProject
		renderSuccessJson(dashboardService.getDataForPlanningDashboard(project))
	}
}
