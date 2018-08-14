package net.transitionmanager.service

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.etl.DataImportHelper
import com.tdsops.etl.DomainClassQueryHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.etl.ProgressCallback
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.FileSystemUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StopWatch
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.transaction.annotation.Propagation

/**
 * DataImportService - contains the methods for dealing with data importing from the ETL process;
 * interacting with the Import Batch management; and posting of data into various domains.
 *
 * The service class is setup for specialized Transactional management. By default all private methods
 * will use the PROPAGATION_MANDATORY, assuming the transaction created by the public methods. The
 * loadETLJsonIntoImportBatch() method in particular creates Transactions at the individual batch level
 * to use transactions more efficiently.
 *
 */
@Slf4j(value='log', category='net.transitionmanager.service.DataImportService')
@Transactional(propagation=Propagation.MANDATORY)
class DataImportService implements ServiceMethods {

	// IOC
	FileSystemService fileSystemService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProgressService progressService
	Scheduler quartzScheduler
	ScriptProcessorService scriptProcessorService

	// TODO : JPM 3/2018 : Move these strings to messages.properties
	static final String SEARCH_BY_ID_NOT_FOUND_MSG = 'Record not found searching by id'
	static final String NO_FIND_QUERY_SPECIFIED_MSG = 'No find/findElse specified for property'
	static final String WHEN_NOT_FOUND_PROPER_USE_MSG = "whenNotFound create only applicable for reference properties"
	static final String FIND_FOUND_MULTIPLE_REFERENCES_MSG = 'Multiple records found for find/elseFind criteria'
	static final String ALTERNATE_LOOKUP_FOUND_MULTIPLE_MSG = 'Multiple records found with current value'

	// TODO : JPM 4/2018 : Augusto - Get these to work first
	static final String PROPERTY_NAME_CANNOT_BE_SET_MSG = "Field {propertyName} can not be set by 'whenNotFound create' statement"
	static final String PROPERTY_NAME_NOT_IN_FIELDS = "Field {propertyName} was not found in ETL dataset"
	static final String PROPERTY_NAME_NOT_IN_DOMAIN = "Invalid field {propertyName} in domain"

	static final Integer NOT_FOUND_BY_ID = -1
	static final Integer FOUND_MULTIPLE = -2

	static final List<String> PROPERTIES_THAT_CANNOT_BE_MODIFIED = [
		'version', 'assetClass', 'createdBy', 'updatedBy', 'project', 'dateCreated', 'lastUpdated'
	]

	// A map of exceptions that the Import Field Process logic uses to deal with exceptions
	//    ignore - fields may appear in ETL import but not directly updated (e.g. Room Locations)
	static final Map DOMAIN_FIELD_EXCEPTIONS = [
		'AssetEntity': [
			'locationSource': [ ignore:true ],
			'locationTarget': [ ignore:true ],
		]
	]

	/**
	 * loadETLJsonIntoImportBatch - the entry point for the initial loading of data into the Import batches.
	 *
	 * This method will create a ImportBatch for each domain present in the importJsonData and then
	 * create ImportBatchRecords for each row of the import JSON meta-data.
	 *
	 * The method is marked NonTransactional so that each the domains in the importJsonData can be handled
	 * as independent transactions.
	 *
	 * @param userLogin
	 * @param project
	 * @param importJsonData
	 * @return a Map containing details about the import creation
	 *      batchesCreated <Integer> - count of the number of batches created
	 *      domains <List><Map> - a list of each domain that records were created
	 *          domainClass <String> - name of the domain
	 *          rowsCreated <Integer> - the count of the records created
	 *			rowsSkipped <Integer> - the count of rows that were skipped due to errors
	 */
	@NotTransactional()
	Map loadETLJsonIntoImportBatch(Project project, UserLogin userLogin, JSONObject importJsonData) {
		// 	return localFunction(project, userLogin, importJsonData)
		// }

		// // TODO : JPM 2/2018 : Delete this closure declaration and the following code will just be part of the above function
		// // This was done because it allows you to save the code repeatedly without having to restart the application every time. It appears
		// // that the @NotTransactional annotation causes issues. This was a neat trick to get around that since the loadETLJsonIntoImportBatch
		// // method wasn't being changed. Groovy is more forgiving with Closures...
		// def localFunction = { Project project, UserLogin userLogin, JSONObject importJsonData ->

		// Map which summarizes the results from the import process.
		Map importResults = [ batchesCreated: 0, domains:[], errors: [] ]

		// A map that contains various objects used throughout the import process
		Map importContext = [
			project: project,
			userLogin: userLogin,
			etlInfo: importJsonData.ETLInfo,

			// The cache will be used to hold on to domain entity references when found or a String if there
			// was an error when looking up the domain object. The key will be the md5hex of the query element
			// of the field.
			cache: new DataImportEntityCache(),

			// The following are reset per domain
			domainClass: null,
			fieldNames: [],
			rowsCreated: 0,
			rowsSkipped: 0,
			rowNumber: 0,
			errors:[]
		]

		// Iterate over the domains and create batches for each
		for (domainJson in importJsonData.domains) {
			importContext.domainClass = domainJson.domain

			log.debug "localFunction() in for loop: importContext=$importContext"

			List<JSONObject> importRows = domainJson.data
			if (! importRows) {
				importResults.errors << "Domain ${importContext.domainClass} contained no data"
				importResults.domains << [ domainClass: importContext.domainClass, rowsCreated: 0, rowsSkipped: 0 ]
			} else {

				// Process each batch in a separate transaction to help with performance and memory
				ImportBatch.withNewTransaction { session ->
					// Attach the project to the current transaction
					// project.attach()

					// Reset the batch level context variables
					importContext.with {
						errors = []
						rowsCreated = 0
						rowsSkipped = 0
						rowNumber = 0
					}
					importContext.fieldNames = domainJson.fieldNames
					importContext.fieldLabelMap = domainJson.fieldLabelMap

					// Create a Transfer Batch for the asset class
					ImportBatch batch = createImportBatch(importContext)

					// Proceed with the import if the dtb is not null (if it is, the errors were already reported and added to the processErrors list).
					if (batch == null) {
						// Creating the batch failed so record the error and metrics for endpoint consumer
						importResults.errors << importContext.errors
						importResults.domains << [ domainClass: importContext.domainClass, rowsCreated: 0 ]

					} else {
						//
						// Process the rows for the batch
						//

						// Import the assets for this batch
						importRowsIntoBatch(session, batch, importRows, importContext)

						// Update the batch with information about the import results
						batch.importResults = DataImportHelper.createBatchResultsReport(importContext)

						// Update the reporting
						importResults.batchesCreated++
						importResults.domains << [ domainClass: importContext.domainClass, batchId: batch.id, rowsCreated: importContext.rowsCreated ]
					}
				}
			}
		}

		return importResults
	}

	/**
	 * NEW ETL - Create a ImportBatch record for a given Domain Class
	 * @param domainClass
	 * @param currentUser
	 * @param project
	 * @return a newly created ImportBatch object
	 */
	//@CompileStatic
	private ImportBatch createImportBatch( Map importContext ) {

		Date warnOnChangesAfter
		if (importContext.etlInfo.warnOnChangesAfter) {
			warnOnChangesAfter = new Date(importContext.etlInfo.warnOnChangesAfter)
		} else {
			warnOnChangesAfter = new Date()
		}

		DataScript dataScript = GormUtil.findInProject(importContext.project, DataScript, importContext.etlInfo.dataScriptId)

		ImportBatch batch = new ImportBatch(
			project: importContext.project,
			status: ImportBatchStatusEnum.PENDING,
			dataScript: dataScript,
			provider: dataScript?.provider,
			domainClassName: importContext.domainClass,
			createdBy: importContext.userLogin.person,
			autoProcess: ( importContext.etlInfo.autoProcess ?: 0 ),
			dateFormat: ( importContext.etlInfo.dataFormat ?: ''),
			fieldNameList: JsonUtil.toJson(importContext.fieldNames),
			fieldLabelMap: JsonUtil.toJson(importContext.fieldLabelMap),
			nullIndicator: (importContext.etlInfo.nullIndicator ?: ''),
			originalFilename: (importContext.etlInfo.originalFilename ?: ''),
			overwriteWithBlanks: (importContext.etlInfo.overwriteWithBlanks ?: 1),
			timezone: ( importContext.etlInfo.timezone ?: 'GMT' ),
			warnOnChangesAfter: warnOnChangesAfter
		)

		// Check if the transfer batch is valid, report the error if not.
		if (!batch.save(failOnError:false)) {
			importContext.errors << "There was an error when creating the import batch for ${importContext.domainClass}"
			log.error 'DataImportService.createImportBatch() failed save: {}', GormUtil.allErrorsString(batch)
			batch.discard()

			return null
		}

		return batch
	}

	/**
	 * Import all the assets for the given batch.
	 *
	 * @param batch - current batch
	 * @param assets - list of assets
	 * @param importContext - additional parameters required for logging
	 */
	//@CompileStatic
	private void importRowsIntoBatch(session, ImportBatch batch, List<JSONObject> importRows, Map importContext) {
		for (rowData in importRows) {
			// Keep track of the row number for reporting
			importContext.rowNumber++

			// Do some initialization of the rowData object if necessary
			if (! rowData.containsKey('errors')) {
				rowData.errors = []
			}

			// Process the fields for this row
			importRow(session, batch, rowData, importContext)
		}
	}

	/**
	 * Import an individual row of data
	 *
	 * @param batch
	 * @param asset - LazyMap with all the field information
	 * @param importContext - additional parameters required for logging
	 */
	// @CompileStatic
	private void importRow(session, ImportBatch batch, JSONObject rowData, Map importContext ) {
		boolean importOfRowOkay = false
		Long domainId = getAndValidateDomainId(rowData, importContext)
		log.debug "importRow() id={}", domainId

		importOfRowOkay = insertRowDataIntoImportBatchRecord(session, batch, rowData, domainId, importContext)

		if (importOfRowOkay) {
			importContext.rowsCreated++
		} else {
			importContext.rowsSkipped++
		}
	}

	/**
	 * Create a new ImportBatchRecord for the given row. Any errors will be added to the importContext.errors list and
	 * the entire row will be skipped.
	 *
	 * @param batch - current batch
	 * @param rowData - the meta-data for the current field to be inserted
	 * @param domainId - the id for the domain object if known
	 * @param importContext - Map of the import context objects
	 * @return true if all of the fields were successfully added to the ImportBatchRecord table or false if there was an error
	 */
	private boolean insertRowDataIntoImportBatchRecord(session, ImportBatch batch, JSONObject rowData, Long domainId, Map importContext ) {

		int rowNum = importContext.rowNumber - 1
		// Detemine if there were duplicates found
		// TODO : JPM 2/2018 : the dupsFound logic is questionable (need to compare against latest JSON). Also this will be
		// good to move to the command object.
		Integer dupsFound = 0
		if (rowData.fields.containsKey('id') && rowData.fields.id.containsKey('find')) {
			dupsFound = ( rowData.fields.id.size() > 1 ? 1 : 0)
		}

		ImportOperationEnum OpValue = ImportOperationEnum.lookup(rowData.op)

		ImportBatchRecord batchRecord = new ImportBatchRecord(
			importBatch: batch,
			operation: OpValue,
			domainPrimaryId: domainId,
			sourceRowId: rowData.rowNum,
			errorCount: rowData.errors.size(),
			errorList: JsonUtil.toJson( (rowData.errors ?: []) ),
			warn: (rowData.warn ? 1 : 0),
			duplicateReferences: dupsFound,
			fieldsInfo: JsonUtil.toJson(rowData.fields)
		)

		if (! batchRecord.save(failOnError:false)) {
			// TODO : JPM 2/2018 : MINOR - Should use the GormUtil.i18n version of the errors
			String gmsg = GormUtil.allErrorsString(batchRecord)
			importContext.errors << "Unable to save row ${rowNum}: ${gmsg}"
			log.debug 'insertRowDataIntoImportBatchRecord() save failed :: {}', gmsg

			// Get rid of the GORM object that can't be saved so the main transaction can
			// still be committed.
			batchRecord.discard()

			return false
		}
		return true
	}

