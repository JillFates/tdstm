package net.transitionmanager.reporting

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.dashboard.InsightDataCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project

/**
 * Controller for getting Insight dashboard data
 */
@Secured('isAuthenticated()')
class WsInsightController implements ControllerMethods {

	InsightService      insightService


	/**
	 * Action to return data for the Insight dashboard.
	 *
	 * @return returns a JSON map of data containing assetsByVendor, dependenciesByVendor, topTags, and applicationsGroupedByDependencies
	 */
	def insightData() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)
		validateCommandObject(context)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			assetsByVendor                   : insightService.assetsByVendor(project, context.max),
			dependenciesByVendor             : insightService.dependenciesByVendor(project, context.max),
			topTags                          : insightService.topTags(project, context.max),
			applicationsGroupedByDependencies: insightService.applicationsGroupedByDependencies(project, context.lowRange, context.highRange),
		]]
	}

	/**
	 * Action to return Vendor data for  insight dashboard of assets and dependencies
	 */
	def vendor() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)
		validateCommandObject(context)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			assetsByVendor      : insightService.assetsByVendor(project, context.max),
			dependenciesByVendor: insightService.dependenciesByVendor(project, context.max),
		]]
	}

	/**
	 * Action tor return
	 */
	def topTags() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)
		validateCommandObject(context)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			topTags: insightService.topTags(project, context.max),
		]]
	}

	/**
	 *
	 */
	def applicationsGroupedByDependencies() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)
		validateCommandObject(context)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			applicationsGroupedByDependencies: insightService.applicationsGroupedByDependencies(project, context.lowRange, context.highRange),
		]]
	}
}
