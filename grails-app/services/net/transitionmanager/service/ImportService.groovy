package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.sql.SqlUtil
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StopWatch
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdssrc.grails.WorkbookUtil
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.DataTransferValue
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.hibernate.FlushMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import grails.transaction.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.DateFormat
import java.util.regex.Matcher

import static com.tdsops.tm.enums.domain.AssetClass.*

// <SL>: Commented this out due to multiple implementations of logging
//@Slf4j
class ImportService implements ServiceMethods {
	private Logger log = LoggerFactory.getLogger(ImportService.class)

	// The spreadsheet columns that are Date format
	private static final List<String> importColumnsDateType = ['MaintExp', 'Retire']
	// SQL statement that is used to insert values the temporary import table
	private static final String DTV_INSERT_SQL =
			"INSERT INTO data_transfer_value " +
				"(asset_entity_id, import_value,row_id, data_transfer_batch_id, field_name, has_error, error_text) VALUES "

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService assetEntityService
	DeviceService deviceService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProgressService progressService
	UserPreferenceService userPreferenceService
	def jdbcTemplate
	CustomDomainService customDomainService
	MessageSource messageSource
	AssetOptionsService assetOptionsService

	static final String indent = '&nbsp;&nbsp;&nbsp;'
	static final String NULL_INDICATOR='NULL'

	// Indicates the number of rows to process before performing a flush/clear of the Hibernate session queue
	static final int HIBERNATE_BATCH_SIZE=20

	static final Map LEGACY_DOMAIN_CLASSES = [
			( ETLDomain.Application.name() ) : ETLDomain.Application,
			( ETLDomain.Database.name() ) : ETLDomain.Database,
			( ETLDomain.Device.name() ) : ETLDomain.Device,
			( 'AssetEntity' ) : ETLDomain.Device,
			( 'Files' ) : ETLDomain.Files,
			( ETLDomain.Storage.name() ) : ETLDomain.Storage
		]

	/**
	 * Used to lookup and validate a batch id exists and is associated to the current project
	 * @param id - the batch id to lookup
	 * @param project - the project that the user is associated with
	 * @return [DataTransferBatch object, String error message if any] - if error then the DTB object will be null
	 */
	List getAndValidateBatch(String id, Project project) {
		String errorMsg
		DataTransferBatch dtb
		Long batchId = NumberUtil.toLong(id)
		if (batchId == null || batchId < 1) {
			errorMsg = 'An invalid batch id was submitted'
		} else {
			dtb = DataTransferBatch.get(batchId)
			if (!dtb) {
				errorMsg = 'Unable to find specified batch'
			} else {
				if (dtb.project.id != project.id) {
					securityService.reportViolation("getAndValidateBatch() call attempted to access batch ($batchId) not associated to user's project ($project.id)")
					errorMsg = 'Unable to locate specified batch'
					dtb = null
				}
			}
		}
		return [dtb, errorMsg]
	}

	/**
	 * Used by the process methods to peform the common validation checks before processing
	 * @param project - the project that the user is logged into and the batch is associated with
	 * @param userLogin - the user login object of whom invoked the process
	 * @param assetClass - the asset class type
	 * @param batchId - the id number of the batch to be processed
	 */
	private DataTransferBatch processValidation(Project project, UserLogin userLogin, AssetClass assetClass, Long batchId) {

		log.info 'processValidate(project:{}, userLogin:{}, assetClass:{}, batchId:{}) started invoked at ({})',
				project.id, userLogin, assetClass, batchId, new Date()

		// TODO BB
		securityService.requirePermission userLogin.toString(), Permission.AssetImport, false,
				"Attempted to process asset imports without permission, project:$project, batchId:$batchId"

		Long id = NumberUtil.toLong(batchId)
		if (id == null || id < 1) {
			securityService.reportViolation("Attempted to process asset imports invalid batch id ($batchId), project:$project", userLogin.toString())
			throw new InvalidParamException("Invalid batch id was requested")
		}

		def dataTransferBatch = DataTransferBatch.get(batchId)
		if (!dataTransferBatch) {
			securityService.reportViolation("Attempted to process asset imports with missing batchId $batchId, project:$project", userLogin.toString())
			throw new InvalidParamException('Unable to find the specified batch')
		}
		if (dataTransferBatch.project.id != project.id) {
			securityService.reportViolation("Attempted to process asset imports with batchId $batchId not assocated with their session ($project.id)", userLogin.toString())
			throw new InvalidParamException('Unable to find the specified batch')
		}

		if (AssetClass.domainNameFor(dataTransferBatch.assetClass) != domainNameFor(assetClass)) {
			throw new InvalidParamException("Specified batch is not for the asset class $assetClass")
		}

		if (dataTransferBatch.statusCode == DataTransferBatch.COMPLETED) {
			log.warn "$warn for previously processed batch $batchId, project:$project"
			throw new InvalidParamException('Specified batch was previously processed')
		}

		return dataTransferBatch
	}

	/**
	 * Utility method for tbeh batch processing to load the import batch data
	 * @param batchId - the batch id number to process
	 * @return a map containing the following elements:
	 *		assetsInBatch - the number of assets in the batch
	 *		eavAttributeSet - the attribute set for set #1
	 *		staffList - the company staff for the project associated with the batch
	 *      dataTransferValueRowList - the list of DataTransferValue row ids
	 */
	private Map loadBatchData(DataTransferBatch dtb) {
		Project project = dtb.project

		Map data = [:]

		data.assetsInBatch = DataTransferValue.executeQuery("select count(distinct rowId) from DataTransferValue where dataTransferBatch=?", [dtb])[0]
		data.dataTransferValueRowList = DataTransferValue.findAll("From DataTransferValue d where d.dataTransferBatch=? group by rowId", [dtb])

		List<Party> companies = partyRelationshipService.getProjectCompanies(project)
		data.staffList = partyRelationshipService.getAllCompaniesStaffPersons(companies)

		return data
	}

	/**
	 * Used to update the progressService with the current state of the import job
	 * @param progressKey - the key used to access the
	 * @param current - index of the current record
	 * @param total - the index of the total number of records to process
	 */
	void jobProgressUpdate(String progressKey, int current, int total) {
		log.debug 'jobProgressUpdate called (current={}, total={})', current, total

		current = current == 0 ? 1 : current

		// Only increment on modulus of 2% so we're not overwhelming the system unless the values are the same
		if (current == total) {
			progressService.update(progressKey, 100, ProgressService.STARTED)
		} else {
			if (total < 1 || current > total) {
				log.error 'jobProgressUpdate() called with invalid total ({}, {}, {})', progressKey, current, total
			} else {
				//int twoPerc = Math.round(total/100*2)
				//if (twoPerc == 0)
				//	twoPerc = 1
				//if (twoPerc > 0 && current.mod(twoPerc)==0) {
					int percComp = Math.round(current/total*100)
					progressService.update(progressKey, percComp, ProgressService.STARTED, "$current of $total")
				//}
			}
		}
	}

	/**
	 * Used to update the progressService with the current state of the import job
	 * @param progressKey - the key used to access the
	 * @param current - index of the current record
	 * @param total - the index of the total number of records to process
	 */
	void jobProgressFinish(String progressKey, String info) {
		log.debug 'jobProgressFinish called'
		progressService.update(progressKey, 100I, ProgressService.COMPLETED, info)
	}

	/**
	 * Used to retrieve the list of valid Device Type values as a Map that is used for faster validation. Note that the
	 * type is force to lowercase.
	 * @return Map of the Device Type names (lowercase) as the key and value (propercase)
	 */
	Map getDeviceTypeMap() {
		// Get a Device Type Map used to verify that device type are valid
		List deviceTypeList = assetEntityService.getDeviceAssetTypeOptions()
		Map deviceTypeMap = new HashMap(deviceTypeList.size())
		deviceTypeList.each { type -> deviceTypeMap[type.toLowerCase()] = type }
		return deviceTypeMap
	}

	/**
	 * Used to review an import batch for any unexpected issues
	 * @param projectId - the id of the project
	 * @param userLoginId - the id of the user that has invoked the process
	 * @param batchId - the id of the batch to examine
	 * @param progressKey - the key used to update the progress bar service
	 * @return An error message if any issues otherwise null
	 */
	Map reviewImportBatch(Long projectId, Long userLoginId, Long batchId, String progressKey) {
		def startedAt = new Date()

		String errorMsg = ''

		String methodName = 'reviewImportBatch()'
		StringBuilder sb = new StringBuilder()

		GormUtil.setSessionFlushMode FlushMode.COMMIT

		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userLoginId)

		boolean performance = true
		def now = new Date()

		DataTransferBatch dtb = DataTransferBatch.get(batchId)
		if (!dtb) {
			return [error:'Unable to find batch id']
		}
		if (dtb.project.id != projectId) {
			securityService.reportViolation("reviewImportBatch() call attempted to access batch ($batchId) not associated to user's project ($projectId)",
					userLogin.toString())
			return [error: 'Unable to locate batch id']
		}

		// <SL> TODO: requires to update tables (data_transfer_batch, eav_entity_type) to use AssetClass instead
		boolean batchIsForDevices = AssetClass.domainNameFor(dtb.assetClass) == "AssetEntity"

		// Get a Device Type Map used to verify that device type are valid
		Map deviceTypeMap = getDeviceTypeMap()

		// A map that will be used to track the invalid referenced Device Types
		Map invalidDeviceTypeMap = [:]

		if (performance) now = new Date()
		List<DataTransferValue> dataTransferValueRowList = DataTransferValue.findAll(
			"From DataTransferValue d where d.dataTransferBatch=? " +
			"and d.dataTransferBatch.statusCode='PENDING' group by rowId", [dtb])
		if (performance) {
			log.debug("Fetching DataTransferValue ROWS took {}", TimeUtil.elapsed(now))
		}

		if (performance) {
			now = new Date()
		}

		List<Long> assetIds = AssetEntity.where {
			project == project
		}.projections {property("id")}.list()

		if (performance) {
			log.debug("Fetching existing asset IDS took {}", TimeUtil.elapsed(now))
		}

		def assetIdList = []
		def dupAssetIds = []
		def notExistedIds = []
		Map mfgModelMatches = [:]

		def assetCount = dataTransferValueRowList.size()

		if (performance) {
			log.info '{} Initialization took {}', methodName, TimeUtil.elapsed(now)
		}

		now = new Date()
		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			int rowNum = dataTransferValueRow + 1
			def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId

			if (batchIsForDevices) {
				String mfgName = DataTransferValue.findWhere(dataTransferBatch: dtb, rowId: rowId, fieldName: "manufacturer")?.importValue
				String modelName = DataTransferValue.findWhere(dataTransferBatch: dtb, rowId: rowId, fieldName: "model")?.importValue

				mfgName = StringUtil.defaultIfEmpty(mfgName, '')
				modelName = StringUtil.defaultIfEmpty(modelName, '')

				boolean found = false
				if (mfgName || modelName) {
					// Check to see if it is in the list
					String key = mfgName + '::::' + modelName
					if (mfgModelMatches.containsKey(key)) {
						mfgModelMatches[key].count++
						found = mfgModelMatches[key].found
					} else {
						found = verifyMfgAndModelExist(mfgName, modelName)
						mfgModelMatches[key] = [found: found, count: 1, mfg: mfgName, model: modelName]
					}
				}

				// Validate the device type only if the Mfg/Model were not found
				if (!found) {
					String deviceType = DataTransferValue.findWhere(dataTransferBatch:dtb, rowId:rowId, fieldName: "assetType")?.importValue
					String invalidType
					if (deviceType?.size()) {
						if (!deviceTypeMap.containsKey(deviceType.toLowerCase())) {
							invalidType = deviceType
						}
					} else {
						invalidType = 'NULL'
					}
					if (invalidType?.size()) {
						if (!invalidDeviceTypeMap.containsKey(invalidType)) {
							invalidDeviceTypeMap[invalidType] = 1
						} else {
							invalidDeviceTypeMap[invalidType]++
						}
					}
				}
			}

			// log.debug "Checking for dups"
			// Checking for duplicate asset ids
			if (assetEntityId && assetIdList.contains(assetEntityId)) {
				dupAssetIds << assetEntityId
			}

			// log.debug "Checking for missing ids"
			// Checking for asset ids which does not exist in database
			if (assetEntityId && !assetIds.contains((Long)(assetEntityId))) {
				notExistedIds << assetEntityId
			}

			assetIdList << assetEntityId

			// Update status and clear hibernate session
			jobProgressUpdate(progressKey, rowNum, assetCount)