	/**
	 *  Check if the field values for row are valid for the given domain class
	 *
	 * @param domainId - the validated specified domain id if it exists
	 * @param rowData - the meta-data object containing all of the row fields
	 * @param importContext - Map of the import context objects
	 * @return true: the asset is valid, false otherwise.
	 */
	private boolean canRowDataBeImported(JSONObject rowData, Long domainId, Map importContext) {
		return DataImportHelper.validateRowData(rowData, domainId, importContext)
	}

	/**
	 * Used to retrieve the domain id from the row data and if present it validates that the domain object
	 * exists and is associated to the current project. It will return one of the following values:
	 *     null - no id was specified
	 *     > 0 - the id number of the valid domain object
	 *     -1 - an error occurred, possible causes (captured in the importContext.errors)
	 *			- invalid number
	 *			- domain object doesn't exist
	 * 			- domain object doesn't belong to the current project
	 *
	 * @param rowData - the meta-data for the current field to be inserted
	 * @param importContext - Map of the import context objects
	 * @return the id of the domain, null if not specified or -1 if there was an error
	 */
	private static Long getAndValidateDomainId(JSONObject rowData, Map importContext) {
		return DataImportHelper.getAndValidateDomainId(rowData, importContext)
	}

	/**
	 * Used to update the ImportBatch to the Queued status
	 * @param batchId - the ID of the batch to be updated
	 * @throws DomainUpdateException if ImportBatch.status was not PENDING at the current time
	 */
	@NotTransactional()
	void setBatchToQueued(Long batchId) {
		ImportBatch.withNewTransaction { session ->
			ImportBatch batch = ImportBatch.get(batchId)

			// if batch is already queued then return
			if (ImportBatchStatusEnum.QUEUED == batch.status) {
				log.debug 'setBatchToQueued() batch {}, status {}', batchId, batch.status
				return
			}

			// Let's lock the batch for a moment so we can check out if it is okay to start
			// processing this batch.
			batch.project.lock()
			batch = batch.refresh()

			// Now make sure nobody altered the batch in the meantime
			// Can only enqueue a batch if it was PENDING
			if (ImportBatchStatusEnum.PENDING != batch.status) {
				log.debug 'setBatchToQueued() batch {}, status {}', batchId, batch.status
				throw new DomainUpdateException('Unable to enqueue batch due to change in status')
			}

			Date queuedAtTime = new Date()

			// Update the batch to the QUEUED state
			batch.with {
				status = ImportBatchStatusEnum.QUEUED
				processProgress = 0
				processLastUpdated = queuedAtTime
				queuedAt = queuedAtTime
				queuedBy = securityService.currentUsername
				processStopFlag = 0
			}
			log.debug 'setBatchToQueued() dirtyProperties {}', batch.dirtyPropertyNames
			batch.save(failOnError:true, flush:true)
		}
	}

	/**
	 * Used to get the next ImportBatch to be processed withing the provided project
	 * @param projectId - the ID of the project where to look for import batches
	 */
	@NotTransactional()
	Map<String, ?> getNextBatchToProcess(Long projectId) {
		ImportBatch.withNewTransaction { session ->

			// Get the list of the ImportBatch IDs that can be processed
			List<?> batchIds = ImportBatch.where {
				project.id == projectId
				status == ImportBatchStatusEnum.QUEUED
			}
			.projections {
				property('id')
				property('queuedBy')
			}
			.sort('queuedAt')
			.list(max: 1)

			if (batchIds) {
				return [batchId: batchIds.get(0)[0], queuedBy: batchIds.get(0)[1]]
			} else {
				// there are no import batches in Queued status
				return null
			}
		}
	}

	/**
	 * Used to update the ImportBatch to the Running status
	 * @param id - the ID of the batch to be updated
	 * @throws DomainUpdateException if ImportBatch.status was not PENDING at the current time
	 */
	@NotTransactional()
	private void setBatchToRunning(Long batchId) {
		ImportBatch.withNewTransaction { session ->
			ImportBatch batch = ImportBatch.get(batchId)

			// Let's lock the batch for a moment so we can check out if it is okay to start
			// processing this batch.
			batch.project.lock()

			// Now make sure nobody altered the batch in the meantime
			// Can only start running a batch if it was QUEUED or PENDING
			if ( ! [ImportBatchStatusEnum.QUEUED, ImportBatchStatusEnum.PENDING].contains(batch.status) ) {
				log.debug 'setBatchToRunning() batch {}, status {}', batchId, batch.status
				throw new DomainUpdateException('Unable to process batch due to change in status')
			}

			Date noProgressInPast2Min = new Date()
			use( groovy.time.TimeCategory ) {
				noProgressInPast2Min = noProgressInPast2Min - 2.minutes
				// noProgressInPast2Min = noProgressInPast2Min - 6.hours
			}

			// Check to see if there are any other batches in the RUNNING state
			// that show progress in the past two minutes.
			int count = ImportBatch.where {
				project.id == batch.project.id
				status == ImportBatchStatusEnum.RUNNING
				processLastUpdated < noProgressInPast2Min
			}.count()

			if (count > 0) {
				throw new DomainUpdateException('Another batch is running, please try again later')
			}

			// Update the batch to the RUNNING state
			batch.with {
				status = ImportBatchStatusEnum.RUNNING
				processProgress = 0
				processLastUpdated = new Date()
				processStopFlag = 0

			}
			log.debug 'setBatchToRunning() dirtyProperties {}', batch.dirtyPropertyNames
			batch.save(failOnError:true, flush:true)
		}
	}

	/**
	 * Used to update the progress of ImportBatch being processed. This will also determine if the
	 * processing should stop. The processStopFlag will be set to 1 to signalthat the processing should stop.
	 * @param batchId - the batch to be updated
	 * @param rowsProcessed - count of the rows that have been processed
	 * @param totalRows - the total number of rows being processed
	 * @param status - the status to set the batch to when the processing has been completed.
	 * @return false if the processing should be stopped.
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	boolean updateBatchProgress( Long batchId, Integer rowsProcessed, Integer totalRows, ImportBatchStatusEnum status = null) {
		ImportBatch.withNewTransaction { session ->
			ImportBatch batch = ImportBatch.get(batchId)
			if (!batch) {
				log.error "ImportBatch($batchId) disappeared during batch processing process"
				throw new DomainUpdateException('The import batch was deleted while being processed')
			}

			// Calculate the % completed and update the batch appropriately
			Integer percComplete = (totalRows > 0 ? Math.round(rowsProcessed / totalRows * 100) : 100)
			batch.processProgress = percComplete
			batch.processLastUpdated = new Date()

			if (status) {
				batch.status = status
			}
			batch.save(failOnError:true)

			return (0 == batch.processStopFlag)
		}
	}

	/**
	 * Creates the Context object (map) that will be used for the processBatch method. Exposed so that tests can create the object correctly too
	 * @param domain - the ETL Domain name that should represent the batch domain type to be processed
	 * @return the Context
	 */
	private Map initContextForProcessBatch( Project project, ETLDomain domain ) {
		return [
			// The cache will be populated with cached entity references where the key is an MD5 of the query or id lookup of domain objects
			cache: new DataImportEntityCache(),

			// The actual class of the domain (e.g. net.transitionmanager.domain.Person)
			domainClass: domain.getClazz(),

			// The Domain name as it is known in the ETL scripting language
			domainClassName: domain.name(),

			// The Domain short name of the Grails domain class
			domainShortName: GormUtil.domainShortName( domain.getClazz() ),

			// The ImportBatchRecord currently being processed
			record: null,

			// The current project
			project: project,

			// The person whom is associated to this process running
			whom: securityService.getUserLoginPerson(),

			//
			staffList: getStaffReferencesForProject(project)
		]
	}

	/**
	 * Used to load a list of all of the staff available to a project
	 * @param project - the project to load staff for
	 * @param context - the Context map used to carry all the shared data for Import logic
	 */
	@NotTransactional()
	List<Person> getStaffReferencesForProject(Project project) {
		List<Party> companies = partyRelationshipService.getProjectCompanies(project)
		return partyRelationshipService.getAllCompaniesStaffPersons(companies)
	}

	/**
	 * Used to process a batch of import records
	 * @param project
	 * @param id - the id of the batch
	 * @param specifiedRecordIds - an optional list of ImportBatchRecord IDs to process
	 * @return the number of rows that were processed
	 */
	@NotTransactional()
	Integer processBatch(Project project, Long batchId, List specifiedRecordIds=null) {
		ImportBatch batch = GormUtil.findInProject(project, ImportBatch, batchId, true)
		Map context

		if (specifiedRecordIds == null) {
			specifiedRecordIds = []
		}
		log.info "processBatch() for batch $batchId of project $project started with requested ids=$specifiedRecordIds"
		//StopWatch stopwatch = new StopWatch()
		//stopwatch.start()

		int rowsProcessed = 0

		String error = validateBatchForProcessing(batch)
		if (error) {
			// TODO : JPM 3/2018 : validateBatchForProcessing is not handled when there are errors
		}

		Exception ex = null
		try {
			setBatchToRunning(batch.id)
			batch.refresh()

			// Get the list of the ImportBatchRecord IDs to be processed
			List<Long> recordIds = ImportBatchRecord.where {
				importBatch.id == batch.id
				status == ImportBatchStatusEnum.PENDING
				if (specifiedRecordIds.size() > 0) {
					id in specifiedRecordIds
				}
			}
			.projections { property('id') }
			.list(sortBy: 'id')

			log.info 'processBatch({}) found {} PENDING rows', batchId, recordIds.size()

			// Initialize the context with things that are going to be helpful throughtout the process
			context = initContextForProcessBatch( project, batch.domainClassName )

			int totalRowCount = recordIds.size()
			int offset = 0
			int setSize = 5
			boolean aborted = false

			// Now iterate over the list in sets of 5 rows
			while (! aborted && recordIds) {
				List recordSetIds = recordIds.take(setSize)
				recordIds = recordIds.drop(setSize)
				List records = ImportBatchRecord.where {
					id in recordSetIds
				}.list(sortBy: 'id')

				ImportBatchRecord.withNewTransaction { status ->
					for (record in records) {
						context.record = record
						rowsProcessed++
						processBatchRecord(batch, record, context, rowsProcessed)
					}
					log.debug 'processBatch({}) clearing Hibernate Session', batchId
					GormUtil.flushAndClearSession()
				}

				aborted = ! updateBatchProgress( batchId, rowsProcessed, totalRowCount)
				if (aborted) {
					log.info 'processBatch({}) received abort process signal and stopped', batchId
				}
			}
		} catch(e) {
			// If an exception happened to occur during this process then we'll save it for the moment and then deal with
			// it after updating the batch appropriately.
			ex = e
		}

		// Now update the status of the ImportBatch based on the number of records still in the
		// PENDING status. If there are any remaining then the batch will return to the PENDING status
		// otherwise will be set to COMPLETED.
		int remaining = ImportBatchRecord.where {
			importBatch.id == batchId
			status == ImportBatchStatusEnum.PENDING
		}.count()

		ImportBatchStatusEnum status = (remaining > 0 ? ImportBatchStatusEnum.PENDING : ImportBatchStatusEnum.COMPLETED)
		updateBatchProgress(batchId, 1, 1, status)

		//log.info "processBatch({}) finished in {} and processed {} records", batchId, stopwatch.endDuration(), rowsProcessed

		// TODO : JPM 3/2018 : Fix the batch status update after processing
		// It occurred to me that throwing the Exception is going to rollback the changes but that the
		// update BatchProgress will be done in another transaction so the results won't be correct. A process needs to be
		// triggered back at the Controller layer after the transaction was rolled back to update the batch status appropriately.
		// Perhaps the above code can be broken out into a separate service call and called subsequent to this method so that the
		// data would be committed appropriately and the the query will have the correct information.

		if (ex) {
			// Now we send the exception back to the user interface
			throw ex
		}

		return rowsProcessed
	}

