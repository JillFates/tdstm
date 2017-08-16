/**
 * Created by David Ontiveros
 */

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Report
import net.transitionmanager.service.ReportService
import net.transitionmanager.service.SecurityService

/**
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 * @see URLMappings.groovy
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	ReportService reportService
	SecurityService securityService

	/**
	 * Returns the list of available reports as a map(json) result.
	 * All reports returned belong to current user project in session.
	 * @return
	 */
    def listReports() {
		Person currentPerson = securityService.loadCurrentPerson()
		List<Report> reportList = reportService.list( securityService.userCurrentProject, currentPerson )
		List<Map> responseList = reportList*.toMap( currentPerson.id )
		renderSuccessJson(responseList)
	}
}

