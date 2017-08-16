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
	 * Create report operation for Asset Explorer endpoint ('Save As' action on Asset Explorer).
	 * @return * @return status:300 json{ "status": "success"/"fail", "data": "OK"/"Error Message"}.
	 */
	def createReport() {
		JSONObject reportJSON = request.JSON
		// permission validation
		if (validateReportCreateAccess(reportJSON)) {
			try {
				reportService.create(reportJSON)
				renderSuccessJson('OK')
			} catch (Exception e) {
				renderFailureJson(e.getMessage())
			}
		}
		else { // If not found, send fail result.
			renderFailureJson("Unauthorized access")
		}
	}


	/**
	 * Validates if person accessing report is authorized to access it.
	 * - should belong to current project in session
	 * - should be either system or shared or current person in session owned
	 * @param report
	 * @return
	 */
	boolean validateReportViewAccess(Report report) {
		return report.project.id == securityService.userCurrentProject.id && (report.isSystem || report.isShared || report.person.id == securityService.currentPersonId)
	}

	/**
	 * Validates if person updating a report has permission to do it.
	 * @param report
	 * @return
	 */
	boolean validateReportUpdateAccess(Report report, JSONObject reportJSON) {
		boolean valid = report.project.id == securityService.userCurrentProject.id
		// system report validation
		if (valid && report.isSystem) {
			valid = securityService.hasPermission(Permission.AssetExplorerSystemEdit)
		} else if (valid && report.person.id == securityService.currentPersonId) { // owned report validation
			valid = securityService.hasPermission(Permission.AssetExplorerEdit)
		}
		// TODO: should we prevent editing other user reports ??

		return valid
	}

	/**
	 * Validates if person creating a report has permission to do it.
	 * @param report
	 * @return
	 */
	boolean validateReportCreateAccess(JSONObject reportJSON) {
		boolean valid = true
		// system report validation
		if (valid && reportJSON.isSystem) {
			valid = securityService.hasPermission(Permission.AssetExplorerSystemCreate)
		} else if (valid) { // owned report validation
			valid = securityService.hasPermission(Permission.AssetExplorerCreate)
		}

		return valid
	}

	/**
	 * Validates if person deleting a report has permission to do it.
	 * @param report
	 * @return
	 */
	boolean validateReportDeleteAccess(Report report) {
		boolean valid = report.project.id == securityService.userCurrentProject.id
		// system report validation
		if (valid && report.isSystem) {
			valid = securityService.hasPermission(Permission.AssetExplorerSystemDelete)
		} else if (valid) { // owned report validation
			valid = securityService.hasPermission(Permission.AssetExplorerDelete)
		}

		return valid
	}
}