	/**
	 * Used to review the batch to determine if it contains then necessary data for the type of operation
	 * @param batch - the batch to be evaluated
	 * @return A string containing an error if one was discovered
	 */
	@NotTransactional()
	def validateBatchForProcessing(ImportBatch batch) {
		String error
		String domainName = batch.domainClassName.name()

		List<String> fieldNames = batch.fieldNameListAsList()

		switch (batch.domainClassName) {
			case ETLDomain.Dependency:
				// Validate that there at a minium of the asset and dependent references
				if (! ( fieldNames.contains('asset') &&  fieldNames.contains('dependent' ) ) ) {
					error = "Processing ${domainName} requires that primary (asset) and supporting (dependent) asset be specified"
				}
				break

			default:
				break
				//log.error "Batch Import process called for unsupported domain ${batch.domainClassName} in batch ${batch.id} in project ${batch.project}"
				//error = "Batch process not supported for domain ${domainName}"
		}

		return error
	}

	/**
	 * Used to update the ImportBatch status to COMPLETED if all rows are COMPLETED or IGNORED otherwise
	 * set the status to PENDING.
	 * @param batch - the batch that the ImportBatchRecord
	 */
	@NotTransactional()
	private void updateBatchStatus(Long batchId) {
		Integer count = ImportBatchRecord.where {
			importBatch.id == batchId
			status != ImportBatchStatusEnum.COMPLETED && status != ImportBatchStatusEnum.IGNORED
		}.count()
		ImportBatchStatusEnum status = (count == 0 ?  ImportBatchStatusEnum.COMPLETED :  ImportBatchStatusEnum.PENDING)

		log.debug 'updateBatchStatus() called for batch {}, Pending count {}, status {}', batchId, count, status.name()

		ImportBatch.where {
			id == batchId
			status != status
		}.updateAll([status: status])
	}

	/**
	 * Used to process a single record of an ImportBatch
	 * @param batch - the batch that the ImportBatchRecord
	 * @param record - the ImportBatchRecord to be processed
	 * @param context - the batch processing context that contains objects used throughout the process
	 */
	private void processBatchRecord(ImportBatch batch, ImportBatchRecord record, Map context, Long recordCount) {
		switch (batch.domainClassName) {
			case ETLDomain.Dependency:
				//processDependencyRecord(batch, record, context, recordCount)
				//break

			case ETLDomain.Application:
			case ETLDomain.Asset:
			case ETLDomain.Database:
			case ETLDomain.Device:
			case ETLDomain.Files:
			case ETLDomain.Storage:
				processEntityRecord(batch, record, context, recordCount)
				break

			default:
				String domain = batch.domainClassName.name()
				log.error "Batch Import process called for unsupported domain $domain in batch ${batch.id} in project ${batch.project}"
				throw new InvalidRequestException("Batch process not supported for domain ${domain}")
		}
	}

	/**
	 * Used to process a single Asset ImportBatchRecord
	 * Note that errors may be recorded a record and/or field level as appropriate.
	 *
	 * @param batch - the batch that the ImportBatchRecord
	 * @param importBatchRecord - the ImportBatchRecord to be processed
	 * @param context - the batch processing context that contains objects used throughout the process
	 * @throws DomainUpdateException when unable to save the record with updates to it
	 */
	void processEntityRecord(ImportBatch batch, ImportBatchRecord record, Map context, Long recordCount) {
		try {
			Map fieldsInfo = JsonUtil.parseJson(record.fieldsInfo)

			resetRecordAndFieldsInfoErrors(record, fieldsInfo)

			Object entity = findOrCreateEntity(fieldsInfo, context)

			if (entity) {
				log.debug 'processEntityRecord() calling bindFieldsInfoValuesToEntity with entity {}, fieldsInfo isa {}', entity, fieldsInfo.getClass().getName()

				// Now add/update the remaining properties on the domain entity appropriately
				Boolean bindingOkay = bindFieldsInfoValuesToEntity(entity, fieldsInfo, context)
				Boolean abandonEntity = true
				if (!bindingOkay) {
					log.warn 'processEntityRecord() binding values failed'
				} else if (recordDomainConstraintErrorsToFieldsInfoOrRecord(entity, context.record, fieldsInfo) ) {
					log.warn "processEntityRecord() binding constraints errors ${GormUtil.allErrorsString(entity)}"
				} else {
					abandonEntity = false
				}

				if (abandonEntity) {
					// Damn it! Couldn't save this sucker...
					entity.discard()
					entity = null
				} else {

					// Determine the correct Operation that was performed and if the record should be saved
					Boolean shouldBeSaved = true
					record.operation = (entity.id ? ImportOperationEnum.UPDATE : ImportOperationEnum.INSERT)
					if ( record.operation == ImportOperationEnum.UPDATE && ! GormUtil.hasUnsavedChanges(entity) ) {
						record.operation = ImportOperationEnum.UNCHANGED
						shouldBeSaved = false
					}

					// log.debug "processEntityRecord() Saving the Dependency"
					if (shouldBeSaved) {
						if (entity.save(failOnError:false)) {
							// If we still have a dependency record then the process must have finished
							// TODO : JPM 3/2018 : Change to use ImportBatchRecordStatusEnum -
							//    Note that I was running to some strange issues of casting that prevented from doing this originally
							// record.status = ImportBatchRecordStatusEnum.COMPLETED
							record.status = ImportBatchStatusEnum.COMPLETED
						} else {
							log.warn 'processEntityRecord() failed to create entity due to {}', GormUtil.allErrorsString(entity)
							entity.discard()
							entity = null
						}
					} else {
						// Discard the entity just incase something unexpected
						entity.discard()
						record.status = ImportBatchStatusEnum.COMPLETED
					}
				}
			}

			// Update the fieldsInfo back into the Import Batch Record
			record.fieldsInfo = JsonUtil.toJson(fieldsInfo)
		} catch (e) {
			record.addError(e.getMessage())
			log.error ExceptionUtil.stackTraceToString("processEntityRecord() Error while processing record ${recordCount}", e)
		}

		// log.debug "processEntityRecord() Saving the ImportBatchRecord with status ${record.status}"
		if (! record.save(failOnError:false, flush:true) ) {
			// Catch the error here but need to throw it outside the try/catch so that it gets recorded at the batch level
			String domainUpdateErrorMsg = GormUtil.allErrorsString(record)
			log.warn 'processEntityRecord() Failed to save ImportBatchRecord changes: {}', domainUpdateErrorMsg
			throw new DomainUpdateException("Unable to update row $recordCount due to " + domainUpdateErrorMsg)
		}
	}

	/**
	 * Used to find a single Entity record or create one if not found
	 *
	 * The find is performed using the meta data provided in the fieldsInfo.  When more than one entity are found then
	 * an error is recorded in the fieldsInfo object and a null is returned. If the domain can not be found and no ID was
	 * specified or resolved during the ETL process then a new domain will be created but only a minimum of fields will
	 * be populated. Any field that appears in the fieldsInfo will not be populated.
	 *
	 * @param fieldsInfo
	 * @param context
	 * @return the entity that is found or created, or null if an error occurs. The error is recorded in the fieldsInfo Map appropriately.
	 */
	private Object findOrCreateEntity(Map fieldsInfo, Map context ) {
		Object entity

		log.debug 'findOrCreateEntity() called'

		entity = fetchEntityByFieldMetaData('id', fieldsInfo, context)

		if (entity == -1) {
			log.debug "findOrCreateEntity() Unable to initially find the entity by ID reference"
		} else {
			if (entity) {
				// UPDATE
				log.debug "findOrCreateEntity() Updating existing entity {}", entity
			} else {
				// CREATE
				log.debug "findOrCreateEntity() Creating new entity"
				entity = createEntity(context.domainClass, fieldsInfo, context)
			}
		}

		// TODO : JPM 3/2018 : Need to review the md5 / cache - Here we just created assets that should be cached.

		return entity
	}

	/**
	 * Used to create a new entity and set various mandatory properties if they won't be set
	 * by the data in fieldsInfo.
	 * @param fieldsInfo
	 * @param context
	 * @return the newly minted domain entity instance
	 */
	Object createEntity(Class domainClass, Map fieldsInfo, Map context) {
		if (! GormUtil.isDomainClass(domainClass)) {
			throw new DomainUpdateException("Class specified (${domainClass.getName()}) is not a valid domain class")
		}

		Object entity = domainClass.newInstance()

		// Populate mandatory properties that can be determined at this time

		List propsInDomain = GormUtil.getDomainPropertyNames(domainClass)

		// project
		if ( 'project' in propsInDomain ) {
			entity.project = context.project
		}

		// moveBundle
		if ('moveBundle' in propsInDomain) {
			// Only bother if it isn't specified in the fieldsInfo which will be set later
			if (! fieldsInfo.containsKey('moveBundle') || ! fieldsInfo['moveBundle'].value ) {
				entity.moveBundle = context.project.getProjectDefaultBundle()
			}
		}

		// Handle Person references
		for (String fname in ['createdBy', 'modifiedBy']) {
			if (fname in propsInDomain && GormUtil.getDomainPropertyType(domainClass, fname) == Person ) {
				entity[fname] = context.whom
			}
		}

		// owner (i.e. project.client)
		if ('owner' in propsInDomain && GormUtil.getDomainPropertyType(domainClass, 'owner') == PartyGroup) {
			entity.owner = context.project.client
		}

		return entity
	}

	/**
	 * Used to bind the values from the fieldsInfo into the domain but skipping over any fields specified
	 * in the fieldsToIngnore parameter.
	 *
	 * Field value assignment logic:
	 *
	 * 		If init contains value and entity property has no value
	 * 		Then set the property to the init value
	 * 		Else if existin value not equal new value (including null)
	 * 		Then set new value as
	 *		If new value is null and field is not nullable then error
	 * 		If field is a reference (e.g. Person)
	 * 		Then attempt to lookup the reference object base on meta-data
	 *
	 * @param domain - the entity to bind the values on
	 * @param fieldsInfo - the Map of fields and their values from the ETL process
	 * @param fieldsToIgnore - a List of field names that should not be bound
	 * @return true if binding did not encounter any errors
	 */
	@Transactional(noRollbackFor=[Exception])
	Boolean bindFieldsInfoValuesToEntity(Object domain, Map fieldsInfo, Map context, List fieldsToIgnore=[]) {
		// TODO - JPM 4/2018 : Refactor bindFieldsInfoValuesToEntity so that this can be used in both the row.field values & the create and update blocks
		//		  JPM 8/2018 : Believe that this may already work. Need to test and remove TODO if so..
		int c = 0
		Boolean noErrorsEncountered = true

		// Used to add errors and set flag in one line within this function
		Closure recordErrorHelper = { fieldName, msg ->
			noErrorsEncountered = false
			addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, msg)
		}

		// Take the complete list of field names in fieldsInfo and remove 'id' plus any passed to the method
		Set<String> fieldNames = fieldsInfo.keySet()
		log.debug 'bindFieldsInfoValuesToEntity() starting with fields to update of: {}', fieldNames.join(', ')
		if (fieldsToIgnore == null) {
			fieldsToIngnore = []
		}
		fieldsToIgnore.addAll(['id'])
		fieldNames = fieldNames - fieldsToIgnore

		Boolean isNewEntity = (Boolean)(domain.id == null)
		String domainShortName = GormUtil.domainShortName(domain)

