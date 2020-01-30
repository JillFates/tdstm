package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.architecturegraph.ArchitectureGraphCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.license.LicenseAdminService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
/**
 * Controller for getting architecture graph data
 */
@SuppressWarnings('GrMethodMayBeStatic')
@Secured('isAuthenticated()')
class WsArchitectureGraphController implements ControllerMethods, PaginationMethods {
	static defaultAction = 'list'


	AssetEntityService       assetEntityService
	UserPreferenceService    userPreferenceService
	LicenseAdminService      licenseAdminService
	ArchitectureGraphService architectureGraphService


	/**
	 * This returns the preferences and data needed for drop downs in the architecture view.
	 */
	@HasPermission(Permission.ArchitectureView)
	def preferences() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		ArchitectureGraphCommand context = populateCommandObject(ArchitectureGraphCommand)
		Project project = securityService.userCurrentProject

		render view: "/common/mapAsJson", model: [data: architectureGraphService.getPreferences(context.assetId, context.levelsUp, context.levelsDown, project)]

	}

	/**
	 * Returns the data needed to generate the application architecture graph
	 */
	@HasPermission(Permission.ArchitectureView)
	def architectureGraph() {
		Project project = securityService.userCurrentProject

		ArchitectureGraphCommand context = populateCommandObject(ArchitectureGraphCommand)

		AssetEntity rootAsset = AssetEntity.get(context.assetId)

		if (rootAsset && rootAsset.project != project) {
			throw new UnauthorizedException()
		}

		// Check if the parameters are null
		if ((context.assetId == null || context.assetId == -1) || (context.levelsUp == null || context.levelsDown == null)) {
			render view: '/common/mapAsJson', model: [data: architectureGraphService.architectureGraphModel(rootAsset, context.levelsUp, context.levelsDown, context.mode)]
			return
		}

		render view: '/common/mapAsJson', model: [data: architectureGraphService.architectureGraphModel(rootAsset, context.levelsUp, context.levelsDown, context.mode)]

	}

	/**
	 * This gets the asset type name map for the architecture graphs legend
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def legend() {
		render view: '/common/mapAsJson', model: [data: assetEntityService.ASSET_TYPE_NAME_MAP]
	}

}
