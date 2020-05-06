package net.transitionmanager.imports

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.etl.DataImportHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.RowResult
import com.tdsops.event.ImportBatchJobSchedulerEventDetails
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.FileSystemUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.EmailDispatch
import net.transitionmanager.common.EmailDispatchService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.ProgressService
import net.transitionmanager.dataImport.SearchQueryHelper
import net.transitionmanager.etl.ETLStreamingReader
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.i18n.Message
import net.transitionmanager.job.ETLTransformDataJob
import net.transitionmanager.party.Party
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.AssetComment
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.tag.TagAssetService
import net.transitionmanager.tag.TagService
import net.transitionmanager.task.AssetComment
import org.grails.web.json.JSONObject
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionStatus

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
@Transactional(propagation=Propagation.MANDATORY)
class DataImportService implements ServiceMethods, EventPublisher {

	// IOC
	AssetService             assetService
	CustomDomainService      customDomainService
	EmailDispatchService 	 emailDispatchService
	FileSystemService        fileSystemService
	ImportBatchService		 importBatchService
	PartyRelationshipService partyRelationshipService
	PersonService            personService
	ProgressService          progressService
	ProjectService           projectService
	Scheduler                quartzScheduler
	ScriptProcessorService   scriptProcessorService
	TagAssetService			 tagAssetService
	TagService 				 tagService
	FailureNotificationService failureNotificationService

	// TODO : JPM 3/2018 : Move these strings to messages.properties
	static final String PROPERTY_NAME_CANNOT_BE_SET_MSG = "Field {propertyName} can not be set by 'whenNotFound create' statement"

	// The property name that will be set on the fieldsInfo of any field that the value is changed during the posting process
	static final String PREVIOUS_VALUE_PROPERTY = 'previousValue'

	// The property in the fieldsInfo for each field that has the 'whenNotFound create' mapping
	static final WHEN_NOT_FOUND_CREATE_PROPERTY = 'create'

	// The property in the fieldsInfo for each field that has the 'whenFound update' mapping
	static final WHEN_FOUND_UPDATE_PROPERTY = 'update'

	// Globally these are fields that will not be allowed to be modified on any domain record
	static final List<String> PROPERTIES_THAT_CANNOT_BE_MODIFIED = [
		'version', 'assetClass', 'createdBy', 'updatedBy', 'project', 'dateCreated', 'lastUpdated'
	]

	// A map of exceptions that the Import Field Process logic uses to deal with exceptions
	//    ignore - fields may appear in ETL import but not directly updated (e.g. Room Locations)
	static final Map DOMAIN_FIELD_EXCEPTIONS = [
		'AssetEntity': [
			'locationSource': [ignore: true],
			'locationTarget': [ignore: true],
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
	 * @param project
	 * @param userLogin
	 * @param datasetFileName
	 * @return a Map containing details about the import creation
	 *      batchesCreated <Integer> - count of the number of batches created
	 *      domains <List><Map> - a list of each domain that records were created
	 *          domainClass <String> - name of the domain
	 *          rowsCreated <Integer> - the count of the records created
	 * 			rowsSkipped <Integer> - the count of rows that were skipped due to errors
	 */
	@NotTransactional()
	Map loadETLJsonIntoImportBatch(
			Project project,
			UserLogin userLogin,
			String datasetFileName,
			Long dataScriptId,
			Boolean sendResultsByEmail = false,
			String progressKey = null)
	{
		DataScript dataScript = GormUtil.findInProject(project, DataScript, dataScriptId, true)
		String groupGuid = StringUtil.generateGuid()

		try {
			return doLoadETLJsonIntoImportBatch(project, userLogin, datasetFileName, groupGuid, dataScript.isAutoProcess, sendResultsByEmail, progressKey)

		} catch (Throwable e) {
			String errorMessage = e.getMessage()
			progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage(), null, ETLProcessor.getErrorMessage(e))

			if (dataScript.isAutoProcess){
				failureNotificationService.notifyDataImportFailure(userLogin.person, dataScript, groupGuid, errorMessage)
			}
			return [errorMessage: errorMessage]
		}
	}

	/**
	 * Init loading of data into the Import batches.
	 */
	@NotTransactional()
	Map doLoadETLJsonIntoImportBatch(
			Project project,
			UserLogin userLogin,
			String datasetFileName,
			String groupGuid,
			Boolean isAutoProcess = false,
			Boolean sendResultsByEmail = false,
			String progressKey = null)
	{

		InputStream datasetInpuStream = fileSystemService.openTemporaryFile(datasetFileName)
		if (!datasetInpuStream) {
			throw new InvalidParamException('Specified input file not found')
		}

		JSONObject importJsonData = null
		try {
			importJsonData = JsonUtil.parseFile(datasetInpuStream)
		} finally {
			datasetInpuStream.close()
		}

		if (!importJsonData) {
			throw new InvalidParamException('Invalid input file not found')
		}

		// Map which summarizes the results from the import process.
		Map importResults = [batchesCreated: 0, domains: [], errors: []]

		// A map that contains various objects used throughout the import process
		Map importContext = [
				project           : project,
				userLogin         : userLogin,
				etlInfo           : importJsonData.ETLInfo,
				status            : isAutoProcess ? ImportBatchStatusEnum.QUEUED : ImportBatchStatusEnum.PENDING,
				guid              : groupGuid,
				sendResultsByEmail: sendResultsByEmail,
				// The cache will be used to hold on to domain entity references when found or a String if there
				// was an error when looking up the domain object. The key will be the md5hex of the query element
				// of the field.
				cache             : new DataImportEntityCache(),

				// The following are reset per domain
				domainClass       : null,
				fieldNames        : [],
				rowsCreated       : 0,
				rowsSkipped       : 0,
				rowNumber         : 0,
				errors            : []
		]

		ImportProgressCalculator progressCalculator =
				new ImportProgressCalculator(
						progressKey,
						importJsonData.domains.sum { (it?.dataSize)?:0 },
						progressService,
						importContext.guid
				)

		progressCalculator.start()

		// Iterate over the domains and create batches for each
		for (domainJson in importJsonData.domains) {

			importContext.domainClass = domainJson.domain
			log.debug "localFunction() in for loop: importContext=$importContext"

			//TODO: John. What we need to do if the import process contains empty results.
			if (domainJson.dataSize == 0) {
				importResults.errors << "Domain ${importContext.domainClass} contained no data"
				importResults.domains << [domainClass: importContext.domainClass, rowsCreated: 0, rowsSkipped: 0]

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
						importResults.domains << [domainClass: importContext.domainClass, rowsCreated: 0]

					} else {

						if (!domainJson.outputFilename){
							throw new InvalidParamException('outputFilename field not specified')
						}

						// Streaming solution splits each domain data.
						InputStream inputStream = fileSystemService.openTemporaryFile(domainJson.outputFilename)
						if (!inputStream) {
							throw new InvalidParamException('Specified input file not found')
						}
						// Import the assets for this batch using Streaming process
						importRowsIntoBatchUsingStreaming(session, batch, inputStream, progressCalculator, importContext)

						// Update the batch with information about the import results
						batch.importResults = DataImportHelper.createBatchResultsReport(importContext)

						// Update the reporting
						importResults.batchesCreated++
						importResults.domains << [domainClass: importContext.domainClass, batchId: batch.id, rowsCreated: importContext.rowsCreated]
					}
				}
			}
		}

