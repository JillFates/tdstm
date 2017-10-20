import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.ApplicationService
import org.apache.commons.lang3.RandomStringUtils

/**
 * Handles WS calls of the ApplicationService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsApplicationController')
class WsAssetImportController implements ControllerMethods {

	ApiActionService apiActionService

	// mock data to use until methods are integrated with database
	static final List<Map> actions = [
			[ id: 10, name: 'Risk Network - Servers', provider: 'Risk Networks', defaultDataScriptId:1],
			[ id:  3, name: 'ServiceNow - Applications', provider: 'ServiceNow', defaultDataScriptId:2],
			[ id:  4, name: 'ServiceNow - Databases', provider: 'ServiceNow', defaultDataScriptId:3],
			[ id:  6, name: 'ServiceNow - Dependiencies', provider: 'ServiceNow', defaultDataScriptId:4],
			[ id:  5, name: 'ServiceNow - LogicalStorage', provider: 'ServiceNow', defaultDataScriptId:5],
			[ id:  1, name: 'ServiceNow - Servers (Linux)', provider: 'ServiceNow', defaultDataScriptId:6],
			[ id:  2, name: 'ServiceNow - Servers (Windows)', provider: 'ServiceNow', defaultDataScriptId:7],
			[ id:  7, name: 'TM - Applications (In scope)', provider: 'TransitionManager', defaultDataScriptId:8],
			[ id:  9, name: 'TM - Applications (Out of scope)', provider: 'TransitionManager', defaultDataScriptId:9],
	]

	static final List<Map> dataScripts = [
			[id:  1, name: 'Risk Network - Servers'],
			[id:  2, name: 'ServiceNow - Applications'],
			[id:  3, name: 'ServiceNow - Databases'],
			[id:  4, name: 'ServiceNow - Dependiencies'],
			[id:  5, name: 'ServiceNow - LogicalStorage'],
			[id:  6, name: 'ServiceNow - Servers (Linux)'],
			[id:  7, name: 'ServiceNow - Servers (Windows)'],
			[id:  8, name: 'TM - Applications (In scope)'],
			[id:  9, name: 'TM - Applications (Out of scope)']
	]

	/**
	 * Used to invoke the fetch job that will call remote system to pull data back to the server. This will return
	 * a map with status, errors and/or the filename of the data file pulled from remote system.
	 *
	 * For testing
	 * 		even ids - returns success
	 * 		odd ids  - returns error
	 * 		invalid ids - returns not found
	 *
	 * @param	actionId - the id of the action to be invoked
	 * @return	JSON Map containing the following
	 * 		status: <String> indicating success|error
	 * 		errors: <List> a list of error messages that occurred
	 * 		filename: <String> the name of the import file if successful
	 */
	@HasPermission(Permission.AssetImport)
	def invokeFetchAction(Long actionId) {


		Map result = [status:'success', errors:[], filename:'']

		// See if we can find an action to be invoked
		ApiAction action = ApiAction.get(actionId)
		if (!action) {
			sendNotFound("Action $actionId Not Found")
			return
		}

		// invoke action and eval the result
		Map actionInvocationResult = apiActionService.invoke(action)
		if (actionInvocationResult.status == 'error') {
			result.status = 'error'
			result.errors << actionInvocationResult.cause
		} else {
			result.filename = actionInvocationResult.filename
		}

		renderAsJson result
	}

	/**
	 * Used to invoke the process that will read in the results of the ETL transformation and load the data into the
	 * data ingestion batch import tables. This will create a batch for each asset class in the JSON data. The results
	 * of the import will be returned from the method.
	 * @param filename - the name of the temporary ETL output JSON file
	 * @return JSON map containing the following:
	 * 		status: <String> indicates results of process (success|error)
	 * 		errors: <List><String> list of the errors
	 * 		batchesCreated: <Integer> the number of batches created
	 */
	@HasPermission(Permission.AssetImport)
	def loadData(String filename) {
		if (!filename) {
			sendInvalidInput('Missing filename')
			return
		}

		if (! filename.endsWith('.json')) {
			sendInvalidInput('File must be JSON format')
			return
		}

		Map result = [status:'success', errors:[], batchesCreated:0]

		// See if we can find an action to be invoked
		result.batchesCreated=1

		renderAsJson result
	}

	/**
	 * Returns a collection of data lists including the actions and data scripts used to populate the form
	 * @param
	 * @return JSON map containing the following:
	 * 		actions: <List><Map>
	 * 			id: <Long> the id of the action
	 * 			name: <String> the name / label of the action
	 * 			provider: <String> the name of the provider associated with the action
	 * 			defaultDataScriptId: <Long> the DataScript that by default will be used to process the data from the action
	 * 		dataScripts: <List><Map>
	 * 			id: <Long> the id of the Data Script
	 * 			name: <String> the name / label of the data script
	 */
	@HasPermission(Permission.AssetImport)
	def manualFormOptions() {
		Map map = [
				actions: actions,
				dataScripts: dataScripts
		]

		renderAsJson map
	}

	/**
	 * Used to invoke an ETL process on the filename (from invokeFetch) passed in using the Data Script that was
	 * specified. It will return the status including counts, errors, and output filename.
	 *
	 * For testing
	 * 		even ids - returns success
	 * 		odd ids  - returns error
	 * 		invalid ids - returns not found
	 *
	 * @param id - the id of the Data Script that should be used to Transform the data
	 * @param filename - the name of the temporary file that was provided by the fetch action
	 * @return	JSON Map containing the following
	 * 		status <String> indicating success|error
	 * 		errors <List> a list of error messages that occurred
	 * 		filename <String> the name of the file that contains the transformed data as JSON if successful
	 * 		results <List><Map> list of the domains that were transformed along with other details
	 * 			[ 	domain: <String> Name of the domain
	 * 				rows: <Integer> number of rows in domain transformed
	 * 				errors: <Integer> number of rows with errors
	 * 			]
	 */
	@HasPermission(Permission.AssetImport)
	def transformData(Long dataScriptId, String filename) {
		Map result = [status:'success', errors:[], results: []]

		if (!dataScriptId) {
			sendInvalidInput 'Missing dataScriptId parameter'
			return
		}
		// See if we can find an action to be invoked
		Map script = dataScripts.find {it.id == dataScriptId}
		if (!script) {
			sendNotFound("DataScript $dataScriptId Not Found")
			return
		}

		if (!filename) {
			sendInvalidInput 'Missing filename parameter'
			return
		}

		if (! (filename.endsWith('.csv') || filename.endsWith('.xml'))) {
			sendInvalidInput 'File type was not CSV or XML'
			return
		}

		// For testing we'll return success for even ids otherwise error
		if (dataScriptId % 2) {
			result.status = 'error'
			result.errors << 'Action failed to connect to service'
		} else {
			result.filename = RandomStringUtils.randomAlphabetic(12) + '.json'
			result.results << [
					domain: 'Application',
					rows: 4,
					errors: 0
			]
		}

		renderAsJson result
	}

	/**
	 * Used to retrieve the raw data from the Fetch or Transform. This requires the name of the file that was generated.
	 * The content-type: will be returned with the request. Initially it will be for CSV or JSON. Future releases will
	 * return XML and Excel. The latter should not be rendered into the DIV but opened with Excel.
	 * @param
	 * @return
	 */
	@HasPermission(Permission.AssetImport)
	def viewData(String filename) {

		if (!filename) {
			sendInvalidInput('Missing filename')
			return
		}

		List<String> parts = filename.split(/\./)
		//render parts.toString()
		//return
		if (parts.size != 2) {
			sendNotFound()
			return
		}
		println "parts[1] = ${parts[1]}"
		switch (parts[1]) {
			case 'json':
				List json = [
						[externalRefId:'12', assetName:'Autonomy', environment:'Production', department:'HR'],
						[externalRefId:'13', assetName:'Citrix', environment:'Production', department:'HR'],
						[externalRefId:'342', assetName:'Corp Tax', environment:'Production', department:'HR'],
						[externalRefId:'343', assetName:'DART', environment:'Production', department:'HR']
				]
				renderAsJson json
				break

			case 'csv':
				setContentTypeCsv()
				render '''id,name,environment,department
12,'Autonomy','PROD','HR'
13,'Citrix','PROD','IT'
342,'Corp Tax','PROD,'FIN'
343,'DART','PROD','FIN'
'''
				break
			default:
				sendNotFound()
		}
	}

}
