/**
 * Created by David Ontiveros
 */

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ReportService

/**
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 * @see URLMappings.groovy
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	ReportService reportService

	/**
	 * Returns the list of available reports as a map(json) result.
	 * All reports returned belong to current user project in session.
	 * @return
	 */
    def listReports() {
		renderSuccessJson(reportService.list())
	}

	/**
	 * Returns an specific Dataview as a map(json) result.
	 * @param id to search by.
	 * @return
	 */
	def getReport(Integer id) {
		renderSuccessJson(reportService.fetch(id))
	}

	/**
	 * Updates an Asset Dataview for Asset Explorer.
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def updateReport(Integer id) {
		renderSuccessJson( [dataView: reportService.update(id, request.JSON)] )
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def createReport() {
		renderSuccessJson( [dataView: reportService.create(request.JSON)] )
	}

}