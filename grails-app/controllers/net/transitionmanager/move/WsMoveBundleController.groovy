package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.Room
import net.transitionmanager.command.MoveBundleCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission


@Secured('isAuthenticated()')
class WsMoveBundleController implements ControllerMethods {

	MoveBundleService moveBundleService

	/**
	 * Return the model necessary for creating new bundles.
	 * @return a map with the current project and its rooms.
	 */
	@HasPermission(Permission.BundleCreate)
	def modelForCreate() {
		Project project = getProjectForWs()
		renderSuccessJson([ projectInstance: project.toMap(), rooms: Room.findAllByProject(project)*.toMap()
		])
	}

	/**
	 * Return the data for populating the view for editing a bundle.
	 * @param moveBundleId - the id of the bundle to be edited.
	 * @return a map with the fields of the bundle, the id of the project and the rooms.
	 */
	@HasPermission(Permission.BundleView)
	def modelForEdit(Long moveBundleId) {
		Project project = getProjectForWs()
		MoveBundle moveBundle = fetchDomain(MoveBundle, [id: moveBundleId], project)
		userPreferenceService.setPreference(UserPreferenceEnum.CURR_BUNDLE, moveBundleId)
		renderSuccessJson([
			moveBundleInstance: moveBundle.toMap(),
			projectId: project.id,
			rooms: Room.findAllByProject(project)*.toMap()
		])
	}

	/**
	 * List all available bundles for this project.
	 * @return
	 */
	@HasPermission(Permission.BundleView)
	def list() {
		Project project = getProjectForWs()
		renderSuccessJson(moveBundleService.moveBundlesByProject(project))
	}

	/**
	 * Delete the given bundle.
	 * @param moveBundleId - the id of the bundle to be deleted.
	 * @return
	 */
	@HasPermission(Permission.BundleDelete)
	def delete(Long moveBundleId) {
		renderSuccessJson(message: moveBundleService.deleteBundle(MoveBundle.get(moveBundleId), getProjectForWs()))
	}

	/**
	 * Endpoint for deleting a bundle along with all its associated assets.
	 * @param moveBundleId
	 * @return
	 */
	@HasPermission(Permission.BundleDelete)
	def deleteBundleAndAssets(Long moveBundleId) {
		MoveBundle moveBundle = fetchDomain(MoveBundle, [id: moveBundleId])
		String message
		if (moveBundle) {
			try{
				moveBundleService.deleteBundleAndAssets(moveBundle)
				message = "MoveBundle $moveBundle deleted"
			}
			catch (e) {
				message = "Unable to Delete MoveBundle and Assets: $e.message"
				renderErrorJson(message)
			}
		}
		else {
			message = "MoveBundle not found with id $moveBundleId"
			renderErrorJson(message)
		}
		renderSuccessJson(message)
	}

	/**
	 * Create or update a bundle.
	 * @param moveBundleId - id of the bundle (if it's an update operation).
	 * @return
	 */
	@HasPermission(Permission.BundleCreate)
	def save(Long moveBundleId) {
		Project project = getProjectForWs()
		MoveBundleCommand moveBundleCommand = populateCommandObject(MoveBundleCommand, false)
		MoveBundle moveBundle = moveBundleService.saveOrUpdate(moveBundleCommand, project, moveBundleId)
		renderSuccessJson(moveBundle.toMap())
	}


}
