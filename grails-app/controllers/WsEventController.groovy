import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;
import grails.validation.ValidationException;


/**
 * {@link Controller} for handling WS calls of the {@link EventService}
 * 
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsEventController {

	def securityService
	def eventService
	
	/**
	 * Provides a list all events and associate bundles for the user's current project
	 * Check {@link UrlMappings} for the right call
	 */
	def listEventsAndBundles = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def currentProject = securityService.getUserCurrentProject()
		
		try {
			def results = eventService.listEventsAndBundles(loginUser, currentProject)
			render(ServiceResults.success(['list' : results]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	
	/**
	 * Provides a list all bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 * Check {@link UrlMappings} for the right call
	 */
	def listBundles = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def currentProject = securityService.getUserCurrentProject()
		def id = params.id
		def useForPlanning = params.useForPlanning
		
		try {
			def results = eventService.listBundles(id, useForPlanning, loginUser, currentProject)
			render(ServiceResults.success(['list' : results]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
}