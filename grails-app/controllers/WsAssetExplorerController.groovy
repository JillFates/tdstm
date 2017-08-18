/**
 * Created by David Ontiveros
 */

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Report
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ReportService
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.web.json.JSONObject

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

	/**
	 * Returns an specific report as a map(json) result.
	 * @param id to search by.
	 * @return
	 */
	def getReport(Integer id) {
		Report report = reportService.fetch(id)
		if (report) {
			// Validate that report actually belongs to current project and is either (system, shared or current user owned).
			if (validateReportViewAccess(report)) {
				renderSuccessJson(report.toMap(securityService.currentPersonId))
			}
			else { // otherwise send unauthorized fail result.
				renderFailureJson("Unauthorized access to $id")
			}
		} else { // If not found, send fail result.
			renderFailureJson("$id not found")
		}
	}

	/**
	 * Endpoint update report operation for Asset Explorer.
	 * @param id
	 * @return status:300 json{ "status": "success"/"fail", "data": "OK"/"Error Message"}.
	 */
	def updateReport(Integer id) {
		Report report = Report.get(id)
		if (report) {
			JSONObject reportJSON = request.JSON
			// permission validation
			if (validateReportUpdateAccess(report, reportJSON)) {
				try {
					reportService.update(report, reportJSON)
					renderSuccessJson('OK')
				} catch (Exception e) {
					renderFailureJson(e.getMessage())
				}
			}
			else { // otherwise send unauthorized fail result.
				renderFailureJson("Unauthorized access to $id")
			}
		} else { // If not found, send fail result.
			renderFailureJson("$id not found")
		}
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def createReport() {
		renderSuccessJson( [dataView: reportService.create(request.JSON).toMap()] )
	}

}