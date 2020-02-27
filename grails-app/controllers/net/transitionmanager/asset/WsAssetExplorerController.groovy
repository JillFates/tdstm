package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission

/**
 * Created by David Ontiveros
 */


import com.tdsops.common.ui.Pagination
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.dataview.DataviewNameValidationCommand
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.command.dataview.DataviewCrudCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.imports.Dataview
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.imports.DataviewService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService
import org.apache.commons.lang3.BooleanUtils
import org.grails.web.json.JSONObject

/**
 *
 * Asset Explorer main controller class that contains basic operation methods for exposed endpoints.
 *
 * @see UrlMappings
 */
@Secured('isAuthenticated()')
class WsAssetExplorerController implements ControllerMethods, PaginationMethods {

	private final static DELETE_OK_STATUS = "Dataview deleted successfully";

	DataviewService dataviewService
	UserPreferenceService userPreferenceService

	/**
	 * Returns the list of available dataviews as a map(json) result.
	 * All Dataviews returned belong to current user project in session.
     *
	 * @return
	 */
	@HasPermission(Permission.UserGeneralAccess)
    def listDataviews() {
		Person currentPerson = securityService.loadCurrentPerson()
		Project currentProject = securityService.userCurrentProject
		List<Dataview> dataviews = dataviewService.list(currentPerson, currentProject)
		List<Map> listMap = dataviews*.toMap(currentProject, currentPerson).sort{a, b -> a.name?.compareToIgnoreCase b.name}
		renderSuccessJson(listMap)
	}

	/**
	 * Returns an specific Dataview as a map(json) result.
	 * @param id - the id of the dataview to return
	 * @param _override - when present for the request for a system view that has an overridden version, this flag is
	 * used to indicate to return the underlying system view instead of the overridden version.
	 * @return the dataView specification and the saveOptions based on the user's permissions and the view being accessedhom
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def getDataview(Integer id) {
		Person whom = currentPerson()
		Project project = projectForWs

		Dataview dataview = dataviewService.fetch(project, whom, id, shouldShowOverridenView())
		Map saveOptions = dataviewService.generateSaveOptions(project, whom, dataview)
		Map dataviewMap = dataview.toMap( project, whom )

		renderSuccessJson([dataView: dataviewMap, saveOptions: saveOptions])
	}

	/**
	 * Used to retrieve the save and saveAs options for a given dataview or when creating a new dataview (no ID passed)
	 * @param id - the dataview ID get the save options for or null to get the save options for a new dataview
	 * @return JSON structure for saveOptions
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def saveOptions(Integer id) {
		Dataview dataview
		Person whom = currentPerson()
		Project project = projectForWs

		if (id) {
			dataview = dataviewService.fetch(project, whom, id, shouldShowOverridenView())
		}

		renderAsJson( saveOptions: dataviewService.generateSaveOptions(project, whom, dataview) )
	}

	/**
	 * Used to determine if the getDataview request stipulated to NOT override the view, hence return the default system view
	 * The _override=0 parameter indicates that when referencing a system view that has been overridden, that the
	 * actual system view should be returned instead.
	 * @return true if the _override parameter is not present
	 */
	private Boolean shouldShowOverridenView() {
		final String overrideParamName = '_override'
		// The _override parameter indicates that when referencing an overridden system view, that the overridden view
		// should be the one to return
		boolean override = true

		if ( params.containsKey(overrideParamName) ) {
			override = BooleanUtils.toBoolean( params[overrideParamName] )
		}

		return override
	}

	/**
	 * Updates an Asset Dataview for Asset Explorer.
	 * The service method will check for permissions AssetExplorerSystemEdit or AssetExplorerEdit
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@HasPermission(Permission.AssetExplorerEdit)
	def updateDataview(Integer id) {
		DataviewCrudCommand command = populateCommandObject(DataviewCrudCommand)
		validateCommandObject(command)
		Project project = projectForWs
		Person whom = currentPerson()
		Map dataviewMap = dataviewService.update(project, whom, id, command).toMap(project, whom)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Create an Asset Dataview for Asset Explorer ('Save As' action on Asset Explorer)
	 * The service method will check for permissions AssetExplorerSystemCreate or AssetExplorerCreate
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@HasPermission(Permission.AssetExplorerCreate)
	def createDataview() {
		DataviewCrudCommand command = populateCommandObject(DataviewCrudCommand)
		validateCommandObject(command)
		Project project = projectForWs
		Person whom = currentPerson()
		Map dataviewMap = dataviewService.create(project, whom, command).toMap(project, whom)
		renderSuccessJson([dataView: dataviewMap])
	}

	/**
	 * Deletes an Asset Dataview for Asset Explorer
	 * The service method will check for permissions AssetExplorerSystemDelete or AssetExplorerDelete
	 * appropriately.
	 * @return status:200 json{ "status": "success"/"fail", "data": "dataview:Object"}
	 */
	@HasPermission(Permission.AssetExplorerDelete)
	def deleteDataview(Integer id) {
		dataviewService.delete(projectForWs, currentPerson(), id)
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
	@HasPermission(Permission.UserGeneralAccess)
    def query(Long id, DataviewUserParamsCommand userParams) {

		if (userParams.hasErrors()) {
			renderErrorJson('User filtering was invalid')
			return
		}

		Project project = projectForWs

		// TODO : JPM 02/20202 : The query logic should validate the user is accessing a vaild dataview (what about overrides too?)
		Dataview dataview = Dataview.get(id)
		if (!dataview) {
			renderErrorJson('Dataview invalid')
			return
		}

        if (!userParams.forExport) {
            Integer limit = Pagination.maxRowForParam(userParams.limit as String)
            userParams.limit = limit
            userPreferenceService.setPreference(UserPreferenceEnum.ASSET_LIST_SIZE, limit)
        }

		Map queryResult = dataviewService.previewQuery(project, dataview, userParams)

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
	@HasPermission(Permission.UserGeneralAccess)
    def previewQuery(DataviewUserParamsCommand userParams) {

        if (userParams.validate()){
            Project project = securityService.userCurrentProject

            Map previewQuery = dataviewService.previewQuery(project, null, userParams)
            renderSuccessJson(previewQuery)
        } else {
            renderErrorJson("Incorrect json data request")
        }
    }

	/**
	 * Retrieve the list of favorite views for the current user.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def favoriteDataviews() {
		Person person = securityService.getUserLoginPerson()
		def favorites = dataviewService.getFavorites(person)
		renderSuccessJson(favorites)
	}

	/**
	 * Favorite the given view.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def addFavoriteDataview(Long id) {
		dataviewService.addFavoriteDataview(projectForWs, currentPerson(), id)
		renderSuccessJson("Dataview ${id} favorited")
	}

	/**
	 * Delete the view from the person's favorite.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def deleteFavoriteDataview(Long id) {
		dataviewService.deleteFavoriteDataview(projectForWs, currentPerson(), id)
		renderSuccessJson("Dataview ${id} removed from the person's favorites.")
	}

	/**
	 * Endpoint to query if a given name is unique across project. Along with the
	 * name to look up, this method also expects:
	 *
	 * - dataViewId.
	 *
	 * The latter is to contemplate the scenario where the user is editing a DataView and this
	 * endpoint is invoked. If the name hasn't changed, it would report the name as not unique.
	 * The parameters are encapsulated inside the DataviewNameValidationCommand.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def validateUniqueName () {
		DataviewNameValidationCommand cmd = populateCommandObject(DataviewNameValidationCommand)
		boolean isUnique = dataviewService.validateUniqueName(cmd)
		renderSuccessJson([isUnique: isUnique])
	}
}
