package net.transitionmanager.service

import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project

/**
 * Handles the logic for CRUD events
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class EventService implements ServiceMethods {

	static transactional = false

	ControllerService controllerService

	/**
	 * Provides a list all bundles associated to a specified project
	 * for the user's current project
	 *
	 * @return the list of events with the associated bundles
	 */
	def listEventsAndBundles() {
		Project project = controllerService.requiredProject

		def result = []

		for (event in MoveEvent.findAllByProject(project)) {
			def bundles = []
			for (moveBundle in event.moveBundles) {
				bundles << [id: moveBundle.id, name: moveBundle.name]
			}

			result.add(id: event.id, name: event.name, bundles: bundles)
		}

		return result
	}

	/**
	 * Provides a list all bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 *
	 * @param eventId the id of the event
	 * @param useForPlanning - a boolean to filter by the useForPlanning property of the move event
	 * @return the list of bundles associated with the event
	 */
	def listBundles(Long eventId, Boolean useForPlanning) {
		Project project = controllerService.requiredProject

		MoveEvent moveEvent

		if (eventId) {
			moveEvent = MoveEvent.get(eventId)
			if (moveEvent != null) {
				securityService.assertCurrentProject moveEvent.project
			} else {
				log.info('Move event is null')
				throw new EmptyResultException()
			}
		}

		def result = []

		def moveBundles
		if (moveEvent != null) {
			if (useForPlanning != null) {
				moveBundles = MoveBundle.findAllByMoveEventAndUseForPlanning(moveEvent, useForPlanning.toBoolean())
			} else {
				moveBundles = MoveBundle.findAllByMoveEvent(moveEvent)
			}
		} else {
			if (useForPlanning != null) {
				def up = useForPlanning.toBoolean()
				moveBundles = MoveBundle.findAll(
					'from MoveBundle where moveEvent = null AND project.client = :client and useForPlanning = :useForPlanning',
					[useForPlanning: up, client: project.client])
			} else {
				moveBundles = MoveBundle.findAll(
					'from MoveBundle where moveEvent = null AND project.client = :client',
					[client: project.client])
			}
		}

		for (moveBundle in moveBundles) {
			result.add(id: moveBundle.id, name: moveBundle.name)
		}

		return result
	}
}
