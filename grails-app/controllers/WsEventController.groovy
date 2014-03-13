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

		// Create some stub data which will be replaced later with EventService calls
		def bundles = { eId ->
			def b = []
			for (def j=1; j < 6; j++) {
				b << [ id: (eId*10 + j), name: "Bundle ${eId}-${j}"]
			}
			return b
		}

		def data = []
		def e = ['Alpha', 'Bravo', 'Charlie', 'Delta', 'Echo']
		for (def i=0; i < e.size(); i++) {
			data << [ id: (i+1), name: "Event ${e[i]}", bundles: bundles(i) ]
		}

		render(ServiceResults.success('list' : data) as JSON)
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
		def id = (params.id && params.id.toInteger() > 0 ? params.id : 'UNASSIGNED' )

		def data = [
			[ id: 1, name: "Bundle ${id}-1"],
			[ id: 2, name: "Bundle ${id}-2"],
			[ id: 3, name: "Bundle ${id}-3"],
			[ id: 4, name: "Bundle ${id}-4"],
		]

		render(ServiceResults.success('list' : data) as JSON)
	}
}