import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.service.ReportService
import net.transitionmanager.service.SecurityService

/**
 * Created by dontiveros
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	ReportService reportService
	SecurityService securityService

    def listReports() {
		Person currentPerson = securityService.loadCurrentPerson()
		renderSuccessJson(reportService.list(securityService.userCurrentProject, currentPerson)*.toMap(currentPerson.id))
	}
}

