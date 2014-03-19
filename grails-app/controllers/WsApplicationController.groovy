import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;
import grails.validation.ValidationException;


/**
 * {@link Controller} for handling WS calls of the {@link ApplicationService}
 * 
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsApplicationController {

	def securityService
	def applicationService
	
	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned 
	 * applications for the user's current project
	 * Check {@link UrlMappings} for the right call
	 */
	def listInBundle = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def currentProject = securityService.getUserCurrentProject()
		
		try {
			def results = applicationService.listInBundle(params.id, loginUser, currentProject)
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