		for (fieldName in fieldNames) {
			if ( fieldName in PROPERTIES_THAT_CANNOT_BE_MODIFIED ) {
				recordErrorHelper(fieldName,'Modifying the field is not allowed')
				continue
			}

			log.debug 'bindFieldsInfoValuesToEntity() Processing {}.{}', domainShortName, fieldName

			// Check for exception fields that should be ignored
			if (DOMAIN_FIELD_EXCEPTIONS."$domainShortName"?."$fieldName"?.'ignore') {
				continue
			}

			if (! GormUtil.isDomainProperty(domain, fieldName)) {
				recordErrorHelper(fieldName, 'Field name is not a property of this domain')
				continue
			}

			def domainValue = domain[fieldName]
			def (value, initValue) = getValueAndInitialize(fieldName, fieldsInfo)
			boolean isInitValue = (initValue != null)

			// The field must have a value to set otherwise the logic will skip over it. As such this logic
			// will determine if the value or init properties have a real value and set a few boolean flags.
			def valueToSet = isInitValue ? initValue : value

			Boolean isReference = GormUtil.isReferenceProperty(domain, fieldName)

			// This will get populated with the domain's existing value for fields to be recorded when different
			Object existingValue = domain[fieldName]

			// --------------------------------------------------
			// Deal with setting field to NULL
			// --------------------------------------------------
			if (valueToSet == null) {
				if (existingValue) {
					// Check to see if the field is nullable
					if (GormUtil.getConstraintValue(domain, fieldName, 'nullable')) {
						if (isReference) {
							existingValue = existingValue.toString()
						}
						_recordChangeOnField(domain, fieldName, null, isInitValue, isNewEntity, fieldsInfo)
					} else {
						addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, 'Field can not be null')
					}
				}
				continue
			}

