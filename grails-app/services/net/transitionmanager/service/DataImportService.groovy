package net.transitionmanager.service

import com.tdsops.common.sql.SqlUtil
import com.tdsops.etl.DataImportHelper
import com.tdsops.etl.ETLDomain
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum

import groovy.util.logging.Slf4j
import grails.transaction.Transactional
import grails.transaction.NotTransactional
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.TransactionDefinition

// LEGACY
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.DataTransferValue

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
	SecurityService securityService

	// List of the domains that are supported by the Legacy Batch import
	static final Map LEGACY_DOMAIN_CLASSES = [
			( ETLDomain.Application.name() ) : ETLDomain.Application,
			( ETLDomain.Database.name() ) : ETLDomain.Database,
			// ( ETLDomain.Device.name() ) : ETLDomain.Device,
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

			// The following are reset per domain
			domainClass: null,
			fields: [],
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
					importContext.fields = domainJson.fields

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
		EavEntityType eavEntityType = EavEntityType.findByDomainName(transDomainName)

		// If the asset class is invalid, return null
		if (! eavEntityType) {
			errors << "Import does not support domain type ${domainClass}"
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
				fieldNameList: importContext.fields,
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

		// TODO : JPM 2/2018 : CRITICAL - presently getting error message that ID must be a numeric value
		Long domainId = getAndValidateDomainId(rowData, importContext)

		// Process the row as long as there wasn't an error with the ID reference
		if (domainId == null || domainId > 0) {

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
		}
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
		for (fieldName in importContext.fields) {

			// println "** insertRowDataIntoDataTransferValues() domainId=$domainId, fieldName=$fieldName"

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
					assetEntityId: domainId,
					importValue: field.originalValue,
					correctedValue: fieldValue,
					rowId: rowNum
				)

				// println "** insertRowDataIntoDataTransferValues() domainId=$domainId for batch ${batch.id}, batchRecord.assetEntityId=${batchRecord.assetEntityId} field:${batchRecord.fieldName} value: ${batchRecord.correctedValue}"

				Boolean triggerFailureTest = (fieldValue == 'oradbsrv02')

				// This should NOT throw an exception because we're dealing errors in a savepoint rollback
				if (! batchRecord.save(failOnError:false) || triggerFailureTest ) {
					// println "** FORCING ROLLBACK"

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

		// TODO : JPM 2/2018 : TM-9598 Should be able drop this map
		final Map operationMap = [
			I:ImportOperationEnum.INSERT,
			U:ImportOperationEnum.UPDATE,
			D:ImportOperationEnum.DELETE
		]
		ImportOperationEnum OpValue = (operationMap.containsKey(rowData.op) ? operationMap[rowData.op] : ImportOperationEnum.UNDETERMINED)
		// TODO : JPM 2/2018 : TM-9598 Should be able to use this command
		// ImportOperationEnum OpValue =  ImportOperationEnum.lookup(rowData.op),

		ImportBatchRecord batchRecord = new ImportBatchRecord(
			importBatch: batch,
			operation: OpValue,
			domainPrimaryId: domainId,
			// TODO : JPM 2/2018 : Replace sourceRowId with value from rowData.rowNum when TM-9510 is implemented
			sourceRowId: rowNum,
			errorCount: rowData.errors.size(),
			errorList: (rowData.errors ?: '[]'),
			warn: (rowData.warn ? 1 : 0),
			duplicateReferences: dupsFound,
			fieldsInfo: rowData.fields
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

}
