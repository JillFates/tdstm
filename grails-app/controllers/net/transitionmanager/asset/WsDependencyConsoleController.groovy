package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.DependencyAnalyzerTabs
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.gorm.transactions.NotTransactional
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.DependencyConsoleCommand
import net.transitionmanager.command.bundle.AssetsAssignmentCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.license.LicenseAdminService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
/**
 * Controller for getting architecture graph data
 */
@SuppressWarnings('GrMethodMayBeStatic')
@Secured('isAuthenticated()')
class WsDependencyConsoleController implements ControllerMethods, PaginationMethods {
	static defaultAction = 'dependencyConsole'

	UserPreferenceService    userPreferenceService
	LicenseAdminService      licenseAdminService
	MoveBundleService        moveBundleService


/**
 * Control function to render the Dependency Analyzer (was Dependency Console)
 * @param Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
 */
	@HasPermission(Permission.DepAnalyzerView)
	@NotTransactional()
	def dependencyConsole() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = projectForWs

		if (!project) {
			return
		}

		// Check for correct URL params
		if (console.subsection || console.groupId) { // is the drill-in URL
			if (!(console.subsection && console.groupId)) { // If any exists, then both should be present in the URL
				throw new InvalidParamException("Subsection and Group Id params are both required.")
			}
			String subsec = console.subsection as String // Check for valid tab name
			if (!(subsec.toUpperCase() in (DependencyAnalyzerTabs.values() as String[]))) {
				throw new InvalidParamException("Invalid Subsection name: ${subsec}")
			}
		}
		//Date start = new Date()
		userPreferenceService.setPreference(
			UserPreferenceEnum.ASSIGNED_GROUP,
			console.assignedGroup ?: userPreferenceService.getPreference(UserPreferenceEnum.ASSIGNED_GROUP) ?: "1"
		)

		Map map = moveBundleService.dependencyConsoleMap(
			project,
			console.bundle,
			console.tagIds,
			console.tagMatch,
			console.assignedGroup,
			null,
			false,
			console.subsection,
			console.groupId,
			console.assetName
		)

		//log.info 'dependencyConsole() : moveBundleService.dependencyConsoleMap() took {}', TimeUtil.elapsed(start)
		render view: '/common/mapAsJson', model: [data: map]
	}

	/**
	 * Controller to render the Dependency Bundle Details Table.
	 * This method is called from the Dependency Analyzer in an Ajax call to render the dependency table.
	 * @param Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyBundleDetails() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		Project project = projectForWs

		if (!project) {
			return
		}

		userPreferenceService.setPreference(
			UserPreferenceEnum.ASSIGNED_GROUP,
			console.assignedGroup ?: userPreferenceService.getPreference(UserPreferenceEnum.ASSIGNED_GROUP) ?: "1"
		)

		Map model = moveBundleService.dependencyConsoleMap(
			project,
			console.bundle,
			console.tagIds,
			console.tagMatch,
			console.assignedGroup,
			null,
			false,
			console.subsection,
			console.groupId,
			console.assetName
		)

		render view: '/common/mapAsJson', model: [data: model]
	}

	/**
	 * Assigns one or more assets to a specified bundle, and add tags
	 */
	@HasPermission(Permission.AssetEdit)
	def assetsAssignment() {
		AssetsAssignmentCommand assetsAssignment = populateCommandObject(AssetsAssignmentCommand)
		validateCommandObject(assetsAssignment)

		Project project = projectForWs

		if (!project) {
			return
		}

		moveBundleService.assignAssets(assetsAssignment.assets, assetsAssignment.tagIds, assetsAssignment.moveBundle, assetsAssignment.planStatus, project)

		renderSuccessJson()

	}
}
