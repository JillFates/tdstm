/**
 * Created by David Ontiveros
 */

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.SecurityService

/**
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 * @see UrlMappings
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	private final static DELETE_OK_STATUS = "Dataview deleted successfully";

	DataviewService dataviewService
	SecurityService securityService

	/**
	 * Returns the list of available dataviews as a map(json) result.
	 * All Dataviews returned belong to current user project in session.
	 * @return
	 */
    def listDataviews() {
		try {
			List<Map> listMap = dataviewService.list()*.toMap(securityService.currentPersonId)
			renderSuccessJson(listMap)
		} catch (Exception e) {
			handleException e, log
		}
	}

	/**
	 * Returns an specific Dataview as a map(json) result.
	 * @param id to search by.
	 * @return
	 */
	def getDataview(Integer id) {
		try {
			Map dataviewMap = dataviewService.fetch(id).toMap(securityService.currentPersonId)
			renderSuccessJson([dataView: dataviewMap])
		} catch (Exception e) {
			handleException e, log
		}
	}

	/**
	 * Updates an Asset Dataview for Asset Explorer.
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def updateDataview(Integer id) {
		try {
			Map dataviewMap = dataviewService.update(id, request.JSON).toMap(securityService.currentPersonId)
			renderSuccessJson([dataView: dataviewMap])
		} catch (Exception e) {
			handleException e, log
		}
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemCreate or AssetExplorerCreate
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def createDataview() {
		try {
			Map dataviewMap = dataviewService.create(request.JSON).toMap(securityService.currentPersonId)
			renderSuccessJson([dataView: dataviewMap])
		} catch (Exception e) {
			handleException e, log
		}
	}

	/**
	 * Deletes an Asset Dataview for Asset Explorer
	 * The service method will check for permissions AssetExplorerSystemDelete or AssetExplorerDelete
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def deleteDataview(Integer id) {
		try {
			dataviewService.delete(id)
			renderSuccessJson([status: DELETE_OK_STATUS] )
		} catch (Exception e) {
			handleException e, log
		}
	}

	/**
	 * Overrided handleExcpetion super class method in ControllerMethods class
	 * to send just json reponse errors if exception is thrown.
	 * @see ControllerMethods#handleException(java.lang.Exception, java.lang.Object)
	 * @param e
	 * @param log
	 */
	void handleException(Exception e, log) {
		log.error(e)
		renderErrorJson(e.message)
	}
}