			if (isReference) {
				// --------------------------------------------------
				// Process reference fields
				// --------------------------------------------------
				// TODO : JPM 6/2018 : Concern -- may have or not a newValue or find results -- this logic won't always error
				// Object refObjectOrErrorMsg = findDomainReferenceProperty(domain, fieldName, newValue, fieldsInfo, context)
				valueToSet = fetchEntityByFieldMetaData(fieldName, fieldsInfo, context, domain)
				switch (valueToSet) {
					case -1:
						noErrorsEncountered = false
						break

					case null:
						// Boolean nullable = GormUtil.getConstraint(domain, fieldName, 'nullable')
						// log.debug 'bindFieldsInfoValuesToEntity() Set reference to null > isNullable={}', nullable, fieldName
						//	 if ( nullable != true) {
						// 	recordErrorHelper(fieldName, 'Unable to resolve reference lookup')
						// } else {
						// 	_recordChangeOnField(domain, fieldName, refObject, isInitValue, isNewEntity, fieldsInfo)
						// 	domain[fieldName] = null
						// }

						recordErrorHelper(fieldName, 'Unable to resolve reference lookup')
						break

					default:
						_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, isNewEntity, fieldsInfo)
				}

			} else {
				// --------------------------------------------------
				// Process native Java data types
				// --------------------------------------------------
				def fieldClassType = GormUtil.getDomainPropertyType(domain.getClass(), fieldName)
				log.debug 'bindFieldsInfoValuesToEntity() processing {}, type={}, value={}', fieldName, fieldClassType.getName(), valueToSet
				// println "bindFieldsInfoValuesToEntity() field $fieldName, value=$valueToSet, fieldClass=${fieldClassType.getName()}"
				try {
					switch (fieldClassType) {
						case String:
							_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, isNewEntity, fieldsInfo)
							break

						case Integer:
							// TODO : JPM 7/2018 : is a null value an error or an allowed value?
							Integer numValueToSet = (NumberUtil.isaNumber(valueToSet) ? valueToSet : NumberUtil.toInteger(valueToSet))
							if (numValueToSet == null) {
								recordErrorHelper(fieldName, 'Value specified must be an numeric value')
							} else {
								_recordChangeOnField(domain, fieldName, numValueToSet, isInitValue, isNewEntity, fieldsInfo)
							}
							break

						case Long:
							Long numValueToSet = (NumberUtil.isaNumber(valueToSet) ? valueToSet : NumberUtil.toLong(valueToSet))
							if (numValueToSet == null) {
								recordErrorHelper(fieldName, 'Value specified must be an numeric value')
							} else {
								_recordChangeOnField(domain, fieldName, numValueToSet, isInitValue, isNewEntity, fieldsInfo)
							}
							break

						case Date:
							// TODO : JPM 7/2018 : Check the database type to see if the type is Date or Datetime and clearTime if the former, parse accordingly too
							if (valueToSet instanceof CharSequence) {
								// If it is a String and is an ISO8601 Date or DateTime format then we can attempt to parse it for them
								if (valueToSet =~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ ) {
									valueToSet = TimeUtil.parseDate(TimeUtil.FORMAT_DATE_TIME_6, valueToSet, TimeUtil.FORMAT_DATE_TIME_6)
								} else if (valueToSet =~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$/ ) {
									valueToSet = TimeUtil.parseDateTime(valueToSet, TimeUtil.FORMAT_DATE_TIME_ISO8601)
								}
								// Attempt to parse a Date / Date Time based on the underlying database table column type
								def mapping = GormUtil.getDomainBinderMapping(domain.getClass())
								def propConfig = mapping.getPropertyConfig(fieldName)
								// log.debug '**** propConfig fieldName={}, valueToSet={}, type={}, propConfig={}', fieldName, valueToSet, propConfig.type, propConfig
							}

							if (! (valueToSet instanceof Date)) {
								String columnType
								recordErrorHelper(fieldName, 'Value must be transformed to a Date or in ISO-8601 format')
							} else {
								valueToSet.clearTime()
								_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, isNewEntity, fieldsInfo)
							}
							break

						case Enum:
							try {
								def enumValue=null

								if (valueToSet != null) {
									if (valueToSet instanceof CharSequence) {
										enumValue = fieldClassType.valueOf(valueToSet.toString())
									} else if (valueToSet.getClass().getName() == fieldClassType.getName()) {
										enumValue = valueToSet
									}
								}
								_recordChangeOnField(domain, fieldName, enumValue, isInitValue, isNewEntity, fieldsInfo)
							} catch (e) {
								recordErrorHelper(fieldName, 'Unable to validate ENUM value')
							}
							break

						case Double:
							_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, isNewEntity, fieldsInfo)
							break

						default:
							log.debug 'bindFieldsInfoValuesToEntity() processing {}, type={}, value={}', fieldName, fieldClassType.getName(), valueToSet
							recordErrorHelper(fieldName, "Import process unable to support setting type ${fieldClassType.getName()}")
					}
				} catch (e) {
					log.error ExceptionUtil.stackTraceToString('Error while setting property', e)
					recordErrorHelper(fieldName, 'Error while setting property - ' + e.message)
				}
				// println "_recordChangeOnField() noErrorsEncountered=$noErrorsEncountered, ${c++}"
			}
		}

		log.debug 'bindFieldsInfoValuesToEntity() dirty fields {}', domain.dirtyPropertyNames
		// println "bindFieldsInfoValuesToEntity() dirty fields ${domain.dirtyPropertyNames}, noErrorsEncountered ${noErrorsEncountered}"

		return noErrorsEncountered
	}

	/**
	 * Used to actually assign values to the domain being created or updated. Additionally it will record the original value when
	 * the record pre-existed back into the fieldsInfo to allow for showing what fields actually changed.
	 *
	 * The original value will be saved to the 'previousValue' property only if the record is pre-existing.
	 *
	 * @param domainInstance - the domain entity to save values to
	 * @param fieldName - the field to update
	 * @param newValue - the value to save
	 * @param isNewEntity - flag if the record is new or existing
	 * @param fieldsInfo - the JSON data from the import batch record
	 */
	@Transactional(noRollbackFor=[Exception])
	void _recordChangeOnField(
		Object domainInstance,
		String fieldName,
		Object newValue,
		Boolean isInitValue,
		Boolean isNewEntity,
		Map fieldsInfo
	) {
		Object existingValue = domainInstance[fieldName]

		log.debug '_recordChangeOnField() for domain {}.{} existingValue={}, newValue={}, isNewRecord={}',
			domainInstance.getClass().getName(), fieldName, existingValue, newValue, isNewEntity

		if ( !isInitValue || (isInitValue && existingValue == null)) {
			boolean isMatch
			if (existingValue && GormUtil.isDomainClass(existingValue.getClass())) {
				// For domain references we need to compare IDs because the objects are not always the same
				isMatch = existingValue.id == newValue.id
			} else {
				isMatch = existingValue == newValue
			}
			if (! isMatch) {
				domainInstance[fieldName] = newValue
				if (! isNewEntity) {
					fieldsInfo[fieldName].previousValue = existingValue.toString()
				}
			}
		}
	}

	/**
	 * Used in an attempt to lookup a domain record using the metadata that is provided by the
	 * ETL process. This method will leverage caching of the domain entities to expedite retrieval
	 * for entities that are frequently cross-referenced (e.g. Clusters to Servers).
	 *
	 * @param fieldName - the domain fieldname
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @param entityInstance - optional used when looking up a reference field
	 * @return will return various results based on searches
	 * 		entity 	: if found
	 *		null	: if not found by alternate or query
	 *		-1 		: an error occurred, which is recorded in the fieldsInfo appropriately
	 *
	 * The process logic should flow as documented here:
	 *
	 * 		If find.results contains a single id
	 *		Then get with id
	 *			If found then done
	 *			Else error
	 *		Else if field.value is an ID (number)
	 *			Then get with id
	 *				If found then done
	 *				Else error
	 *		Else if find.query specified then requery
	 *			If found one (1) then done
	 *			Else if found more than one (1) then error
	 *		Try searching by alternate key value in field.value
	 *			if found one (1) then done else error
	 *
	 * The structure looks like the following:
	 *	{
	 * 		fields": {
	 * 			"asset": {
	 *				// Search by Alternate Key Example
	 * 				"value": "xraysrv01",
	 *				// Search by primary ID Example
	 * 				"value": 114052,
	 * 				"originalValue": "114052",   // THIS MAY BE GOING AWAY JPM 7/2018
	 *              "previousValue": "23432" // This is set during the POSTING process for existing records being updated
	 * 				"error": false,
	 *				"errors": [ "Lookup by ID was not found"],
	 * 				"warn": false,
	 * 				"find": {
	 * 					"query": [
	 *						[ domain: 'Device', kv: [ assetName: 'xraysrv01', assetType: 'Server' ] ]
	 *						[ domain: 'Device', kv: [ assetName: 'xraysrv01'] ]
	 *					],
	 *					"matchOn": 2,
	 *					"results": [12312,123123,123123123]
	 * 				}
	 * 			},
	 *
	 * @test Integration
	 */
	@Transactional(noRollbackFor=[Exception])
	private Object fetchEntityByFieldMetaData(String fieldName, Map fieldsInfo, Map context, Object entityInstance=null) {
		// This will be populated with the entity object or error message appropriately
		Object entity

		// This will be used to check/set cache for previously searched items
		String md5

		log.debug 'fetchEntityByFieldMetaData() called with domain {} on row {}, fieldName {}, value={}',
			context.domainShortName, context.record.sourceRowId, fieldName, fieldsInfo[fieldName]?.value

		// This is a QA Easter Egg to test the error handling
		if (fieldsInfo[fieldName]?.value == '!~! Go ahead, Make my day !~!') {
			throw new InvalidRequestException('Do you feel lucky, punk?')
		}

		boolean foundInCache=false
		boolean errorPreviouslyRecorded = false
		boolean fieldIsId = fieldName == 'id'
		boolean fieldIsInFieldsInfo = fieldsInfo.containsKey(fieldName)

		// Flags that a search by ID failed which will result in an error so that duplicates are not created
		boolean searchedById = false

		Class domainClass = classOfDomainProperty(fieldName, fieldsInfo, context)
		String domainShortName = GormUtil.domainShortName(domainClass)

		while (true) {

			if ( ! fieldIsInFieldsInfo && ! fieldIsId) {
				// Shouldn't happen but just in case...
				entity = "Reference property $fieldName is missing from ETL output"
				break
			}

			//
			// Now going to try up to 5+ different ways to find the domain entity
			//

			if (fieldIsInFieldsInfo) {
				// 1. See if this property based on ID is in the cache already
				md5 = generateMd5OfFieldsInfoField(domainShortName, fieldName, fieldsInfo)
				// log.debug 'fetchEntityByFieldMetaData() has cache key {}', md5
				entity = context.cache.get(md5)
				if (entity) {
					log.debug 'fetchEntityByFieldMetaData() resolved by method 1 (cache ID {}, {})', md5, entity
					foundInCache=true
					break
				}

				// 2. Attempt to find the domain by the ID in the property field.value (Number or String)
				entity = fetchEntityById(domainClass, fieldName, fieldsInfo, context)
				if (entity) {
					if (entity == NOT_FOUND_BY_ID) {
						// Didn't find but we did have an ID
						searchedById = true
					} else {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 2 (ID)'
						break
					}
				}

				// 3. Attempt to find domain with the single result (find.results[0])
				if ( _hasSingleFindResult(fieldName, fieldsInfo) ) {
					searchedById = true
					entity = _fetchEntityByFindResults(fieldName, fieldsInfo, context)
					if (entity) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 3 (find results)'
						break
					}
				}

				// Fail out if the field had a previously set/resolved ID
				if (searchedById) {
					// This is when we give up because there were attempts by previously specified or resolved ID but
					// now attempting to retrieve has failed indicating that the entity was deleted. As such we do NOT
					// what a create a new record.
					log.info 'fetchEntityByFieldMetaData() failed to resolve by ELT ID reference - domain {}, field {}',
						domainClass.getName(), fieldName
					entity = SEARCH_BY_ID_NOT_FOUND_MSG
					break
				}

				// 4. Attept to find domain by re-applying the find/elseFind queries
				if ( _hasFindQuery(fieldName, fieldsInfo)) {
					List entities = _performQueryAndUpdateFindElement(fieldName, fieldsInfo, context)
					int qtyFound = entities?.size() ?: 0
					if (qtyFound == 1) {
						entity = entities[0]
						log.debug 'fetchEntityByFieldMetaData() resolved by method 4 (requery), found 1 '
						break
					} else if (qtyFound > 1 ) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 4 (requery), found {}', qtyFound
						entity = FIND_FOUND_MULTIPLE_REFERENCES_MSG
						errorPreviouslyRecorded = true
						break
					}
				}

				// 5. Attempt to find domain by alternate key (which is the least precise)
				// If the value was a String and try looking up the entity by it's alternate key (e.g. assetName or name)
				def searchValue = getValueOrInitialize(fieldName, fieldsInfo)
				Map findResult = _fetchEntityByAlternateKey(domainClass, searchValue, fieldName, fieldsInfo, context)
				// entities = findDomainByAlternateProperty(fieldName, fieldsInfo, context)
				if (findResult.error) {
					addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, findResult.error)
					entity = findResult.error
					break
				} else {
					int qtyFound = findResult.entities?.size() ?: 0
					if (qtyFound == 1) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 5 (alternate key), found 1 by {}', fieldsInfo[fieldName].value
						entity = findResult.entities[0]
						break
					} else if (qtyFound > 1 ) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 5 (alternate key), found {} by []', qtyFound, fieldsInfo[fieldName].value
						entity = ALTERNATE_LOOKUP_FOUND_MULTIPLE_MSG
						break
					}
				}
			} // if (fieldIsInFieldsInfo) {

			// 6. Attempt for certain domain classes (e.g. AssetDependency) that weren't found
			switch (domainClass) {
				case AssetDependency:
					entity = _fetchAssetDependencyByAssets(fieldsInfo, context)
					break

				case Person:
					// When the Person is a reference in another domain then we can pass it into the fetchPerson logic
					Person existingPerson = entityInstance ? entityInstance[fieldName] : null
					String searchValue = getValueOrInitialize(fieldName, fieldsInfo)
					String errorMsg
					(entity, errorMsg) = _fetchPerson(existingPerson,  searchValue, fieldName, fieldsInfo, context)
					if (errorMsg) {
						// If no entity was found then we want to capture the error message to save in the cache
						entity = errorMsg
						addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, errorMsg)
					}
					break
			}

			break
		}

		// Cache the entity or error message for the lookup (unless it was found in cache above)
		if (! foundInCache && md5) {
			log.debug ('fetchEntityByFieldMetaData() added to cache: key {}, class {}, fieldName {}, entity {}', md5, domainShortName, fieldName, entity)
			context.cache.put(md5, entity)
		}

		// Deal with setting the error message if the entity wasn't found
		if ( (entity instanceof CharSequence) ) {
			if (! errorPreviouslyRecorded) {
				addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, entity)
			}
			entity = -1
		}

		return entity
	}

	/**
	 * Used by the fetchEntityByFieldMetaData method to find an AssetDependency by the asset and dependent assets specified
	 * in the fieldsInfo appropriately. If either assets can not be located then a null is returned.
	 *
	 * @return the Dependency if found
	 * @test None
	 */
	@Transactional(noRollbackFor=[Exception])
	private AssetDependency _fetchAssetDependencyByAssets(Map fieldsInfo, Map context ) {
		AssetEntity primary
		AssetEntity supporting
		AssetDependency dependency

		log.debug '_fetchAssetDependencyByAssets() was called'

		primary = fetchEntityByFieldMetaData('asset', fieldsInfo, context)
		if (primary in AssetEntity) {
			log.debug '_fetchAssetDependencyByAssets() primary asset was found'
			supporting = fetchEntityByFieldMetaData('dependent', fieldsInfo, context)
			if (supporting in AssetEntity) {
				log.debug '_fetchAssetDependencyByAssets() supporting asset was found'
				dependency = AssetDependency.where {
					asset.id == primary.id
					dependent.id == supporting.id
				}.find()
				if (dependency) {
					log.debug '_fetchAssetDependencyByAssets() asset dependency was found'
				}
			}
		}

		return dependency
	}

	/**
	 * Used by fetchEntityByFieldMetaData to get the entity by the field.value containing an ID as a Number or
	 * String. If the value is a String containing a number it could actually be the name of the entity so if that
	 * is so then it will not flag the NOT_FOUND_BY_ID if not found.
	 *
	 * @return One of three values:
	 * 		entity : The entity instance if ID was specified and found
	 *		null : ID was not specified
	 *		NOT_FOUND_BY_ID : if ID specified but not found
	 * @test Integration
	 */
	@Transactional(noRollbackFor=[Exception])
	Object fetchEntityById(Class domainClass, String fieldName, Map fieldsInfo, Map context) {
		Object entity
		Boolean searchedById = false
		Boolean valueIsString = (fieldsInfo[fieldName].value instanceof CharSequence)
		Long id = NumberUtil.toPositiveLong(fieldsInfo[fieldName].value)
		// log.debug 'fetchEntityById() isaNumber={}, isaString={}, idValue={}', isaNumber, isaString, idValue
		if (id) {
			searchedById = true
			entity = GormUtil.findInProject(context.project, domainClass, id)
		}
		log.debug 'fetchEntityById() domainClass={}, fieldName={}, id={}, entity={}', domainClass.getName(), fieldName, id, entity

		if (searchedById && ! entity) {
			return valueIsString ? null : NOT_FOUND_BY_ID
		} else {
			return entity
		}
	}

	/**
	 * Called by fetchEntityByFieldMetaData.
	 * Used to determine if the fieldsInfo for a property has a single result.
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return true if there is a single result otherwise false
	 * @test Integration
	 */
	@Transactional(noRollbackFor=[Exception])
	Boolean _hasSingleFindResult(String propertyName, Map fieldsInfo) {
		Boolean hasSingleResult = fieldsInfo[propertyName].find?.results?.size() == 1
		log.debug '_hasSingleFindResult() for field {} has single result? {}', propertyName, hasSingleResult
		return hasSingleResult
	}

	/**
	 * Used to determine if the fieldsInfo for a property has a find/elseFind query specified
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return true if there is one or more queries defined
	 * @test Integration
	 */
	@Transactional(noRollbackFor=[Exception])
	Boolean _hasFindQuery(String propertyName, Map fieldsInfo) {
		Boolean hasFindQuery = fieldsInfo[propertyName].find?.query?.size() > 0
		log.debug '_hasFindQuery() for field {} has find query? {}', propertyName, hasFindQuery
		return hasFindQuery
	}

	/**
	 * Called by fetchEntityByFieldMetaData.
	 * Used to fetch a single domain entity based on the results of the find/elseFind commands having
	 * found a single entity. The _hasSingleFindResult method must be called first to determine if this
	 * method should be called.
	 *
	 * @param propertyName - the property that has find results to lookup the object
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return One of two values:
	 * 		entity : the entity instance referenced in find results if found
	 *		null : the find result reference was not found, must of been deleted
	 */
	@Transactional(noRollbackFor=[Exception])
	private Object _fetchEntityByFindResults(String propertyName, Map fieldsInfo, Map context) {
		Object entity=null
		if (_hasSingleFindResult(propertyName, fieldsInfo)) {
			Map find = fieldsInfo[propertyName].find ?: null
			Long entityId = find.results[0]
			String domainName = find.query[0].domain
			// Get the class of the domain specified in find of the ETL script
			Class domainClass = ETLDomain.lookup(domainName)?.getClazz()

			if (domainClass) {
				// Now get the entity by the id in the results
				entity = GormUtil.findInProject(context.project, domainClass, entityId, false)
			} else {
				// This really should never happen but just in case
				throw new RuntimeException("ETL find/elseFind references invalid domain '${domainName}'")
			}
		}
		return entity
	}

	/**
	 * Called by fetchEntityByFieldMetaData
	 * Used to query for domain entities using the meta-data generated by the find/elseFind commands in the
	 * ELT DataScript. After performing the queries it will update the find section of the fieldsInfo with the
	 * the results. It will return a list of the entities found.
	 *
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return list of entities found
	 */
	@Transactional(noRollbackFor=[Exception])
	private List<Object> _performQueryAndUpdateFindElement(String propertyName, Map fieldsInfo, Map context) {
		List<Object> entities = []

		// If the lookup is for a reference field then it is mandatory in the script to account for this via
		if ( ! fieldsInfo[propertyName].find?.query || fieldsInfo[propertyName].find.query.size() == 0 ) {
			addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, NO_FIND_QUERY_SPECIFIED_MSG)
		} else {
			// log.debug '_performQueryAndUpdateFindElement() for property {}: Searching with query={}', propertyName, fieldsInfo[propertyName].find?.query
			int recordsFound = 0
			int foundMatchOn = -1

			// Iterate over the list of Queries until something is found
			//  and update the find section appropriately.
			for (query in fieldsInfo[propertyName].find.query) {
				foundMatchOn++

				// Use the ETL find logic to try searching for the domain entities
				ETLDomain whereDomain = ETLDomain.lookup(query.domain)
				entities = DomainClassQueryHelper.where(whereDomain, context.project, query.kv, false)

				recordsFound = entities.size()
				if (recordsFound > 0) {
					break
				}
			}

			// Update the field section of the fieldsInfo with the results of the this series of queries
			fieldsInfo[propertyName].find.with() {
				matchOn = (recordsFound > 0 ? foundMatchOn : 0)
				fieldsInfo[propertyName].find.size = recordsFound
				fieldsInfo[propertyName].find.results = entities*.id
			}

			log.debug '_performQueryAndUpdateFindElement() for property={}, find={}', propertyName, fieldsInfo[propertyName].find
			// Record error on the field if more than one entity was found
			if (recordsFound > 1) {
				addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, FIND_FOUND_MULTIPLE_REFERENCES_MSG)
			}
		}

		return entities
	}

	/**
	 * Called by fetchEntityByFieldMetaData
	 * Used by the createReferenceDomain to locate other reference domain objects (e.g. manufacturer or model) that will be set
	 * on the entity being created.
	 * @param entity - the Entity that is being created
	 * @param refDomainPropName - the property name of the entity for which the reference is going to be searched
	 * @param fieldsInfo - the information map of all of the parent record properties
	 * @param referenceFieldName - the field name in the parent record for which the reference domain is being searched (e.g. roomSource of AssetEntity)
	 * @param context - the map that contains the holy grail of the Import Batch processing
	 * @return A map containing
	 *		entities: List of reference domain entities that were found
	 * 		error: A String with any error encountered
	 */
	@Transactional(noRollbackFor=[Exception])
	Map _fetchEntityByAlternateKey(Class domainClass, String searchValue, String referenceFieldName, Map fieldsInfo, Map context) {
		Map result = [entities: [], error: '']
		log.debug '_fetchEntityByAlternateKey() domainClass {}, searchValue {}', domainClass.getName(), searchValue

		if (searchValue?.size() > 0) {
			// Class refDomainClass = GormUtil.getDomainPropertyType(domainClass, refDomainPropName)
			String refDomainName = GormUtil.domainShortName(domainClass)

			// Make sure that the domain has an alternateLookup defined on the class
			// if (! GormUtil.getAlternateKeyPropertyName(refDomainClass)) {
			// 	result.error = "Reference ${refDomainPropName} of domain ${refDomainName} does not support alternate key lookups")
			// 	return result
			// }

			Map extraCriteria = [:]
			// TODO : JPM 6/2018 : This requires that we have access to the parent instance so we can snag manufacturer or other related fields
			/*
			if (refDomainName == 'Model') {
				// The first query of Model will be by Name + Mfg & assetType (if they are specified)
				if (entity.manufacturer) {
					extraCriteria.put('manufacturer', entity.manufacturer)
				} else {
					result.error = 'Manufacturer is required in order to find model by alternate key reference'
					return result
				}

				// TODO : JPM 6/2018 : why did I add this as additional criteria? May need to add back
				// if (entity.assetType) {
				// 	extraCriteria.put('assetType', entity.assetType)
				// }
			}
			*/

			// TODO : JPM 6/2018 : Searching rooms/racks requires knowing the target field that we're looking up the resource. Therefore
			// we need to pass the parentPropertyName into this logic...
			switch (refDomainName) {
				case 'Room':
					// Get the Location field
					extraCriteria.put('source', (referenceFieldName == 'roomSource' ? 1 : 0))
					break

				case 'Rack':
					// Resolve the Room first
					boolean isSource = referenceFieldName == 'rackSource'
					String roomFieldName = isSource ? 'roomSource' : 'roomTarget'
					if (fieldsInfo.containsKey(roomFieldName)) {
						Room room = fetchEntityByFieldMetaData(roomFieldName, fieldsInfo, context)
						if (! room) {
							result.error = 'Unable to resolve room'
						} else {
							extraCriteria.put('room.id', room.id)
						}
					} else {
						result.error = 'Room must be included to set rack'
					}

					extraCriteria.put('source', (isSource ? 1 : 0))
					break

				case 'Model':
					// Need to get the Manufacturer ID
					// TODO : 6/2018 : properly get the mfg id
					if (fieldsInfo.containsKey('manufacturer')) {
						Manufacturer mfg = fetchEntityByFieldMetaData('manufacturer', fieldsInfo, context)
						if (! mfg) {
							result.error = 'Unable to resolve manufacturer'
						} else {
							extraCriteria.put('manufacturer.id', mfg.id)
						}
					} else {
						result.error = 'Manufacturer must be included to set model'
					}
					break
			}

			if (! result.error) {
				List entities = GormUtil.findDomainByAlternateKey(domainClass, searchValue, context.project, extraCriteria)
				int numFound = entities ? entities.size() : 0
				log.debug '_fetchEntityByAlternateKey() domainClass={}, searchValue={}, extraCriteria={}, found={}',
					domainClass.getName(), searchValue, extraCriteria, numFound

				if (numFound > 0) {
					result.entities = entities
				} else {
					// what to do here?
				}
			}
		}

		return result
	}

	/**
	 * Used fetch the person based on a string search value
	 *
	 * The logic will first check to see if the name matches that of the person currently assigned to the
	 * field if pre-existing then if not it will then use the PersonService to lookup the person by their name.
	 *
	 * When errors encountered or multiple references found then an error will be recorded into the fieldsInfo appropriately.
	 *
	 * Note this this method is dependent on the staffList being populated in the context object for performances reasons.
	 * It also assumes that the value is not the person ID and that there is a searchValue (not null).
	 *
	 * @param existingPerson - the existing person for update operations where field previously set otherwise null
	 * @param searchValue - the name or email address of the person to fetch
	 * @param fieldName - the field name of the Person object
	 * @param fieldsInfo - the Map of the fields
	 * @param context - the context containing the goodies for the import batch process
	 * @return a list containing:
	 *     1) the Person if found/created
	 *     2) an error message if an error encountered or multiple references were found
	 */
	@Transactional(noRollbackFor=[Exception])
	List _fetchPerson(Person existingPerson, String searchValue, String fieldName, Map fieldsInfo, Map context) {
		Person person
		String errorMsg
		Boolean isEmail = searchValue.contains('@')

		// println "*** _fetchPerson() Existing Person ${existingPerson ? existingPerson.toString() + ' ' + existingPerson.email : 'null'}, searchBy $searchValue"
		// If the pre-existing person check if searchValue matches the person
		if (existingPerson) {
			if ( (isEmail && existingPerson.email.equalsIgnoreCase(searchValue) ) ||
				 (! isEmail && existingPerson.toString().equalsIgnoreCase(searchValue))
			) {
				person = existingPerson
			}
		}

		if (!person) {
			if (isEmail) {
				person = context.staffList.find { it.email.equalsIgnoreCase(searchValue) }
				if (!person) {
					errorMsg = 'Unable to find person by email address'
				}
			} else {
				try {
					Map resultMap = personService.findOrCreatePerson(searchValue, context.project, context.staffList)
					if (resultMap) {
						if (resultMap.isAmbiguous) {
							errorMsg = 'Multiple references found for value'
						} else if (resultMap.person) {
							person = resultMap.person
						}
					}
					if (!resultMap || ! resultMap.person && ! resultMap.isAmbiguous) {
						errorMsg = 'Unable to locate person'
					}
				} catch (e) {
					errorMsg = e.message
				}
			}
		}

		return [person, errorMsg]
	}

	/**
	 * Used to determine what the actual class is of a particular domain property. In the case of AssetEntity the logic
	 * logic needs to determine which type is actually intended based on the ETLDomain property name (e.g. Device, Asset, etc)
	 * @param propertyName - the property to get the class type for
	 * @param fieldsInfo - the ETL info on the fields of the entity
	 * @param context - the process context map
	 * @return the class name of the property
	 */
	@Transactional(noRollbackFor=[Exception])
	private Class classOfDomainProperty(String propertyName, Map fieldsInfo, Map context) {
		Class domainClassToCreate
		ETLDomain ed
		String errorMsg

		if (! GormUtil.isDomainProperty(context.domainClass, propertyName)) {
			errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_DOMAIN, [propertyName:propertyName])
			log.debug 'classOfDomainProperty() {}', errorMsg
		} else {
			while ( true ) {
				if (propertyName == 'id') {
					domainClassToCreate = context.domainClass
					break
				}

				if (! fieldsInfo.containsKey(propertyName)) {
					errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_FIELDS, [propertyName:propertyName])
					log.debug 'classOfDomainProperty() {}', errorMsg
					break
				}

				Boolean isIdentifierProperty = GormUtil.isDomainIdentifier(context.domainClass, propertyName)
				Boolean isReferenceProperty = GormUtil.isReferenceProperty(context.domainClass, propertyName)
				log.debug 'classOfDomainProperty() for property {}, isIdentifierProperty {}, isReferenceProperty {}', propertyName, isIdentifierProperty, isReferenceProperty

				// propertyName MUST be a reference or identifier for this function otherwise record an error
				if (! ( isIdentifierProperty || isReferenceProperty ) ) {
					errorMsg = WHEN_NOT_FOUND_PROPER_USE_MSG
					log.debug 'classOfDomainProperty() {}', errorMsg
					break
				}

				if (isIdentifierProperty) {
					log.debug 'classOfDomainProperty() is the identifier'
					domainClassToCreate = context.domainClass
					break
				}

				// Get the type for the property of domain class being processed by the batch
				domainClassToCreate = GormUtil.getDomainPropertyType(context.domainClass, propertyName)

				if (isReferenceProperty) {
					// We need to try and resolve what class to create. Most times it is just the class type of the property in the
					// parent domain. In the case of AssetEntity however the class could be AssetEntity, Application, Database, etc.
					// In order to know which the assumption is that there will be a find.query and that the first search is going to
					//be precisely what that DataScript developer intended to be created.

					String classShortName = GormUtil.domainShortName(domainClassToCreate)
					if (classShortName in ['AssetEntity']) {
						// Try looking for the exact class type in the find.query
						List query = fieldsInfo[propertyName].find?.query
						if (query?.size() > 0) {
							ed = ETLDomain.lookup(query[0].domain)
							domainClassToCreate = ed.getClazz()
						} else {
							// Need to look into the create kv map for 'assetClass' to see if the DataScript developer specified it
							Map createInfo = fieldsInfo[propertyName].create ?: [:]
							if (createInfo.containsKey('assetClass')) {
								ed = ETLDomain.lookup(createInfo['assetClass'])
								domainClassToCreate = ed.getClazz()
							}
						}
					}
					break
				}

				break
			}
		}

		if (errorMsg) {
			addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, errorMsg)
		}

		log.debug 'classOfDomainProperty() for property {} for class {} type is {}',
			propertyName,
			context.domainClass.getName(),
			( domainClassToCreate ? domainClassToCreate.getName() : 'Not a Reference' )

		return domainClassToCreate
	}

	/**
	 * Used to swap around the order of properties when processing fields
	 * For some functionality the order of the fields will be critial that one or more are done ahead of others such
	 * as Manufacturer and Model.
	 * @param fieldNames - a set of the field names to reorder
	 * @return the reordered list
	 */
	@Transactional(noRollbackFor=[Exception])
	private List fixOrderInWhichToProcessFields(Set fieldNames) {
		List list = fieldNames.toList()
		// TODO : JPM 4/2018 : Disabled this functionality until I can figure out why the Set order is screwed up.
		/*
 		int indexOfMfg = list.indexOf('manufacturer')
		int indexOfModel = list.indexOf('model')
		if (indexOfMfg && indexOfModel && indexOfMfg > indexOfModel) {
			log.debug 'fixOrderInWhichToProcessFields found mfg in {} and model in {}', indexOfMfg, indexOfModel
			// Need to swap the two around
			list[indexOfMfg] = 'model'
			list[indexOfModel] = 'manufacturer'
		}
		*/
		return list
	}

	/**
	 * Used to retrieve the value and initialize values from the fieldsInfo for a fieldName
	 * @param fieldName
	 * @param fieldsInfo
	 * @return List containing [value, initialValue]
	 */
	@Transactional(noRollbackFor=[Exception])
	private List getValueAndInitialize(String fieldName, Map fieldsInfo) {
		def value = fieldsInfo[fieldName]['value']
		def init = fieldsInfo[fieldName]['init']

		// Note the test of initValue and fieldName being a LazyMap. In testing it was discovered that accessing certain JSONObject node elements was
		// returning a LazyMap instead of a null value. Tried to reproduce in simple testcase but unsuccessful therefore had to add this
		// extra test.  See ticket TM-10981.
		value = (value instanceof groovy.json.internal.LazyMap) ? null : value
		init = (init instanceof groovy.json.internal.LazyMap) ? null : init
		return [value, init]
	}

	/**
	 * Returns the initialize value or value from the fieldsInfo of a field
	 * @param fieldName
	 * @param fieldsInfo
	 * @return the initialize value if set otherwise the value property
	 */
	@Transactional(noRollbackFor=[Exception])
	private Object getValueOrInitialize(String fieldName, Map fieldsInfo) {
		def (value, init) = getValueAndInitialize(fieldName, fieldsInfo)
		return (init != null ? init : value)
	}

	/**
	 * Used to clear out the errors recorded at the field level and row level and the record level count
	 * @param record - the import batch record to clear
	 * @param fieldsInfo - the Map of the record.fieldsInfo to be cleared
	 */
	@Transactional(noRollbackFor=[Exception])
	private void resetRecordAndFieldsInfoErrors(ImportBatchRecord record, Map fieldsInfo) {
		record.errorCount = 0
		record.resetErrors()
		for (field in fieldsInfo) {
			field.value.errors = []
		}
	}

	/**
	 * This is used to perform the validate on a domain object and will save the errors back into the
	 * fieldsInfo map appropriately or into the ImportBatchRecord if the constraint failure was on a property
	 * that is not in the fieldsInfo.
	 *
	 * @param domain - the domain that is being created or updated
	 * @param record - the import record being processed (errors can be logged to this object)
	 * @param fieldsInfo - the Map of the fields that came from the ETL process
	 * @return true if an error was recognized otherwise false
	 */
	@Transactional(noRollbackFor=[Exception])
	private Boolean recordDomainConstraintErrorsToFieldsInfoOrRecord(Object domain, ImportBatchRecord record, Map fieldsInfo) {
		boolean errorsFound = ! domain.validate()

		if (errorsFound) {

			for (error in domain.errors.allErrors) {
				log.debug "recordDomainConstraintErrorsToFieldsInfoOrRecord() error: $error"
				String property = error.getField()
				String errorMsg = i18nMessage(error)
				if (fieldsInfo[property]) {
					fieldsInfo[property].errors << errorMsg
				} else {
					// A contraint failed on a property that wasn't one of the fields in the fields loaded from the ETL
					record.addError(errorMsg)
				}
			}
		}

		return errorsFound
	}

	/**
	 * Used to add error message to either the fieldsInfo or directly to the record. The error will be added to the fieldsInfo if the
	 * property name exists otherwise the message is stuffed into the record directly
	 *
	 * @param propertyName - the propertyName that the error should be recorded against
	 * @param fieldsInfo - yeah that map of the fields from ETL
	 * @param context - the context that has a reference to the ImportBatchRecord that the error may be stuffed into
	 * @param errorMsg - the obvious error message
	 */
	@Transactional(noRollbackFor=[Exception])
	private void addErrorToFieldsInfoOrRecord(String propertyName, Map fieldsInfo, Map context, CharSequence errorMsg) {
		if (propertyName && fieldsInfo[propertyName]) {
			fieldsInfo[propertyName].errors << errorMsg.toString()
		} else {
			context.record.addError(errorMsg.toString())
		}
	}

	/**
	 * Used to access the error list for the infoFields of a particular field/property
	 */
	@Transactional(noRollbackFor=[Exception])
	private List getFieldsInfoFieldErrors(String propertyName, Map fieldsInfo) {
		return fieldsInfo[propertyName].errors
	}

	/**
	 * Used to tally the number of errors that were recorded on the import record and/or on
	 * the individual fields within the fieldsInfo Map.
	 * @param record - the Import Record being assessed
	 * @param fieldsInfo - the Map of fields from the ETL process
	 * @return the number of errors found
	 */
	@Transactional(noRollbackFor=[Exception])
	private Integer tallyNumberOfErrors(ImportBatchRecord record, Map fieldsInfo) {
		Integer count = 0
		for (field in fieldsInfo) {
			count += (field.value['errors'] ? field.value['errors'].size() : 0 )
		}
		count += record.errorListAsList().size()
		return count
	}

	/**
	 * Used to generate the MD5 value of the Map that is used to query for a domain of a particular
	 * fieldName. This will toString the Map of fieldName query names/values in order to create an unique key
	 * that can be used to cache the results afterward.
	 *
	 * The MD5 string will be composed like the following:
	 *		Dependency:asset
	 *		:value=123:
	 *		query=[[assetName:"xraysrv01", assetType:"VM"]]
	 *
	 * @param fieldName - the name of the field to fetch the Query element from the map
	 * @param fieldsInfo - the Map of all of the fields for the current row that came from the ETL process
	 * @return the MD5 32 character String of the query element
	 */
	@Transactional(noRollbackFor=[Exception])
	private String generateMd5OfFieldsInfoField(String domainShortName, String fieldName, Map fieldsInfo) {
		StringUtil.md5Hex(
			"${domainShortName}:${fieldName}" +
			":value=${fieldsInfo[fieldName].value}:query=" +
			( fieldsInfo[fieldName].find.containsKey('query') ? fieldsInfo[fieldName].find.query.toString() : 'NO-QUERY-SPECIFIED')
		)
	}

	/**
	 * Transform the Data from ETL as part of the Asset Import process' first step.
	 *
	 * @param project
	 * @param dataScriptId
	 * @param filename
	 * @return
	 */
	@NotTransactional()
	Map transformEtlData(Long projectId, Long dataScriptId, String filename, String progressKey = null) {
		Map result = [filename: '']
		Project project = Project.get(projectId)

		// TODO : SL - 05/2018 : Find if we still need to keep this validation since it was already done
		// when scheduled a transformation job
		DataScript dataScript = null
		String errorMsg = null

		if (!dataScriptId) {
			errorMsg = 'Missing required dataScriptId parameter'
		} else if (!filename) {
			errorMsg = 'Missing filename parameter'
		} else if (!fileSystemService.temporaryFileExists(filename)) {
			errorMsg = 'Specified input file not found'
		} else {
			List<String> allowedExtensions = fileSystemService.getAllowedExtensions()
			if (!FileSystemUtil.validateExtension(filename, allowedExtensions)) {
				errorMsg = i18nMessage(Message.FileSystemInvalidFileExtension)
			} else {
				// See if we can find a DataScript.
				dataScript = GormUtil.findInProject(project, DataScript, dataScriptId, true)

				if (!dataScript.etlSourceCode) {
					errorMsg = 'DataScript has no source specified'
				}
			}
		}

		// If there was an error in the previous validations, throw an exception.
		if (errorMsg) {
			throw new InvalidParamException(errorMsg)
		}

		// The progress closure that will be used by the ETL process to report back to this service the overall progress
		ProgressCallback updateProgressClosure = { Integer percentComp, Boolean forceReport, ProgressCallback.ProgressStatus status, String detail ->
			// if progress key is not provided, then just skip updating progress service
			// this is useful during integration test invocation
			if (progressKey) {
				// log.debug "updateProgressClosure() ${percentComp}%, forceReport=$forceReport, status=$status, detail=$detail"
				progressService.update(progressKey, percentComp, status.name(), detail)
			}
		} as ProgressCallback

		// get full path of the temporary file containing data
		String inputFilename = fileSystemService.getTemporaryFullFilename(filename)

		// TODO : JPM 6/2018 : TM-11017 This call fails silently in one of the DataImportServiceIntegrationSpec tests
		log.debug "transformEtlData() calling scriptProcessorService.executeAndSaveResultsInFile"
		def (ETLProcessor etlProcessor, String outputFilename) = scriptProcessorService.executeAndSaveResultsInFile(
			project,
			dataScript?.id,
			dataScript.etlSourceCode,
			inputFilename,
			updateProgressClosure)

		log.debug "transformEtlData() returned from call"

		result.filename = outputFilename

		return result
	}

	/**
	 * Schedule a quartz job for the ETL transform data process
	 * @param project
	 * @param dataScriptId
	 * @param filename
	 * @return Map - containing the progress key created to monitor the job execution progress
	 */
	@NotTransactional()
	Map<String, String> scheduleETLTransformDataJob(Project project, Long dataScriptId, String filename) {
		DataScript dataScript = null
		String errorMsg = null

		if (!dataScriptId) {
			errorMsg = 'Missing required dataScriptId parameter'
		} else if (!filename) {
			errorMsg = 'Missing filename parameter'
		} else if (!fileSystemService.temporaryFileExists(filename)) {
			errorMsg = 'Specified input file not found'
		} else {
			List<String> allowedExtensions = fileSystemService.getAllowedExtensions()
			if (!FileSystemUtil.validateExtension(filename, allowedExtensions)) {
				errorMsg = i18nMessage(Message.FileSystemInvalidFileExtension)
			} else {
				// See if we can find a DataScript and it belongs to current user project.
				dataScript = GormUtil.findInProject(project, DataScript, dataScriptId, true)

				if (!dataScript.etlSourceCode) {
					errorMsg = 'DataScript has no source specified'
				}
			}
		}

		// If there was an error in the previous validations, throw an exception.
		if (errorMsg) {
			throw new InvalidParamException(errorMsg)
		}

		String key = 'ETL-Transform-Data-' + dataScriptId + '-' + StringUtil.generateGuid()
		progressService.create(key, ProgressService.PENDING)

		// Kickoff the background job to generate the tasks
		def jobTriggerName = 'TM-ETLTransformData-' + project.id + '-' + dataScriptId + '-' + StringUtil.generateGuid()

		// The triggerName/Group will allow us to controller on import
		Trigger trigger = new SimpleTriggerImpl(jobTriggerName)
		trigger.jobDataMap.projectId = project.id
		trigger.jobDataMap.dataScriptId = dataScriptId
		trigger.jobDataMap.filename = filename
		trigger.jobDataMap.progressKey = key
		trigger.setJobName('ETLTransformDataJob')
		trigger.setJobGroup('tdstm-etl-transform-data')
		quartzScheduler.scheduleJob(trigger)

		log.info('scheduleJob() {} kicked of an ETL transform data process for script and filename ({},{})',
				securityService.currentUsername, dataScriptId, filename)

		// return progress key
		return ['progressKey': key]
	}
}

		// The following was my initial attempt to get the marshalling of the fieldsInfo JSON into a hierarchical set of Command
		// Objects.


		// ETLDataRecordFieldsCommand record = JsonUtil.mapToObject(importBatchRecord.fieldsInfo, ETLDataRecordFieldsCommand)
		// ETLDataRecordFieldsCommand record = JsonUtil.mapToObject(importBatchRecord.fieldsInfo, new TypeReference<Map<String, ETLDataRecordFieldsCommand>>(){} )
		// new TypeReference<Map<String, String>>(){}
		/*
		 * Tried https://www.mkyong.com/java/how-to-convert-java-map-to-from-json-jackson/ with no luck
		 */
		// Map<String, ETLDataRecordFieldsPropertyCommand> record = new HashMap<String, ETLDataRecordFieldsPropertyCommand>();
		// try {
		// 	ObjectMapper mapper = new ObjectMapper();
		// 	map = mapper.readValue(importBatchRecord.fieldsInfo, new TypeReference<Map<String, ETLDataRecordFieldsPropertyCommand>>(){});
		// } catch (JsonGenerationException e) {
		// 	println "${e.getMessage()}"
		// } catch (JsonMappingException e) {
		// 	println "${e.getMessage()}"
		// } catch (IOException e) {
		// 	println "${e.getMessage()}"
		// }
		//  def record = bindData( ETLDataRecordFieldsCommand, importBatchRecord.fieldsInfo, '')

		// @SuppressWarnings("unchecked")
		// private List convertToListIfString(Object o) {
		// 	if (o instanceof CharSequence) {
		// 		List list = new ArrayList();
		// 		list.add(o instanceof String ? o : o.toString());
		// 		o = list;
		// 	}
		// 	return (List) o;
		//  }


