import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import net.transitionmanager.service.FileSystemService
import org.apache.commons.lang3.RandomStringUtils
import org.hibernate.transform.Transformers

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
	ScriptProcessorService scriptProcessorService

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
		ApiAction action = ApiAction.read(actionId)
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
		Project project = getProjectForWs()
		Person person = currentPerson()

		if (!filename) {
			sendInvalidInput('Missing filename')
			return
		}

		if (! filename.endsWith('.json')) {
			sendInvalidInput('File must be JSON format')
			return
		}

		InputStream inputStream = fileSystemService.openTemporaryFile(filename)
		if (!inputStream) {
			sendInvalidInput 'Specified input file not found'
			return
		}


		Map importResults = dataImportService.loadJsonIntoImportBatch(person.userLogin, project, inputStream)

		inputStream.close()

		Map result = [status:'success', errors:[], batchesCreated: importResults.batchesCreated]

		renderAsJson result
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
		Project project = getProjectForWs()

		Map result = [status:'success', errors:[], results: []]

		if (!dataScriptId) {
			sendInvalidInput 'Missing dataScriptId parameter'
			return
		}
		// See if we can find an action to be invoked
		DataScript dataScript = DataScript.read(dataScriptId)
		if (!dataScript) {
			sendNotFound("DataScript $dataScriptId Not Found")
			return
		}

		if (! dataScript.etlSourceCode) {
			result.status='error'
			result.errors << 'DataScript has no source specified'
			return result
		}

		if (!filename) {
			sendInvalidInput 'Missing filename parameter'
			return
		}

		if (! (filename.endsWith('.csv') || filename.endsWith('.xml'))) {
			sendInvalidInput 'File type was not CSV or XML'
			return
		}

		if (! fileSystemService.temporaryFileExists(filename)) {
			sendInvalidInput 'Specified input file not found'
		}

		String inputFilename = fileSystemService.getTemporaryFullFilename(filename)

		Map etlResults = scriptProcessorService.execute(project, dataScript.etlSourceCode, inputFilename)

		def (String outputFilename, OutputStream os) = fileSystemService.createTemporaryFile('import-','json')

		result.filename = outputFilename

		try {
			os << (etlResults as JSON)
			os.close()
		}
		catch(e) {
			result.success='error'
			result.errors << 'Unable to write output file'
			log.error 'transformData() failed to write output logfile : {}', e.getMessage()
		}

		renderAsJson result
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

		Map map = [ : ]
		List<Map> tmpList = []

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
			tmpList << [
					id: it.id,
					name: "${it.provider.name} - ${it.name}",
					provider:it.provider.name,
					defaultDataScriptId: (it.defaultDataScript?.id ?: 0)
			]
		}
		tmpList = tmpList.sort { it.name }
		map.actions = tmpList


		tmpList = []
		where = DataScript.where { project == project }.readOnly(true)
		where.list(order:'name').each() {
			tmpList << [ id:it.id, name: "${it.provider.name} - ${it.name}" ]
		}

		map.dataScripts = tmpList

		renderAsJson map
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

		response.outputStream << is
		is.close()

	}
}
