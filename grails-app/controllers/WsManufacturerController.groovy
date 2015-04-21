import java.util.Map;

import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;

import grails.validation.ValidationException;

/**
 * {@link Controller} for handling WS calls of the {@link ManufacturerService}
 *
 * @author Diego Scarpa
 */
class WsManufacturerController {

	def controllerService
	def manufacturerService

	/*
	 * Merge to manufacturers
	 *
	 * Check {@link UrlMappings} for the right call
	 */
	def merge() {
		try {
			controllerService.checkPermissionForWS("EditModel")

			manufacturerService.merge(params.id, params.fromId)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

}