/**
 * DataImportEntityCache is a caching object used for the Data Import that retains the results of
 * searches based on the query specs of a field. The cache will contain the error of the query results
 * or the cache of the entity as a Map containing the domain and it's ID. The get method will reconstitute
 * the object. This was done so that the cache doesn't load ALL of the objects into memory.
 */
class DataImportEntityCache {
	Map cache = [:]

	// Used primarilly for testing
	String lastKey = null

	/**
	 * Used to retrieve the cached Entity or Error Message
	 *
	 * If the entity was cached then it will be refetched from the database as it may have changed
	 * and we want to avoid ballooning memory with all domain objects in memory.
	 * @param key - the key that the reference was stored
	 * @return String error or domain entity object
	 */
	Object get(String key) {
		Object value = cache[key]

		if (value instanceof Map) {
			value = value.clazz.get(value.id)
		}

		return value
	}

	/**
	 * Used to add an object to the cache
	 * @param key - the key that the object will be referencable by
	 * @param value - the entity object or error message
	 */
	void put(String key, Object value) {
		lastKey = key
		if (GormUtil.isDomainClass(value)) {
			// For domain objects we are caching the domain Class and ID
			cache.put(key, [clazz: value.getClass(), id: value.id])
		} else {
			cache.put(key, value)
		}
	}

