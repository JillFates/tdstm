package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.tm.domain.AssetEntityHelper
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferValue
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.FlushMode
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport

import java.util.regex.Matcher

@Slf4j
class ImportService implements ServiceMethods {

	GrailsApplication grailsApplication
	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService assetEntityService
	DeviceService deviceService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProgressService progressService
	SecurityService securityService

	// The number of assets to process before clearing the hibernate session
	static final int CLEAR_SESSION_AFTER = 100

	static final String indent = '&nbsp;&nbsp;&nbsp;'
	static final String NULL_INDICATOR='NULL'

	// Indicates the number of rows to process before performing a flush/clear of the Hibernate session queue
	static final int HIBERNATE_BATCH_SIZE=20

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

		// log.debug('dataTransferBatch.eavEntityType?.domainName = {}', dataTransferBatch.eavEntityType?.domainName)
		// log.debug('AssetClass.domainNameFor(assetClass) = {}', AssetClass.domainNameFor(assetClass))

		if (dataTransferBatch.eavEntityType?.domainName != AssetClass.domainNameFor(assetClass)) {
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

		// TODO : JPM 1/2017 : The staffList is getting ONLY the staff of the client but should be getting all staff on the project
		data.staffList = partyRelationshipService.getAllCompaniesStaffPersons(project.client)

		def domainName = dtb.eavEntityType?.domainName
		if (domainName.equals('AssetEntity')) {
			domainName = 'Server'
		}
		// TODO : JPM 11/2014 : loadBatchData() has hard-code AssetEntity to Server switch for EavAttributeSet - someday it should just be Device...
		data.eavAttributeSet = EavAttributeSet.findByAttributeSetName(domainName)
		assert data.eavAttributeSet != null

		// log.debug 'loadBatchData({}) for dtb.eavEntityType?.domainName={}, domainName=[{}], data.eavAttributeSet={}',
		//		dtb.id, dtb.eavEntityType?.domainName, domainName, data.eavAttributeSet

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
		StringBuffer sb = new StringBuffer()

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

		boolean batchIsForDevices = dtb.eavEntityType?.domainName == "AssetEntity"

		// Get a Device Type Map used to verify that device type are valid
		Map deviceTypeMap = getDeviceTypeMap()

		// A map that will be used to track the invalid referenced Device Types
		Map invalidDeviceTypeMap = [:]

		if (performance) now = new Date()
		def dataTransferValueRowList = DataTransferValue.findAll(
			"From DataTransferValue d where d.dataTransferBatch=? " +
			"and d.dataTransferBatch.statusCode='PENDING' group by rowId", [dtb])
		if (performance) log.debug 'Fetching DataTransferValue ROWS took {}', TimeUtil.elapsed(now)

		if (performance) now = new Date()

		List<Long> assetIds = AssetEntity.where {
			project == project
		}.projections {property("id")}.list()

		if (performance) log.debug 'Fetching existing asset IDS took {}', TimeUtil.elapsed(now)

		def eavAttributeSet = EavAttributeSet.get(1)

		def assetIdList = []
		def dupAssetIds = []
		def notExistedIds = []
		Map mfgModelMatches = [:]

		EavEntityType deviceEavEntityType = EavEntityType.findByDomainName('AssetEntity')
		EavAttribute mfgEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'manufacturer')
		EavAttribute modelEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'model')
		EavAttribute deviceTypeEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'assetType')

		log.debug '{} deviceEavEntityType={}, mfgEavAttr={}, modelEavAttr={}, deviceTypeEavAttr={}',
				methodName, deviceEavEntityType, mfgEavAttr, modelEavAttr, deviceTypeEavAttr

		def assetCount = dataTransferValueRowList.size()

		if (performance) {
			log.info '{} Initialization took {}', methodName, TimeUtil.elapsed(now)
		}

		now = new Date()
		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			int rowNum = dataTransferValueRow + 1
			def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId

			// log.debug '***{} {} of {}', methodName, dataTransferValueRow, assetCount

			if (batchIsForDevices) {
				String mfgName = DataTransferValue.findWhere(dataTransferBatch: dtb, rowId: rowId, eavAttribute: mfgEavAttr)?.importValue
				String modelName = DataTransferValue.findWhere(dataTransferBatch: dtb, rowId: rowId, eavAttribute: modelEavAttr)?.importValue

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
					String deviceType = DataTransferValue.findWhere(dataTransferBatch:dtb, rowId:rowId, eavAttribute:deviceTypeEavAttr)?.importValue
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

		//if (log.debugEnabled) {
		//	mfgModelMatches.each {k,v -> sb.append("Mfg/Model Matches $k: $v <br>")}
		//}

		sb.append('<br>Review took ' + elapsedTime + ' to complete')

		String info=sb.toString()

		jobProgressFinish(progressKey, info)

		dtb.importResults = combineDataTransferBatchImportResults(dtb, info)

		return [elapsedTime: elapsedTime, info:info]
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
						String domainName = dtb.eavEntityType?.domainName
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
	private void processRequiredProperties(project, assetObj, rowNum, warnings, errorConflictCount, tzId, dtFormat) {
		List requiredProps = ['assetTag', 'moveBundle', 'planStatus', 'validation']
		requiredProps.each { prop ->
			if (!assetObj[prop]) {
				Map dtvMap = [
					importValue: '',
					eavAttribute: [attributeCode : prop ]
				]
				assetEntityAttributeLoaderService.setCommonProperties(project, assetObj, dtvMap, rowNum, warnings, errorConflictCount, tzId, dtFormat)
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

		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, AssetClass.APPLICATION, batchId)
		if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = data.eavAttributeSet
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

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(Application, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(Application, 'blank', true)
		def dtvList

		//
		// Main loop that will iterate over the row ids from the import batch
		//
		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			now = new Date()
			startedAt = new Date()
			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			application = assetEntityAttributeLoaderService.findAndValidateAsset(project, userLogin, Application, assetEntityId, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets, rowNum)
			if (application == null)
				continue

			if (!application.id) {
				// Initialize extra properties for new application asset
			}

			// Iterate over the properties and set them on the asset
			dtvList.each {
				def attribName = it.eavAttribute.attributeCode
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
						assetEntityAttributeLoaderService.setCommonProperties(project, application, it, rowNum, warnings, errorConflictCount, tzId, dtFormat)

				} // switch(attribName)

				if (warnMsg) {
					warnings << warnMsg
					log.warn warnMsg
					errorConflictCount++
				}

			}	// dtvList.each

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, application, rowNum, warnings, errorConflictCount, tzId, dtFormat)

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
		AssetClass assetClass = AssetClass.DEVICE
		def domainClass = AssetClass.domainClassFor(assetClass)

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
		if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = data.eavAttributeSet
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		// Get a Device Type Map used to verify that device type are valid
		Map deviceTypeMap = getDeviceTypeMap()

		counts.room = Room.countByProject(project)
		counts.rack = Rack.countByProject(project)

		//
		// Iterate over the rows
		//
		def dtvList
		def rowNum
		def asset

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
				asset = assetEntityAttributeLoaderService.findAndValidateAsset(project, userLogin, domainClass, assetEntityId, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets, rowNum)
				if (!asset) {
					continue
				}

				if (asset.id) {
					existingAssetsList << asset
				} else {
					// Initialize extra properties for new asset
				}

				def isNewValidate = (!asset.id)

				// This will hold any of the source/target location, room and rack information
				def locRoomRack = [source: [:], target: [:] ]

				// Vars caught in the each loop below to be used to create mfg/model appropriately
				String mfgName, modelName, usize, deviceType

				// Vars caught in the each loop for setting the chassis and position
				String sourceChassis, targetChassis, sourceBladePosition, targetBladePosition

				// Iterate over the attributes to update the asset with
				dtvList.each {
					def attribName = it.eavAttribute.attributeCode

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

					// log.debug "Processing attribName=$attribName"

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
						case ~/(source|target)(Location|Room|Rack)/:
							// def field = Matcher.lastMatcher[0][1]
							def disposition = Matcher.lastMatcher[0][1]
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
							assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat)
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
				processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat)

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
							errors = deviceService.assignDeviceToLocationRoomRack(
								asset,
								d[disposition + 'Location'],
								d[disposition + 'Room'],
								d[disposition + 'Rack'],
								(disposition == 'source'))
						}
						if (errors) {
							warnings << "Unable to set $disposition Loc/Room/Rack (row $rowNum) : $errors"
						}
					}


				} // ['source', 'target'].each

				// log.debug "$methodName asset $asset $asset.sourceLocation/$asset.sourceRoom/$asset.sourceRack"

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
		AssetClass assetClass = AssetClass.DATABASE
		def domainClass = AssetClass.domainClassFor(assetClass)

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
		if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

		// Fetch all of the common data shared by all of the import processes
		def now = new Date()
		Map data = loadBatchData(dataTransferBatch)
		if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = data.eavAttributeSet
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		def nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		def blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		def dtvList

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			now = new Date()
			startedAt = new Date()

			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch,rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			def asset = assetEntityAttributeLoaderService.findAndValidateAsset(project, userLogin, domainClass,
				assetEntityId, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets, rowNum)
			if (!asset)
				continue

			if (!asset.id) {
				// Initialize extra properties for new asset
			}

			dtvList.each {
				def attribName = it.eavAttribute.attributeCode

				// If trying to set to NULL - call the closure to update the property and move on
				if (it.importValue == "NULL") {
					assetEntityAttributeLoaderService.setToNullOrBlank(asset, attribName, it.importValue, nullProps, blankProps)
					return
				}

				switch (attribName) {
					// case ?:

					default:
						// Try processing all common properties
						assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat)

				}

			}

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat)

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
		AssetClass assetClass = AssetClass.STORAGE
		def domainClass = AssetClass.domainClassFor(assetClass)

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

		def eavAttributeSet = data.eavAttributeSet
		List staffList = data.staffList
		List dataTransferValueRowList = data.dataTransferValueRowList
		int assetCount = dataTransferValueRowList.size()

		jobProgressUpdate(progressKey, 1, assetCount)

		List nullProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'nullable', true)
		List blankProps = GormUtil.getDomainPropertiesWithConstraint(domainClass, 'blank', true)

		List dtvList

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++) {
			now = new Date()
			startedAt = new Date()

			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			def rowNum = rowId+1

			// Get all of the property values imported for a given row
			dtvList?.clear()
			dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

			Long assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			def asset = assetEntityAttributeLoaderService.findAndValidateAsset(project, userLogin, domainClass, assetEntityId, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets, rowNum)

			if (asset == null)
				continue

			if (!asset.id) {
				// Initialize extra properties for new asset
				// asset.scale = SizeScale.GB
			}

			dtvList.each {
				def attribName = it.eavAttribute.attributeCode

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

				switch (attribName) {
					// case ?:

					default:
						// Try processing all common properties
						assetEntityAttributeLoaderService.setCommonProperties(project, asset, it, rowNum, warnings, errorConflictCount, tzId, dtFormat)
				}

			}

			// Update various common properties that may not have been set by the above loop
			processRequiredProperties(project, asset, rowNum, warnings, errorConflictCount, tzId, dtFormat)

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
		Map map = personService.findPerson(name, project, projectStaff)

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
}
