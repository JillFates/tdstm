import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;
import grails.validation.ValidationException;


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
	def preferences = {
		def data = [:]
		def prefs = ( params.id ? params.id?.split(',') : [] )
		prefs.each { p -> data[p] = userPreferenceService.getPreference(p) }

		render(ServiceResults.success('preferences' : data) as JSON)
	}

}