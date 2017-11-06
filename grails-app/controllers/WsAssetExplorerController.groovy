/**
 * Created by David Ontiveros
 */

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.FavoriteDataview
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.dataview.DataviewSpec

/**
 *
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 *
 * @see UrlMappings
 */
@Secured('isAuthenticated()')
// TODO: John. Can we remove this logger since we aren't using in this class? Also we have an implementation in ControllerMethods.handleException
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

	private final static DELETE_OK_STATUS = "Dataview deleted successfully";

	DataviewService dataviewService
	SecurityService securityService
	UserPreferenceService userPreferenceService

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
     *	    "justPlanning": true,
     *		"filters: {
     *			"columns: [
     *				{"domain": "common", "property": "environment", "filter": "production|development" },
     *				{"domain": "common", "property": "assetName", "filter": "exchange" }
     *			],
     *		}
     *	}
     *
     * Response:
     *	[
     *		data:[
     *		    [
     *		    	common.id: 12,
     *		    	common.name: 'Exchange',
     *	    		common.class: 'Application',
     *	    		common.bundle: 'M1',
     *	    		application.sme: 'Joe',
     *	    		application.owner: 'Tony'
     *		    ],
     *		    [
     *			    common.id: 23,
     *		    	common.name: 'VM123',
     *		    	common.class: 'Device',
     *		    	common.bundle: 'M1',
     *		    	device.os: 'Windows'
     *		    	device.serial: '123123123',
     *		    	device.tag: 'TM-234'
     *		    ]
     *	    ],
     * 		   pagination: [
     * 		        offset: 350,
     * 		        max: 100,
     * 		        total: 472
     * 		   ]
     *	]
     *
     */
    @Secured('isAuthenticated()')
    def query(Long id, DataviewUserParamsCommand userParams) {
        Project project = securityService.userCurrentProject

		if (userParams.hasErrors()) {
			renderErrorJson('User filtering was invalid')
			return
		}

		Dataview dataview = Dataview.get(id)
		if (!dataview) {
			renderErrorJson('Dataview invalid')
			return
		}

		Map queryResult = dataviewService.query(project, dataview, userParams)
		userPreferenceService.setPreference(UserPreferenceEnum.ASSET_LIST_SIZE, userParams.limit)

        renderSuccessJson(queryResult)
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
	 *		"justPlanning": true,
     *		"filters": {
     *			"domains": [ "application", "device" ],
     *			"columns": [
     *						{ "domain": "common", "property": "environment", "filter": "production" },
     *						{ "domain": "application", "property": "bundle", "filter": "" }
     *			]
     *		}
     *	}
     *
     * Response:
     * 	  @see query
     */
    @Secured('isAuthenticated()')
    def previewQuery(DataviewUserParamsCommand userParams) {

        if(userParams.validate()){
            Project project = securityService.userCurrentProject

            DataviewSpec dataviewSpec = new DataviewSpec(userParams)

            Map previewQuery = dataviewService.previewQuery(project, dataviewSpec)
            renderSuccessJson(previewQuery)
        } else {
            renderErrorJson("Incorrect json data request")
        }
    }

	/**
	 * Retrieve the list of favorite views for the current user.
	 */
	@Secured("isAuthenticated()")
	def favoriteDataviews() {
		try{
			def favorites = dataviewService.getFavorites()
			renderSuccessJson(favorites)
		} catch (Exception e) {
			renderErrorJson(e.getMessage())
		}

	}

	/**
	 * Favorite the given view.
	 */
	@Secured("isAuthenticated()")
	def addFavoriteDataview(Long id) {
		try{
			dataviewService.addFavoriteDataview(id)
			renderSuccessJson("Dataview ${id} favorited")
		} catch (Exception e) {
			renderErrorJson(e.getMessage())
		}

	}

	/**
	 * Delete the view from the person's favorite.
	 */
	@Secured("isAuthenticated()")
	def deleteFavoriteDataview(Long id) {
		try{
			dataviewService.deleteFavoriteDataview(id)
			renderSuccessJson("Dataview ${id} removed from the person's favorites.")
		} catch (Exception e) {
			renderErrorJson(e.getMessage())
		}

	}
}