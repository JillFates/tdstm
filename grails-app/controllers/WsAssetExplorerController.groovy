/**
 * Created by David Ontiveros
 */

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ReportService
import net.transitionmanager.service.SecurityService

/**
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 * @see URLMappings.groovy
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	private final static DELETE_OK_STATUS = "Dataview deleted successfully";

	ReportService reportService
	SecurityService securityService

	/**
	 * Returns the list of available reports as a map(json) result.
	 * All reports returned belong to current user project in session.
	 * @return
	 */
    def listReports() {
		List<Map> listMap = reportService.list()*.toMap(securityService.currentPersonId)
		renderSuccessJson(listMap)
	}

	/**
	 * Returns an specific Dataview as a map(json) result.
	 * @param id to search by.
	 * @return
	 */
	def getReport(Integer id) {
		Map dataviewMap = reportService.fetch(id).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Updates an Asset Dataview for Asset Explorer.
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def updateReport(Integer id) {
		Map dataviewMap = reportService.update(id, request.JSON).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemCreate or AssetExplorerCreate
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def createReport() {
		Map dataviewMap = reportService.create(request.JSON).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Deletes an Asset Dataview for Asset Explorer
	 * The service method will check for permissions AssetExplorerSystemDelete or AssetExplorerDelete
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def deleteReport(Integer id) {
		reportService.delete(id)
		renderSuccessJson([status: DELETE_OK_STATUS] )
	}

}