	/**
	 * Returnes the # of objects contained in the cache
	 */
	Long size() {
		return cache.size()
	}
}


	/**
	 * Used to set a property onto a domain object
	 * @param domain - the domain entity to be manipulated
	 * @param propertyName - the property to be set
	 * @param value - the value to set onto the domain object
	 * @param parentPropertyName - the property name of the parent property that the domain will be assigned to
	 * @param context - the grand poopa of objects for the Import Process
	 * @return null if successful otherwise a string containing the error message that occurred
	 */
	// private String setDomainPropertyWithValue(Object domainInstance, String propertyName, Object value, String parentPropertyName, Map fieldsInfo, Map context) {
	// 	String errorMsg = null
	// 	while (true) {
	// 		// TODO : JPM 4/2018 : Lookup the FieldSpecs for assets to get the label names for errors
	// 		if ( (propertyName in PROPERTIES_THAT_CANNOT_BE_MODIFIED) ) {
	// 			// errorMsg = "Field ${propertyName} can not be set by 'whenNotFound create' statement"
	// 			errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_CANNOT_BE_SET_MSG, [propertyName:propertyName])
	// 			break
	// 		} else {
	// 			if (! GormUtil.isDomainProperty(domainInstance, propertyName)) {
	// 				// TODO : JPM 6/2018 : Error message to specific - not only for whenNotFound
	// 				errorMsg = "Unknown field ${propertyName} in 'whenNotFound create' statement"
	// 				break
	// 			}

	// 			// Check if the field is a reference property
	// 			if (GormUtil.isReferenceProperty(domainInstance, propertyName)) {
	// 				// TODO : JPM 6/2016 : fix do to parentPropertyName being dropped from method and return value changes
	// 				Object refObject = findDomainReferenceProperty(domainInstance, propertyName, value, parentPropertyName, fieldsInfo, context)
	// 				if (refObject instanceof CharSequence) {
	// 					errorMsg = refObject
	// 				} else {
	// 					// Only set if different so as not to trigger the dirty flag unnecessarily
	// 					if (domainInstance[propertyName] != refObject) {
	// 						domainInstance[propertyName] = refObject
	// 					}
	// 				}
	// 				// if (NumberUtil.isaNumber(value)) {
	// 				// 	// Perform lookup by ID
	// 				// 	// TODO : JPM 4/2018 : Refactor into Gorm as a single function
	// 				// 	Class refDomainClass = GormUtil.getDomainPropertyType(domainClassToCreate, propertyName)
	// 				// 	def entity = GormUtil.findInProject(context.project, refDomainClass, value)

	// 				// 	if (entity) {
	// 				// 		domainInstance[propertyName] = entity
	// 				// 	} else {
	// 				// 		errorMsg = "No reference record found for field ${propertyName} (${value}) by ID in 'whenNotFound create'"
	// 				// 		break
	// 				// 	}
	// 				// 	// searchForDomainById(refDomainClass, propertyName, value, fieldsInfo, context)
	// 				// } else if (! StringUtil.isBlank(value)) {
	// 				// 	// Attempt the find the reference by the alternate key
	// 				// 	List references = _fetchEntityByAlternateKey(domainInstance, propertyName, value, parentPropertyName, fieldsInfo, context)
	// 				// 	Integer numFound = references.size()
	// 				// 	if (numFound == 1) {
	// 				// 		domainInstance[propertyName] = references[0]
	// 				// 	} else if (numFound > 1) {
	// 				// 		errorMsg = "Multiple references found for field ${propertyName} by Name ($value) in 'whenNotFound create'"
	// 				// 		break
	// 				// 	} else {
	// 				// 		errorMsg = "No reference record found for field ${propertyName} by Name ($value) in 'whenNotFound create'"
	// 				// 		break
	// 				// 	}
	// 				// }
	// 			} else {
	// 				// Just a normal data type (e.g. Date, Integer, String, etc)
	// 				// TODO : JPM 4/2018 : Need to deal with ENUM and Date being resolved
	// 				if (domainInstance[propertyName] != value) {
	// 					domainInstance[propertyName] = value
	// 				}
	// 			}
	// 		}
	// 		break
	// 	}
	// 	return errorMsg
	// }

	/**
	 * Used to create a domain record using the "create" structure in the ETL meta-data for a specified property. If the
	 * reference domain entity could not be created then the error(s) will be recorded into the property of fieldsInfo map.
	 *
	 * @param propertyName - the name of the property in the fields ETL datastructure
	 * @param context - the context map that the process uses to cart crap around
	 * @return the reference domain object if successfully created
	 */
	// private Object createReferenceDomain(String propertyName, Map fieldsInfo, Map context) {
	// 	Object entity
	// 	String errorMsg
	// 	Map createInfo



	// 	log.debug 'createReferenceDomain() CREATING reference entity for property {}', propertyName
	// 	while (true) {
	// 		if (!fieldsInfo.containsKey(propertyName)) {
	// 			errorMsg = "Property $propertyName was missing from ETL meta-data"
	// 			break
	// 		}



	// 		createInfo = fieldsInfo[propertyName].create



	// 		if (! createInfo) {
	// 			errorMsg = "Missing necessary 'whenNotFound create' information to create new asset"
	// 			break
	// 		}



	// 		Class domainClassToCreate = classOfDomainProperty(propertyName, fieldsInfo, context)
	// 		if (domainClassToCreate == null) {
	// 			errorMsg = "Unable to determine class for property $propertyName"
	// 			break
	// 		}



	// 		// Determine what we're trying to create by looking at the property and getting the Domain class
	// 		entity = domainClassToCreate.newInstance()



	// 		// load with the values from the create key/value pairs
	// 		List fieldNames = fixOrderInWhichToProcessFields(createInfo.keySet())
	// 		for (fieldName in fieldNames) {
	// 			errorMsg = setDomainPropertyWithValue(entity, fieldName, createInfo[fieldName], propertyName, fieldsInfo, context)
	// 			// TODO : JPM 4/2018 : Change so that all errors are recorded against the reference field instead of just the first error encountered (Augusto)
	// 			if (errorMsg) {
	// 				break
	// 			}
	// 		}
	// 		if (errorMsg) {
	// 			break
	// 		}



	// 		//
	// 		// Handle different required properties
	// 		// TODO : JPM 4/2018 : Refactor to make this method smaller and more testable
	// 		//



	// 		// Project
	// 		if (GormUtil.isDomainProperty(entity, 'project')) {
	// 			entity.project = context.project
	// 		}



	// 		// MoveBundle
	// 		if (GormUtil.isDomainProperty(entity, 'moveBundle') && ! entity.moveBundle ) {
	// 			// TODO : JPM 4/2018 : The bundle may have been set with reference logic above
	// 			entity.moveBundle = context.project.getProjectDefaultBundle()
	// 		}



	// 		if (! entity.validate() ) {
	// 			log.debug "createReferenceDomain() failed : {}", GormUtil.allErrorsString(entity)
	// 			// TODO : JPM 4/2018 : Change to populate field with list of i18n errors (August - good one to work on)
	// 			// Populate field with list of errors
	// 			// clear the error message
	// 			// entity.discard()
	// 			errorMsg = "Failed to create record for $propertyName : " + GormUtil.errorsAsUL(entity)
	// 			break
	// 		}



	// 		// TODO : JPM 3/2018 : change failOnError:false throughout this code at some point
	// 		//log.info "Creating $propertyName reference for domain ${context.domainShortName} : $entity"
	// 		entity.save(failOnError:true, flush:true)



	// 		// Replace the cache reference of the query with that of the new entity
	// 		String md5 = generateMd5OfFieldsInfoField(context.domainShortName, propertyName, fieldsInfo)
	// 		log.debug "createReferenceDomain() replaced cache $md5 with {}", entity
	// 		// TODO : JPM 4/2018 : change to cache the ID of the object instead of the object to conserve memory
	// 		context.cache.put(md5, entity)



	// 		break
	// 	} // while(true)



	// 	if (errorMsg) {
	// 		if (entity) {
	// 			entity.discard()
	// 		}
	// 		log.warn 'createReferenceDomain() failed - property={}, error={}, create={}', propertyName, errorMsg, createInfo
	// 		addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, errorMsg)
	// 	}



	// 	return entity
	// }

	/**
	 * Used to find the a domain entity or reference domain when the field.value is a String and the domain has an
	 * alternate key defined. Note that this lookup is generally only useful for domain entities that typical have
	 * unique names. For domains such as Application this method will more than likely return multiple results.
	 *
	 * @param propertyName - the property name that the id value will be used and will be populated with error if not found
	 * @param fieldsInfo - the fields map that came from the ETL process
	 * @return the list of domain entities found for the property
	 */
	// private List<Object> XXfindDomainByAlternateProperty(String propertyName, Map fieldsInfo, Map context) {
	// 	String notFoundByID = 'Entity was not found by ID'
	// 	List<Object> entities=[]

	// 	def value = fieldsInfo[propertyName]?.value
	// 	if ( value && (value instanceof CharSequence) ) {
	// 		Class domainClass = GormUtil.getDomainClassOfProperty(context.domainClass, propertyName)
	// 		entities = GormUtil.findDomainByAlternateKey(domainClass, value, propertyName, context.project)
	// 		log.debug 'findDomainByAlternateProperty() found={}',entities?.size()
	// 	}
	// 	return entities
	// }

	/**
	 * Used to find a single domain object by using various techniques including:
	 *    1. By ID
	 *    2. By find/elseFind
	 *    3. By String value (Alternate Key or special handlers such as Person)
	 * @param domainInstance
	 * @param propertyName
	 * @param value
	 * @param fieldsInfo
	 * @param context
	 * @return A String if there was an error or the Entity object
	 */
	// private Object XXfindDomainReferenceProperty(Object domainInstance, String propertyName, Object value, Map fieldsInfo, Map context) {
	// 	Object entity = null
	// 	String errorMsg = null

	// 	// TODO : JPM : 6/2018 : Check find results first ?

	// 	Class refDomainClass = GormUtil.getDomainPropertyType(domainInstance, propertyName)

	// 	Long id = NumberUtil.isaNumber(value) ? value : null
	// 	boolean isaString = (value instanceof CharSequence)
	// 	if ( id == null && isaString) {
	// 		id = NumberUtil.toPositiveLong(value)
	// 	}

	// 	log.debug 'findDomainReferenceProperty() Searching {}, property {}, value {}, isaString {}, id {}',
	// 		refDomainClass?.getName(), propertyName, value, isaString, id

	// 	if (id) {
	// 		// Perform lookup by ID
	// 		// TODO : JPM 4/2018 : Refactor into Gorm as a single function
	// 		entity = GormUtil.findInProject(context.project, refDomainClass, id)
	// 	} else if (isaString && ! StringUtil.isBlank(value)) {
	// 		// Attempt the find the reference by the alternate key
	// 		// List references = _fetchEntityByAlternateKey(domainInstance, propertyName, value, parentPropertyName, fieldsInfo, context)
	// 		// TODO : JPM 6/2018 : does _fetchEntityByAlternateKey need the propertyName argument?
	// 		Map result = _fetchEntityByAlternateKey(refDomainClass, value, propertyName, fieldsInfo, context)
	// 		if (result.error) {
	// 			errorMsg = result.error
	// 		} else {
	// 			int numFound = result.entities.size()
	// 			if (numFound == 1) {
	// 				entity = result.entities[0]
	// 			} else if (numFound > 1) {
	// 				errorMsg = 'Multiple references found'
	// 			}
	// 		}
	// 	}

	// 	log.debug 'findDomainReferenceProperty() result {}', (errorMsg ?: entity)

	// 	return errorMsg ?: entity
	// }

