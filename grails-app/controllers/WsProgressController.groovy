import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import grails.validation.ValidationException;

/**
 * {@link Controller} for handling WS calls of the {@link ProgressService}
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsProgressController {

	def progressService
	def securityService
	
	/**
	 * Gets the status of the progress of a async task
	 */
	def get = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			def progressMap = progressService.get(id)

			render(ServiceResults.success(progressMap) as JSON)
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
	
	/**
	 * Returns the list of pending progresses
	 */
	def list = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		try {
			def progressMap = progressService.list()

			render(ServiceResults.success(progressMap) as JSON)
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