		if (isAutoProcess && importResults.batchesCreated > 0) {
			log.debug "Notify ImportBatchJon with importResults:$importResults"
			notify(ImportBatchJobSchedulerEventDetails.NEXT_BATCH_READY, new ImportBatchJobSchedulerEventDetails(project.id, importResults.domains.first()?.batchId, userLogin.username))
		}

		progressCalculator.finish()
		return importResults
	}

	/**
	 * Import all the assets for the given InputStream.
	 * It reads Map<String, ?> rowData from InputStream parameter.
	 * For each row read, it creates an new row using {@code DataImportService#importRow}
	 *
	 * @param batch - current batch
	 * @param inputStream - list of assets in an instance of {@code InputStream}
	 * @param importContext - additional parameters required for logging
	 * @see ETLStreamingReader#eachRow(groovy.lang.Closure)
	 */
	private void importRowsIntoBatchUsingStreaming(session, ImportBatch batch, InputStream inputStream, ImportProgressCalculator progressCalculator, Map importContext) {

		new ETLStreamingReader(inputStream).eachRow { Map<String, ?> rowData ->
			log.debug "Processing row with content: ${rowData}"
			// Keep track of the row number for reporting
			importContext.rowNumber++

			// Do some initialization of the rowData object if necessary
			if (rowData.errors == null) {
				rowData.errors = []
			}

			// Process the fields for this row
			importRow(session, batch, rowData, importContext)
			progressCalculator.increase()
		}
	}

	/**
	 * NEW ETL - Create a ImportBatch record for a given Domain Class
	 * @param domainClass
	 * @param currentUser
	 * @param project
	 * @return a newly created ImportBatch object
	 */
	//@CompileStatic
	private ImportBatch createImportBatch(Map importContext) {

		Date warnOnChangesAfter
		if (importContext.etlInfo.warnOnChangesAfter) {
			warnOnChangesAfter = new Date(importContext.etlInfo.warnOnChangesAfter)
		} else {
			warnOnChangesAfter = new Date()
		}

		DataScript dataScript = GormUtil.findInProject(importContext.project, DataScript, importContext.etlInfo.dataScriptId)

		ImportBatch batch = new ImportBatch()
		batch.project = importContext.project
		batch.status = importContext.status
        batch.groupGuid = importContext.guid
        batch.queuedAt = new Date()
        batch.queuedBy = importContext.userLogin.username
        batch.sendNotification = importContext.sendResultsByEmail
		batch.dataScript = dataScript
		batch.provider = dataScript?.provider
		batch.domainClassName = importContext.domainClass
		batch.createdBy = importContext.userLogin.person
		batch.autoProcess = (importContext.etlInfo.autoProcess ?: 0)
		batch.dateFormat = (importContext.etlInfo.dataFormat ?: '')
		batch.fieldNameList = JsonUtil.toJson(importContext.fieldNames)
		batch.fieldLabelMap = JsonUtil.toJson(importContext.fieldLabelMap)
		batch.nullIndicator = (importContext.etlInfo.nullIndicator ?: '')
		batch.originalFilename = (importContext.etlInfo.originalFilename ?: '')
		batch.overwriteWithBlanks = (importContext.etlInfo.overwriteWithBlanks ?: 1)
		batch.timezone = (importContext.etlInfo.timezone ?: 'GMT')
		batch.warnOnChangesAfter = warnOnChangesAfter

		// Check if the transfer batch is valid, report the error if not.
		if (!batch.save(failOnError: false)) {
			importContext.errors << "There was an error when creating the import batch for ${importContext.domainClass}"
			log.error 'DataImportService.createImportBatch() failed save: {}', GormUtil.allErrorsString(batch)
			batch.discard()

			return null
		}

		return batch
	}

	private void importRow(session, ImportBatch batch, RowResult rowData, Map importContext) {
		importRow(session, batch, rowData.properties, importContext)
	}

	/**
	 * Import an individual row of data
	 *
	 * @param batch
	 * @param asset - LazyMap with all the field information
	 * @param importContext - additional parameters required for logging
	 */
	// @CompileStatic
	private void importRow(session, ImportBatch batch, Map rowData, Map importContext ) {
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
	private boolean insertRowDataIntoImportBatchRecord(session, ImportBatch batch, Map rowData, Long domainId, Map importContext ) {

		int rowNum = importContext.rowNumber - 1
		// Detemine if there were duplicates found
		// TODO : JPM 2/2018 : the dupsFound logic is questionable (need to compare against latest JSON). Also this will be
		// good to move to the command object.
		Integer dupsFound = ( rowData.fields?.id?.find?.results?.size() > 1 ? 1 : 0 )

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
			fieldsInfo: (rowData.fields as JSON).toString(),
			comments: rowData.comments?JsonUtil.toJson(rowData.comments):'[]'
		)

		if (rowData?.tags?.add || rowData?.tags?.remove || rowData?.tags?.replace) {
			batchRecord.saveTagsAsMap(rowData.tags)
		}

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
	private static Long getAndValidateDomainId(Map rowData, Map importContext) {
		return DataImportHelper.getAndValidateDomainId(rowData, importContext)
	}

	/**
	 * Used to update the ImportBatch to the Queued status
	 * @param batchId - the ID of the batch to be updated
	 * @throws net.transitionmanager.exception.DomainUpdateException if ImportBatch.status was not PENDING at the current time
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
			batch.save(flush:true)
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
			batch.save(flush:true)
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
		batch.save()

		return (0 == batch.processStopFlag)
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
			staffList: getStaffReferencesForProject(project),

			// Prepares field Specs cache from database
			fieldSpecProject: customDomainService.createFieldSpecProject(project),

			// Tags Map used to validate tags in an ImportBatchRecord creation
			projectTags: tagService.tagMapByName(project)
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
		Long projectId = project.id
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
				ImportBatchRecord.withNewTransaction { DefaultTransactionStatus status ->
					for (record in records) {
						context.record = record
						rowsProcessed++
						try {
							processBatchRecord(batch, record, context, rowsProcessed)
						} catch (e) {
							record.addError(e.getMessage())
							record.save()
							log.error ExceptionUtil.stackTraceToString("processEntityRecord() Error while processing record ${rowsProcessed}", e, 80)
						}
					}

					log.debug 'processBatch({}) clearing Hibernate Session', batchId
					GormUtil.flushAndClearSession()

					//TM-17058 to fix the slow project.refresh() from TM-16534 The refresh was taking 10 to 20 seconds.
					project = Project.get(projectId)
				}
				batch.refresh()
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

		batch.refresh()
		if (batch.sendNotification) {
			sendNotification(batch, context)
		}

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
			case ETLDomain.Task:
			case ETLDomain.Manufacturer:
			case ETLDomain.Model:
			case ETLDomain.Room:
			case ETLDomain.Rack:
			case ETLDomain.Person:
			case ETLDomain.Bundle:
			case ETLDomain.Event:
				processEntityRecord(batch, record, context, recordCount)
				break

			default:
				String domain = batch.domainClassName.name()
				log.error "Batch Import process called for unsupported domain $domain in batch ${batch.id} in project ${batch.project}"
				throw new InvalidRequestException("processEntityRecord ${domain}")
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
	@Transactional(noRollbackFor=[Throwable])
	void processEntityRecord(ImportBatch batch, ImportBatchRecord record, Map context, Long recordCount) {
		Object entity
		Map fieldsInfo

		try {
			fieldsInfo = record.fieldsInfoAsMap()
			resetRecordAndFieldsInfoErrors(record, fieldsInfo)
			entity = findOrCreateEntity(fieldsInfo, context)
			Boolean shouldSaveEntity = false
			Boolean assetHasCommentsToSave = record.hasComments()
			Boolean hasTagsToAssociate = record.hasTags()

			if (entity && entity != -1) {
				log.debug 'processEntityRecord() calling bindFieldsInfoValuesToEntity with entity {}, fieldsInfo isa {}', entity, fieldsInfo.getClass().getName()

				// flag to determine if entity is new, this is used below
				boolean isNewEntity = entity.id == null

				// Now add/update the remaining properties on the domain entity appropriately
				boolean allIsGoodHere = bindFieldsInfoValuesToEntity(entity, fieldsInfo, context)
				if (!allIsGoodHere) {
					log.warn 'processEntityRecord() binding values failed, batch {}, record {}', batch.id, record.id
				} else {
					shouldSaveEntity = isNewEntity || GormUtil.hasUnsavedChanges(entity)

					// Save the Entity domain record appropritately
					if (shouldSaveEntity) {
						if (entity.save(failOnError: false, flush: true, deepValidate: false)) {
							// If the entity is a Person then it should automatically be associate it to company and project
							if (isNewEntity && Person.isAssignableFrom(entity.class)) {
								associatePersonToCompanyAndProject(context.project, entity)
							}
						} else {
							log.info 'processEntityRecord() batch {}, row {}, constraints errors {}', batch.id, record.id, GormUtil.allErrorsString(entity)
							recordDomainConstraintErrorsToFieldsInfoOrRecord(entity, context.record, fieldsInfo)
							allIsGoodHere = false
						}
					}

					// Save Comments to Assets appropriately
					if (allIsGoodHere && assetHasCommentsToSave) {
						List<String> comments = record.commentsAsList()
						saveCommentsForEntity(comments, entity, context)
					}

					if (allIsGoodHere && hasTagsToAssociate) {
						// The saveTagsForEntity will return the number of actual tag manipulations that occurred
						Integer tagChanges = saveTagsForEntity(record, entity, context)
						if (tagChanges == -1) {
							hasTagsToAssociate = false
							allIsGoodHere = false
						} else if (tagChanges == 0) {
							hasTagsToAssociate = false
						}
					}
				}

				// Finalize the record status or clean up from encountering errors
				if (allIsGoodHere) {
					// If we still have a dependency record then the process must have finished
					// TODO : JPM 3/2018 : Change to use ImportBatchRecordStatusEnum -
					// Note that I was running to some strange issues of casting that prevented from doing this originally
					// record.status = ImportBatchRecordStatusEnum.COMPLETED
					record.status = ImportBatchStatusEnum.COMPLETED
					if (shouldSaveEntity || assetHasCommentsToSave) {
						record.operation = (isNewEntity ? ImportOperationEnum.INSERT : ImportOperationEnum.UPDATE)
					} else {
						record.operation = ImportOperationEnum.UNCHANGED
					}

					// Bump the lastUpdated for assets if there were no changes on the asset other than comments. Note
					// that the tags service automatical bumps the asset lastModified.
					if ( !shouldSaveEntity && assetHasCommentsToSave ) {
						entity.lastUpdated = new Date()
						if (! entity.save(failOnError: false, flush: true, deepValidate: false)) {
							recordDomainConstraintErrorsToFieldsInfoOrRecord(entity, context.record, fieldsInfo)
							allIsGoodHere = false
						}
					}
				}

				// Handle clean up if something went sideways
				if (! allIsGoodHere) {
					// Damn it! Couldn't save this sucker...
					entity.discard()
					entity = null
					record.operation = (isNewEntity ? ImportOperationEnum.INSERT : ImportOperationEnum.UPDATE)
					record.status = ImportBatchStatusEnum.PENDING
				}
			}
		} catch (e) {
			record.addError(e.getMessage())
			log.error ExceptionUtil.stackTraceToString("processEntityRecord() Error while processing record ${recordCount}", e, 80)

			try {
				entity.discard()
				record.status = ImportBatchStatusEnum.PENDING
			} catch (Exception en) {
				//discard throws an exception java.lang.IllegalStateException: cannot generate an EntityKey when id is null.
				//We need to to the discard to get the errors to be populated correctly, but we don't care about this error.
				log.warn('entity.discard() had an error: batch {}, record {}, error {}', batch.id, record.id, en.getMessage(), )
			}
		}

		// Update the fieldsInfo back into the Import Batch Record
		record.fieldsInfo = JsonUtil.toJson(fieldsInfo)
		// Register error count accordingly
		record.errorCount = tallyNumberOfErrors(record, fieldsInfo)

		// log.debug "processEntityRecord() Saving the ImportBatchRecord with status ${record.status}"
		if (! record.save(failOnError: false, flush: true, deepValidate: false)) {
			// Catch the error here but need to throw it outside the try/catch so that it gets recorded at the batch level
			String domainUpdateErrorMsg = GormUtil.allErrorsString(record)
			log.warn 'processEntityRecord() Failed to save ImportBatchRecord changes: {}', domainUpdateErrorMsg
			throw new DomainUpdateException("Unable to update row $recordCount due to " + domainUpdateErrorMsg)
		}
	}

	@Transactional(noRollbackFor=[Throwable])
	void sendNotification(ImportBatch batch, Map context) {

		try {
			// Check if all the ImportBatch records created together in the auto-process
			// are already finished
			int count = ImportBatch.where {
				groupGuid == batch.groupGuid
				id != batch.id
				status in [ImportBatchStatusEnum.QUEUED, ImportBatchStatusEnum.RUNNING]
			}.count()

			if (count == 0){

				Map<String, ?> batchesWithSummary = importBatchService.findBatchesWithSummary(batch.project, null, null, batch.groupGuid)
				List<Map<String, ?>> batchesModel = batchesWithSummary.values().collect { Map<String, ?> batchSummary ->
					[
						id       : batchSummary.id,
						status   : batchSummary.status.label,
						domain   : batchSummary.domainClassName,
						records  : batchSummary.recordsSummary.count,
						processed: batchSummary.recordsSummary.processed,
						pending  : batchSummary.recordsSummary.pending,
						erred    : batchSummary.recordsSummary.erred,
						ignored  : batchSummary.recordsSummary.ignored,
						inserted : batchSummary.recordsSummary.inserted,
						updated  : batchSummary.recordsSummary.updated,
						deleted  : batchSummary.recordsSummary.deleted,
						unchanged: batchSummary.recordsSummary.unchanged,
						tbd      : batchSummary.recordsSummary.tbd
					]
				}

				String paramsJson = ([batches: batchesModel] as JSON).toString()
				EmailDispatch ed = new EmailDispatch()
				ed.origin = EmailDispatchOrigin.TASK
				ed.subject = 'TransitionManager Import Batch Summary'
				ed.bodyTemplate = 'batchPostingResults'
				ed.paramsJson = paramsJson
				ed.toAddress = context.whom.email
				ed.toPerson = context.whom
				ed.createdBy = context.whom

				if (ed.validate()){
					ed.save()
					emailDispatchService.createEmailJob(ed, [
						person : context.whom.email,
					])
				}
			}

		} catch(Throwable throwable){

			log.error ExceptionUtil.stackTraceToString("Failed to send email notification for import batch processing for batchId: ${batch.id} and groupGuid: ${batch.groupGuid}", throwable), throwable
		}

	}

	/**
	 * This is used record any coinstraints violation erros on a domain object and will save the errors back into the
	 * fieldsInfo map appropriately or into the ImportBatchRecord if the constraint failure was on a property
	 * that is not in the fieldsInfo.
	 *
	 * @param domain - the domain that is being created or updated
	 * @param record - the import record being processed (errors can be logged to this object)
	 * @param fieldsInfo - the Map of the fields that came from the ETL process
	 */
	@Transactional(noRollbackFor=[Throwable])
	private void recordDomainConstraintErrorsToFieldsInfoOrRecord(Object domain, ImportBatchRecord record, Map fieldsInfo) {
		for (error in  domain.errors.allErrors) {
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

	/**
	 * Saves, deletes or replaces tags associated with entity parameter.
	 * It uses {@code context#projectTags} for validations.
	 * It also manages {@code ImportBatchRecord#status} and
	 * {@code ImportBatchRecord#operation} logic.
	 * @param record an instance of {@code ImportBatchRecord} used in the import process.
	 * @param entity an instance of a TDS domain classes, populated and ready to be used in tag association.
	 * @param tagsInfo a Map with tags as results of an ETL script executed.
	 * @param context a Map with context variables. It is used for validation purposes and project with tags association.
	 * @return The number of tags that were assigned/unassiged to the asset OR -1 if there was an error
	 */
	@Transactional(noRollbackFor=[Throwable])
	Integer saveTagsForEntity(ImportBatchRecord record, Object entity, Map<String, ?> context) {

		Map<String, ?> tagsInfo = record.tagsAsMap()
		String error = validateTagReferences(tagsInfo, context.projectTags)

		if (error) {
			/* This scenarios would occur if after the ETL process was run and project tag(s) were deleted before
			   the batch was processed. */
			record.addError(error)
			return -1
		}

		Integer countOfTagChanges = 0

		// Get the list of tags associated to the domain entity
		List<TagAsset> entityTags = tagAssetService.list(context.project, entity.id)

		// Convert the list of assigned asset tags to a Map for faster access
		Map<String, TagAsset> assetTagMap = entityTags.collectEntries { TagAsset tagAsset ->
			[(tagAsset.tag.name): tagAsset]
		}

		// Removal of tags
		for (tagName in tagsInfo.remove) {
			TagAsset tagAsset = assetTagMap[tagName]
			if (tagAsset) {
				tagAssetService.removeTags(context.project, [tagAsset.id])
				countOfTagChanges++
			}
		}

		// Addition of tags
		for (tagName in tagsInfo.add) {
			if (! assetTagMap.containsKey(tagName)) {
				Long tagId = context.projectTags[tagName]
				tagAssetService.applyTags(context.project, [tagId], entity.id)
				countOfTagChanges++
			}
		}

		// Replacement of tags which will be a map of objects with the key/value being the original tag/new tag
		tagsInfo.replace.each { String originalTag, String newTag ->
			// Remove the original tag if exists
			if (assetTagMap.containsKey(originalTag)) {
				TagAsset originalAssetTag = assetTagMap[originalTag]
				tagAssetService.removeTags(context.project, [originalAssetTag.id])
				countOfTagChanges++

				// Add the new tag if it doesn't already exist
				if (! assetTagMap.containsKey(newTag)) {
					Long newTagId = context.projectTags[newTag]
					tagAssetService.applyTags(context.project, [newTagId], entity.id)
				}
			}
		}

		return countOfTagChanges
	}

	/**
	 * Saves {@code AssetComment} List for domain classes. It creates instances using {@code AssetCommentType.COMMENT} type
	 * @param comments a {@code List} of {@code String} values.
	 * @param entity an instance of a domain to be used to link new instances of {@code AssetComment}.
	 * @param context a {@code Map}  with context information.
	 * 			It contains project field used in {@code AssetComment} creation.
	 */
	@Transactional(noRollbackFor=[Throwable])
	void saveCommentsForEntity(List<String> comments, Object entity, Map context) {
		comments.each { String comment ->
			new AssetComment(
				project: context.project,
				comment: comment,
				commentType: AssetCommentType.COMMENT,
				assetEntity: entity
			).save(deepValidate: false)
		}
	}

	/**
	 * This method should be used after any SearchQueryHelper.findEntityByMetaData calls to record errors
	 * into the field or import batch record errors appropriately.
	 * @param fieldName - the field that was being queried
	 * @param fieldsInfo - the map with all of the fields information
	 * @param context - the bag with all of the details being passed around - note that SearchQueryHelperErrors list will be added by SearchQueryHelper.findEntityByMetaData method
	 * @return true if there were any recognized errors otherwise false
	 */
	private boolean recordAnySearchQueryHelperErrors(String fieldName, Map fieldsInfo, Map context) {
		boolean hasErrors = context.searchQueryHelperErrors?.size() > 0
		if (hasErrors) {
			for (String errorMsg in context.searchQueryHelperErrors) {
				addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, errorMsg)
			}
		}
		return hasErrors
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
	@Transactional(noRollbackFor=[Throwable])
	private Object findOrCreateEntity(Map fieldsInfo, Map context ) {
		Object entity
		log.debug 'findOrCreateEntity() called'

		entity = SearchQueryHelper.findEntityByMetaData('id', fieldsInfo, context)
		// Any errors from the function call will be stuffed into context
		recordAnySearchQueryHelperErrors('id', fieldsInfo, context)

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
	 * Used to create a new entity and set various mandatory properties if the fields won't be set
	 * by the data in fieldsMap
	 * @param domainClass - the domain class to be created
	 * @param fieldsMap - a map of the fields that is either the fieldsInfo map or the whenNotFound create map
	 * @param context
	 * @return the newly minted entity instance
	 */
	@Transactional(noRollbackFor=[Throwable])
	Object createEntity(def domainClass, Map fieldsMap, Map context) {
		if (! GormUtil.isDomainClass(domainClass)) {
			throw new DomainUpdateException("Class specified (${domainClass.getName()}) is not a valid domain class")
		}

		Object entity = domainClass.newInstance()

		List propsInDomain = GormUtil.getDomainPropertyNames(domainClass)

		// project
		if ( 'project' in propsInDomain ) {
			entity.project = context.project
		}

		// Add default values for an assetEntity
		if (AssetEntity.isAssignableFrom(domainClass)) {
			entity = customDomainService.setCustomFieldsDefaultValue(context.fieldSpecProject, domainClass, entity)
		}

		// moveBundle
		if ('moveBundle' in propsInDomain) {
			// Only set the bundle to its default if it isn't specified in the fieldsMap which will be set later
			Object bundle = fieldsMap['moveBundle']
			if (! (bundle && ( ((bundle instanceof Map) && bundle.value) || (bundle)) ) ) {
				entity.moveBundle = projectService.getDefaultBundle((Project)context.project)
			}
		}

		// Handle Person references
		for (String fname in ['createdBy', 'modifiedBy']) {
			if (fname in propsInDomain && GormUtil.getDomainPropertyType(domainClass, fname) == Person ) {
				entity[fname] = context.whom
			}
		}

		// Handle owner references (i.e. project.client)
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
	@Transactional(noRollbackFor=[Throwable])
	Boolean bindFieldsInfoValuesToEntity(Object domain, Map fieldsInfo, Map context, List fieldsToIgnore=[]) {
		// TODO - JPM 4/2018 : Refactor bindFieldsInfoValuesToEntity so that this can be used in both the row.field values & the create and update blocks
		//		  JPM 8/2018 : Believe that this may already work. Need to test and remove TODO if so..
		int c = 0
		Boolean noErrorsEncountered = true
		String errMsg

		// Used to add errors and set flag in one line within this function
		Closure recordErrorHelper = { fieldName, msg ->
			noErrorsEncountered = false
			addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, msg)
		}

		// Take the complete list of field names in fieldsInfo and remove 'id' plus any passed to the method
		List<String> fieldNames
		if (fieldsInfo.values().first()?.containsKey('fieldOrder')) {
			// When we have the field order we'll sort them so that the fields are processed in a logical order that solves issues
			// with hierarchical fields (Manufacturer/Model or Room/Rack). Without the sorting the first past of import processing sometimes
			// processes Model before Mfg a not found error occurs.
			fieldNames = fieldsInfo.sort{ it.value.fieldOrder }.collect{ it.key }
		} else {
			fieldNames = fieldsInfo.keySet() as List
		}

		log.debug 'bindFieldsInfoValuesToEntity() starting with fields to update of: {}', fieldNames.join(', ')
		if (fieldsToIgnore == null) {
			fieldsToIngnore = []
		}
		fieldsToIgnore.addAll(['id'])
		fieldNames = fieldNames - fieldsToIgnore

		Boolean isNewEntity = (Boolean)(domain.id == null)
		String domainShortName = GormUtil.domainShortName(domain)
		Class domainClass = domain.getClass()

		for (fieldName in fieldNames) {
			if ( fieldName in PROPERTIES_THAT_CANNOT_BE_MODIFIED ) {
				recordErrorHelper(fieldName,'Modifying the field is not allowed')
				continue
			}

			log.debug 'bindFieldsInfoValuesToEntity() Processing {}.{} class {}', domainShortName, fieldName, domainClass

			// Check for exception fields that should be ignored
			if (DOMAIN_FIELD_EXCEPTIONS."$domainShortName"?."$fieldName"?.'ignore') {
				continue
			}
			if (! GormUtil.isDomainProperty(domainClass, fieldName)) {
				recordErrorHelper(fieldName, 'Field name is not a property of this domain')
				continue
			}

			def domainValue = domain[fieldName]
			def (value, initValue) = SearchQueryHelper.getValueAndInitialize(fieldName, fieldsInfo)
			boolean isInitValue = (initValue != null)

			// The field must have a value to set otherwise the logic will skip over it. As such this logic
			// will determine if the value or init properties have a real value and set a few boolean flags.
			def valueToSet = isInitValue ? initValue : value

			Boolean isReference = GormUtil.isReferenceProperty(domain, fieldName)

			// This will get populated with the domain's existing value for fields to be recorded when different
			Object existingValue = domain[fieldName]

			// --------------------------------------------------
			// Deal with setting the field to NULL when it's not a reference. References are handled separately.
			// --------------------------------------------------
			if (!isReference && valueToSet == null) {
				if (existingValue) {
					// Check to see if the field is nullable
					if (GormUtil.getConstraintValue(domain, fieldName, 'nullable')) {
						if (isReference) {
							existingValue = existingValue.toString()
						}
						_recordChangeOnField(domain, fieldName, null, isInitValue, fieldsInfo)
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
				Class refDomain = GormUtil.getDomainPropertyType(domainClass, fieldName)


				valueToSet = SearchQueryHelper.findEntityByMetaData(fieldName, fieldsInfo, context, domain)

				//TM-16914 fixes an issue where the value already exists in this session, and the valueToSet becomes detached.
				if(!valueToSet.isAttached() && valueToSet.id == existingValue.id){
					valueToSet = existingValue
				}

				recordAnySearchQueryHelperErrors(fieldName, fieldsInfo, context)
				log.debug 'bindFieldsInfoValuesToEntity() {} {} {} {}',
					fieldName, refDomain.getName(), (valueToSet ? valueToSet.getClass().getName() : null), valueToSet
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

						// Check to see if there is a create block which would ultimately resolve the issue subsequently
						if (hasWhenNotFoundCreate(fieldName, fieldsInfo)) {
							valueToSet = createReferenceEntityFromWhenNotFound(fieldName, fieldsInfo, context)
							if (valueToSet) {
								_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, fieldsInfo)
								// Update cache for this reference object
								Class domainClassToCreate
								(domainClassToCreate, errMsg) = SearchQueryHelper.classOfDomainProperty(fieldName, fieldsInfo, context.domainClass)
								if (errMsg) {
									recordErrorHelper(fieldName, errMsg)
								} else {
									String refDomainShortName = GormUtil.domainShortName(domainClassToCreate)
									String md5 = SearchQueryHelper.generateMd5OfFieldsInfoField(refDomainShortName, fieldName, fieldsInfo)
									log.debug "bindFieldsInfoValuesToEntity() Updating cache value for key {} with {}", md5, valueToSet
									context.cache.put(md5, valueToSet)
								}
							} else {
								log.debug "bindFieldsInfoValuesToEntity() call to createReferenceEntityFromWhenNotFound failed!"
								// The createReferenceEntityFromWhenNotFound must of recorded some error in the propertyName of fieldsInfo
								noErrorsEncountered = false
							}
						} else {
							recordErrorHelper(fieldName, 'Unable to manage this reference record')
						}
						break

					default:
						_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, fieldsInfo)

						if (hasWhenFoundUpdate(fieldName, fieldsInfo)) {
							// Process the 'whenFound update' logic if it was specified on the reference
							Map fieldsValueMap = fieldsInfo[fieldName][WHEN_FOUND_UPDATE_PROPERTY]
							List<String> errMsgList = updateReferenceEntityFromWhenFound(valueToSet, fieldsValueMap, context)
							if (errMsgList && errMsgList.size() > 0) {
								recordErrorHelper(fieldName, errMsgList.join(','))
							}
						}
				}

			} else {
				// --------------------------------------------------
				// Process native Java data types
				// --------------------------------------------------
				errMsg = setNonReferenceField(domain, fieldName, valueToSet, isInitValue, fieldsInfo)
				if (errMsg) {
					recordErrorHelper(fieldName, errMsg)
				}
			}
		}
		log.debug 'bindFieldsInfoValuesToEntity() dirty fields {}', domain.dirtyPropertyNames
		return noErrorsEncountered
	}

	/**
	 * Used to assign values to domain fields for non-reference field types (aka Java types). When the fieldsInfo map is
	 * included then any changed field will get recorded.
	 *
	 * @param domainInstance - the domain entity to save values to
	 * @param fieldName - the field to update
	 * @param newValue - the value to save
	 * @param isInitValue - flag if the value being set is an initialize only value
	 * @param fieldsInfo - the JSON data from the import batch record
	 * @return a String containing an error that occurred otherwise null
	 */
	@Transactional(noRollbackFor=[Throwable])
	String setNonReferenceField(Object domain, String fieldName, Object valueToSet, Boolean isInitValue=false, Map fieldsInfo=null) {
		String errorMsg
		Class fieldClassType = GormUtil.getDomainPropertyType(domain.getClass(), fieldName)
		log.debug 'setNonReferenceField() processing {}, type={}, value={}', fieldName, fieldClassType.getName(), valueToSet
		try {
			switch (fieldClassType) {
				case String:
					_recordChangeOnField(domain, fieldName, valueToSet as String, isInitValue, fieldsInfo)
					break

				case Integer:
					// TODO : JPM 7/2018 : is a null value an error or an allowed value?
					Integer numValueToSet = (NumberUtil.isaNumber(valueToSet) ? valueToSet : NumberUtil.toInteger(valueToSet))
					if (numValueToSet == null) {
						errorMsg = 'Value specified must be a numeric value'
					} else {
						_recordChangeOnField(domain, fieldName, numValueToSet, isInitValue, fieldsInfo)
					}
					break

				case Long:
					Long numValueToSet = (NumberUtil.isaNumber(valueToSet) ? valueToSet : NumberUtil.toLong(valueToSet))
					if (numValueToSet == null) {
						errorMsg = 'Value specified must be an numeric value'
					} else {
						_recordChangeOnField(domain, fieldName, numValueToSet, isInitValue, fieldsInfo)
					}
					break

				case Boolean:
					Boolean boolVal = StringUtil.toBoolean(valueToSet)
					_recordChangeOnField(domain, fieldName, boolVal, isInitValue, fieldsInfo)
					break

				case Date:
					// TODO : JPM 7/2018 : Check the database type to see if the type is Date or Datetime and clearTime if the former, parse accordingly too
					if (valueToSet instanceof CharSequence) {
						try {
							// If it is a String and is an ISO8601 Date or DateTime format then we can attempt to parse it for them
							if (valueToSet =~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ ) {
								valueToSet = TimeUtil.parseDate(TimeUtil.FORMAT_DATE_ISO8601, valueToSet, TimeUtil.FORMAT_DATE_ISO8601)
							} else if (valueToSet =~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}Z$/ ) {
								valueToSet = TimeUtil.parseDateTime(valueToSet, TimeUtil.FORMAT_DATE_TIME_ISO8601)
							} else if (valueToSet =~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$/ ) {
								valueToSet = TimeUtil.parseDateTime(valueToSet, TimeUtil.FORMAT_DATE_TIME_ISO8601_2)
							}
						} catch (e) {
							errorMsg = 'Error parsing date: ' + e.message
							break
						}
					}

					if (! (valueToSet instanceof Date)) {
						String columnType
						errorMsg = 'Value must be a Date or in ISO-8601 format (yyyy-MM-dd | yyyy-MM-ddTHH:mm:ssZ)'
					} else {
						// Attempt to parse a Date / DateTime based on the underlying database table column type
						// def mapping = GormUtil.getDomainBinderMapping(domain.getClass())
						// def propConfig = mapping.getPropertyConfig(fieldName)
						// valueToSet.clearTime()
						_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, fieldsInfo)
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
						_recordChangeOnField(domain, fieldName, enumValue, isInitValue, fieldsInfo)
					} catch (e) {
						errorMsg = 'Unable to determine ENUM value'
					}
					break

				case Double:
					if ((valueToSet instanceof CharSequence)) {
						valueToSet = NumberUtil.toDouble(valueToSet)
						if (valueToSet == null) {
							errorMsg = 'Unable to convert value to Double'
							break
						}
					}
					_recordChangeOnField(domain, fieldName, valueToSet, isInitValue, fieldsInfo)
					break

				default:
					log.debug 'setNonReferenceField() processing {}, type={}, value={}', fieldName, fieldClassType.getName(), valueToSet
					errorMsg = "Unsupported data type ${fieldClassType.getName()}"
			}
		} catch (e) {
			log.error ExceptionUtil.stackTraceToString('Error while setting property', e)
			errorMsg = 'Error while setting property - ' + e.message
		}

		return errorMsg
	}

	/**
	 * Used to determine if there is a 'whenNotFound create' data structure for a given property in the ETL meta-data
	 * @param fieldName - the fieldname in question
	 * @param fieldsInfo - the map of all fields in the ETL meta data
	 * @return true if the field contains the 'create' map and the map contains key/value pairs otherwise false
	 */
	Boolean hasWhenNotFoundCreate(fieldName, fieldsInfo) {
		return (fieldsInfo[fieldName].containsKey(WHEN_NOT_FOUND_CREATE_PROPERTY)
			&& fieldsInfo[fieldName][WHEN_NOT_FOUND_CREATE_PROPERTY] != null
			&& fieldsInfo[fieldName][WHEN_NOT_FOUND_CREATE_PROPERTY]?.size() > 0)
	}

	/**
	 * Used to determine if there is a 'whenFound update' data structure for a given property in the ETL meta-data
	 * @param fieldName - the fieldname in question
	 * @param fieldsInfo - the map of all fields in the ETL meta data
	 * @return true if the field contains the 'update' map and the map contains key/value pairs otherwise false
	 */
	Boolean hasWhenFoundUpdate(fieldName, fieldsInfo) {
		return (fieldsInfo[fieldName].containsKey(WHEN_FOUND_UPDATE_PROPERTY)
			&& fieldsInfo[fieldName][WHEN_FOUND_UPDATE_PROPERTY] != null
			&& fieldsInfo[fieldName][WHEN_FOUND_UPDATE_PROPERTY].size() > 0)
	}

	/**
	 * Used to actually assign values to the domain being created or updated. Additionally if the fieldsInfo is
	 * included then the previousValue will be recorded in fieldsInfo when the field changes on an existing entity.
	 *
	 * @param domainInstance - the domain entity to save values to
	 * @param fieldName - the field to update
	 * @param newValue - the value to save
	 * @param isInitValue - flag if the value being set is an initialize only value (optional default false)
	 * @param fieldsInfo - the JSON data from the import batch record (optional)
	 */
	@Transactional(noRollbackFor=[Throwable])
	void _recordChangeOnField( Object domainInstance, String fieldName, Object newValue, Boolean isInitValue=false, Map fieldsInfo = null) {
		Object existingValue = domainInstance[fieldName]
		Boolean isNewEntity = (! domainInstance.id)

		log.debug '_recordChangeOnField() for {}.{} existingValue={}, newValue={}, isNewRecord={}, isInitValue={}',
			domainInstance.getClass().getName(), fieldName, existingValue, newValue, isNewEntity, isInitValue

		if ( !isInitValue || (isInitValue && existingValue == null)) {
			boolean isMatch
			if (existingValue && GormUtil.isDomainClass(existingValue.getClass())) {
				// For domain references we need to compare IDs because the objects are not always the same
				isMatch = existingValue.id == newValue.id
			} else {
				isMatch = existingValue == newValue
			}
			if (! isMatch) {
				log.debug "_recordChangeOnField() changed field {} = '{}'", fieldName, newValue
				domainInstance[fieldName] = newValue
				if (! isNewEntity) {
					log.debug '_recordChangeOnField() dirtyPropertyNames={}', domainInstance.dirtyPropertyNames
				}

				// Record the change on the fieldsInfo if it was passed in
				if (fieldsInfo && ! isNewEntity) {
					fieldsInfo[fieldName][PREVIOUS_VALUE_PROPERTY] = existingValue.toString()
				}
			}
		}
	}

	/**
	 * Used to clear out the errors recorded at the field level and row level and the record level count.
	 * It also removes previousValue key from fieldsInfo when it exist so previous value data shown is more accurate.
	 * @param record - the import batch record to clear
	 * @param fieldsInfo - the Map of the record.fieldsInfo to be cleared
	 * @param tagsInfo - the Map of the record.tags to be cleared
	 *
	 */
	@Transactional(noRollbackFor=[Throwable])
	private void resetRecordAndFieldsInfoErrors(ImportBatchRecord record, Map fieldsInfo) {
		record.errorCount = 0
		record.resetErrors()
		for (field in fieldsInfo) {
			field.value.errors = []
			if (field.value.containsKey(PREVIOUS_VALUE_PROPERTY)) {
				field.value.remove(PREVIOUS_VALUE_PROPERTY)
			}
		}
	}

	/**
	 * Used to validate tags defined in {@code ImportBatchRecord#tags}.
	 * @param tagsInfo - the Map of the record.tags to be cleared
	 * @param projectTags - the Map of the project tags used for validation.
	 *
	 */
	@Transactional(noRollbackFor=[Throwable])
	private String validateTagReferences(Map tagsInfo, Map projectTags) {

		Set tagsNames = [] as Set
		tagsNames.addAll(tagsInfo.add)
		tagsNames.addAll(tagsInfo.remove)
		tagsNames.addAll(tagsInfo.replace.keySet())
		tagsNames.addAll(tagsInfo.replace.values())

		Set tagsNotFound = tagsNames.findAll { !projectTags.containsKey(it) }
		if (!tagsNotFound.isEmpty()){
			return "The follow tag(s) were not found: ${tagsNotFound.join(', ')} in current project"
		} else {
			return null
		}
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
	@Transactional(noRollbackFor=[Throwable])
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
	@Transactional(noRollbackFor=[Throwable])
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
	@Transactional(noRollbackFor=[Throwable])
	private Integer tallyNumberOfErrors(ImportBatchRecord record, Map fieldsInfo) {
		Integer count = 0
		for (field in fieldsInfo) {
			count += (field.value['errors'] ? field.value['errors'].size() : 0 )
		}
		count += record.errorListAsList().size()
		return count
	}

	/**
	 * Used to create a reference entity and populate it with the name/value pairs from the whenNotFound data structure. If any
	 * errors occurred while setting up the new entity one or more errors will be recorded against the reference property field.
	 *
	 * @param referenceFieldName - the field name of the reference object in the primary entity
	 * @param fieldsInfo - the ETL meta data for the current row
	 * @param context - the context containing all the goodies for the process
	 * @return the newly minted domain object or null if an error occurred
	 */
	@Transactional(noRollbackFor=[Throwable])
	Object createReferenceEntityFromWhenNotFound(String referenceFieldName, Map fieldsInfo, Map context) {
		Object entity
		List<String> errorMsgs = []

		log.debug 'createReferenceEntityFromWhenNotFound() CREATING reference entity for property {}', referenceFieldName

		// Do some initial validation
		if (!fieldsInfo.containsKey(referenceFieldName)) {
			errorMsgs << "Property $referenceFieldName was missing from ETL meta-data"
		} else {
			Map referenceField = fieldsInfo[referenceFieldName]
			Map fieldsValueMap = referenceField[WHEN_NOT_FOUND_CREATE_PROPERTY]
			if (! fieldsValueMap) {
				errorMsgs << "Missing 'whenNotFound create' to create new entity"
			} else {

				// Determine the class to be created (AssetEntity can be tricking due to the inheritance)
				Class domainClassToCreate
				String errMsg
				(domainClassToCreate, errMsg) = SearchQueryHelper.classOfDomainProperty(referenceFieldName, fieldsInfo, context.domainClass)
				if (errMsg) {
					errorMsgs << errMsg
				} else {

					// Create the domain and set any of the require properties that are required
					entity = createEntity(domainClassToCreate, fieldsValueMap, context)
					boolean isPerson = Person.isAssignableFrom(entity.class)

					// Check if isPerson and fieldsValueMap contains 'fullName' which means that user is looking for a person by fullName
					// so we need to replace that with the correct parsing of full name into [firstName, lastName and middleName]
					if (isPerson && fieldsValueMap.containsKey(Person.FULLNAME_KEY)) {
						replaceFullnameEntryByPersonFullnameMap(fieldsValueMap)
					}

					fieldsValueMap.each { fieldName, value ->
						errMsg = setDomainPropertyWithValue(entity, fieldName, fieldsValueMap,  context, referenceFieldName)
						if (errMsg) {
							errorMsgs << errMsg
						}
					}

					// Attempt to save this sucker
					if (errorMsgs.size() == 0) {
						if (entity.save(flush:true, failOnError:false)) {
							if (isPerson) {
								associatePersonToCompanyAndProject(context.project, entity)
							}
						} else {
							errorMsgs << GormUtil.allErrorsString(entity)
						}
					}
				}
			}
		}

		// If there were any errors then record them and cleanup
		if (errorMsgs.size() > 0) {
			for (String msg in errorMsgs) {
				addErrorToFieldsInfoOrRecord(referenceFieldName, fieldsInfo, context, msg)
			}

			if (entity) {
				// Discard this new entity that errored while attempting to process
				entity.discard()
				entity = null
			}
		}

		return entity
	}

	/**
	 * Used to update a reference entity with values from the 'whenFound update' ETL command. This will set each of
	 * the fields that were specified in the command. If there are any errors then the changes will rollback and
	 * the error message(s) returned.
	 *
	 * @param entity - the reference entity
	 * @param fieldsValueMap - the map of field namess and values
	 * @param context - the context containing all the goodies for the process
	 * @return a list of any errors that were encountered
	 */
	@Transactional(noRollbackFor=[Throwable])
	List<String> updateReferenceEntityFromWhenFound(Object entity, Map fieldsValueMap, Map context) {
		List<String> errMsgs = []

		log.debug 'updateReferenceEntityFromWhenFound() UPDATING reference entity {} {} with {}',
			entity.getClass().getName(), entity, fieldsValueMap

		// check if isPerson and fieldsValueMap contains 'fullName' which means that user is lookign for a person by fullName
		// so we need to replace that with the correct parsing of full name into [firstName, lastName, middlename]
		if (Person.isAssignableFrom(entity.class) && fieldsValueMap.containsKey(Person.FULLNAME_KEY)) {
			replaceFullnameEntryByPersonFullnameMap(fieldsValueMap)
		}

		// Get the list of all the fields to be set from the create section of the fieldsInfo of the referenceField
		fieldsValueMap.each { fieldName, value ->
			String failureMsg = setDomainPropertyWithValue(entity, fieldName, fieldsValueMap, context)
			if (failureMsg) {
				errMsgs << failureMsg
			}
		}

		// Attempt to save this sucker
		if (errMsgs.size() == 0) {
			if (! entity.save(flush:true, failOnError:false)) {
				errMsgs << GormUtil.allErrorsString(entity)
			}
		}

		// If there were any errors then record them and cleanup
		if (errMsgs.size() > 0) {
			// Discard this new entity that errored while attempting to process
			entity.discard()
			log.debug 'updateReferenceEntityFromWhenFound() encountered errors: {}', errMsgs
		}

		return errMsgs
	}

	/**
	 * Used to set a property on a domain entity for the whenNotFound create/whenFound update commands
	 * @param entity - the domain entity to be manipulated
	 * @param fieldName - the field to be set
	 * @param fieldsValueMap - a map consistenting of the values that each of the fields will be set to
	 * @param context - the grand poopa of objects for the Import Process
	 * @return null if successful otherwise a string containing the error message that occurred
	 */
	@Transactional(noRollbackFor=[Throwable])
	String setDomainPropertyWithValue(Object entity, String fieldName, Map fieldsValueMap, Map context, String referenceFieldName = '') {
		String errorMsg = null
		log.debug 'setDomainPropertyWithValue() called with {}.{}',
			entity.getClass().getName(), fieldName

		while (true) {
			// TODO : JPM 4/2018 : Lookup the FieldSpecs for assets to get the label names for errors
			if ( (fieldName in PROPERTIES_THAT_CANNOT_BE_MODIFIED) ) {
				errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_CANNOT_BE_SET_MSG, [propertyName:fieldName])
				break
			} else {
				if (! GormUtil.isDomainProperty(entity, fieldName)) {
					errorMsg = "Unknown field name ${fieldName}"
					break
				}

				if (GormUtil.isReferenceProperty(entity, fieldName)) {
					List<Object> entities

					// Attempt to find the reference object
					(entities, errorMsg) = SearchQueryHelper.fetchReferenceOfEntityField(entity, fieldName, fieldsValueMap, context, referenceFieldName)
					if (! errorMsg) {
						if (entities == null) {
							errorMsg = "Reference field $fieldName does not support alternate key lookup"
						} else if (entities.size() > 1) {
							errorMsg = "Multiple results were found for reference field $fieldName"
						} else if (entities.size() == 0) {
							errorMsg = "Unable to find reference for field $fieldName"
						}
					}
					if (errorMsg) {
						break
					}

					// Only set if different so as not to trigger the dirty flag unnecessarily
					if (entity[fieldName] != entities[0]) {
						entity[fieldName] = entities[0]
					}

				} else {
					// Set the non-reference / Java types (e.g. Date, Integer, String, Boolean)
					errorMsg = setNonReferenceField(entity, fieldName, fieldsValueMap[fieldName]?.value)
				}
			}
			break
		}
		return errorMsg
	}

	/**
	 * Schedule a quartz job for the ETL transform data process
	 * @param project
	 * @param dataScriptId
	 * @param filename
	 * @param sendNotification
	 * @return Map - containing the progress key created to monitor the job execution progress
	 */
	@NotTransactional()
	Map<String, String> scheduleETLTransformDataJob(Project project, Long dataScriptId, String filename, Boolean sendNotification = false) {
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
					errorMsg = 'ETL Script has no source specified'
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
		trigger.jobDataMap.sendNotification = sendNotification
		trigger.jobDataMap.userLogin = securityService.loadCurrentPerson().userLogin
		trigger.setJobName(ETLTransformDataJob.class.name)
		trigger.setJobGroup('tdstm-etl-transform-data')
		quartzScheduler.scheduleJob(trigger)

		log.info('scheduleJob() {} kicked of an ETL transform data process for script and filename ({},{})',
				securityService.currentUsername, dataScriptId, filename)

		// return progress key
		return ['progressKey': key, 'jobTriggerName': jobTriggerName]
	}

	/**
	 * Load the fieldsValueMap appropriate with the returned name map (e.g. fieldsValueMap.put('firstName', nameMap.first) )
	 * @param fieldsValueMap - map contanining person fullName key
	 */
	private void replaceFullnameEntryByPersonFullnameMap(Map fieldsValueMap) {
		Map nameMap = personService.parseName(fieldsValueMap.get(Person.FULLNAME_KEY))
		fieldsValueMap.remove(Person.FULLNAME_KEY)
		if (nameMap) {
			fieldsValueMap.put('firstName', nameMap.first)
			fieldsValueMap.put('lastName', nameMap.last)
			fieldsValueMap.put('middleName', nameMap.middle)
		}
	}

	/**
	 * Person object is created presently via the ETL process there is no consideration
	 * for the company that they are associated to or the project.
	 * This method does that association.
	 *
	 * @param project - current user project
	 * @param person - new person being created
	 */
	private void associatePersonToCompanyAndProject(Project project, Person person) {
		partyRelationshipService.addCompanyStaff(project.client, person)
		personService.addToProjectSecured(project, person)

	}
}

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
