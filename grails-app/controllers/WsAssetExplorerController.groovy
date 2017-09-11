/**
 * Created by David Ontiveros
 */


import com.tds.asset.AssetEntity
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.command.PaginationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.dataview.DataviewSpec

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
		List<Map> listMap = dataviewService.list()*.toMap(securityService.currentPersonId)
		renderSuccessJson(listMap)
	}

	/**
	 * Returns an specific Dataview as a map(json) result.
	 * @param id to search by.
	 * @return
	 */
	def getDataview(Integer id) {
		Map dataviewMap = dataviewService.fetch(id).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Updates an Asset Dataview for Asset Explorer.
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def updateDataview(Integer id) {
		Map dataviewMap = dataviewService.update(id, request.JSON).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemCreate or AssetExplorerCreate
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def createDataview() {
		Map dataviewMap = dataviewService.create(request.JSON).toMap(securityService.currentPersonId)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Deletes an Asset Dataview for Asset Explorer
	 * The service method will check for permissions AssetExplorerSystemDelete or AssetExplorerDelete
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@Secured('isAuthenticated()')
	def deleteDataview(Integer id) {
		dataviewService.delete(id)
		renderSuccessJson([status: DELETE_OK_STATUS] )
	}

	/**
	 * Performs a query for the Asset Explorer data grid using a saved View Specification plus
	 * filter parameters that the user may have entered plus their preferences for the view.
	 * @params id - the reference id to the persisted dataView (URI)
	 * @param pagination - the pagination parameters (offset, limit) (JSON)
	 * @param userParams - the filter and sorting that the user has control of when using the view (JSON)
	 * @return A JSON List containing maps of each asset's properties (JSON)
	 *
	 * Request:
	 * 	POST data = {
	 *		"offset":5,
	 *		"limit": 25,
	 *		"sortDomain: "application",
	 *		"sortField: "sme",
	 *		"sortOrder: "a",
	 *		"filters: {
	 *			"columns: [
	 *				{"domain": "common", "property": "environment", "filter": "production|development" },
	 *				{"domain": "common", "property": "assetName", "filter": "exchange" },
	 *			],
	 *		}
	 *	}
	 *
	 * Response:
	 *	[
	 *		[
	 *			common.id: 12,
	 *			common.name: 'Exchange',
	 *			common.class: 'Application',
	 *			common.bundle: 'M1',
	 *			application.sme: 'Joe',
	 *			application.owner: 'Tony'
	 *		],
	 *		[
	 *			common.id: 23,
	 *			common.name: 'VM123',
	 *			common.class: 'Device',
	 *			common.bundle: 'M1',
	 *			device.os: 'Windows'
	 *			device.serial: '123123123',
	 *			device.tag: 'TM-234'
	 *		]
	 *	]
	 *
	 */
	@Secured('isAuthenticated()')
	def query(Long id, DataviewUserParamsCommand userParams, PaginationCommand pagination) {
		Project project = securityService.userCurrentProject

		if (userParams.hasErrors()) {
			renderErrorJson('User filtering was invalid')
			return
		}

		// Eventually we'll incorporate the Users' override preferences on the dataview
		// DataviewUserPreference userPref
		// List<Map> data = dataviewService.query(project, id, userPref, userParams, pagination)

		List<Map> data = dataviewService.query(project, AssetEntity, id, userParams, pagination)
		renderSuccessJson(data)
	}

	/**
	 * Similar to query, the previewQuery method performs a query for the Asset Explorer data grid
	 * using a View Specification passed in to the query. There would be no user preferences because
	 * the view doesn't exist yet.
	 * @param pagination - the pagination parameters (offset, limit) (JSON)
	 * @return A JSON List containing maps of each asset's properties (JSON)
	 *
	 * Request:
	 * 	URI: /tdstm/ws/assetExplorer/previewQuery
	 *	data = {
	 *		"offset": 5,
	 *		"limit": 25,
	 *		"sortDomain": "application",
	 *		"sortField": "bundle',
	 *		"sortOrder": "a",
	 *		"filters": {
	 *			"domains": [ "application", "device" ],
	 *			"columns": [
	 *						{ "domain": "common", "property": "environment", "filter": "production" },
	 *						{ "domain": "application", "property": "bundle", "filter": "" }
	 *			],
	 *		}
	 *	}
	 *
	 * Response:
	 * 	  @see query
	 */
    @Secured('isAuthenticated()')
    def previewQuery(DataviewUserParamsCommand userParams, PaginationCommand pagination) {

        if(userParams.validate()){
            Project project = securityService.userCurrentProject

			DataviewSpec dataviewSpec = new DataviewSpec(userParams)
            List<Map> data = dataviewService.previewQuery(project, dataviewSpec)
            renderSuccessJson(data)

        } else {
            renderSuccessJson([status: "Incorrect json data request"])
        }
    }

}