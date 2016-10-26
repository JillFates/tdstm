import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.ManufacturerService

import grails.plugin.springsecurity.annotation.Secured

/**
 * Handles WS calls of the ManufacturerService.
 *
 * @author Diego Scarpa
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsManufacturerController')
class WsManufacturerController implements ControllerMethods {

	ControllerService controllerService
	ManufacturerService manufacturerService

	/**
	 * Merge to manufacturers
	 */
	def merge() {
		try {
			controllerService.checkPermissionForWS('EditModel')
			manufacturerService.merge(params.id, params.fromId)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}
}
