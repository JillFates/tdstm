import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.stereotype.Controller
/**
 * {@link Controller} for handling WS calls of the {@link UserService}
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsUserController {

	def securityService
	def userPreferenceService

	/**
	 * Used to access a list of one or more user preferences
	 * @param id - a comma separated list of the preference(s) to be retrieved
	 * Check {@link UrlMappings} for the right call
	 * @example GET ./ws/user/preferences/EVENT,BUNDLE
	 * @return a MAP of the parameters (e.g. preferences:[EVENT:5, BUNDLE:30])
	 */
	def preferences() {
		def data = [:]
		def prefs = ( params.id ? params.id?.split(',') : [] )
		prefs.each { p -> data[p] = userPreferenceService.getPreference(p) }

		render(ServiceResults.success('preferences' : data) as JSON)
	}

	/**
	 * Used to set a user preference through an AJAX call
	 * @param code - the preference code for the preference that is being set
	 * @param value - the value to set the preference to
	 */
	def savePreference () {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def currentProject = securityService.getUserCurrentProject()

		try {
			def prefCode = params.code?.toString() ?: ''
			def prefValue = params.value ?: ''

			userPreferenceService.savePreference(prefCode, prefValue)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (InvalidParamException e) {
			ServiceResults.respondWithError(response, 'Invalid preference value')
		} catch (InvalidRequestException e) {
			ServiceResults.respondWithError(response, 'Invalid preference code')
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

}
