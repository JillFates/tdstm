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

	InsightService insightService

	/**
	 * Action to return Provider data for insight dashboard of assets and dependencies.
	 */
	def provider() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			assetsAndDependenciesByProvider: insightService.assetsAndDependenciesByProvider(project, context.max),
		]]
	}

	/**
	 * Action for returning top tags data by assets.
	 */
	def topTags() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			topTags: insightService.topTags(project, context.max),
		]]
	}

	/**
	 * Action for returning assets broken down by OS and environment.
	 */
	def assetsByOsAndEnvironment() {
		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			assetsByOsAndEnvironment: insightService.assetsByOsAndEnvironment(project),
		]]
	}

	/**
	 * Action for returning devices by events.
	 */
	def devicesByEvent() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			devicesByEvent: insightService.devicesByEvent(project, context.max),
		]]
	}

	/**
	 * Action for returning assets broken down by provider and asset type.
	 */
	def AssetsByProviderAndAssetType() {
		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			AssetsByProviderAndAssetType: insightService.AssetsByProviderAndAssetType(project),
		]]
	}

	/**
	 * This returns the applications "blast radius" which is applications Grouped by the number of dependencies they have.
	 */
	def applicationsBlastRadius() {
		InsightDataCommand context = populateCommandObject(InsightDataCommand)

		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: [
			applicationsGroupedByDependencies: insightService.applicationsGroupedByDependencies(project, context.lowRange, context.highRange),
		]]
	}
}