			project = flushAndClearSession(rowNum, project)

		} // for

		if (performance) log.debug 'Reviewing {} batch records took {}', assetCount, TimeUtil.elapsed(now)

		sb.append("<h3>Review Import for Batch $batchId</h3><ul><li>Assets in batch: $assetCount</li></ul>")

		// Log missed Mfg/Model references
		def missingMfgModel = mfgModelMatches.findAll { k, v -> !v.found}
		if (missingMfgModel) {
			sb.append("<b>Missing Mfg / Model references:</b><ul>")
			missingMfgModel.each { k, d ->
				sb.append("<li>Mfg: $d.mfg | Model: $d.model | $d.count reference${d.count > 1 ? '(s)' : ''}</li>")
			}
			sb.append('</ul>')

			// TM-6495 Re-attach to the latest session since we can lose it on the loop above
			userLogin.attach()
			if (!securityService.hasPermission(userLogin, Permission.ModelCreateFromImport)) {
				sb.append("$indent <b>Note: You do not have the permission necessary to create models during import</b><br>")
			}
		}

		if (dupAssetIds) {
			sb.append("<b>Duplicated asset IDs (col A) $dupAssetIds</b><br>")
			sb.append('<br>')
		}

		if (notExistedIds) {
			sb.append("<b>No match found for asset IDs (col A) #$notExistedIds</b><br>")
			sb.append('<br>')
		}

		if (invalidDeviceTypeMap) {
			sb.append("<b>Invalid device type${invalidDeviceTypeMap.size() > 1 ? 's' : ''} specified:</b><ul>")
			invalidDeviceTypeMap.each { k, v ->
				sb.append("<li>$k ($v)" + '</li>')
			}
			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(startedAt).toString()

		log.info '{} Review process of {} batch records took {}', methodName, assetCount, elapsedTime

		sb.append('<br>Review took ' + elapsedTime + ' to complete')

		String info = sb.toString()

		jobProgressFinish(progressKey, info)

		dtb.importResults = combineDataTransferBatchImportResults(dtb, info)

		return [elapsedTime: elapsedTime, info: info]
	}

	/**
	 * Used to verify that the Manufacturer/Model exists in the model table
	 * @param searchMfgName - the name of the manufacturer to lookup
	 * @param searchModelName - the model name to lookup
	 * @return false if not found
	 */
	private boolean verifyMfgAndModelExist(searchMfgName, searchModelName) {

		boolean mfgBlank = StringUtil.isBlank(searchMfgName)
		boolean modelBlank = StringUtil.isBlank(searchModelName)

		// Don't need to search for a match if mfg/model are blank
		if (mfgBlank || modelBlank) {
			return false
		}

		Manufacturer mfg
		if (!mfgBlank) {
			mfg = Manufacturer.findByName(searchMfgName)
			if (!mfg) {
				mfg = ManufacturerAlias.findByName(searchMfgName)?.manufacturer
				log.debug "verifyMfgAndModelExist() lookup Mfg by alias found: $mfg"
			}
			if (!mfg) {
				log.debug "verifyMfgAndModelExist() failed on MFG for $searchModelName, $searchMfgName"
				return false
			}
		}

		if (!modelBlank) {
			Model model = Model.findByModelName(searchModelName)
			if (!model) {
				model = ModelAlias.findByNameAndManufacturer(searchModelName, mfg)?.model
				log.debug "verifyMfgAndModelExist() lookup Model by alias found: $model"
			}
			if (!model) {
				log.debug "verifyMfgAndModelExist() failed to find MODEL for $searchModelName, $searchMfgName"
				return false
			}
		}

		return true
	}

	/**
	 * Used to validate that it is okay to process the specified batch for given project id
	 * @param projectId - the id of the project that the user is currently assigned to
	 * @param batchId - the id of the batch to be processed
	 * @param progressKey - the progressService key to be used to track the process if the statusCode is in POSTING mode
	 * @return an error message if any or null if it is safe to proceed
	 */
	String validateImportBatchCanBeProcessed(Long projectId, Long userId, Long batchId, String progressKey=null) {
		String errorMsg
		while (true) {
			DataTransferBatch dtb = DataTransferBatch.get(batchId)
			if (!dtb) {
				errorMsg = "Unable to locate batch id ($batchId)"
				break
			}

			// Validate that the project matches the user's
			if (dtb.project.id != projectId) {
				securityService.reportViolation("attemped to post import batch ($batchId) that is not associated with current project ($projectId)", UserLogin.get(userId).toString())
				errorMsg = 'Invalid batch id was submitted'
				break
			}

			// The kickoff step will set the the status to POSTING so there are two cases:
			// 1. The kickoff should have status = PENDING
			// 2. The job should have POSTING and we have a valid progressKey

			if (!progressKey && dtb.statusCode != DataTransferBatch.PENDING) {
				errorMsg = "The batch must be in the $DataTransferBatch.PENDING status in order to process"
				break
			}

			// Check the status of the batch and make sure it is still PENDING
			if (progressKey && dtb.statusCode == DataTransferBatch.POSTING && progressKey != dtb.progressKey) {
				errorMsg = 'It appears that someone is presently processing this batch'
				break
			}

			if (dtb.statusCode == 'COMPLETED') {
				errorMsg = 'The batch has already been completed'
				break
			}

			break
		}
		return errorMsg
	}

	/**
	 * This is a stub function that will invoke the various processAssetClassImport method and can be called from the controller or job
	 * @param projectId - the id of the project that the user that invoked the task
	 * @param userLoginId - the id of the UserLogin object for the user
	 * @param batchId - the id number of the DataTransferBatch to be processed
	 * @param progressKey - the key reference the progressService job to update users of the progress
	 * @param timeZoneId - the timezone of the current user
	 * @param dtFormat - the date time format of the current user
	 * @return map of the various attributes returned from the service
	 */
	@Transactional
	Map invokeAssetImportProcess(Long projectId, Long userLoginId, Long batchId, String progressKey, timeZoneId, dtFormat) {
		Map results = [:]
		String errorMsg
		String methodName = 'invokeAssetImportProcess()'

		while (true) {
			try {
				log.debug '{} Current flushMode={}', methodName, GormUtil.getSessionFlushMode()
				// Set the hibernate flush mode to be controlled by us for performance reasons
				GormUtil.setSessionFlushMode FlushMode.COMMIT

				errorMsg = validateImportBatchCanBeProcessed(projectId, userLoginId, batchId, progressKey)
				if (errorMsg) {
					break
				}

				DataTransferBatch.withTransaction { tx ->

					DataTransferBatch dtb = DataTransferBatch.get(batchId)

					// Update the batch status to POSTING
					dtb.statusCode = DataTransferBatch.POSTING
					if (!dtb.save(flush:true, failOnError:true)) {
						errorMsg = "Unable to update batch status : ${GormUtil.allErrorsString(dtb)}"
					}

					if (!errorMsg) {
						// Figure out which service method to invoke based on the DataTransferBatch entity type domain name
						// String domainName = AssetClass.domainNameFor(dbt.assetClass)
						String domainName = AssetClass.domainNameFor(dtb.assetClass)
						assert domainName
						String servicMethodName = 'process' + domainName + 'Import'

						results = this."$servicMethodName"(projectId, userLoginId, batchId, progressKey, timeZoneId, dtFormat)
						errorMsg = results.error
						dtb = dtb.merge()
						if (errorMsg) {
							dtb.statusCode = DataTransferBatch.PENDING
							dtb.importResults = combineDataTransferBatchImportResults(dtb, errorMsg)
						} else {
							dtb.statusCode = DataTransferBatch.COMPLETED
							dtb.importResults = combineDataTransferBatchImportResults(dtb, results.info)
							results.batchStatusCode = DataTransferBatch.COMPLETED
						}
						if (!dtb.validate() || !dtb.save(flush:true)) {
							errorMsg = "Unable to import assets: ${GormUtil.allErrorsString(dtb)}"
							log.error(errorMsg)
						}
					}

					// TODO : JPM 5/2016 : invokeAssetImportProcess() should probably only call the flushAndClearSession
					//                     every few hundred or even one thousand rows
					GormUtil.flushAndClearSession(1, 1)
				}
			} catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
				errorMsg = e.message
			} catch (RuntimeException e) {

				// For non-TM exceptions, we don't want to show everything to the user
				errorMsg = 'An error occurred while processing the import. Please contact support for assistance.'
				if (log.debugEnabled) {
					errorMsg = "$errorMsg $e.message"
				}
				log.error "deviceProcess() failed : $e.message : userLogin ($userLoginId) : batchId $batchId", e
			}
			break
		}

		if (errorMsg) {
			jobProgressFinish(progressKey, errorMsg)
			return [error:errorMsg, batchStatusCode: DataTransferBatch.PENDING]
		} else {
			jobProgressFinish(progressKey, results.info)
			return results
		}
	}

	/**
	 * Private method to combine the DataTransferBatch importResults information with additional Results from posting of assets.
	 * This will attempt to append the additional results but if it would exceed the maxSize constraint it will
	 * attempt to save the latest results or something less appropriately.
	 * @param dtb - the DataTransferBatch to be updated
	 * @param results - A Sting containing the information to save
	 * @return The string of the combined results (Process Results <hr> Import Results)
	 */
	private String combineDataTransferBatchImportResults(DataTransferBatch dtb, String results) {
		Long maxSizeImportResults = GormUtil.getConstraintMaxSize(DataTransferBatch, 'importResults')
		String current = dtb.importResults ?: ''
		String s = ''
		def currSize = current.size()
		def resultsSize = results.size()
		if ((resultsSize+currSize) <= maxSizeImportResults) {
			s = results + (currSize > 0 ? "\n<hr>\n" + current : '')
		} else if (resultsSize <= maxSizeImportResults) {
			s = results
		} else {
			String msg = "<p>The process results contents exceeded the size that can be saved to the database so it was not saved.</p>\n<hr>\n"
			def msgSize = msg.size()
			if ((msgSize + currSize) <= maxSizeImportResults) {

				s = msg + dtb.importResults
			} else {
				s = msg
			}
		}
		return s
	}

	/**
	 * This method will iterate over a list of properties that may not have been set during the import process and then
	 * assign default values appropriately.
	 */
	private void processRequiredProperties(project, assetObj, rowNum, warnings, errorConflictCount, tzId, dtFormat, List<Map<String, ?>> fieldSpecs) {
		List requiredProps = ['assetTag', 'moveBundle', 'planStatus', 'validation']
		requiredProps.each { prop ->
			if (!assetObj[prop]) {
				Map dtvMap = [
					importValue: '',
					eavAttribute: [attributeCode : prop ]
				]
				Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == prop || field["label"] == prop) }
				if (fieldSpec == null) {
					fieldSpec = ["label": prop, "field": prop, "control": "String"]
				}
				assetEntityAttributeLoaderService.setCommonProperties(project, assetObj, dtvMap, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpec)
			}
		}
	}


	/**
	 * This process will iterate over the assets imported into the specified batch and update the Application appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginID - the id of user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param progressKey - the key reference the progressService job to update users of the progress
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 * @param dtFormat - the date time format of the current user
	 * @return map of the various attributes returned from the service
	 */
	private Map processApplicationImport(Long projectId, Long userLoginId, Long batchId, String progressKey, tzId, dtFormat) {
		AssetClass assetClass = APPLICATION
		def domainClass = domainClassFor(assetClass)

		// Flag if we want performance information throughout the method
		boolean performance=true
		def startedAt = new Date()
		def importStartedAt = new Date()

		String methodName = 'processApplicationImport()'
		def newVal
		def warnings = []
		def ignoredAssets = []
		def personMap = []

		def insertCount = 0
		def personsAdded = 0
		def errorConflictCount = 0
		def updateCount = 0
		def errorCount = 0
		def batchRecords = 0
		def unknowAssetIds = 0
		def unknowAssets = ""
		def existingAssetsList = []
		def assetEntityErrorList = []
		def assetsList = []
		def application
		int assetCount

		String warnMsg

		//
		// Load initial data for method
		//
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userLoginId)

		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, assetClass, batchId)
		if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = null
		List staffList = data.staffList

		List teams = partyRelationshipService.getStaffingRoles()

		List dataTransferValueRowList = data.dataTransferValueRowList
		assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		if (log.debugEnabled) {
			def fubar = new StringBuilder("Staff List\n")
			staffList.each { fubar.append("   $it.id $it\n") }
			log.debug fubar.toString()
		}

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		List<Map<String, ?>> fieldSpecs = customDomainService.allFieldSpecs(project, assetClass.toString())[assetClass.toString()]["fields"]

		def dtvList

		//
		// Main loop that will iterate over the row ids from the import batch
		//
		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			startedAt = new Date()
			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			application = assetEntityAttributeLoaderService.findAndValidateAsset(
				project, userLogin,
				domainClass, assetEntityId, dataTransferBatch, dtvList,
				errorCount, errorConflictCount,
				ignoredAssets, rowNum, fieldSpecs)

			if (application == null) {
				continue
			}

			// Iterate over the properties and set them on the asset
			dtvList.each {
				String attribName = it.fieldName
				Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == attribName || field["label"] == attribName) }

				it.importValue = it.importValue.trim()

				// If trying to set to NULL - call the closure to update the property and move on
				if (it.importValue == "NULL") {
					// Set the property to NULL appropriately
					assetEntityAttributeLoaderService.setToNullOrBlank(application, attribName, it.importValue, nullProps, blankProps)
					if (newVal) {
						// Error messages are returned otherwise it updated
						warnings << "$newVal for row $rowNum, asset $assetEntity"
						errorConflictCount++
					}
					return
				}

				// Skip over any blank values
				if (it.importValue == '') {
					return
				}

				warnMsg = ''

				switch (attribName) {
					// Deal with the properties that support #propertyName, @team, and person.id as values
					case ~/shutdownBy|startupBy|testingBy|sme|sme2|appOwner/:
						String existingValue = application[attribName] ? application[attribName].toString() : ''
						if (existingValue && existingValue == it.importValue) {
							// If the value hasn't changed then we don't need to do anything
							// Note that names get converted from # to their names during export
							log.debug "processApplicationImport() $attribName name was unchanged ${it.importValue}"
							break
						}

						Map assignMap = assignWhomToAsset(application, attribName, it.importValue, staffList, teams, project)

						if (assignMap.createByName) {
							// assignWhomToAsset didn't find the person by name therefore a person should be created
							Map resultMap
							try {
								// TODO : JPM 1/2017 : refactor to just create the person since we already tried to
								// lookup the person in the assignWhomToAsset function.
								resultMap = personService.findOrCreatePerson(it.importValue, project, staffList)
							} catch (e) {
								warnMsg = "Failed create $attribName (${it.importValue}) on row $rowNum - ${e.getMessage()}"
							}

							if ( ! warnMsg && resultMap?.person) {
								application[attribName] = resultMap.person

								// Now check for warnings
								if (resultMap.isAmbiguous) {
									warnMsg = "Ambiguous name for $attribName (${it.importValue}) in application ${application.assetName} on row $rowNum. Name set to ${resultMap.person}"
								}

								if (resultMap.isNew)
									personsAdded++

							} else if ( resultMap?.error ) {
								warnMsg = "Person assignment to $attribName for App ${application.assetName} on row $rowNum failed. ${resultMap.error}"
							}
						} else if (assignMap.errMsg) {
							warnMsg = "${assignMap.errMsg} for property $attribName of App ${application.assetName} on row $rowNum"
						} else if (assignMap.whom) {
							// This is a hack to address the hardcoding of the By fields in App CRUD
							if (assignMap.whom instanceof String) {
								// TODO : JPM 1/2017 : remove the fixupHashtag TM-5894
								application[attribName] = AssetEntityHelper.fixupHashtag(assignMap.whom)
							}
						}

						break

					case ~/shutdownBy|startupBy|testingBy/:
						if (it.importValue.size()) {
							if (it.importValue[0] in ['@', '#']) {
								// TODO : JPM 5/2016 : TM-4889 processApplicationImport needs to validate By @ and # imports
								application[attribName] = it.importValue
							} else {

								String existingName = application[attribName] ? application[attribName].toString() : ''
								if (existingName && existingName == it.importValue) {
									// The name hasn't changed so we can skip this task
									log.debug "processApplicationImport() $attribName name was unchanged $it.importValue"
									break
								}

								def resultMap
								try {
									resultMap = personService.findOrCreatePerson(it.importValue, project, staffList)
								} catch (e) {
									warnMsg = "Failed to find or create $attribName ($it.importValue) on row $rowNum - $e.message"
								}

								if (!warnMsg && resultMap?.person) {
									application[attribName] = resultMap.person.id

									// Now check for warnings
									if (resultMap.isAmbiguous) {
										warnMsg = "Ambiguous name for $attribName ($it.importValue) in application $application.assetName on row $rowNum. Name set to $resultMap.person"
									}

									if (resultMap.isNew)
										personsAdded++

								} else if (resultMap?.error) {
									warnMsg = "Person assignment to $attribName for App $application.assetName on row $rowNum failed. $resultMap.error"
								}

							}
						}
						break

					case ~/shutdownFixed|startupFixed|testingFixed/:
						if (it.importValue) {
							application[attribName] = it.importValue.equalsIgnoreCase("yes") ? 1 : 0
						}
						break

					default:
						// Try processing all common properties
						assetEntityAttributeLoaderService.setCommonProperties(project, application, it, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpec)

				} // switch(attribName)

				if (warnMsg) {
					warnings << warnMsg
					log.warn warnMsg
					errorConflictCount++
				}

			}	// dtvList.each

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, application, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpecs)

			// Save the asset if it was changed or is new
			(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
				application, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

			// Update status and clear hibernate session
			jobProgressUpdate(progressKey, rowNum, assetCount)

			project = flushAndClearSession(rowNum, project)

			log.info "$methodName processed row $rowNum in ${TimeUtil.elapsed(startedAt)}"

		} // for

		def assetIdErrorMess = unknowAssets ? '(' + unknowAssets.substring(0, unknowAssets.length() - 1) + ')' : unknowAssets

		def sb = new StringBuilder(
			"<h3>Process Results for Batch $batchId:</h3><ul>" +
			"<li>Assets in Batch: $assetCount</li>" +
			"<li>Records Inserted: $insertCount</li>"+
			"<li>Records Updated: $updateCount</li>" +
			"<li>Asset Errors: $errorCount</li> "+
			"<li>Persons Added: $personsAdded</li>" +
			"<li>Attribute Errors: $errorConflictCount</li>" +
			"<li>AssetId Errors: $unknowAssetIds$assetIdErrorMess</li></ul> "
		)

		if (warnings || ignoredAssets) {
			sb.append("<b>Warning:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(importStartedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"

		sb.append("<br>Process batch took $elapsedTime to complete")

		return [elapsedTime: elapsedTime, assetCount: assetCount, info: sb.toString()]
	}

	/**
	 * This process will iterate over the assets imported into the specified batch and update the devices appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginId - the id of the user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param progressKey - the key reference the progressService job to update users of the progress
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 * @param dtFormat - the date time format of the current user
	 * @return map of the various attributes returned from the service
	 */
	private Map processAssetEntityImport(Long projectId, Long userLoginId, Long batchId, String progressKey, tzId, dtFormat) {
		String methodName='processAssetEntityImport()'
		AssetClass assetClass = DEVICE
		def domainClass = domainClassFor(assetClass)

		GormUtil.setSessionFlushMode FlushMode.COMMIT

		boolean performance=true
		def startedAt = new Date()
		def importStartedAt = new Date()

		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userLoginId)

		def newVal
		List warnings = []
		List missingMfgModel = []
		List ignoredAssets = []
		def insertCount = 0
		def errorConflictCount = 0
		def updateCount = 0
		def errorCount = 0
		def unknowAssetIds = 0
		def unknowAssets = ""
		def assetsList = []
		def assetEntityErrorList = []
		def existingAssetsList = []

		// Get Room and Rack counts for stats at the end
		def counts = [:]
		Map mfgModelMap = [:]

		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, assetClass, batchId)
		if (performance) {
			log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"
		}

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) {
			log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"
		}

		def eavAttributeSet = null
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		List<Map<String, ?>> fieldSpecs = customDomainService.allFieldSpecs(project, assetClass.toString())[assetClass.toString()]["fields"]

		// Get a Device Type Map used to verify that device type are valid
		Map deviceTypeMap = getDeviceTypeMap()

		counts.room = Room.countByProject(project)
		counts.rack = Rack.countByProject(project)

		//
		// Iterate over the rows
		//
		def dtvList
		def rowNum
		AssetEntity asset

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			try {
				now = new Date()
				startedAt = new Date()

				def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
				rowNum = rowId+1
				log.debug "**** ROW $rowNum"

				// Get all of the property values imported for a given row
				dtvList?.clear()
				dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

				Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
				asset = assetEntityAttributeLoaderService.findAndValidateAsset(
					project, userLogin, domainClass, assetEntityId, dataTransferBatch, dtvList,
					errorCount, errorConflictCount, ignoredAssets, rowNum, fieldSpecs)
				if (!asset) {
					continue
				}

				if (asset.id) {
					existingAssetsList << asset
				}

				def isNewValidate = (!asset.id)

				// This will hold any of the source/target location, room and rack information
				def locRoomRack = [
						  source: [:],
						  target: [:]
				]

				/*
				 * BEGIN: Initialize the SOURCE and TARGET location used to preserve missing values
				 * we will override those that changed in the next block
				 */
				if ( asset.roomSource ) {
					locRoomRack.source.sourceLocation = asset.roomSource.location
					locRoomRack.source.sourceRoom = asset.roomSource.roomName
				}

				if ( asset.rackSource ) {
					locRoomRack.source.sourceRack = asset.rackSource.tag
				}

				if ( asset.roomTarget ) {
					locRoomRack.target.targetLocation = asset.roomTarget.location
					locRoomRack.target.targetRoom = asset.roomTarget.roomName
				}

				if ( asset.rackTarget ) {
					locRoomRack.target.targetRack = asset.rackTarget.tag
				}

				/*
				 * END: Initialize SOURCE and TARGET location
				 */

				// Vars caught in the each loop below to be used to create mfg/model appropriately
				String mfgName, modelName, usize, deviceType

				// Vars caught in the each loop for setting the chassis and position
				String sourceChassis, targetChassis, sourceBladePosition, targetBladePosition

				// Iterate over the attributes to update the asset with
				dtvList.each {
					String attribName = it.fieldName
					Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == attribName || field["label"] == attribName) }

					// If trying to set to NULL - call the closure to update the property and move on
					if (it.importValue == "NULL") {
						// Set the property to NULL appropriately
						newVal = assetEntityAttributeLoaderService.setToNullOrBlank(asset, attribName, it.importValue, nullProps, blankProps)
						if (newVal) {
							// Error messages are returned otherwise it updated
							warnings << "$newVal for row $rowNum, asset $asset"
							errorConflictCount++
						}
						return
					}

					switch (attribName) {
						case ~/sourceTeamMt|targetTeamMt|sourceTeamLog|targetTeamLog|sourceTeamSa|targetTeamSa|sourceTeamDba|targetTeamDba/:
							// Legacy columns that are no longer used - see TM-3128
							break
						case 'manufacturer':
							mfgName = it.correctedValue ?: it.importValue
							break
						case 'model':
							modelName = it.correctedValue ?: it.importValue
							break
						case "assetType":
							deviceType = it.correctedValue ?: it.importValue
							break
						case "usize":
							usize = it.correctedValue ?: it.importValue
							break
						case "sourceChassis":
							sourceChassis = it.correctedValue ?: it.importValue
							// appendBladeToChassis(project, asset, it.importValue, true, warnings, rowNum)
							break
						case "targetChassis":
							targetChassis = it.correctedValue ?: it.importValue
							// appendBladeToChassis(project, asset, it.importValue, false, warnings, rowNum)
							break
						case 'sourceBladePosition':
							sourceBladePosition = it.correctedValue ?: it.importValue
							break
						case 'targetBladePosition':
							targetBladePosition = it.correctedValue ?: it.importValue
							break

						// case ~/^(location|room|rack)(Source|Target)\.(.+)/:
						case ~/(location|room|rack)(Source|Target)/:
							// def field = Matcher.lastMatcher[0][1]
							def disposition = Matcher.lastMatcher[0][2]
							disposition = disposition.toLowerCase()
							log.debug "*** disposition=$disposition"
							// def child = Matcher.lastMatcher[0][3]

							def val = (it.correctedValue ?: it.importValue)?.trim()
							if (val?.size()) {
								// Store the properties into the map to be used later
								locRoomRack[disposition][attribName] = val
							}
							break

						default:
							// Try processing all common properties
							assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpec)
					}
				}

				//
				// Process the Mfg / Model / Device Type assignment by utilizing a cache of the various mfg/model names
				//
				mfgName = StringUtil.defaultIfEmpty(mfgName, '')
				modelName = StringUtil.defaultIfEmpty(modelName, '')
				deviceType = StringUtil.defaultIfEmpty(deviceType, '')
				String mmKey = "$mfgName::$modelName::$deviceType::$usize::${isNewValidate ? 'new' : 'existing'}"
				def mfg, model

				Map mmm
				if (mfgModelMap.containsKey(mmKey)) {
					log.debug "$methodName Found cached mfgModelMap for key $mmKey, hasErrors=${mfgModelMap[mmKey].errorMsg?.size()}"
					// We've already processed this mfg/model/type/usize/new|existing combination before so work from the cache
					mfgModelMap[mmKey].refCount++
					mmm = mfgModelMap[mmKey]
				} else {
					log.debug "$methodName Did not find asset in cache so calling assetEntityAttributeLoaderService.assignMfgAndModelToDevice()"
					// We got a new combination so we have to do the more expensive lookup and possibly create mfg and model if user has perms
					Map results = assetEntityAttributeLoaderService.assignMfgAndModelToDevice(userLogin, asset, mfgName,
						modelName, deviceType, deviceTypeMap, usize, securityService.hasPermission(userLogin, Permission.ModelCreateFromImport))
					log.debug "$methodName call to assetEntityAttributeLoaderService.assignMfgAndModelToDevice() resulted in: $results"
					mmm = [
						errorMsg: results.errorMsg,
						warningMsg: results.warningMsg,
						mfgId: asset.manufacturer?.id,
						modelId: asset.model?.id,
						deviceType: asset.assetType,
						refCount: 1
					]
					if (results.cachable)
						mfgModelMap[mmKey] = mmm
				}

				// Now check the values against the Mfg/Model Map
				if (mmm.warningMsg?.size()) {
					warnings << "WARNING: $asset.assetName (row $rowNum) - $mmm.warningMsg"
				}
				if (mmm.errorMsg?.size()) {
					warnings << "ERROR: $asset.assetName (row $rowNum) - $mmm.errorMsg"
					errorCount++
					continue
				} else {
					if (asset.manufacturer?.id != mmm.mfgId)
						asset.manufacturer = Manufacturer.get(mmm.mfgId)
					if (asset.model?.id != mmm.modelId)
						asset.model = Model.get(mmm.modelId)
					asset.assetType = mmm.deviceType

					log.debug "$methodName Just set MfgModelType isDirty=${asset.isDirty()} asset $asset $asset.model $asset.manufacturer $asset.assetType"
				}

				// Update various common properties that may not have been set by the above loop
				processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpecs)

				//
				// Deal with Chassis
				//
				if (asset.isaBlade()) {
					if (!StringUtil.isBlank(sourceChassis)) {
						appendBladeToChassis(project, asset, sourceChassis, sourceBladePosition, true, warnings, rowNum)
					}
					if (!StringUtil.isBlank(targetChassis)) {
						appendBladeToChassis(project, asset, targetChassis, targetBladePosition, false, warnings, rowNum)
					}
				}

				//
				// Assign the Source/Target Location/Room/Rack properties for the asset
				//
				def errors
				['source', 'target'].each { disposition ->
					def d = locRoomRack[disposition]
					if (d?.size()) {
						// Check to see if they are trying to clear the fields with the NULL setting
						if (d.values().contains(NULL_INDICATOR)) {
							warnings << "NULL not supported for unsetting Loc/Room/Rack in $disposition (row $rowNum)"
							return
						}

						// Need to capitalize the disposition to form the property names correctly
						//disposition = disposition.capitalize()

						//Check that the chassis room is the same that the room defined in the spreadsheet
						def validChassisRoom = true
						if (asset.isaBlade()) {
							def chassis = asset[disposition + 'Chassis']
							if (chassis) {
								def roomProp = disposition == 'source' ? 'roomSource' : 'roomTarget'
								if (chassis[roomProp]) {
									def room = chassis[roomProp]
									if (!room.roomName.equals(d[disposition + 'Room'])) {
										validChassisRoom = false
										warnings << "Chassis room and device room don't match (row $rowNum)"
									}
								}
							}
						}

						if (validChassisRoom) {
							// Need to capitalize the disposition to form the property names correctly
							disposition = disposition.capitalize()
							errors = deviceService.assignDeviceToLocationRoomRack(
								asset,
								d['location' + disposition],
								d['room' + disposition],
								d['rack' + disposition],
								(disposition == 'Source'))
						}
						if (errors) {
							warnings << "Unable to set $disposition Loc/Room/Rack (row $rowNum) : $errors"
						}
					}


				} // ['source', 'target'].each

				log.debug "$methodName About to try saving isDirty=${asset.isDirty()} asset $asset $asset.model $asset.manufacturer $asset.assetType"
				// Save the asset if it was changed or is new
				(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
					asset, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

				if (performance) log.debug "$methodName Updated/Adding DEVICE() took ${TimeUtil.elapsed(now)}"

				jobProgressUpdate(progressKey, rowNum-1, assetCount)

				project = flushAndClearSession(rowNum, project)

				log.info "$methodName processed row $rowNum in ${TimeUtil.elapsed(startedAt)}"

			} catch (Exception e) {
				log.error("Can't process import row: $e.message", e)
				if (TransactionAspectSupport.currentTransactionStatus().isRollbackOnly()) {
					throw e
				} else {
					warnings << "$e.message (row $rowNum)"
					asset.discard()
				}
			}
		} // for

		// Update assets racks, cabling data once process done
		// TODO : JPM 9/2014 : updateCablingOfAssets was commented out until we figure out what to do with this function (see TM-3308)
		// assetEntityService.updateCablingOfAssets(modelAssetsList)

		// Update Room and Rack counts for stats
		counts.room = Room.countByProject(project) - counts.room
		counts.rack = Rack.countByProject(project) - counts.rack

		def assetIdErrorMess = unknowAssets ? '(' + unknowAssets.substring(0, unknowAssets.length() - 1) + ')' : unknowAssets

		def sb = new StringBuilder(
			"<h3>Process Results for Batch $batchId:</h3><ul>" +
			"<li>Assets in Batch: $data.assetsInBatch</li>" +
			"<li>Records Inserted: $insertCount</li>"+
			"<li>Records Updated: $updateCount</li>" +
			"<li>Rooms Created: $counts.room</li>" +
			"<li>Racks Created: $counts.rack</li>" +
			"<li>Asset Errors: $errorCount </li> "+
			"<li>Attribute Errors: $errorConflictCount</li>" +
			"<li>AssetId Errors: $unknowAssetIds$assetIdErrorMess</li>" +
			"</ul>"
		)

		if (warnings || missingMfgModel || ignoredAssets) {
			sb.append("<b>Warnings:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (missingMfgModel)
				appendIssueList(sb, 'Unable to assign Mfg/Model to device', missingMfgModel)

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(importStartedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"
		sb.append("<br>Elapsed time to process batch: $elapsedTime")

		return [elapsedTime: elapsedTime.toString(), assetCount: assetCount, info: sb.toString()]
	}

	/**
	 * This process will iterate over the assets imported into the specified batch and update the Database appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginId - the id of the user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param progressKey - the key reference the progressService job to update users of the progress
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 * @param dtFormat - the date time format of the current user
	 * @return map of the various attributes returned from the service
	 */
	private Map processDatabaseImport(Long projectId, Long userLoginId, Long batchId, String progressKey, tzId, dtFormat) {
		String methodName='processDatabaseImport()'
		AssetClass assetClass = DATABASE
		def domainClass = domainClassFor(assetClass)

		def startedAt = new Date()
		def importStartedAt = new Date()

		boolean performance=true
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userLoginId)

		def newVal
		def assetEntityErrorList = []
		def assetsList = []
		def warnings = []
		def ignoredAssets = []

		def insertCount = 0
		def errorConflictCount = 0
		def updateCount = 0
		def errorCount = 0
		def batchRecords = 0
		def unknowAssetIds = 0
		def unknowAssets = ""
		def modelAssetsList = []
		def existingAssetsList = []

		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, assetClass, batchId)
		if (performance) {
			log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"
		}

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) {
			log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"
		}

		def eavAttributeSet = null
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		List<Map<String, ?>> fieldSpecs = customDomainService.allFieldSpecs(project, assetClass.toString())[assetClass.toString()]["fields"]

		def dtvList

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			startedAt = new Date()

			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch,rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			def asset = assetEntityAttributeLoaderService.findAndValidateAsset(
				project, userLogin,
				domainClass, assetEntityId,
				dataTransferBatch, dtvList,
				errorCount, errorConflictCount,
				ignoredAssets, rowNum, fieldSpecs)

			if (!asset) {
				continue
			}

			dtvList.each {
				String attribName = it.fieldName
				Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == attribName || field["label"] == attribName) }

				// If trying to set to NULL - call the closure to update the property and move on
				if (it.importValue == "NULL") {
					assetEntityAttributeLoaderService.setToNullOrBlank(asset, attribName, it.importValue, nullProps, blankProps)
					return
				}

				// Try processing all common properties
				assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpec)
			}

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpecs)

			// Save the asset if it was changed or is new
			(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
				asset, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

			jobProgressUpdate(progressKey, rowNum, assetCount)

			project = flushAndClearSession(rowNum, project)

			log.info "$methodName processed row $rowNum in ${TimeUtil.elapsed(startedAt)}"

		} // for

		def assetIdErrorMess = unknowAssets ? '(' + unknowAssets.substring(0, unknowAssets.length() - 1) + ')' : unknowAssets

		def sb = new StringBuilder("<h3>Process Results for Batch $batchId:</h3><ul>" +
			"<li>Assets in Batch: $assetCount</li>" +
			"<li>Records Inserted: $insertCount</li>" +
			"<li>Records Updated: $updateCount</li>" +
			"<li>Asset Errors: $errorCount</li>"+
			"<li>Attribute Errors: $errorConflictCount</li>" +
			"<li>AssetId Errors: $unknowAssetIds$assetIdErrorMess</li>"
		)

		if (warnings || ignoredAssets) {
			sb.append("<b>Warnings:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(importStartedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"
		sb.append("<br>Elapsed time to process batch: $elapsedTime")

		return [elapsedTime: elapsedTime.toString(), assetCount: assetCount, info: sb.toString()]
	}

	/**
	 * This process will iterate over the assets imported into the specified batch and update the Logical Storage (aka Files) appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginId - the id of the user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param progressKey - the key reference the progressService job to update users of the progress
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 * @param dtFormat - the date time format of the current user
	 * @return map of the various attributes returned from the service
	 */
	private Map processFilesImport(Long projectId, Long userLoginId, Long batchId, String progressKey, tzId, dtFormat) {
		String methodName='processAssetEntityImport()'
		AssetClass assetClass = STORAGE
		def domainClass = domainClassFor(assetClass)

		boolean performance=true
		def startedAt = new Date()
		def importStartedAt = new Date()

		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userLoginId)

		def assetEntityErrorList = []
		def assetsList = []
		def newVal
		def warnings = []
		def ignoredAssets = []

		// def dataTransferBatch
		def insertCount = 0
		def errorConflictCount = 0
		def updateCount = 0
		def errorCount = 0
		def batchRecords = 0
		def unknowAssetIds = 0
		def unknowAssets = ""
		def existingAssetsList = []

		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, assetClass, batchId)
		if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = null
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		List nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		List blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		List<Map<String, ?>> fieldSpecs = customDomainService.allFieldSpecs(project, assetClass.toString())[assetClass.toString()]["fields"]

		List dtvList

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			startedAt = new Date()

			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			def asset = assetEntityAttributeLoaderService.findAndValidateAsset(
				project, userLogin, domainClass,
				assetEntityId, dataTransferBatch, dtvList,
				errorCount, errorConflictCount,
				ignoredAssets, rowNum, fieldSpecs)

			if (asset == null) {
				continue
			}

			dtvList.each {
				String attribName = it.fieldName
				Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == attribName || field["label"] == attribName) }

				println "*** attribName=$attribName, ${it.getClass().getName()}"

				// If trying to set to NULL - call the closure to update the property and move on
				if (it.importValue == "NULL") {
					// Set the property to NULL appropriately
					assetEntityAttributeLoaderService.setToNullOrBlank(asset, attribName, it.importValue, nullProps, blankProps)
					if (newVal) {
						// Error messages are returned otherwise it updated
						warnings << "$newVal for row $rowNum, asset $assetEntity"
						errorConflictCount++
					}
					return
				}

				// Try processing all common properties
				assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpec)
			}

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat, fieldSpecs)

			// Save the asset if it was changed or is new
			(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
				asset, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

			// Update status and clear hibernate session
			jobProgressUpdate(progressKey, rowNum, assetCount)

			project = flushAndClearSession(rowNum, project)

			log.info "$methodName processed row $rowNum in ${TimeUtil.elapsed(startedAt)}"

		} // for

		def assetIdErrorMess = unknowAssets ? '(' + unknowAssets.substring(0, unknowAssets.length() - 1) + ')' : unknowAssets

		def sb = new StringBuilder("<b>Process results for Batch $batchId:</b><ul>" +
			"<li>Assets in Batch: $assetCount</li>" +
			"<li>Records Inserted: $insertCount</li>"+
			"<li>Records Updated: $updateCount</li>" +
			"<li>Asset Errors: $errorCount </li> "+
			"<li>Attribute Errors: $errorConflictCount</li>" +
			"<li>AssetId Errors: $unknowAssetIds$assetIdErrorMess</li>" +
			"</ul>"
		)

		if (warnings || ignoredAssets) {
			sb.append("<b>Warnings:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(importStartedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"
		sb.append("<br>Elapsed time to process batch: $elapsedTime")

		return [elapsedTime: elapsedTime.toString(), assetCount: assetCount, info: sb.toString()]
	}

	/**
	 * Used to append a list of ignored assets if any to a StringBuilder buffer
	 * @param StringBuilder the message buffer
	 * @param title - the title of the UL
	 * @param List<AssetEntity> list of ignored assets
	 */
	private void appendIgnoredAssets(StringBuilder sb, List assets) {
		int count = assets.size()
		if (count) {
			String title = "$count asset${count != 1 ? 's where' : ' was'} skipped due to having been updated since the export was done:"
			sb.append("<li>$title<ul>")
			assets.each { sb.append("<li>$it</li>") }
			sb.append('</ul></li>')
		}
		sb.append('</ul></li>')
	}

	/**
	 * Used to append a list of ignored assets if any to a StringBuilder buffer
	 * @param StringBuilder the message buffer
	 * @param title - the title of the UL
	 * @param List<AssetEntity> list of ignored assets
	 */
	private void appendIssueList(StringBuilder sb, String title, List issues) {
		if (issues.size()) {
			sb.append("<li>$title<ul>")
			issues.each { sb.append("<li>$it</li>") }
			sb.append('</ul></li>')
		}
		sb.append('</ul></li>')
	}

	@Transactional(noRollbackFor=[InvalidRequestException, EmptyResultException])
	void appendBladeToChassis(Project project, AssetEntity assetEntity, String chassisIdName, String bladePosition, boolean isSource, List warnings, int rowNum) {
		log.debug "appendBladeToChassis() chassisIdName=$chassisIdName, bladePosition=$bladePosition, isSource=$isSource"
		if (!StringUtils.isBlank(chassisIdName) && assetEntity.isaBlade()) {
			def chassisType = isSource?'source':'target'
			if (chassisIdName.startsWith("id:")) {
				// Parse out the  chassis id: Name
				String chassisKey = null
				String chassisName = null
				// Check if there is a chassis name in the cell too
				if (chassisIdName.indexOf(' ') > 0) {
					chassisKey = chassisIdName.substring(0, chassisIdName.indexOf(' '))
					chassisName = chassisIdName.substring(chassisIdName.indexOf(' ') + 1, chassisIdName.size())
				} else {
					chassisKey = chassisIdName
				}
				def chassisId = chassisKey.substring(3, chassisKey.size())
				Long id = NumberUtil.toLong(chassisId)
				if (id != null && id > 0) {
					def chassis = AssetEntity.get(id)
					if (chassis) {
						if (chassis.project.id != project.id) {
							securityService.reportViolation("in appendBladeToChassis - tried to access asset ($id) not associated with project ($project.id)")
							warnings << "ERROR: No $chassisType chassis with id $chassisId found (row $rowNum)"
						} else {
							// Validates that the chassis name match with the chassis found
							if (chassisName != null && (!chassisName.equals(chassis.assetName))) {
								warnings << "WARNING: Chassis ($chassisIdName) for $chassisType does not match referenced name ($chassis.assetName) (row $rowNum)"
							}
							String bladeWarnings = assetEntityService.assignBladeToChassis(project, assetEntity, chassis.id.toString(), isSource, bladePosition)
							if (bladeWarnings)
								warnings << "WARNING: Blade $chassisName $bladeWarnings (row $rowNum)"
						}
					} else {
						warnings << "ERROR: No $chassisType chassis with id $chassisId found (row $rowNum)"
					}
				} else {
					warnings << "ERROR: Invalid $chassisType chassis id ($chassisId) specified (row $rowNum)"
				}
			} else {
				// Proccess chassis name
				def chassis = AssetEntity.findAllByAssetNameAndProject(chassisIdName, project)
				if (chassis.size() > 0) {
					// Check if we found more than one chassis
					if (chassis.size() > 1) {
						warnings << "ERROR: A non-unique blade chassis name ($chassisIdName) for $chassisType was referenced (row $rowNum)"
					} else  {
						def sChassis = chassis[0]
						String bladeWarnings = assetEntityService.assignBladeToChassis(project, assetEntity, sChassis.id.toString(), isSource, bladePosition)
						if (bladeWarnings)
							warnings << "WARNING: Blade $chassisIdName $bladeWarnings (row $rowNum)"
					}
				} else {
					warnings << "ERROR: No $chassisType chassis found with name ($chassisIdName) (row $rowNum)"
				}
			}
		}
	}

	/**
	 * Clear out the Hibernate session of objects no longer needed to help performance. It will also merge the existing
	 * @param rowsProcessed - number of row processed
	 * @param project - the project object
	 * @return current project
	 */
	Project flushAndClearSession(int rowsProcessed, Project project) {
		if (GormUtil.flushAndClearSession(rowsProcessed, HIBERNATE_BATCH_SIZE)) {
			project = GormUtil.mergeWithSession(project)
		}
		return project
	}

	/**
	 * This method attempts to assign a person, team, or indirect person/team reference to a
	 * an asset property. In this case the property is a String field and will be populated with
	 * one of the three following values:
	 *
	 *     12345         - reference to a person id
	 *     @TEAM_CODE    - refers to a Team Code
	 *     #propertyName - refers to another asset property that might contain a person or team
	 *                     reference (e.g. sme, sme2)
	 *
	 * The input value can be on of the above as well person references can be done by email address
	 * or their name. In those two cases, the person is looked up accordingly and their ID number is
	 * assigned to the asset property.
	 *
	 * Input values support:
	 *	  Person's Id 		  "123456"
	 *	  Person's Email 	  "example@mail.com" -> replaced with Person.id
	 *	  Person's Name 	  "John Doe" -> replaced with Person.id
	 *	  Team Code 		  "@APP_COORD"
	 *	  Property Reference  "#sme2"
	 *
	 * Note: when the value contains the person's name, there is a chance that when attempting to
	 * lookup the person, that what is supplied matches more than one person. In that case, the
	 * isAmbiguous property will be set to true to indicate that the name was ambiguous.
	 *
	 * @param asset 		- asset being updated
	 * @param property 		- which asset's property is to be given a value.
	 * @param value 		- value to be assigned.
	 * @param projectStaff 	- List of staff assigned to the project.
	 * @param staffingRoles - List of team codes.
	 * @param project
	 *
	 * @return Map =>
	 *		whom - the appropriate whom value that is assigned to the property if a valid match occurred
	 *		errMsg - error message if a problem arrose while performing lookups
	 *		createByName - a flag indicating that the person should be created by name
	 *		isAmbiguous - a flag indicating if multiple people matched the lookup
	 */
	Map assignWhomToAsset(AssetEntity asset, String property, String value, List projectStaff, List staffingRoles, Project project){
		Map result =  [whom:null, isAmbiguous:false, createByName:false, errMsg:'']

		if (value) {
			switch(value){
				// Team reference @TEAM_CODE
				case ~/@.*/:
					assignWhomHelperByTeamCode(value, staffingRoles, result)
					break

				// Indirect property reference using hashtag (#property)
				case ~/#.*/:
					assignWhomHelperByHashtag(value, asset, result)
					break

				// By Person's Id
				case ~/\d+/:
					assignWhomHelperByPersonId(value, projectStaff, result)
					break

				// Person's email address.
				case ~/.*@.*/:
					assignWhomHelperByEmail(value, projectStaff, result)
					break

					// Person's name (2 or more chars)
				case ~/^[a-zA-Z\s]{2,}.*/:
					assignWhomHelperByName(value, project, projectStaff, result)
					break

				default:
					result.errMsg = "Invalid Staff reference ($value)"
			}

			if (result.whom && ! result.errMsg) {
				// Got a good match so we set the property value
				boolean isPersonProperty = ['sme', 'sme2', 'appOwner'].contains(property)
				if ( asset[property] != result.whom ) {
					if (isPersonProperty) {
						if (result.whom instanceof Person) {
							asset[property] = result.whom
						} else {
							result.errMsg = "Property only supports person references"
						}
					} else {
						if (result.whom instanceof Person) {
							asset[property] = result.whom.id?.toString()
						} else {
							asset[property] = result.whom
						}

					}
				}
			}
		}

		return result
	}

	/**
	 * Used by assignWhomToAsset to validate that the a person by name is valid
	 * @param name - the person's name to find
	 * @param project - the project to find the staff member of
	 * @param projectStaff - the list of the staff assigned to the project
	 * @param result - the Map that is returned by the method that is updated by this method
	 */
	private void assignWhomHelperByName(String name, Project project, List projectStaff, Map result) {
		PersonService personService = grailsApplication.mainContext.personService

		// Search across the project staff list for person by name
		Map map = personService.findPerson(name, project, projectStaff, false)

		if (map.person) {
			if (map.isAmbiguous) {
				result.isAmbiguous = true
				result.errMsg = "Staff by name ($name) found multiple people"
			} else {
				result.whom = map.person
			}
		} else {
			result.createByName = true
			result.errMsg = "Person name ($name) was not found"
		}
	}

	/**
	 * Used by assignWhomToAsset to validate that a team code @TEAM is valid
	 * @param name - the team code to find
	 * @param projectStaff - the list of the staff assigned to the project
	 * @param result - the Map that is returned by the method that is updated by this method
	 */
	private void assignWhomHelperByTeamCode(String name, List staffingRoles, Map result) {
		String teamName = name[1..-1]
		def team = staffingRoles.find { it.id == teamName || it.description == teamName }
		if (team) {
			log.debug "** found team $team"
			result.whom = "@" + team.id
		} else {
			result.errMsg = "Unknown team ($teamName)"
		}
	}

	/**
	 * Used by assignWhomToAsset to validate that a hashtag reference (#propertyName) exists
	 * @param hashtag - the hashtag to validate
	 * @param asset - the domain asset class to compare property name to
	 * @param result - the Map that is returned by the method that is updated by this method
	 */
	private void assignWhomHelperByHashtag(String hashtag, AssetEntity asset, Map result) {
		if (AssetEntityHelper.getPropertyNameByHashReference(asset, hashtag)) {
			result.whom = hashtag
		} else {
			result.errMsg = "Invalid property reference (${hashtag})"
		}
	}

	/**
	 * Used by assignWhomToAsset to validate a reference by person id is part of the project staffing
	 * @param id - the persons' id
	 * @param projectStaff - the list of the staff assigned to the project
	 * @param result - the Map that is returned by the method that is updated by this method
	 */
	private void assignWhomHelperByPersonId(String id, List projectStaff, Map result) {
		Long personId = NumberUtil.toPositiveLong(id, 0)
		if ( personId ) {
			result.whom = projectStaff.find { it.id == personId }
			if (!result.whom) {
				result.errMsg = "Staff by id ($id) not found"
			}
		} else {
			result.errMsg = "Staff reference ($id) is invalid"
		}
	}

	/**
	 * Used by assignWhomToAsset to validate a reference by person email is part of the project staffing
	 * @param personId - the persons' email address
	 * @param projectStaff - the list of the staff assigned to the project
	 * @param result - the Map that is returned by the method that is updated by this method
	 */
	private void assignWhomHelperByEmail(String email, List projectStaff, Map result) {
		def person = projectStaff.find { it.email?.toLowerCase() == email.toLowerCase() }
		if ( person ) {
			result.whom = person
		} else {
			result.errMsg = "Staff referenced by email ($email) not found"
		}
	}

	/**
	 * Validates an uploaded asset import workbook and create the corresponding batch
	 * for reviewing and processing the data
	 * @param project - the project
	 * @param file - the workbook
	 * @param params - http parameters received in the controller
	 * @return
	 */
	@Transactional
	def validateAndProcessWorkbookSheets(Project project, CommonsMultipartFile file, Map<String, String> params) {
		def stopwatch = new StopWatch()
		stopwatch.start()

		// ------
		// Some variables that are referenced by the following closures
		// ------
		boolean flagToManageBatches = false

		// List of all of the error/warning messages tracked during the import
		List errorMsgList = []

		// List of all of the rows that were skipped
		List skipped = []

		// The list of sheets that use the common import process (or at least for reporting Dependencies, Cabling, Comments)
		List sheetList = ['Devices', 'Applications', 'Databases', 'Storage', 'Dependencies', 'Cabling', 'Comments']
		// This will retain the results from the various spreadsheet tabs that use the common import process
		Map uploadResults = [:]
		// Initialize the results
		sheetList.each { uploadResults[it] = [addedCount: 0, skippedCount: 0, processed: false, summary: ''] }

		DataTransferBatch dataTransferBatch

		// A closure to track the results of the different sheets being processed
		def processResults = { String theSheetName, theResults ->
			if (!uploadResults.containsKey(theSheetName)) {
				throw new InvalidParamException("Unhandled Sheet \'${theSheetName}\' - please contact support")
			}

			// Save the transfer batch so it can be used by the saveProcessResultsToBatch closure below
			dataTransferBatch = theResults.dataTransferBatch

			uploadResults[theSheetName].with {
				addedCount = theResults.added
				skippedCount = theResults.skipped?.size() ?: 0
				processed = true
				summary = theResults.summary
				errorList = theResults.errors ?: []
				erroredCount = (errorList?.size() ?: 0)
			}
			uploadResults.skipped = theResults.skipped

			if (theResults.skipped) {
				skipped.addAll(theResults.skipped)
			}

			if (theResults.errors) {
				errorMsgList.addAll(theResults.errors)
			}

			// Set flag so user is later prompted to process the batch(es)
			if (theResults.added > 0) {
				flagToManageBatches = true
			}
		}

		/**
		 * A closure for saving the results back into the task batch
		 * Note that the taskBatch which is created in a function get tucked into the results so that we can
		 * use it here to save the results. While ugly, this was a quick way of getting it to work.
		 * @param theSheetName - the name of the sheet
		 * @param theResults - contains the results from the processResults function
		 */
		def saveProcessResultsToBatch = { theSheetName, theResults ->
			// Generate the results and save into the batch for historical reference
			StringBuilder sprtbMsg = generateResults(theResults, theResults[theSheetName].skipped, [theSheetName], false)
			if (dataTransferBatch != null) {
				dataTransferBatch.importResults = sprtbMsg.toString()
				dataTransferBatch.save()
			} else {
				throw new Exception(sprtbMsg.toString())
			}
		}

		setBatchId 0
		setTotalAssets 0

		if (!params.dataTransferSet) {
			throw new InvalidParamException("Import request was missing expected parameter(s)")
		}

		DataTransferSet dataTransferSet = DataTransferSet.get(params.dataTransferSet)
		if (!dataTransferSet) {
			throw new InvalidParamException("Unable to locate Data Import definition for ${params.dataTransferSet}")
		}

		// create workbook
		def workbook
		def titleSheet
		def sheetNameMap = ['Title', 'Applications', 'Devices', 'Databases', 'Storage', 'Dependencies', 'Cabling']
		Map appNameMap = [:]
		Map databaseNameMap = [:]
		Map filesNameMap = [:]
		Date exportTime
		def dataTransferAttributeMapSheetName
		int devicesAdded  = 0
		int appAdded   = 0
		int dbAdded  = 0
		int filesAdded = 0

		int dependencyCount = 0
		int cablingCount = 0

		try {
			workbook = WorkbookFactory.create(file.inputStream)
			def sheetNames = WorkbookUtil.getSheetNames(workbook)
			def flag = 0
			def sheetNamesLength = sheetNames.size()
			for(int i=0;  i < sheetNamesLength; i++) {
				if (sheetNameMap.contains(sheetNames[i].trim())) {
					flag = 1
				}
			}

			def sheetConf = [:]

			// Get the title sheet
			titleSheet = workbook.getSheet("Title")

			if (titleSheet != null) {
				// Validate spreadsheet project Id with current user project Id.
				String sheetProjectId = WorkbookUtil.getStringCellValue(titleSheet, 1, 3)
				if (!project.id.toString().equals(sheetProjectId)) {
					throw new InvalidParamException("The spreadsheet provided project Id does not match current user project.")
				}

				try {
					String tzId = WorkbookUtil.getStringCellValue(titleSheet, 1, 8)
					String dateFormatType = WorkbookUtil.getStringCellValue(titleSheet, 1, 9)
					def dateTimeFormatter = TimeUtil.createFormatterForType(dateFormatType, TimeUtil.FORMAT_DATE_TIME_22)
					def dateFormatter = TimeUtil.createFormatterForType(dateFormatType, TimeUtil.FORMAT_DATE_TIME_12)

					String userTzId = userPreferenceService.timeZone
					String userDTFormat = userPreferenceService.dateFormat

					def userDateFormatter = TimeUtil.createFormatterForType(userDTFormat, TimeUtil.FORMAT_DATE)

					sheetConf.tzId = tzId
					sheetConf.dateFormatType = dateFormatType
					sheetConf.dateFormatter = dateFormatter
					sheetConf.userDateFormatter = userDateFormatter

					exportTime = WorkbookUtil.getDateTimeCellValue(titleSheet, 1, 7, tzId, dateTimeFormatter)
				} catch (Exception e) {
					log.info "Was unable to read the datetime for 'Export on': $e.message"
					throw new InvalidParamException("The 'Exported On' datetime was not found or was invalid in the Title sheet")
				}
			} else {
				throw new InvalidParamException("The required Title sheet was not found in the uploaded spreadsheet")
			}

			Map importResults
			String sheetName, domainClassName

			log.info "upload() Initializtion loading took ${stopwatch.lap()}"

			// ----
			// Devices Sheet
			// ----
			if (params.asset == 'asset') {
				log.info "upload() beginning Devices"
				sheetName='Devices'
				domainClassName = 'AssetEntity'
				importResults = processSheet(project, dataTransferSet, workbook, sheetName,
						'Id', 'Name', 0, domainClassName, exportTime, sheetConf, DEVICE)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Devices took ${stopwatch.lap()}"
			}

			// ----
			// Applications Sheet
			// ----
			if (params.application == 'application') {
				log.info "upload() beginning Applications"
				sheetName='Applications'
				domainClassName = 'Application'
				importResults = processSheet(project, dataTransferSet, workbook, sheetName,
						'Id', 'Name', 0, domainClassName, exportTime, sheetConf, APPLICATION)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Applications took ${stopwatch.lap()}"
			}

			// ----
			// Database Sheet
			// ----
			if (params.database == 'database') {
				log.info "upload() beginning Databases"
				sheetName='Databases'
				domainClassName = 'Database'
				importResults = processSheet(project, dataTransferSet, workbook, sheetName,
						'Id', 'Name', 0, domainClassName, exportTime, sheetConf, DATABASE)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Databases took ${stopwatch.lap()}"
			}

			// ----
			// Storage Sheet
			// ----
			if (params.storage == 'storage') {
				log.info "upload() beginning Logical Storage"
				sheetName='Storage'
				domainClassName = 'Files'
				importResults = processSheet(project, dataTransferSet, workbook, sheetName,
						'Id', 'Name', 0, domainClassName, exportTime, sheetConf, STORAGE)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Logical Storage took ${stopwatch.lap()}"
			}

			// Process Dependencies
			if (params.dependency == 'dependency') {
				log.info "upload() beginning Dependencies"
				def dependencySheet = workbook.getSheet("Dependencies")
				def dependencySheetRow = dependencySheet.getLastRowNum()

				for (int row = 1; row <= dependencySheetRow; row++) {
					// Check AssetName column (C) for not being blank
					def name = WorkbookUtil.getStringCellValue(dependencySheet, 2, row)
					if (name) {
						dependencyCount++
					}
				}

				// Set the session for progress meter
				setTotalAssets dependencyCount

				importResults = initializeImportResultsMap()
				importResults.rowsProcessed = dependencySheetRow

				int dependencySkipped = 0
				int dependencyAdded = 0
				int dependencyUpdated = 0
				int dependencyErrored = 0
				int dependencyUnchanged = 0

				// A closure used to handle errors
				def dependencyError = { msg ->
					importResults.errors << msg
					dependencyErrored++
					dependencySkipped--
				}

				List<AssetOptions> assetDepTypeList = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
				List<AssetOptions> assetDepStatusList = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)

				def lookupValue = { String value, List<AssetOptions> list ->
					for (it in list) {
						if (it.equalsIgnoreCase(value)) {
							return it
						}
					}
					'Unknown'
				}

				for (int r = 1; r <= dependencySheetRow ; r++) {
					// Assume that the dependency is skipped and we'll decrement when the row is saved at the bottom
					dependencySkipped++

					int rowNum = r + 1

					// This will clear the session every 50 rows
					if (GormUtil.flushAndClearSession(rowNum)) {
						project = GormUtil.mergeWithSession(project)
					}

					def assetId
					def assetIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 1, r)
					if (assetIdCell) {
						// TODO : JPM 5/2016 : upload() why is the dependency processing escaping quotes on columns that should be numbers?
						assetId = NumberUtils.toDouble(assetIdCell.replace("'","\\'"), 0).round()
					}

					String assetName
					String assetClass
					if (!assetId) {
						assetName = WorkbookUtil.getStringCellValue(dependencySheet, 2, r).replace("'","\\'")
						assetClass = WorkbookUtil.getStringCellValue(dependencySheet, 3, r).replace("'","\\'")

						if (!assetName) {
							dependencyError "Missing AssetId (in B$rowNum) or AssetName (in C$rowNum)"
							continue
						}
					}

					// ----
					// Try to lookup the AssetDependency record based on the depId (column A)
					// ----
					Long depId
					String depIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 0, r)
					AssetDependency assetDep
					if (depIdCell) {
						depId = NumberUtil.toPositiveLong(depIdCell, -1)
						if (depId == -1) {
							importResults.errors << "Invalid AssetDependencyId number '$depIdCell' (in A$rowNum)"
							continue
						}
						if (depId > 0) {
							assetDep = AssetDependency.get(depId)
							if (!assetDep) {
								dependencyError "AssetDependencyId '$depId' not found (in A$rowNum)"
								continue
							}
							if (assetDep.asset.project.id != project.id) {
								securityService.reportViolation("attempted to access assetDependency ($depId) not assigned to project ($project.id)")
								dependencyError " invalid AssetDependencyId reference '$depId' (in A$rowNum)"
								continue
							}
							log.debug "upload() found dependency by id $depId"
						}
					}

					// ----
					// Lookup the asset
					// ----
					AssetEntity asset
					if (assetId) {
						asset = AssetEntity.get(assetId)
						if (!asset) {
							dependencyError "Dependency asset by AssetId ($assetId) not found (in B$rowNum)"
							continue
						}
						if (asset.project.id != project.id) {
							securityService.reportViolation("attempted to access asset ($assetId) not assigned to project ($project.id)")
							dependencyError "Invalid reference of AssetId ($assetId) (row $rowNum)"
							continue
						}
					} else {
						def assets = AssetEntity.findAllByAssetNameAndProject(assetName, project)
						if (!assets) {
							dependencyError "Asset not found by AssetName '$assetName' (row $rowNum)"
							continue
						}

						if (assets.size() > 1) {
							asset = assets.find { it.assetType == assetClass }
							if (asset == null) {
								dependencyError "Asset by AssetName '$assetName' found duplicated assets (row $rowNum)"
								continue
							}
						} else {
							asset = assets[0]
						}
					}

					// ----
					// Lookup the dependent asset
					// ----
					AssetEntity dependent
					def dependencyId = NumberUtils.toDouble(WorkbookUtil.getStringCellValue(dependencySheet, 4, r).replace("'","\\'"), 0).round()
					if (dependencyId) {
						dependent = AssetEntity.get(dependencyId)
						if (!dependent) {
							dependencyError "Asset by DependentId ($dependencyId) not found (row $rowNum)"
							continue
						}
						if (dependent.project.id != project.id) {
							securityService.reportViolation("attempted to access dependent ($dependencyId) not assigned to project ($project.id)")
							dependencyError "Invalid reference of DependentId ($dependencyId) (row $rowNum)"
							continue
						}
					} else {
						def depName = WorkbookUtil.getStringCellValue(dependencySheet, 5, r).replace("'","\\'")
						def depClass = WorkbookUtil.getStringCellValue(dependencySheet, 6, r).replace("'","\\'")
						def assets = AssetEntity.findAllByAssetNameAndProject(depName, project)
						if (!assets) {
							dependencyError "Asset by DependentName ($depName) not found (row $rowNum)"
							continue
						}
						if (assets.size() > 1) {
							dependent = assets.find { it.assetType == depClass }
							if (dependent == null) {
								dependencyError "Asset by DependentName '$depName' found duplicated names (row $rowNum)"
								continue
							}
						} else {
							dependent = assets[0]
						}
					}

					// Prevent the creation of dependencies of an asset to itself.
					if (asset.id == dependent.id) {
						dependencyError("Creating a dependency for an asset to itself is not allowed (row $rowNum).")
						continue
					}

					boolean isNew = false
					if (!assetDep) {

						// Try finding the dependency by the asset and the dependent
						assetDep = AssetDependency.findByAssetAndDependent(asset, dependent)

						if (!assetDep) {
							assetDep = new AssetDependency(createdBy: securityService.loadCurrentPerson())
							isNew = true
						} else {
							Object[] msgParams = [asset.assetName, dependent.assetName]
							String msg = messageSource.getMessage("assetEntity.dependency.warning",msgParams , LocaleContextHolder.getLocale())
							dependencyError "$msg (row $rowNum)"
							continue
						}
					}

					if (assetDep) {
						assetDep.asset = asset
						assetDep.dependent = dependent

						def tmpType = WorkbookUtil.getStringCellValue(dependencySheet, 7, r, "").replace("'","\\'")
						def luv = lookupValue(tmpType, assetDepTypeList)
						if (tmpType && tmpType != 'Unknown' && luv == 'Unknown') {
							dependencyError "Invalid Type specified ($tmpType) for row $rowNum"
							continue
						}
						assetDep.type = luv

						// TODO : JPM 5/2016 : the status should probably have the same default value as the tmpType above
						def tmpStatus = WorkbookUtil.getStringCellValue(dependencySheet,10, r, "").replace("'","\\'") ?:
								(isNew ? "Unknown" : assetDep.status)
						luv = lookupValue(tmpStatus, assetDepStatusList)
						if (tmpStatus != 'Unknown' && luv == 'Unknown') {
							dependencyError "Invalid Status specified ($tmpStatus) for row $rowNum"
							continue
						}
						assetDep.status = luv

						if (StringUtils.isNotEmpty(WorkbookUtil.getStringCellValue(dependencySheet, 8, r, ""))) {
							assetDep.dataFlowFreq = WorkbookUtil.getStringCellValue(dependencySheet, 8, r, "", true)
						}
						if (StringUtils.isNotEmpty(WorkbookUtil.getStringCellValue(dependencySheet, 9, r, ""))) {
							assetDep.dataFlowDirection = WorkbookUtil.getStringCellValue(dependencySheet, 9, r, "", true)
						}

						def depComment = WorkbookUtil.getStringCellValue(dependencySheet, 11, r, "").replace("'","\\'")
						def length = depComment.length()
						if (length > 255) {
							depComment = StringUtil.ellipsis(depComment,255)
							dependencyError  "The comment was trimmed to 255 characters (row $rowNum)"
						}

						assetDep.comment = depComment
						assetDep.c1 = WorkbookUtil.getStringCellValue(dependencySheet, 12, r, "").replace("'","\\'")
						assetDep.c2 = WorkbookUtil.getStringCellValue(dependencySheet, 13, r, "").replace("'","\\'")
						assetDep.c3 = WorkbookUtil.getStringCellValue(dependencySheet,14, r, "").replace("'","\\'")
						assetDep.c4 = WorkbookUtil.getStringCellValue(dependencySheet, 15, r, "").replace("'","\\'")
						assetDep.updatedBy = securityService.loadCurrentPerson()

						// Make sure that there are no domain constraint errors
						if (assetDep.hasErrors()) {
							dependencyError "Validation errors exist (row $rowNum) : ${GormUtil.allErrorsString(assetDep)}"
							continue
						}

						if (!isNew && !assetDep.dirtyPropertyNames) {
							dependencyUnchanged++
							dependencySkipped--
							continue
						}

						if (!isNew && assetDep.dirtyPropertyNames) {
							log.info "upload() Changed fields $assetDep.dirtyPropertyNames of Dependency $assetDep.id"
							dependencyUpdated++
						}

						// Attempt to save the record
						if (!assetDep.save(flush:true)) {
							dependencyError "Dependency save failed for row $rowNum : ${GormUtil.allErrorsString(assetDep)}"
							continue
						}

						if (isNew) {
							dependencyAdded++
						}
						dependencySkipped--
					}
				}

				importResults.summary = "$dependencySheetRow Rows read, $dependencyAdded Added, $dependencyUpdated Updated, $dependencyUnchanged Unchanged, $dependencyErrored Errored, $dependencySkipped Skipped"
				processResults('Dependencies', importResults)
				log.info "upload() Dependencies took ${stopwatch.lap()}"

			} // Process Dependencies

			// ----
			// Process Cabling
			// ----
			if (params.cabling=='cable') {
				log.info "upload() beginning Cabling"
				def cablingSheet = workbook.getSheet("Cabling")
				def cablingSheetRow = cablingSheet.getLastRowNum()
				setTotalAssets cablingSheetRow

				def resultMap = assetEntityService.saveImportCables(cablingSheet)
				importResults = initializeImportResultsMap()

				importResults.rowsProcessed = cablingSheetRow
				importResults.errors = resultMap.warnMsg
				importResults.summary = "$importResults.rowsProcessed Rows read, $resultMap.cablingUpdated Updated, $resultMap.cablingSkipped Skipped, ${importResults.errors.size()} Errored"
				processResults('Cabling', importResults)

				log.info "upload() Cabling took ${stopwatch.lap()}"
			}

			// ----
			// Process Comments Imports
			// ----
			if (params.comment=='comment') {
				log.info "upload() beginning Comments"
				def commentsSheet = workbook.getSheet("Comments")

				int commentAdded = 0
				int commentUpdated = 0
				int commentUnchanged = 0
				int commentCount = commentsSheet.getLastRowNum()
				//def skippedUpdated = 0
				//def skippedAdded = 0

				setTotalAssets commentCount

				importResults = initializeImportResultsMap()
				importResults.rowsProcessed = commentCount

				// TODO : JPM 11/2014 : Refactor the lookup of PartyGroup.get(18) to be TDS lookup - see TM-3570
				List staffList = partyRelationshipService.getAllCompaniesStaffPersons([project.client, PartyGroup.get(18) ])
				// List staffList // not used in the PersonService at this time
				int r
				int rowNum
				try {
					for (r = 1; r <= commentCount; r++) {
						rowNum = r + 1

						// Clear the Hibernate Session periodically for performance purposes
						if (GormUtil.flushAndClearSession(rowNum)) {
							project = GormUtil.mergeWithSession(project)
						}

						boolean recordForAddition = false
						int cols = 0
						def commentIdImported = WorkbookUtil.getStringCellValue(commentsSheet, cols, r).replace("'","\\'")
						AssetComment assetComment
						if (commentIdImported) {
							def commentId = NumberUtil.toPositiveLong(commentIdImported, -1)
							if (commentId < 1) {
								//skippedUpdated++
								importResults.errors << "Invalid commentId number'$commentIdImported' (row $rowNum)"
								continue
							}
							assetComment = AssetComment.get(commentId)
							if (!assetComment) {
								//skippedUpdated++
								importResults.errors << "CommentId '$commentId' was not found (row $rowNum)"
								continue
							}
							if (assetComment.project != project) {
								securityService.reportViolation("attempted to access assetComment ($commentId) not assigned to project ($project.id)")
								importResults.errors << "Invalid CommentId '$commentIdImported' was specified (row $rowNum)"
								continue
							}
						} else {
							assetComment = new AssetComment(project: project, isImported: true)
							recordForAddition = true
						}

						assetComment.commentType = AssetCommentType.COMMENT

						String assetIdStr = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r).replace("'","\\'")
						Long assetId = NumberUtil.toPositiveLong(assetIdStr, -1)
						if (assetId > 0) {
							AssetEntity assetEntity = AssetEntity.findByIdAndProject(assetId, project)
							if (assetEntity) {
								assetComment.assetEntity = assetEntity
							} else {
								importResults.errors << "The assetId '$assetIdStr' was not found (row $rowNum)"
								//recordForAddition ? skippedAdded++ : skippedUpdated++
								continue
							}
						} else {
							importResults.errors << "An Invalid assetId '$assetIdStr' was specified (row $rowNum)"
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							continue
						}

						// Grab the category
						//def categoryInput = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r)?.replace("'","\\'")?.toLowerCase()?.trim()
						def categoryInput = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r, "", true)?.toLowerCase()?.trim()
						if (AssetCommentCategory.list.contains(categoryInput)) {
							//assetComment.category = categoryInput ?: AssetCommentCategory.GENERAL
							assetComment.category = categoryInput
						} else {
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							importResults.errors << "Invalid category '$categoryInput' specified (row $rowNum)"
							continue
						}

						// Try reading the created date as a date and if that fails try as a string and parse
						cols++
						Collection<String> validFormats = [
								TimeUtil.FORMAT_DATE_TIME,
								TimeUtil.FORMAT_DATE_TIME_22,
								TimeUtil.FORMAT_DATE_TIME_24,
								TimeUtil.FORMAT_DATE_TIME_25,
								TimeUtil.FORMAT_DATE
						]
						def dateCreated = WorkbookUtil.getDateCellValue(commentsSheet, cols, r, getSession(), validFormats)
						if (!dateCreated) {
							dateCreated = new Date()
						}

						// We need to keep track of the dateCreated change as it turns out the dirtyPropertyNames will NOT return this property
						boolean dateChanged = false
						if (dateCreated) {
							dateChanged = dateCreated != assetComment.dateCreated
							assetComment.dateCreated = dateCreated
						}

						// Get the createdBy person
						def createdByImported = StringUtils.strip(WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r))
						Person person = createdByImported ? personService.findPerson(createdByImported, project, staffList, false)?.person :
								securityService.loadCurrentPerson()

						if (person) {
							assetComment.createdBy = person
						} else {
							importResults.errors <<  "Created by person '$createdByImported' was not found (row $rowNum)"
							continue
						}

						if (!personService.hasAccessToProject(person, project)) {
							importResults.errors << "Created by '$person' whom does not have access to project or a login (row ${rowNum})"
							continue
						}

						assetComment.comment = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r)

						List dirty = assetComment.getDirtyPropertyNames()
						if (!recordForAddition && dirty.size() == 0 && !dateChanged) {
							commentUnchanged++
							continue
						}

						if (!assetComment.save()) {
							importResults.errors << "Save failed (row $rowNum) : ${GormUtil.allErrorsString(assetComment)}"
						} else {
							if (recordForAddition) {
								commentAdded++
							} else {
								commentUpdated++
							}
						}
					}
				} catch (e) {
					importResults.errors << "Import Failed at row $rowNum due to error '$e.message'"
					log.error "Comment Import failed for $securityService.currentUsername on row $rowNum : ${ExceptionUtil.stackTraceToString(e)}"
				}
				importResults.summary = "$importResults.rowsProcessed Rows read, $commentAdded Added, $commentUpdated Updated, $commentUnchanged Unchanged, ${importResults.errors.size()} Errors"
				processResults('Comments', importResults)
				log.info "upload() Comments took ${stopwatch.lap()}"

			} // Process Comment Imports

		} catch(NumberFormatException e) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			throw new InvalidParamException(e.message)
		} catch(Exception e) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			throw e
		}

		return generateResults(uploadResults, skipped, sheetList, flagToManageBatches)
	}

	/**
	 * Used to generate the import results
	 * @param results - a Map containing the information collected during the import process
	 * @param skipped - a List of the rows that were skipped
	 * @param sheetList - a List that contains the name of each sheet that was included in the import process
	 * @param notifyManageBatches - a boolean to control if the Manage Batches message is included in the results
	 */
	private StringBuilder generateResults(Map results, List skipped, List sheetList, boolean notifyManageBatches) {
		StringBuilder message = new StringBuilder("<h3>Spreadsheet import was successful</h3>\n")
		if (notifyManageBatches) {
			message.append("<p>Please click the Manage Batches below to review and post these changes</p><br>\n")
		}
		message.append("<p>Results: <ul>\n")
		sheetList.each {
			if (results[it].processed) {
				if (results[it].summary) {
					message.append("<li>$it: ${results[it].summary}</li>\n")
				} else {
					message.append("<li>$it: ${results[it].addedCount} loaded</li>\n")
				}
			}
		}
		message.append("</ul></p><br>\n")

		// Handle the errors and skipped rows
		if (sheetList.find { results[it].errorList?.size() }) {
			message.append("<p>Errors: <ul>\n")
			sheetList.each {
				if (results[it].processed) {
					if (results[it].errorList.size()) {
						message.append("<li>$it:<ul>")
						message.append(results[it].errorList.collect { "<li>$it</li>"}.join("\n"))
						message.append("</li></ul>\n")
					}
				}
			}
			message.append("\n</ul></p>\n")
		}

		if (skipped?.size()) {
			message.append("<br><p>Rows Skipped: <ul>\n")
			message.append("<li>${skipped.size()} spreadsheet row${skipped.size()==0 ? ' was' : 's were'} skipped: <ul>")
			message.append(skipped.collect { "<li>$it</li>" }.join("\n"))
			message.append("\n</ul></p>\n")
		}

		message.append("</p>\n")
		return message
	}

	/**
	 * A helper closure used to convert the cells in a row into values for an insert statement
	 * @param sqlStrBuff - the StringBuilder to append the VALUES sql into
	 * @param errorMsgList - the running list of error messages
	 * @param sheetRef - the Sheet being processed
	 * @param rowOffset - the row # (offset starting at 0)
	 * @param colOffset - the column # (offset starting at 0)
	 * @param dtaMapField - the DataTransferAttribute Map property for the current column
	 * @param entityId - the id of the asset if it was referenced in import
	 * @param dtBatchId - the batch number of the import
	 * @references formatDate
	 * @return An error message if it failed to add the value to the buffer
	 */
	private String rowToImportValues(
		Map sheetInfo,
		StringBuilder sqlStrBuff,
		Sheet sheetRef,
		Integer rowOffset,
		Integer colOffset,
		Map<String, ?> dtaMapField,
		String entityId,
		Long dtBatchId
	) {
		String cellValue = null
		String errorMsg = ""
		// Get the Excel column code (e.g. column 0 = A, column 1 = B)
		String colCode = WorkbookUtil.columnCode(colOffset)

		try {
			cellValue = WorkbookUtil.getStringCellValue(sheetRef, colOffset, rowOffset, '', true)
			if (cellValue == NULL_INDICATOR) {
				// TODO : check for columns that don't support NULL clearing
			} else {
				if ((dtaMapField["label"] in importColumnsDateType))  {
					if (!StringUtil.isBlank(cellValue)) {
						def dateValue = WorkbookUtil.getDateCellValue(sheetRef, colOffset, rowOffset, (DateFormat) sheetInfo.dateFormatter)
						// Convert to string in the Date format
						if (dateValue) {
							cellValue = TimeUtil.formatDate(dateValue, sheetInfo.userDateFormatter)
						} else {
							cellValue = ""
						}
					}

				} else {
					// TODO : sizeLimit can lookup known properties to know if there are limits
					int sizeLimit = 255
					if (cellValue?.size() > sizeLimit) {
						cellValue = cellValue.substring(0,sizeLimit)
						errorMsg = "Error column $dtaMapField.columnName ($colCode) value length exceeds $sizeLimit chars"
					}
				}
			}
		} catch (e) {
			log.debug "rowToImportValues() exception - ${ExceptionUtil.stackTraceToString(e)}"
			errorMsg = "Error column $dtaMapField.columnName ($colCode) - $e.message"
		}

		// Only create a value if the field isn't blank
		if (!StringUtil.isBlank(cellValue)) {
			if (!errorMsg) {
				if (sqlStrBuff && sqlStrBuff.size() > 0) {
					sqlStrBuff.append(SqlUtil.COMMA)
				}
				sqlStrBuff.append(SqlUtil.LEFT_PARENTHESIS)
				sqlStrBuff.append(entityId).append(SqlUtil.COMMA)
				sqlStrBuff.append(SqlUtil.STRING_QUOTE).append(cellValue).append(SqlUtil.STRING_QUOTE).append(SqlUtil.COMMA)
				sqlStrBuff.append(rowOffset).append(SqlUtil.COMMA)
				sqlStrBuff.append(dtBatchId).append(SqlUtil.COMMA)
				// sqlStrBuff.append(SqlUtil.NULL).append(SqlUtil.COMMA)
				sqlStrBuff.append(SqlUtil.STRING_QUOTE).append(dtaMapField["field"]).append(SqlUtil.STRING_QUOTE).append(SqlUtil.COMMA)
				sqlStrBuff.append(errorMsg ? 1 : 0).append(SqlUtil.COMMA)
				sqlStrBuff.append(SqlUtil.STRING_QUOTE).append(errorMsg).append(SqlUtil.STRING_QUOTE)
				sqlStrBuff.append(SqlUtil.RIGHT_PARENTHESIS)
			}
		}

		return errorMsg
	}

	/**
	 * A helper closure used to perform the actual insert statement into the dataTransfer table
	 * @param sheetName - the name of the tab for error reporting
	 * @param rowOffset - the row # (offset starting at 0)
	 * @param dtValues - the StringBuilder that contains the VALUES(...), VALUES(...), ...
	 * @param results - a map that keeps track of errors, skips and rows added
	 * @return True if insert was successful otherwise false
	 */
	private boolean insertRowValues(String sheetName, int rowOffset, StringBuilder dtValues, Map results) {
		boolean success = true
		try {
			log.debug "insertRowValues() SQL=${DTV_INSERT_SQL} ${dtValues.toString()}"
			jdbcTemplate.execute(DTV_INSERT_SQL + dtValues.toString())
			results.added ++
		} catch (Exception e) {
			results.errors << "Insert failed : $e.message"
			log.error("insertRowValues() Importing row ${rowOffset + 1} failed : $e.message", e)
			success = false
		}
		return success
	}

	/**
	 * Creates the transfer batch header for the various assets. Note that it also sets a value on the Session
	 * that is used for the progress bar. If it fails, the caller should return to the user.
	 * @param project
	 * @param dataTransferSet
	 * @param entityClassName - The name of the Domain class
	 * @param numOfAssets - The estimated number of assets to be imported
	 * @param exportTime - The datetime that the spreadsheet was originally exported
	 * @return The DataTransferBatch object if successfully created otherwise null
	 */
	@Transactional
	private DataTransferBatch createTransferBatch(
		Project project,
		DataTransferSet dataTransferSet,
		String entityClassName,
		int numOfAssets,
		Date exportTime ) {

		Class<?> clazz = LEGACY_DOMAIN_CLASSES[entityClassName]?.getClazz()
		AssetClass assetClass = AssetClass.lookup(clazz)

		def dtb = new DataTransferBatch(
				statusCode: "PENDING",
				transferMode: "I",
				dataTransferSet: dataTransferSet,
				project: project,
				userLogin: securityService.loadCurrentUserLogin(),
				// exportDatetime: GormUtil.convertInToGMT(exportTime, tzId),
				exportDatetime: exportTime,
				assetClass:assetClass
			)

		if (!dtb.save()) {
			log.error "createTransferBatch() failed save - ${GormUtil.allErrorsString(dtb)}"
			return null
		}

		setBatchId dtb.id
		setTotalAssets numOfAssets
		// log.debug "createTransferBatch() created $dtb"
		return dtb
	}

	/**
	 * @return A map of the various information about the sheet:
	 * 	sheet                  // The POI Sheet object
	 * 	sheetName              // The name of the sheet as it appears on the spreadsheet tab
	 * 	dtaMapList             // The ataTransferAttribute(s) used for this sheet
	 * 	rowCount               // The number of rows in the spreadsheet
	 * 	assetCount             // The number of assets (rows with no names are not counted)
	 * 	columnCount            // The number of columns in the spreadsheet
	 * 	colNamesOrdinalMap     // Map of the columns an the values being the ordinal position/column in sheet
	 * 	nameColumnIndex        // int index/offset of the 'Name' column in the spreadsheet
	 * 	assetIdColumnLabel     // String of column label/header for the asset id property
	 * 	assetIdColumnIndex     // int of asset id column index/offset/column number
	 * 	domainPropertyNameList // The list of the column names in the spreadsheet
	 */
	private Map getSheetInfo(Project project, Workbook spreadsheetWB, String sheetName, String assetIdColLabel,
							 String columnName, int headerRow, DataTransferSet dataTransferSet, AssetClass assetClass) {
		int nameColumnIndex = -1
		Map colNamesOrdinalMap = [:]

		Sheet sheet
		try {
			sheet = spreadsheetWB.getSheet(sheetName)
		} catch (e) {
			throw new RuntimeException("The '$sheetName' sheet is missing from the import spreadsheet")
		}

		// Get the DataTransferAttributeMap list of properties for the sheet
		List dtaMapList = null

		// Get the spreadsheet column header labels as a Map [label:ordinalPosition]
		int colCount = WorkbookUtil.getColumnsCount(sheet)
		for (int c = 0; c < colCount; c++) {
			String cellContent = WorkbookUtil.getStringCellValue(sheet, c, headerRow)
			colNamesOrdinalMap[cellContent] = c
		}

		List<Map<String, ?>> domainPropertyNameList = customDomainService.allFieldSpecs(project, assetClass.toString(), true)[assetClass.toString()]["fields"]

		// Make sure that the required columns are in the spreadsheet
		checkSheetForMissingColumns(sheetName, domainPropertyNameList, colNamesOrdinalMap)

		// Find the 'Name' column index and then look at each row to count how many assets will be imported
		nameColumnIndex = getColumnIndexForName(sheet, sheetName, columnName, colCount, headerRow)

		int numOfAssets = 0
		int rowsInSheet = sheet.lastRowNum
		for (int row = 1; row <= rowsInSheet; row++) {
			String assetName = WorkbookUtil.getStringCellValue(sheet, nameColumnIndex, row)
			if (assetName?.trim().size()) numOfAssets++
		}

		Map sheetInfo = [sheet: sheet, sheetName: sheetName, dtaMapList: dtaMapList, rowCount: rowsInSheet,
						 assetCount: numOfAssets, columnCount: colCount, colNamesOrdinalMap: colNamesOrdinalMap,
						 nameColumnIndex: nameColumnIndex, assetIdColumnLabel: assetIdColLabel,
						 assetIdColumnIndex: 0, domainPropertyNameList: domainPropertyNameList]

		return sheetInfo
	}

	/**
	 * A helper to deal with the repeated process for validating each sheet
	 * @param sheetName - the name of the sheet being validated
	 * @param entityMapColumnList - the list of the mapped column names expected
	 * @param sheetColumnNameList - the list of the spreadsheet tab column names
	 */
	private void checkSheetForMissingColumns(String sheetName, domainPropertyList, sheetColumnNameList) {
		List missingCols = getMissingColumns(domainPropertyList, sheetColumnNameList)
		if (missingCols) {
			throw new RuntimeException("missing expected columns $sheetName:${missingCols.join(', ')}")
		}
	}

	/**
	 * Used to compare the sheet headers to the eav mapping of expected column names
	 * @param entityMapColumnList - the names that are expected
	 * @param sheetColumnNames - the column names in the sheet
	 * @return a List the missing columns or blank if okay
	 */
	private List getMissingColumns(List entityMapColumnList, Map sheetColumnNames) {
		entityMapColumnList.findAll { Map<String, ?> field ->
			field["label"] != "DepGroup" && !sheetColumnNames.containsKey(field["label"])
		}
	}

	/**
	 * Look up the column index for a given column name
	 * @param sheetObject - the actual sheet object
	 * @param sheetName - the name of the sheet
	 * @param columnName - the name of the column to lookup
	 * @param colCount - the number of columns in the sheet
	 * @param rowOffset - the row offset to the header itself
	 * @return the index value as an int
	 */
	private int getColumnIndexForName(sheetObject, sheetName, columnName, colCount, rowOffset) {
		for (int index = 0; index <= colCount; index++) {
			if (WorkbookUtil.getStringCellValue(sheetObject, index, 0) == columnName) {
				return index
			}
		}

		throw new RuntimeException("unable to find '$columnName' column in sheet '$sheetName'")
	}

	// Method process one of the asset class sheets
	private Map processSheet(project, dataTransferSet, workbook, sheetName, assetIdColName,
							 assetNameColName, headerRowNum, domainName, timeOfExport, sheetConf, AssetClass assetClass) {

		Map results = initializeImportResultsMap()
		try {
			Map sheetInfo = getSheetInfo(project, workbook, sheetName, assetIdColName, 'Name', headerRowNum, dataTransferSet, assetClass)
			DataTransferBatch dataTransferBatch = createTransferBatch(project, dataTransferSet, domainName,
					sheetInfo.assetCount, timeOfExport)
			if (!dataTransferBatch) {
				throw new InvalidParamException("Failed to create import batch for the '$sheetName' tab. Please contact support if the problem persists.")
			}

			sheetInfo << sheetConf
			importSheetValues(results, dataTransferBatch, sheetInfo)
			results.dataTransferBatch = dataTransferBatch

			log.debug "processSheet() sheet $sheetName results = $results"
		} catch (e) {
			log.debug "processSheet() exception : ${ExceptionUtil.stackTraceToString(e)}"
			results.errors << "Sheet $sheetName failed to process - $e.message"
		}

		return results
	}

	/**
	 * Iterates over the spreadsheet rows and loads each of the cells into the DataTransferValue table
	 * @param results - the map used to track errors, skipped rows and count of what was added
	 * @param dataTransferBatch - the batch to insert the rows into
	 * @param sheetInfo - the map of all of the sheet information
	 * @return a Map containing the following elements
	 *		List errors - a list of errors
	 *		List skipped - a list of skipped rows
	 *		Integer added - a count of rows added
	 */
	private Map importSheetValues(Map results, DataTransferBatch dataTransferBatch, Map sheetInfo) {

		Sheet sheetObject = sheetInfo.sheet
		Map colNamesOrdinalMap = sheetInfo.colNamesOrdinalMap
		int assetNameColIndex = sheetInfo.nameColumnIndex
		String assetSheetName = sheetInfo.sheetName

		Project project = dataTransferBatch.project

		// Verify that the sheet has the Asset Id Column by name that we are expecting
		if (!colNamesOrdinalMap.containsKey(sheetInfo.assetIdColumnLabel)) {
			results.errors << "$assetSheetName Sheet - missing asset id column name '$sheetInfo.assetIdColumnLabel'"
		} else {

			results.rowsProcessed = sheetInfo.rowCount

			// Iterate over each row in the spreadsheet
			for(int r = 1; r <= sheetInfo.rowCount ; r++) {
				boolean rowHasErrors = false
				String errorMsg
				def assetId
				StringBuilder sqlValues = new StringBuilder()

				// Make sure that the asset has the mandatory name
				def assetName = WorkbookUtil.getStringCellValue(sheetObject, assetNameColIndex, r)
				if (!assetName) {
					errorMsg = "missing required 'name'"
					results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
					rowHasErrors = true
				} else {

					// Now check to see if the asset references a pre-existing asset by id #
					assetId = WorkbookUtil.getStringCellValue(sheetObject, 0, r)
					if (assetId) {
						// Switch to a positive long and if null then it is bogus
						Long id = NumberUtil.toPositiveLong(assetId)
						if (id == null) {
							errorMsg = "invalid assetId format '$assetId'"
						} else {
							def asset = AssetEntity.get(id)
							if (!asset) {
								errorMsg = "asset not found '$assetId'"
							} else if (asset.project != project) {
								errorMsg = "invalid asset id '$assetId'"
								securityService.reportViolation("attempted to access asset ($assetId) associated to different project")
							}
						}
					} else {
						assetId = 'null'
					}

					if (errorMsg) {
						results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
						continue
					}

					for (int cols = 0; cols < sheetInfo.columnCount; cols++) {
						String attribName
						String columnHeader = WorkbookUtil.getStringCellValue(sheetObject, cols, 0)
						attribName = columnHeader
						def dtaAttrib = sheetInfo.domainPropertyNameList.find { Map<String, ?> field ->
							field["label"] == attribName
						}

						if (dtaAttrib != null) {
							// Add the SQL VALUES(...) to the sqlValues StringBuilder for the current spreadsheet cell
							errorMsg = rowToImportValues(sheetInfo, sqlValues, sheetObject, r, cols, dtaAttrib, assetId, dataTransferBatch.id)
							if (errorMsg) {
								rowHasErrors = true
								results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
							}
						}
					}
				}

				if (rowHasErrors) {
					log.debug "importSheetValues() rowHasErrors - $errorMsg"
					// Clear the error msg so it doesn't get reported again below since it was already reported in the above for col loop
					errorMsg = ''
				} else {
					try {
						// Attempt to actual insert the values that represent the current row of data
						insertRowValues(assetSheetName, r, sqlValues, results)
					} catch (e) {
						log.warn "importSheetValues() insert failed $e.message (sheet:$assetSheetName, row:$r) - ${ExceptionUtil.stackTraceToString(e)}"
						errorMsg = "Failed to insert data due to $e.message"
					}
				}

				if (errorMsg) {
					results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
				}

			} // for r

			results.summary = "$results.rowsProcessed Rows read, $results.added Loaded, ${results.errors.size()} Errored"
		}

		return results
	}

	/**
	 * Returns the Map used to track the results of imports for each tab
	 * @return map template of import stats/data
	 */
	private Map initializeImportResultsMap() {
		[errors: [], skipped: [], summary: '', added: 0]
	}

	private void setBatchId(long id) {
		session.setAttribute 'BATCH_ID', id
	}

	private void setTotalAssets(long count) {
		session.setAttribute 'TOTAL_ASSETS', count
	}

}
