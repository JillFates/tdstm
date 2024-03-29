package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.StopWatch
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.action.ApiAction
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.command.InitiateTransformDataActionCommand
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import org.apache.commons.io.FilenameUtils

/**
 * Handles WS calls of the ApplicationService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='log', category='grails.app.controllers.WsApplicationController')
class WsAssetImportController implements ControllerMethods {

	ApiActionService apiActionService
	DataImportService dataImportService
	FileSystemService fileSystemService

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
	def invokeFetchAction(Long id) {
		ApiAction action = fetchDomain(ApiAction, params)

		// Invoke action and eval the result
		ApiActionResponse actionInvocationResult = apiActionService.invoke(action)

		if (actionInvocationResult.successful) {
			renderSuccessJson( [ filename: actionInvocationResult.filename ] )
		} else {
			renderErrorJson(actionInvocationResult.error)
		}
	}

	/**
	 * Used to invoke the process that will read in the results of the ETL transformation and load the data into the
	 * data ingestion batch import tables. This will create a batch for each asset class in the JSON data. The results
	 * of the import will be returned from the method.
	 *
	 * @param filename - the name of the temporary ETL output JSON file
	 * @return JSON map containing the following:
	 * 		progressKey: <String> progress key used to retrieve progress of
	 * 			{@link DataImportService#scheduleETLTransformDataJob(net.transitionmanager.project.Project, java.lang.Long, java.lang.String, java.lang.Boolean)},
	 * 		jobTriggerName: <String> Trigger name of the executed.
	 */
	@HasPermission(Permission.AssetImport)
	def loadData(String filename) {
		Project project = getProjectForWs()
		Person person = currentPerson()

		if (!filename) {
			throw new InvalidParamException('Missing filename')
		}

		if (! filename.endsWith('.json')) {
			throw new InvalidParamException('File must be JSON format')
		}

		Map result = dataImportService.scheduleImportDataJob(project, person.userLogin, filename)
		renderSuccessJson(result)
	}

	/**
	 * Used to invoke an ETL process on the filename (from invokeFetch) passed in using the DataScript that was
	 * specified. It will return the status including counts, errors, and output filename.
	 *
	 * For testing
	 * 		even ids - returns success
	 * 		odd ids  - returns error
	 * 		invalid ids - returns not found
	 *
	 * @param id - the id of the DataScript that should be used to Transform the data
	 * @param filename - the name of the temporary file that was provided by the fetch action
	 * @param sendResultsByEmail - Defines if an auto import process results should be sent by email
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
	def initiateTransformData(){
		def stopwatch = new StopWatch()
		stopwatch.start()
		InitiateTransformDataActionCommand actionCommand  = populateCommandObject(InitiateTransformDataActionCommand)

		Project project = getProjectForWs()
		Map result = dataImportService.scheduleETLTransformDataJob(
			project,
			actionCommand.dataScriptId,
			actionCommand.filename,
			actionCommand.sendNotification
		)
		renderSuccessJson(result)
		log.info 'transformData() ETL Transformation took {}', stopwatch.endDuration()
	}

	/**
	 * Returns a collection of data lists including the actions and datascripts used to populate the form
	 * @param
	 * @return JSON map containing the following:
	 * 		actions: <List><Map>
	 * 			id: <Long> the id of the action
	 * 			name: <String> the name / label of the action
	 * 			provider: <String> the name of the provider associated with the action
	 * 			defaultDataScriptId: <Long> the DataScript that by default will be used to process the data from the action
	 * 		dataScripts: <List><Map>
	 * 			id: <Long> the id of the DataScript
	 * 			name: <String> the name / label of the datascript
	 */
	@HasPermission(Permission.AssetImport)
	def manualFormOptions() {
		Project project = getProjectForWs()

		Map results = [ : ]
		List<Map> actions = []

		def where = ApiAction.where { project == project && producesData == 1}.readOnly(true)
				/*
				.projections {
					property 'id'
					property 'name'
					provider {
						property 'provider.name'
					}
				}
				*/

		where.list().each() {
			actions << [
					id: it.id,
					name: "${it.provider.name} - ${it.name}",
					provider:it.provider.name,
					defaultDataScriptId: (it.defaultDataScript?.id ?: 0)
			]
		}
		actions = actions.sort { it.name }
		results.actions = actions


		List<Map> dataScripts = []
		where = DataScript.where { project == project }.readOnly(true)
		where.list().each() {
			dataScripts << [
					id:it.id,
					name: "${it.provider.name} - ${it.name}",
					isAutoProcess: it.isAutoProcess
			]
		}
		dataScripts = dataScripts.sort { it.name }
		results.dataScripts = dataScripts

		renderAsJson results
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

		if (! fileSystemService.temporaryFileExists(filename)) {
			sendNotFound()
			return
		}

		InputStream is = fileSystemService.openTemporaryFile(filename)
		if (!is) {
			log.warn 'viewData() attempted to output file but InputStream was null for {}', filename
			sendNotFound()
			return
		}

		if(FilenameUtils.getExtension(filename) == 'json') { // TODO: Add support for Excel/CSV file JSON responses in WS.
			renderSuccessJson(JsonUtil.parseInputStream(is))
		} else {
			renderErrorJson("Error: Preview must be in JSON format.")
		}
		is.close()

	}
}
