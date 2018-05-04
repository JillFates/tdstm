package net.transitionmanager.service

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.etl.DataImportHelper
import com.tdsops.etl.DomainClassQueryHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.FileSystemUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StopWatch
import com.tdssrc.grails.StringUtil
import grails.converters.JSON
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.DataTransferValue
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.annotation.Propagation
// LEGACY
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
	ScriptProcessorService scriptProcessorService
	SecurityService securityService

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

	// List of the domains that are supported by the Legacy Batch import
	static final Map LEGACY_DOMAIN_CLASSES = [
			( ETLDomain.Application.name() ) : ETLDomain.Application,
			( ETLDomain.Database.name() ) : ETLDomain.Database,
			( ETLDomain.Device.name() ) : ETLDomain.Device,
			( ETLDomain.Storage.name() ) : ETLDomain.Storage
		]

	/**
	 * loadETLJsonIntoImportBatch - the entry point for the initial loading of data into the Import batches.
	 *
	 * This method will create a DataTransferBatch for each domain present in the importJsonData and then
	 * create DataTransferValues for each field the rows specified in the meta-data.
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
		return localFunction(project, userLogin, importJsonData)
	}

	// TODO : JPM 2/2018 : Delete this closure declaration and the following code will just be part of the above function
	// This was done because it allows you to save the code repeatedly without having to restart the application every time. It appears
	// that the @NotTransactional annotation causes issues. This was a neat trick to get around that since the loadETLJsonIntoImportBatch
	// method wasn't being changed. Groovy is more forgiving with Closures...
	def localFunction = { Project project, UserLogin userLogin, JSONObject importJsonData ->

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
			cache: [:],

			// The following are reset per domain
			domainClass: null,
			fieldNames: [],
			rowsCreated: 0,
			rowsSkipped: 0,
			rowNumber: 0,
			errors:[],
			isLegacy:false
		]

		// Iterate over the domains and create batches for each
		for (domainJson in importJsonData.domains) {
			importContext.domainClass = domainJson.domain
			importContext.isLegacy = isLegacy( importContext.domainClass )

			log.debug "localFunction() in for loop: importContext=$importContext"

			List<JSONObject> importRows = domainJson.data
			if (! importRows) {
				importResults.errors << "Domain ${importContext.domainClass} contained no data"
				importResults.domains << [ domainClass: importContext.domainClass, rowsCreated: 0, rowsSkipped: 0 ]
			} else {

				Class batchClass = (importContext.isLegacy ? DataTransferBatch : ImportBatch)

				// Process each batch in a separate transaction to help with performance and memory
				batchClass.withNewTransaction { session ->
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

					// Create a Transfer Batch for the asset class
					def batch = createBatch(importContext)

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
						importResults.domains << [ domainClass: importContext.domainClass, rowsCreated: importContext.rowsCreated ]
					}
				}
			}
		}

		return importResults
	}

	/**
	 * Used to determine if the importContext at the moment is for the Legacy DataImportBatch or the
	 * new ImportBatch system.
	 * @param domainName - the name of the domain used by the Import process
	 * @return true if Legacy otherwise false
	 */
	private Boolean isLegacy( String domainName ) {
		return LEGACY_DOMAIN_CLASSES.containsKey( domainName )
	}

	/**
	 * This is used to create either an ImportBatch or legacy DataTransferBatch object
	 * @param importContext - the map with all the juicy information used by the Import process
	 * @return either an ImportBatch or DataTransferBatch Domain instance or null if failed to be created
	 */
	private Object createBatch(Map importContext) {
		if ( importContext.isLegacy ) {
			return createDataTransferBatch(importContext)
		} else {
			return createImportBatch(importContext)
		}
	}

	/**
	 * LEGACY - Create a Transfer Batch for the given Asset Class.
	 * @param domainClass
	 * @param currentUser
	 * @param project
	 * @return
	 */
	private DataTransferBatch createDataTransferBatch(Map importContext) {
		DataTransferSet dts = DataTransferSet.findBySetCode('ETL')
		if (!dts) {
			dts = DataTransferSet.get(1)
		}

		String transDomainName = LEGACY_DOMAIN_CLASSES[importContext.domainClass]

		// Check if the domain class is valid
		EavEntityType eavEntityType = EavEntityType.findByDomainName(
			(transDomainName.equals('Device') ? 'AssetEntity' : transDomainName)
		)

		// If the asset class is invalid, return null
		if (! eavEntityType) {
			importContext.errors << "Import does not support domain type ${transDomainName}"
			return null
		}

		DataTransferBatch batch = new DataTransferBatch(
				project: importContext.project,
				userLogin: importContext.userLogin,
				statusCode: DataTransferBatch.PENDING,
				transferMode: "I",
				eavEntityType: eavEntityType,
				dataTransferSet: dts
			)

		// Check if the transfer batch is valid, report the error if not.
		if (! batch.save(failOnError:false)) {
			importContext.errors << "There was an error when creating the import batch for ${domainClass}"
			log.error 'DataImportService.createDataTransferBatch() failed save: {}', GormUtil.allErrorsString(batch)
			batch.discard()

			return null
		}

		return batch
	}

	/**
	 * NEW ETL - Create a ImportBatch record for a given Domain Class
	 * @param domainClass
	 * @param currentUser
	 * @param project
	 * @return a newly created ImportBatch object
	 */
	private ImportBatch createImportBatch( Map importContext ) {

		Date warnOnChangesAfter
		if (importContext.etlInfo.warnOnChangesAfter) {
			warnOnChangesAfter = new Date(importContext.etlInfo.warnOnChangesAfter)
		} else {
			warnOnChangesAfter = new Date()
		}

		ImportBatch batch = new ImportBatch(
				project: importContext.project,
				status: ImportBatchStatusEnum.PENDING,
				provider: importContext.etlInfo.provider,
				dataScript: (importContext.etlInfo.dataScript ?: ''),
				domainClassName: importContext.domainClass,
				createdBy: importContext.userLogin.person,
				// createdBy: importContext.etlInfo.createdBy,
				autoProcess: ( importContext.etlInfo.autoProcess ?: 0 ),
				dateFormat: ( importContext.etlInfo.dataFormat ?: ''),
				fieldNameList: JsonUtil.toJson(importContext.fieldNames),
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
	 * @param dataTransferBatch - current batch
	 * @param assets - list of assets
	 * @param importContext - additional parameters required for logging
	 */
	private void importRowsIntoBatch(session, Object batch, List<JSONObject> importRows, Map importContext) {
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
	 * @param dataTransferBatch
	 * @param asset - LazyMap with all the field information
	 * @param importContext - additional parameters required for logging
	 */
	private void importRow(session, Object batch, JSONObject rowData, Map importContext ) {
		boolean canImportRow=false

		Long domainId = getAndValidateDomainId(rowData, importContext)

		// TODO : JPM 3/2018 : Need to review this code further to see if we can clean this up
		// Process the row as long as there wasn't an error with the ID reference
		// if (domainId == null || domainId > 0) {

			// Validate that the row can be processed, any errors will be captured in importContext.errors
			// TODO : JPM 2/2018 : MINOR - Review and fix the canRowDataBeImported logic, wait for Dependency imports
			// canImportRow = canRowDataBeImported(rowData, domainId, importContext)
			canImportRow=true

			if (canImportRow) {
				if (importContext.isLegacy) {
					canImportRow = insertRowDataIntoDataTransferValues(session, batch, rowData, domainId, importContext )
				} else {
					canImportRow = insertRowDataIntoImportBatchRecord(session, batch, rowData, domainId, importContext )
				}
			}
		// }

		if (canImportRow) {
			importContext.rowsCreated++
		} else {
			importContext.rowsSkipped++
		}

	}

	/**
	 * LEGACY -- Create a new DataTransferValue for each field in the rowData. Any errors will be added to the importContext.errors list and
	 * the entire row will be skipped. This method is implemented with a NESTED transaction so that it will rollback all of the fields
	 * that were added prior to the error.
	 *
	 * @param dataTransferBatch - current batch
	 * @param rowData - the meta-data for the current field to be inserted
	 * @param domainId - the id for the domain object if known
	 * @param importContext - Map of the import context objects
	 * @return true if all of the fields were successfully added to the DataTransferValue table or false if there was an error
	 */
	private boolean insertRowDataIntoDataTransferValues(session, DataTransferBatch batch, JSONObject rowData, Long domainId, Map importContext ) {

		int rowNum = importContext.rowNumber - 1
		// Tried using sessionFactory.currentSession but that session object did not have the createSavepoint() method
		def savePoint = session.createSavepoint()

		Boolean hadError = false
		// Iterate over the list of field names that the ETL metadata indicates are in the rowData object
		for (fieldName in importContext.fieldNames) {

			// If the current field is the id, skip it (avoid inserting it into the database).
			if (fieldName != 'id') {

				JSONObject field = rowData.fields[fieldName]

				// TODO : JPM 2/2018 : MINOR - Revisit this later - this implementation is iffy - will be important for Dependency implementation
				// def fieldValue = DataImportHelper.resolveFieldValue(fieldName, field)

				def fieldValue = field?.value

				// Don't bother with String values that are empty or any types that are null
				if ( fieldValue == null || ( (fieldValue instanceof CharSequence) && StringUtil.isBlank(fieldValue) ) ) {
					continue
				}

				DataTransferValue batchRecord = new DataTransferValue(
					dataTransferBatch: batch,
					fieldName: fieldName,
					assetEntityId: ( domainId < 0 ? null : domainId ),
					importValue: field.originalValue,
					correctedValue: fieldValue,
					rowId: rowNum
				)

				// Boolean triggerFailureTest = (fieldValue == 'oradbsrv02')
				Boolean triggerFailureTest = false

				// This should NOT throw an exception because we're dealing errors in a savepoint rollback
				if (! batchRecord.save(failOnError:false) || triggerFailureTest ) {

					// TODO : JPM 2/2018 : MINOR - Should use the GormUtil.i18n version of the errors
					importContext.errors << "Unable to save field $fieldName on row ${rowNum}: ${GormUtil.allErrorsString(batchRecord)}"

					// Get rid of the GORM object that can't be saved so the main transaction can
					// still be committed.
					batchRecord.discard()

					// Roll back so none of the fields for this one row will be saved
					session.rollbackToSavepoint(savePoint)

					hadError = true
					break
				}
			}
		}

		// TODO : JPM 2/2018 : Determine if the releaseSavepoint should be called after the rollbackToSavepoint
		// It seems to work okay but would like confirmation.
		session.releaseSavepoint(savePoint)

		return true
	}

	/**
	 * Create a new ImportBatchRecord for the given row. Any errors will be added to the importContext.errors list and
	 * the entire row will be skipped.
	 *
	 * @param dataTransferBatch - current batch
	 * @param rowData - the meta-data for the current field to be inserted
	 * @param domainId - the id for the domain object if known
	 * @param importContext - Map of the import context objects
	 * @return true if all of the fields were successfully added to the DataTransferValue table or false if there was an error
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

		ImportOperationEnum OpValue =  ImportOperationEnum.lookup(rowData.op)

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
				throw DomainUpdateException('The import batch was deleted while being processed')
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
			cache: [:],

			// The actual class of the domain (e.g. net.transitionmanager.domain.Person)
			domainClass: domain.getClazz(),

			// The Domain name as it is known in the ETL scripting language
			domainClassName: domain.name(),

			// The Domain short name of the Grails domain class
			domainShortName: GormUtil.domainShortName( domain.getClazz() ),

			// The ImportBatchRecord currently being processed
			record: null,

			project: project
		]
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
		def stopwatch = new StopWatch()
		stopwatch.start()

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
						log.debug '\n\nprocessBatch() processing row {}', rowsProcessed
						context.record = record
						processBatchRecord(batch, record, context)
						rowsProcessed++
					}
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

		log.info "processBatch({}) finished in {} and processed {} records", batchId, stopwatch.endDuration(), rowsProcessed

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
				log.error "Batch Import process called for unsupported domain $domain in batch ${batch.id} in project ${batch.project}"
				error = "Batch process not supported for domain ${domainName}"
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
	private void processBatchRecord(ImportBatch batch, ImportBatchRecord record, Map context) {
		switch (batch.domainClassName) {
			case ETLDomain.Dependency:
				processDependencyRecord(batch, record, context)
				break

			default:
				String domain = batch.domainClassName.name()
				log.error "Batch Import process called for unsupported domain $domain in batch ${batch.id} in project ${batch.project}"
				throw new InvalidRequestException("Batch process not supported for domain ${domain}")
		}
	}

	/**
	 * Used to process a single AssetDependency ImportBatchRecord
	 * Note that errors may be recorded a record and/or field level as appropriate.
	 *
	 * @param batch - the batch that the ImportBatchRecord
	 * @param importBatchRecord - the ImportBatchRecord to be processed
	 * @param context - the batch processing context that contains objects used throughout the process
	 * @return a list of errors if any were detected during the process.
	 */
	def processDependencyRecord = { ImportBatch batch, ImportBatchRecord record, Map context ->
		Object primary, supporting
		AssetDependency dependency
		Map fieldsInfo = JsonUtil.parseJson(record.fieldsInfo)
		Project project = batch.project
		resetRecordAndFieldsInfoErrors(record, fieldsInfo)

		while (true) {
			// Try finding the Dependency by it's id if specified in fieldsInfo
			if (fieldsInfo.containsKey('id')) {
				def findDomainByIdResult = lookupDomainRecordByFieldMetaData('id', fieldsInfo, context)
				if (NumberUtil.isaNumber(findDomainByIdResult)) {
					// the fields.Info.id.value had a number but the id was not found which is an error
					log.debug "processDependencyRecord() lookupDomainRecordByFieldMetaData() failed ($findDomainByIdResult)"
					break
				}

				// Yes! Found the elusive sucker!
				dependency = findDomainByIdResult
			}

			// Try looking up both Asset References using the id and/or query elements in fieldsInfo
			primary = lookupDomainRecordByFieldMetaData('asset', fieldsInfo, context)
			supporting = lookupDomainRecordByFieldMetaData('dependent', fieldsInfo, context)

			// log.debug 'processDependencyRecord() primary={}', primary
			// log.debug 'processDependencyRecord() primary errors={}', fieldsInfo['asset'].errors
			// log.debug 'processDependencyRecord() supporting={}', supporting
			// log.debug 'processDependencyRecord() supporting errors={}', fieldsInfo['dependent'].errors
			// if ( primary < 0  || supporting < 0|| fieldsInfo['asset'].errors || fieldsInfo['dependent'].errors ) {
			if ( fieldsInfo['asset'].errors || fieldsInfo['dependent'].errors ) {
				// log.debug 'processDependencyRecord() primary create={}, errors={}', fieldsInfo['asset'].create, fieldsInfo['asset'].errors
				// log.debug 'processDependencyRecord() supporting create={}, errors={}', fieldsInfo['dependent'].create, fieldsInfo['dependent'].errors

				// If there were multiple assets found then we can't create/update the dependency
				log.debug "processDependencyRecord() Abandoning the creation/updating of AssetDependency due to errors in the assets"
				break
			}

			// log.debug "processDependencyRecord() before createReferenceDomain: primary asset: $primary, supporting: $supporting"

			// Attempt to create the assets if it don't exist using the create structure generated by the ETL process
			if (! primary) {
				primary = createReferenceDomain('asset', fieldsInfo, context)
				// log.debug 'processDependencyRecord() after createReferenceDomain: primary: {}', primary
			}
			if (! supporting) {
				supporting = createReferenceDomain('dependent', fieldsInfo, context)
				// log.debug 'processDependencyRecord() after createReferenceDomain: supporting: {}', supporting
			}

			// Try finding & updating or creating the dependency with primary and supporting assets that were found
			if (primary && supporting) {
				dependency = findAndUpdateOrCreateDependency(primary, supporting, fieldsInfo, context)
			}
			break
		}

		// Tally up all the errors that may have occurred during the process
		record.errorCount = tallyNumberOfErrors(record, fieldsInfo)

		// Now update the Import record
		if (dependency) {
			if (record.errorCount) {
				log.debug "processDependencyRecord() Failing due to errors (${record.errorCount})"
				// Trash the dependency
				dependency.discard()
				dependency = null
			} else {
				record.operation = (dependency.id ? ImportOperationEnum.UPDATE : ImportOperationEnum.INSERT)

				// log.debug "processDependencyRecord() Saving the Dependency"
				if (dependency.save(failOnError:false)) {
					// If we still have a dependency record then the process must have finished
					// TODO : JPM 3/2018 : Change to use ImportBatchRecordStatusEnum -
					//    Note that I was running to some strange issues of casting that prevented from doing this originally
					// record.status = ImportBatchRecordStatusEnum.COMPLETED
					record.status = ImportBatchStatusEnum.COMPLETED
				} else {
					log.warn "processDependencyRecord() failed to create Dependency ${GormUtil.allErrorsString(dependency)}"
					dependency.discard()
					dependency = null
				}
			}
		}

		// Update the fieldsInfo back into the Import Batch Record
		record.fieldsInfo = JsonUtil.toJson(fieldsInfo)
		// log.debug "processDependencyRecord() Saving the ImportBatchRecord with status ${record.status}"
		if (! record.save(failOnError:false) ) {
			log.warn "processDependencyRecord() Failed saving ImportBatchRecord : ${ GormUtil.allErrorsString(record) }"
		}
	}

	/**
	 * Used by the processDependencyRecord method to either create a new dependency and set the assets on it or
	 * to update the asset references if they've changed. If either the primary or supporting asset are missing then
	 * a null will be returned since a dependency can not be created or re-referenced to a non-existing asset.
	 *
	 * @param dependency - the existing dependency if it was found by the id previously
	 * @param primary - the primary asset
	 * @param supporting - the supporting asset
	 * @return the new or updated dependency or null if primary or supporting are null
	 */
	private AssetDependency findAndUpdateOrCreateDependency(AssetEntity primary, AssetEntity supporting, Map fieldsInfo, Map context ) {
		AssetDependency dependency

		if (primary && supporting) {

			// If there is the primary & supporting asset and no dependency yet then try and find it by the two assets
			dependency = AssetDependency.where {
				asset.id == primary.id
				dependent.id == supporting.id
			}.find()

			if (dependency) {
				// UPDATE
				log.debug 'findAndUpdateOrCreateDependency() UPDATING DEPENDENCY ID {}', dependency.id
				// Update the primary or supporting assets if they changed
				if (dependency.asset.id != primary.id) {
					log.debug "findAndUpdateOrCreateDependency() Updated primary asset on Dependency"
					dependency.asset = primary
				}
				if (dependency.dependent.id != supporting.id) {
					log.debug "findAndUpdateOrCreateDependency() Updated supporting asset on Dependency"
					dependency.dependent = supporting
				}

			} else {
				// CREATE
				dependency = new AssetDependency(asset: primary, dependent: supporting)
				log.debug "findAndUpdateOrCreateDependency() Creating new Dependency"
			}

			// Now add/update the remaining properties on the domain entity appropriately
			Boolean bindingOkay = bindFieldsInfoValuesToEntity(dependency, fieldsInfo, context, ['asset', 'dependent'])
			if (! bindingOkay || recordDomainConstraintErrorsToFieldsInfoOrRecord(dependency, context.record, fieldsInfo) ) {
				if (bindingOkay) {
					// Must of been a contraints issue then
					log.warn "processDependencyRecord() Got errors after binding data ${GormUtil.allErrorsString(dependency)}"
				}
				// Damn it! Couldn't save this sucker...
				dependency.discard()
				dependency = null
			}
		}

		// TODO : JPM 3/2018 : Need to review the md5 / cache - Here we just created assets that should be cached.

		return dependency
	}

	/**
	 * This is used to attempt to lookup a domain record using the metadata that is provided by the
	 * ETL process.
	 *
	 * @param domainClassName - the domain class name used in the ETL script
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return will return various results based on searches
	 * 		entity 	: if found
	 *		null	: if not found by alternate or query
	 *		-1 		: an error occurred
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
	 * 				"originalValue": "114052",
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
	 */
	private Object lookupDomainRecordByFieldMetaData(String propertyName, JSONObject fieldsInfo, Map context) {
		// This will be populated with the entity object or error message appropriately
		Object entity

		// This will be used to check/set cache for previously searched items
		String md5

		log.debug 'lookupDomainRecordByFieldMetaData() called with domain {} on row {}, propertyName {}, value={}',
			context.domainShortName, context.record.sourceRowId, propertyName, fieldsInfo[propertyName]?.value

		boolean recordNewError=true
		boolean foundInCache=false

		boolean working = true
		while (working) {
			if ( ! fieldsInfo[propertyName] ) {
				// Shouldn't happen but just in case...
				entity = "Reference property $propertyName is missing from ETL output"
				break
			}

			// See if this property based on ID is in the cache already
			md5 = generateMd5OfFieldsInfoField(context.domainShortName, propertyName, fieldsInfo)
			log.debug 'lookupDomainRecordByFieldMetaData() has cache key {}', md5
			entity = context.cache[md5]
			if (entity) {
				log.debug 'lookupDomainRecordByFieldMetaData() found in cache ID {}', entity
				foundInCache=true
				if (entity instanceof String) {
					recordNewError = false
				}
				break
			}

			// Attempt to find the domain by the ID in find.results or the value if numeric
			entity = searchForDomainById(propertyName, fieldsInfo, context)
			if (entity) {
				break
			}

			// Try requerying the domain using the find.query elements
			List entities = performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)
			int qtyFound = entities?.size() ?: 0
			if (qtyFound == 1) {
				entity = entities[0]
				log.debug 'lookupDomainRecordByFieldMetaData() FOUND ID {} with ETL find/elseFind criteria', entity.id
				break
			} else if (qtyFound > 1 ) {
				log.debug "lookupDomainRecordByFieldMetaData() FOUND MULTIPLE - ETL find/elseFind criteria returned {} entities", qtyFound
				entity = FIND_FOUND_MULTIPLE_REFERENCES_MSG
				break
			}

			// Let's see if the value was a String and try looking up the entity by it's alternate key (e.g. assetName or name) if the
			// domain has one specified
			entities = findDomainByAlternateProperty(propertyName, fieldsInfo, context)
			qtyFound = entities?.size() ?: 0
			if (qtyFound == 1) {
				log.debug 'lookupDomainRecordByFieldMetaData() found by alternate property with value: {}', fieldsInfo[propertyName].value
				entity = entities[0]
			} else if (qtyFound > 1 ) {
				log.debug "lookupDomainRecordByFieldMetaData() called findDomainByAlternateProperty returned {} entities", qtyFound
				entity = ALTERNATE_LOOKUP_FOUND_MULTIPLE_MSG
			}
			break
		}

		// Save the result to cache regardless of found, not found or error (unless it was found in cache above)
		if (md5 && ! foundInCache) {
			context.cache[md5] = entity
		}

		// Deal with entity being set to an error message
		if (entity instanceof CharSequence) {
			if (recordNewError) {
				addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, entity)
			}
			entity = -1
		}

		return entity
	}

	/**
	 * Used to retrieve a domain entity by the id reference if it is specified in the fieldsInfo map that
	 * was generated by the ETL process. This is meant to be called by lookupDomainRecordByFieldMetaData().
	 *
	 * The logic will attempt two ways to determine the ID:
	 *    1) If the 'find.result' element has been populated and there was a single ID. Note that if there were multiple
	 *		 IDs in the results, that may have changed since the ETL process ran so lookupDomainRecordByFieldMetaData may
	 * 		 retry the queries if this fails to find something.
	 *	  2) If 'id' field is a numeric object (Integer or Long)
	 *
	 * If either scenario resulted in an ID and the entity can not be found then NOT_FOUND_BY_ID(-1) will be returned
	 *
	 * Note: If the domain contains the project field then it will be used in the query criteria.
	 *
	 * @param domainClass - the class to attempt to find by id
	 * @param refPropertyName - the property name that the id value will be used and will be populated with error if not found
	 * @param isReference - flag that indicates that the property is a reference otherwise can assume it is the identifier field
	 * @param fieldsInfo - the fields map that came from the ETL process
	 * @return the domain object if found; -1 of id is number and not found; otherwise null
	 */
	private Object searchForDomainById(String propertyName, JSONObject fieldsInfo, Map context) {
		String notFoundByID = 'Entity was not found by ID'
		Object domain=null
		Long id
		Map info = fieldsInfo[propertyName]

		// Try to get the ID number from the find results or from the field if it was populated with an ID directly
		if (info.find?.results?.size() == 1) {
			// Ignore if the find.query found more than 1
			id = info.find.results[0]
		} else if (NumberUtil.isaNumber(info.value)) {
			id = info.value
		}
		log.debug 'searchForDomainById() resolved id to {}', id

		if (id) {
			// Attempt to access the entity by the ID for the appropriate domain class
			// If Identifier then the domainClass otherwise the domain class of the property
			Boolean isReference = GormUtil.isReferenceProperty(context.domainClass, propertyName)
			Class domainClassToQuery = GormUtil.getDomainClassOfProperty(context.domainClass, propertyName)
			log.debug 'searchForDomainById() going to search class {}', domainClassToQuery.getName()
			def entity = GormUtil.findInProject(context.project, domainClassToQuery, id)
			if (entity) {
				log.debug 'searchForDomainById() found entity {}', entity.id
				return entity
			} else {
				log.debug 'searchForDomainById() did not find entity'
				addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, SEARCH_BY_ID_NOT_FOUND_MSG)
				return NOT_FOUND_BY_ID
			}
		}
		return null
	}

	/**
	 * Used to find the a domain entity or reference domain when the field.value is a String and the domain has an
	 * alternate key defined. Note that this lookup is generally only useful for domain entities that typical have
	 * unique names. For domains such as Application this method will more than likely return multiple results.
	 *
	 * @param propertyName - the property name that the id value will be used and will be populated with error if not found
	 * @param fieldsInfo - the fields map that came from the ETL process
	 * @return the list of domain entities found for the property
	 */
	private List<Object> findDomainByAlternateProperty(String propertyName, Map fieldsInfo, Map context) {
		String notFoundByID = 'Entity was not found by ID'
		List<Object> entities=[]

		def value = fieldsInfo[propertyName]?.value
		if ( value && (value instanceof CharSequence) ) {
			Class domainClass = GormUtil.getDomainClassOfProperty(context.domainClass, propertyName)
			entities = GormUtil.findDomainByAlternateKey(domainClass, value, context.project)
			log.debug 'findDomainByAlternateProperty() found={}',entities.size()
		}
		return entities
	}

	/**
	 * Used to query for domain entities using the meta-data generated by the find/elseFind commands in the
	 * ELT DataScript. After performing the queries it will update the find section of the fieldsInfo with the
	 * the results. It will return a list of the entities found.
	 *
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return list of entities found
	 */
	private List<Object> performQueryAndUpdateFindElement(String propertyName, Map fieldsInfo, Map context) {
		List<Object> entities = []

		// If the lookup is for a reference field then it is mandatory in the script to account for this via
		if ( ! fieldsInfo[propertyName].find?.query || fieldsInfo[propertyName].find.query.size() == 0 ) {
			addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, NO_FIND_QUERY_SPECIFIED_MSG)

		} else {

			// log.debug 'performQueryAndUpdateFindElement() for property {}: Searching with query={}', propertyName, fieldsInfo[propertyName].find?.query
			int recordsFound = 0
			int foundMatchOn = 0

			// Iterate over the list of Queries until something is found
			//  and update the find section appropriately.
			for (query in fieldsInfo[propertyName].find.query) {
				foundMatchOn++

				// Use the ETL find logic to try searching for the domain entities
				ETLDomain whereDomain = ETLDomain.lookup(query.domain)
				entities = DomainClassQueryHelper.where(whereDomain, context.project, query.kv)

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

			log.debug 'performQueryAndUpdateFindElement() for property={}, find={}', propertyName, fieldsInfo[propertyName].find
			// Record error on the field if more than one entity was found
			if (recordsFound > 1) {
				addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, FIND_FOUND_MULTIPLE_REFERENCES_MSG)
			}
		}

		return entities
	}

	/**
	 * Used to determine what the actual class is of a particular domain property. In the case of AssetEntity the logic
	 * logic needs to determine which type is actually intended based on the ETLDomain property name (e.g. Device, Asset, etc)
	 * @param propertyName - the property to get the class type for
	 * @param fieldsInfo - the ETL info on the fields of the entity
	 * @param context - the process context map
	 * @return the class name of the property
	 */
	private Class classOfDomainProperty(String propertyName, Map fieldsInfo, Map context) {
		Class domainClassToCreate
		ETLDomain ed
		String errorMsg
		log.debug 'classOfDomainProperty() for property {}', propertyName

		if (! GormUtil.isDomainProperty(context.domainClass, propertyName)) {
			errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_DOMAIN, [propertyName:propertyName])
		}
		while ( ! errorMsg ) {
			Boolean isIdentifierProperty = GormUtil.isDomainIdentifier(context.domainClass, propertyName)
			Boolean isReferenceProperty = GormUtil.isReferenceProperty(context.domainClass, propertyName)
			log.debug 'classOfDomainProperty() for property {}, isIdentifierProperty {}, isReferenceProperty {}', propertyName, isIdentifierProperty, isReferenceProperty

			if (! fieldsInfo.containsKey(propertyName)) {
				errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_FIELDS, [propertyName:propertyName])
				break
			}
			// propertyName MUST be a reference or identifier for this function otherwise record an error
			if (! ( isIdentifierProperty || isReferenceProperty ) ) {
				errorMsg = WHEN_NOT_FOUND_PROPER_USE_MSG
				break
			}

			if (isIdentifierProperty) {
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
		if (errorMsg) {
			addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, errorMsg)
		}
		return domainClassToCreate
	}

	/**
	 * Used to create a domain record using the "create" structure in the ETL meta-data for a specified property. If the
	 * reference domain entity could not be created then the error(s) will be recorded into the property of fieldsInfo map.
	 *
	 * @param propertyName - the name of the property in the fields ETL datastructure
	 * @param context - the context map that the process uses to cart crap around
	 * @return the reference domain object if successfully created
	 */
	private Object createReferenceDomain(String propertyName, Map fieldsInfo, Map context) {
		Object entity
		String errorMsg
		Map createInfo

		log.debug 'createReferenceDomain() CREATING reference entity for property {}', propertyName
		while (true) {
			if (!fieldsInfo.containsKey(propertyName)) {
				errorMsg = "Property $propertyName was missing from ETL meta-data"
				break
			}

			createInfo = fieldsInfo[propertyName].create

			if (! createInfo) {
				errorMsg = "Missing necessary 'whenNotFound create' information to create new asset"
				break
			}

			Class domainClassToCreate = classOfDomainProperty(propertyName, fieldsInfo, context)
			if (domainClassToCreate == null) {
				errorMsg = "Unable to determine class for property $propertyName"
				break
			}

			// Determine what we're trying to create by looking at the property and getting the Domain class
			entity = domainClassToCreate.newInstance()

			// load with the values from the create key/value pairs
			List fieldNames = fixOrderInWhichToProcessFields(createInfo.keySet())
			for (fieldName in fieldNames) {
				errorMsg = setDomainPropertyWithValue(entity, fieldName, createInfo[fieldName], propertyName, fieldsInfo, context)
				// TODO : JPM 4/2018 : Change so that all errors are recorded against the reference field instead of just the first error encountered (Augusto)
				if (errorMsg) {
					break
				}
			}
			if (errorMsg) {
				break
			}

			//
			// Handle different required properties
			// TODO : JPM 4/2018 : Refactor to make this method smaller and more testable
			//

			// Project
			if (GormUtil.isDomainProperty(entity, 'project')) {
				entity.project = context.project
			}

			// MoveBundle
			if (GormUtil.isDomainProperty(entity, 'moveBundle') && ! entity.moveBundle ) {
				// TODO : JPM 4/2018 : The bundle may have been set with reference logic above
				entity.moveBundle = context.project.getProjectDefaultBundle()
			}

			if (! entity.validate() ) {
				log.debug "createReferenceDomain() failed : {}", GormUtil.allErrorsString(entity)
				// TODO : JPM 4/2018 : Change to populate field with list of i18n errors (August - good one to work on)
				// Populate field with list of errors
				// clear the error message
				// entity.discard()
				errorMsg = "Failed to create record for $propertyName : " + GormUtil.errorsAsUL(entity)
				break
			}

			// TODO : JPM 3/2018 : change failOnError:false throughout this code at some point
			//log.info "Creating $propertyName reference for domain ${context.domainShortName} : $entity"
			entity.save(failOnError:true, flush:true)

			// Replace the cache reference of the query with that of the new entity
			String md5 = generateMd5OfFieldsInfoField(context.domainShortName, propertyName, fieldsInfo)
			log.debug "createReferenceDomain() replaced cache $md5 with {}", entity
			// TODO : JPM 4/2018 : change to cache the ID of the object instead of the object to conserve memory
			context.cache[md5] = entity

			break
		} // while(true)

		if (errorMsg) {
			if (entity) {
				entity.discard()
			}
			log.warn 'createReferenceDomain() failed - property={}, error={}, create={}', propertyName, errorMsg, createInfo
			addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, errorMsg)
		}

		return entity
	}

	/**
	 * Used to swap around the order of properties when processing fields
	 * For some functionality the order of the fields will be critial that one or more are done ahead of others such
	 * as Manufacturer and Model.
	 * @param fieldNames - a set of the field names to reorder
	 * @return the reordered list
	 */
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
	 * Used to set a property onto a domain object
	 * @param domain - the domain entity to be manipulated
	 * @param propertyName - the property to be set
	 * @param value - the value to set onto the domain object
	 * @param parentPropertyName - the property name of the parent property that the domain will be assigned to
	 * @param context - the grand poopa of objects for the Import Process
	 * @return null if successful otherwise a string containing the error message that occurred
	 */
	private String setDomainPropertyWithValue(Object domainInstance, String propertyName, Object value, String parentPropertyName, Map fieldsInfo, Map context) {
		String errorMsg = null
		List<String> propertiesThatCannotBeSet = ['id', 'version', 'assetClass', 'createdBy', 'updatedBy', 'project']
		while (true) {
			// TODO : JPM 4/2018 : Lookup the FieldSpecs for assets to get the label names for errors
			if ( (propertyName in propertiesThatCannotBeSet) ) {
				// errorMsg = "Field ${propertyName} can not be set by 'whenNotFound create' statement"
				errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_CANNOT_BE_SET_MSG, [propertyName:propertyName])
				break
			} else {
				if (! GormUtil.isDomainProperty(domainInstance, propertyName)) {
					errorMsg = "Unknown field ${propertyName} in 'whenNotFound create' statement"
					break
				}

				// Check if the field is a reference property
				if (GormUtil.isReferenceProperty(domainInstance, propertyName)) {
					if (NumberUtil.isaNumber(value)) {
						// Perform lookup by ID
						// TODO : JPM 4/2018 : Refactor into Gorm as a single function
						Class refDomainClass = GormUtil.getDomainPropertyType(domainClassToCreate, propertyName)
						def entity = GormUtil.findInProject(context.project, refDomainClass, item.value)

						if (entity) {
							domainInstance[propertyName] = entity
						} else {
							errorMsg = "No reference record found for field ${propertyName} by ID in 'whenNotFound create'"
							break
						}
						// searchForDomainById(refDomainClass, propertyName, item.value, fieldsInfo, context)
					} else if (! StringUtil.isBlank(value)) {
						// Attempt the find the reference by the alternate key
						List references = findReferenceDomainByAlternateKey(domainInstance, propertyName, value, parentPropertyName, fieldsInfo, context)
						Integer numFound = references.size()
						if (numFound == 1) {
							domainInstance[propertyName] = references[0]
						} else if (numFound > 1) {
							errorMsg = "Multiple references found for field ${propertyName} by Name in 'whenNotFound create'"
							break
						} else {
							errorMsg = "No reference record found for field ${propertyName} by Name in 'whenNotFound create'"
							break
						}
					}
				} else {
					// Just a normal data type (e.g. Date, Integer, String, etc)
					// TODO : JPM 4/2018 : Need to deal with ENUM and Date being resolved
					domainInstance[propertyName] = value
				}
			}
			break
		}
		return errorMsg
	}

	/**
	 * Used by the createReferenceDomain to local other reference domain objects (e.g. manufacturer or model) that will be set
	 * on the entity being created.
	 * @param entity - the Entity that is being created
	 * @param refDomainPropName - the property name of the entity for which the reference is going to be searched
	 * @param parentPropertyName - the property of the parent record for which the reference domain is being created for (used for reporting errors)
	 * @param fieldsInfo - the information map of all of the parent record properties
	 * @param context - the map that contains the holy grail of the Import Batch processing
	 * @return A list of the reference domain entities that were found
	 */
	List findReferenceDomainByAlternateKey(Object entity, String refDomainPropName, String searchValue, String parentPropertyName, Map fieldsInfo, Map context) {
		List entities = []
		if (searchValue) {
			Class refDomainClass = GormUtil.getDomainPropertyType(entity, refDomainPropName)
			String refDomainName = GormUtil.domainShortName(refDomainClass)

			// Make sure that the domain has an alternateLookup defined on the class
			if (! GormUtil.getAlternateKeyPropertyName(refDomainClass)) {
				addErrorToFieldsInfoOrRecord(parentPropertyName, fieldsInfo, context,
					"Field reference ${refDomainPropName} of domain ${refDomainName} not supported in 'whenNotFound create'")
				return entities
			}

			Map extraCriteria = [:]
			if (refDomainName == 'Model') {
				// The first query of Model will be by Name + Mfg & assetType (if they are specified)
				if (entity.manufacturer) {
					extraCriteria.put('manufacturer', entity.manufacturer)
				} else {
					addErrorToFieldsInfoOrRecord(parentPropertyName, fieldsInfo, context,
						"Manufacturer required in order to find model reference for 'whenNotFound create'")
					return entities
				}
				if (entity.assetType) {
					extraCriteria.put('assetType', entity.assetType)
				}
			}

			entities = GormUtil.findDomainByAlternateKey(refDomainClass, searchValue, context.project, extraCriteria)
			int numFound = entities.size()
			if (numFound == 0) {
				// Try a few alternative lookups based on the domain classes
				switch (refDomainName) {
					case 'Manufacturer':
						Manufacturer mfg = Manufacturer.where { name == searchValue }.find()
						if (mfg) {
							entities = [mfg]
						} else {
							mfg = ManufacturerAlias.where { name == searchValue }.find()?.manufacturer
							if (mfg) {
								entities = [mfg]
							}
						}
						break

					case 'Model':
						if (entity.manufacturer && entity.assetType) {
							// Searched already by mfg + name + assetType, so now try just by mfg + name
							extraCriteria = [manufacturer:entity.manufacturer]
							entities = GormUtil.findDomainByAlternateKey(refDomainClass, searchValue, context.project, extraCriteria)
						}

						// If not found then try by looking up the alias of the model
						if (!entities) {
							Model model = ModelAlias.where { name == searchValue && manufacturer == entity.manufacturer }.find()
							if (model) {
								entities = [model]
							}
						}
						break

					case 'Person':
						// TODO : JPM 4/2018 : Implement Person lookup

					default:
						throw new RuntimeException("findReferenceDomainByAlternateKey() called for unsupported type ${refDomainName}")
				}
			}
		}
		return entities
	}

	/**
	 * Used to clear out the errors recorded at the field level and row level and the record level count
	 * @param record - the import batch record to clear
	 * @param fieldsInfo - the Map of the record.fieldsInfo to be cleared
	 */
	private void resetRecordAndFieldsInfoErrors(ImportBatchRecord record, Map fieldsInfo) {
		record.errorCount = 0
		record.resetErrors()
		for (field in fieldsInfo) {
			field.value.errors = []
		}
	}

	/**
	 * Used to bind the values from the fieldsInfo into the domain but skipping over any fields specified
	 * in the fieldsToIngnore parameter.
	 *
	 * @param domain - the entity to bind the values on
	 * @param fieldsInfo - the Map of fields and their values from the ETL process
	 * @param fieldsToIgnore - a List of field names that should not be bound
	 * @return true if binding did not encounter any errors
	 */
	private Boolean bindFieldsInfoValuesToEntity(Object domain, Map fieldsInfo, Map context, List fieldsToIgnore=[]) {

		// TODO - JPM 4/2018 : Refactor bindFieldsInfoValuesToEntity so that this can be used in both the row.field values + the create and update blocks
		Boolean noErrorsEncountered = true
		Set<String> fieldNames = fieldsInfo.keySet()
		if (fieldsToIgnore == null) {
			fieldsToIngnore = []
		}
		fieldsToIgnore.addAll(['id'])
		fieldNames =  fieldNames - fieldsToIgnore

		// If no new value and init contains value and entity property has no value
		// Then set the property to the init value
		// Else if new value and current value not equal new value
		// Then set new value

		Map fieldsValues = [:]
		for (fieldName in fieldNames) {
			Boolean isReference = GormUtil.isReferenceProperty(domain, fieldName)

			def newValue = fieldsInfo[fieldName].value
			def domainValue = domain[fieldName]
			def initValue = fieldsInfo[fieldName].init
			boolean hasInitializeValue = (initValue != null)

			if (hasInitializeValue) {
				if (isReference) {
					noErrorsEncountered = false
					addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, "The 'initialize' command is not supported for identifier or reference properties ($fieldName)")
					continue
				}

				if (domainValue == null || (domainValue instanceof String) && domainValue == '') {
					fieldsValues.put(fieldName, initValue)
				}
			} else {
				if (isReference) {
					// TODO : JPM 4/2018 : Need to implement Reference properties
					throw new RuntimeException("bindFieldsInfoValuesToEntity() does not yet support setting reference properties ($fieldName)")
				} else {
					// TODO : JPM 4/2018 : Implement data type checking (use Grails BINDING logic to address ENUM, Date, etc)
					Class fieldClassType = GormUtil.getDomainPropertyType(domain.getClass(), fieldName)
					if (String == fieldClassType) {
						if (newValue != null && newValue != domainValue) {
							fieldsValues.put(fieldName, newValue)
						}
					} else {
						throw new RuntimeException("bindFieldsInfoValuesToEntity() does not yet support setting type (${fieldClassType.getName()}) for property ($fieldName)")
					}
				}
			}
		}
		GormUtil.bindMapToDomain(domain, fieldsValues, fieldsToIgnore)

		return noErrorsEncountered
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
	private void addErrorToFieldsInfoOrRecord(String propertyName, JSONObject fieldsInfo, Map context, String errorMsg) {
		if (propertyName && fieldsInfo[propertyName]) {
			fieldsInfo[propertyName].errors << errorMsg
		} else {
			context.record.addError(errorMsg)
		}
	}

	/**
	 * Used to access the error list for the infoFields of a particular field/property
	 */
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
	Map transformEtlData(Project project, Long dataScriptId, String filename) {
		Map result = [filename:'']
		DataScript dataScript = null
		String errorMsg = null

		if (!dataScriptId) {
			errorMsg = 'Missing required dataScriptId parameter'
		} else if (!filename) {
			errorMsg = 'Missing filename parameter'
		} else if (! fileSystemService.temporaryFileExists(filename)) {
			errorMsg = 'Specified input file not found'
		} else {
			List<String> allowedExtensions = fileSystemService.getAllowedExtensions()
			if (! FileSystemUtil.validateExtension(filename, allowedExtensions)) {
				errorMsg = i18nMessage(Message.FileSystemInvalidFileExtension)
			} else {
				// See if we can find a DataScript.
				dataScript = GormUtil.findInProject(project, DataScript, dataScriptId, true)

				if (! dataScript.etlSourceCode) {
					errorMsg = 'DataScript has no source specified'
				}
			}

		}

		// If there was an error in the previous validations, throw an exception.
		if (errorMsg) {
			throw new InvalidParamException(errorMsg)
		}

		String inputFilename = fileSystemService.getTemporaryFullFilename(filename)
		ETLProcessor etlProcessor = scriptProcessorService.execute(project, dataScript.etlSourceCode, inputFilename)
		def (String outputFilename, OutputStream os) = fileSystemService.createTemporaryFile('import-','json')
		result.filename = outputFilename
		try {
			os << (etlProcessor.result.toMap() as JSON)
			os.close()
		}
		catch(e) {
			log.error 'transformData() failed to write output logfile : {}', e.getMessage()
			throw new RuntimeException('Unable to write output file : ' + e.message)
		}

		return result
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


