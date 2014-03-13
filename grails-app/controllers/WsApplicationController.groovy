import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;
import grails.validation.ValidationException;


/**
 * {@link Controller} for handling WS calls of the {@link EventService}
 * 
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsApplicationController {

	def securityService
	
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

		// If params.id > 0 then need to validate that the bundle is associated to the user's current project

		// Create some stub data which will be replaced later with EventService calls
		def apps = [
			[id: 400, name: 'Oracle'],
			[id: 402, name: 'ERP'],
			[id: 404, name: 'Salesforce']
		]
		def unassigned = [
			[id: 500, name: 'Business Objects'],
			[id: 503, name: 'MSSQL'],
			[id: 509, name: 'TaxMan']
		]

		def data = ( params.id == '0' ? unassigned : apps )

		render(ServiceResults.success('list' : data) as JSON)
	}
}