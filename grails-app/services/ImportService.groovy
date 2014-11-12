//import org.apache.commons.lang.math.NumberUtils
//import org.apache.commons.lang.StringUtils
//import org.codehaus.groovy.grails.commons.GrailsClassUtils
//import com.tds.asset.AssetCableMap
import com.tdssrc.eav.EavAttribute
//import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.eav.EavEntityType
//import com.tdsops.tm.enums.domain.SizeScale
//import com.tdsops.tm.enums.domain.AssetCableStatus
//import com.tdssrc.grails.DateUtil

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttributeSet
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher

class ImportService {

	boolean transactional = true
	
	def assetEntityAttributeLoaderService
	def assetEntityService
	def deviceService
	def personService
	def rackService
	def roomService
	def securityService
	def partyRelationshipService

	def sessionFactory
	
	static final STATUS_COMPLETE = 'COMPLETED'
	static final STATUS_PENDING = 'PENDING'
	// The number of assets to process before clearing the hibernate session 
	static final int CLEAR_SESSION_AFTER = 100

	static final String indent = '&nbsp;&nbsp;&nbsp;'
	static final String NULL_INDICATOR='NULL'

	/** 
	 * Used by the process methods to peform the common validation checks before processing
	 * @param project - the project that the user is logged into and the batch is associated with
	 * @param userLogin - the user login object of whom invoked the process
	 * @param assetClass - the asset class type
	 * @param batchId - the id number of the batch to be processed
	 */
	private DataTransferBatch processValidation(Project project, UserLogin userLogin, AssetClass assetClass, String batchId) {

		log.info "processValidate(project:${project.id}, userLogin:$userLogin, assetClass:${assetClass.toString()}, batchId:$batchId) started invoked at (${new Date().toString()})"

		if (! securityService.hasPermission(userLogin, 'Import')) {
			securityService.reportViolation("Attempted to process asset imports without permission, project:$project, batchId:$batchId")
			throw new UnauthorizedException('You do not have permission to process asset imports')
		}

		Long id = NumberUtil.toLong(batchId)
		if (id == null || id < 1) {
			securityService.reportViolation("Attempted to process asset imports invalid batch id ($batchId), project:$project")
			throw new InvalidParamException("Invalid batch id was requested")
		}

		def dataTransferBatch = DataTransferBatch.get(batchId)
		if (! dataTransferBatch) {
			securityService.reportViolation("Attempted to process asset imports with missing batchId $batchId, project:$project")
			throw new InvalidParamException('Unable to find the specified batch')
		}
		if (dataTransferBatch.project.id != project.id) {
			securityService.reportViolation("Attempted to process asset imports with batchId $batchId not assocated with their session (${project.id})")
			throw new InvalidParamException('Unable to find the specified batch')
		}
log.debug("dataTransferBatch.eavEntityType?.domainName = ${dataTransferBatch.eavEntityType?.domainName}")
log.debug("AssetClass.domainNameFor(assetClass) = ${AssetClass.domainNameFor(assetClass)}")
		if ( dataTransferBatch.eavEntityType?.domainName != AssetClass.domainNameFor(assetClass)) {
			throw new InvalidParamException("Specified batch is not for the asset class $assetClass")
		}

		if ( dataTransferBatch.statusCode == STATUS_COMPLETE ) {
			log.warm "$warn for previously processed batch $batchId, project:$project"
			throw new InvalidParamException('Specified batch was previously processed')
		}

		return dataTransferBatch
	}

	/**
	 * Utility method for tbeh batch processing to load the import batch data 
	 * @param batchId - the batch id number to process
	 * @return a map containing the following elements:
	 *		assetsInBatch - the number of assets in the batch
	 *		dataTransferValueRowList - the entire list of DataTransferValue being processed
	 *		eavAttributeSet - the attribute set for set #1
	 *		staffList - the company staff for the project associated with the batch
	 */	
	private Map loadBatchData(DataTransferBatch dtb) {
		Project project = dtb.project

		Map data = [:]
		data.assetsInBatch = DataTransferValue.executeQuery("select count(distinct rowId) from DataTransferValue where dataTransferBatch=?", [dtb])[0]
		data.dataTransferValueRowList = DataTransferValue.findAll("From DataTransferValue d where d.dataTransferBatch=? and d.dataTransferBatch.statusCode='PENDING' group by rowId", [dtb])
		// data.dataTransferValues = DataTransferValue.findAllByDataTransferBatch(dtb)
		data.eavAttributeSet = EavAttributeSet.findById(1)
		data.staffList = partyRelationshipService.getAllCompaniesStaffPersons(project.client)

		return data
	} 

	/** 
	 * Used to initialize the session in order to track the progress of the process
	 * @param session - the browser Session object
	 * @param  assetCount - the number of assets to be processed by the batch
	 */
	void initProgress(session, assetCount) {
		session.setAttribute("TOTAL_BATCH_ASSETS", assetCount)
		session.setAttribute("TOTAL_PROCESSES_ASSETS", 0)
	}

	/**
	 * Used to update the session with the current counter
	 * @param session - the browser session
	 * @param currentCount - the number of assets currently completed
	 */
	void updateProgress(session, currentCount) {
		session.setAttribute("TOTAL_PROCESSES_ASSETS", currentCount)
	}

	/**
	 * Used to retrieve the list of valid Device Type values as a Map that is used for faster validation. Note that the
	 * type is force to lowercase.
	 * @return Map of the Device Type names as the key and value boolean true that has no real meaning
	 */
	Map getDeviceTypeMap() {
		// Get a Device Type Map used to verify that device type are valid
		List deviceTypeList = assetEntityService.getDeviceAssetTypeOptions()
		HashMap deviceTypeMap = new HashMap(deviceTypeList.size())
		deviceTypeList.each { type -> deviceTypeMap[type.toLowerCase()] = true }
		return deviceTypeMap
	}

	/**
	 * Used to review an import batch for any unexpected issues 
	 * @param projectId - the id of the project
	 * @param userLoginId - the id of the user that has invoked the process
	 * @param batchId - the id of the batch to examine
	 * @return An error message if any issues otherwise null
	 */
	Map reviewImportBatch(Long projectId, Long userLoginId, Long batchId, session) {
		def startedAt = new Date()
		String errorMsg = ''

		String methodName = 'reviewImportBatch()'
		StringBuffer sb = new StringBuffer()
		
		Project project = Project.read(projectId)
		UserLogin userLogin = UserLogin.read(userLoginId)

		boolean performance=true
		def now = new Date()

		DataTransferBatch dtBatch = DataTransferBatch.get(batchId)
		if (!dtBatch) {
			return [error:'Unable to find batch id']
		}
		if (dtBatch.project.id != projectId) {
			securityService.reportViolation("reviewImportBatch() call attempted to access batch ($batchId) not associated to user's project ($projectId)")
			return [error:'Unable to locate batch id']
		}

		boolean batchIsForDevices = dtBatch.eavEntityType?.domainName == "AssetEntity"
		
		// Get a Device Type Map used to verify that device type are valid
		Map deviceTypeMap = getDeviceTypeMap()

		// A map that will be used to track the invalid referenced Device Types
		HashMap invalidDeviceTypeMap = new HashMap()

		if (performance) now = new Date()
		def dataTransferValueRowList = DataTransferValue.findAll(
			"From DataTransferValue d where d.dataTransferBatch=? " +
			"and d.dataTransferBatch.statusCode='PENDING' group by rowId", [dtBatch])
		if (performance) log.debug "Fetching DataTransferValue ROWS took ${TimeUtil.elapsed(now)}"

		if (performance) now = new Date()
		def assetIds = AssetEntity.findAllByProject(project)?.id
		if (performance) log.debug "Fetching existing asset IDS took ${TimeUtil.elapsed(now)}"

		def eavAttributeSet = EavAttributeSet.findById(1)

		def assetIdList = []
		def dupAssetIds = []
		def notExistedIds = []
		Map mfgModelMatches = [:]

		EavEntityType deviceEavEntityType = EavEntityType.findByDomainName('AssetEntity')
		EavAttribute mfgEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'manufacturer')
		EavAttribute modelEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'model')
		EavAttribute deviceTypeEavAttr = EavAttribute.findWhere(entityType: deviceEavEntityType, attributeCode: 'assetType')

		log.debug "$methodName deviceEavEntityType=$deviceEavEntityType, mfgEavAttr=$mfgEavAttr, modelEavAttr=$modelEavAttr, deviceTypeEavAttr=$deviceTypeEavAttr"

		if (performance) now = new Date()
		def assetCount = dataTransferValueRowList.size()
		initProgress(session, assetCount)

		for (int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++ ) {
			if (dataTransferValueRow.mod(50) == 0) {
				log.info "$methodName reviewed ${dataTransferValueRow+1} rows of ${assetCount} for batch id $batchId"
			}

			if (false && dataTransferValueRow > 20) {
				sb.append("Aborted process early")
				break
			}

			// log.debug("Processing $dataTransferValueRow of $assetCount")
			def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
			int rowNum = rowId + 1
			def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
			
			if (batchIsForDevices) {
				String mfgName = DataTransferValue.findWhere(dataTransferBatch:dtBatch, rowId:rowId, eavAttribute:mfgEavAttr)?.importValue
				String modelName = DataTransferValue.findWhere(dataTransferBatch:dtBatch, rowId:rowId, eavAttribute:modelEavAttr)?.importValue

				mfgName = StringUtil.defaultIfEmpty(mfgName, '')
				modelName = StringUtil.defaultIfEmpty(modelName, '')

				boolean found = false
				if (mfgName.size() > 0 || modelName.size() > 0) {
					// Check to see if it is in the list
					String key = "$mfgName::::$modelName"
					if (mfgModelMatches.containsKey(key)) {
						mfgModelMatches[key].count++
						found = mfgModelMatches[key].found
					} else {
						found = verifyMfgAndModelExist(mfgName, modelName)
						mfgModelMatches[key] = [ found: found, count: 1, mfg: mfgName, model: modelName]
					}
				}

				// Validate the device type only if the Mfg/Model were not found
				if (! found) {
					String deviceType = DataTransferValue.findWhere(dataTransferBatch:dtBatch, rowId:rowId, eavAttribute:deviceTypeEavAttr)?.importValue
					if (!deviceTypeMap.containsKey(deviceType.toLowerCase())) {
						if (!invalidDeviceTypeMap.containsKey(deviceType)) {
							invalidDeviceTypeMap[deviceType] = 1
						} else {
							invalidDeviceTypeMap[deviceType]++
						}
					}
				}
			}
			
			// log.debug "Checking for dups"
			// Checking for duplicate asset ids
			if (assetEntityId && assetIdList.contains(assetEntityId))
				dupAssetIds << assetEntityId
			
			// log.debug "Checking for missing ids"
			// Checking for asset ids which does not exist in database
			if (assetEntityId && !assetIds.contains((Long)(assetEntityId)))
				notExistedIds << assetEntityId
				
			assetIdList << assetEntityId

			updateProgress(session, rowNum)
		}

		if (performance) log.debug "Reviewing $assetCount batch records took ${TimeUtil.elapsed(now)}"

		sb.append("<b>Process results of review for batch ${batchId}:</b><ul>" + 
			"<li>Assets in batch: $assetCount</li>" +
			"</ul>"
		) 

		// Log missed Mfg/Model references
		def missingMfgModel = mfgModelMatches.findAll { k, v -> ! v.found} 
		if (missingMfgModel) {
			sb.append("<b>Missing Mfg / Model references:</b><ul>")
			missingMfgModel.each { k, d ->
				sb.append("<li>Mfg: ${d.mfg} | Model: ${d.model} | ${d.count} reference${d.count > 1 ? '(s)' : ''}</li>") 
			}
			sb.append('</ul>')
			boolean canCreateMfgAndModel = securityService.hasPermission(userLogin, 'NewModelsFromImport')
			if (!canCreateMfgAndModel) {
				sb.append("$indent <b>Note: You do not have the permission necessary to create models during import</b><br>")
			}

			sb.append('<br>')
		}

		// Log found Mfg/Model references
		/*
		mfgModelMatches.each { k, d ->
			if (d.found)
				sb.append("Mfg: ${d.mfg} Model: ${d.model} found - ${d.count} reference(s)<br>") 
		}
		*/

		if( dupAssetIds ) {
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
			sb.append('</ul><br>')
		}
	
		def elapsedTime = TimeUtil.elapsed(startedAt).toString()

		log.info "$methodName Review process of $assetCount batch records took $elapsedTime"

		//if (log.isDebugEnabled()) {
		//	mfgModelMatches.each {k,v -> sb.append("Mfg/Model Matches $k: $v <br>")} 
		//}

		sb.append("<br>Review took $elapsedTime to complete")

		return [elapsedTime: elapsedTime, info: sb.toString()]
	}

	/**
	 * Used to verify that the Manufacturer/Model exists in the model table
	 * @param searchMfgName - the name of the manufacturer to lookup
	 * @param searchModelName - the model name to lookup
	 * @return false if not found
	 */
	private boolean verifyMfgAndModelExist(searchMfgName, searchModelName) {
		 
		boolean found=false
		boolean mfgBlank = StringUtil.isBlank(searchMfgName)
		boolean modelBlank = StringUtil.isBlank(searchModelName)

		// Don't need to search for a match if mfg/model are blank
		if ( mfgBlank || modelBlank)
			return false

		Manufacturer mfg 
		if (! mfgBlank ) {
			mfg = Manufacturer.findByName(searchMfgName)
			if( !mfg ) {
				mfg = ManufacturerAlias.findByName( searchMfgName )?.manfacturer
				log.debug "verifyMfgAndModelExist() lookup Mfg by alias found: $mfg"
			}
			if (!mfg) {
				log.debug "verifyMfgAndModelExist() failed on MFG for $searchModelName, $searchMfgName"
				return false
			}
		}

		if (! modelBlank) {
			Model model = Model.findByModelName(searchModelName)
			if (! model) {
				model = ModelAlias.findByNameAndManufacturer(searchModelName, mfg)?.model
				log.debug "verifyMfgAndModelExist() lookup Model by alias found: $model"
			}
			if (! model) {
				log.debug "verifyMfgAndModelExist() failed to find MODEL for $searchModelName, $searchMfgName"
				return false
			}
		}

		return true
	}

	/**	
	 * This process will iterate over the assets imported into the specified batch and update the devices appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginId - the id of the user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param session - the controller session that invoked this method (used to update session variables used for progress updates)
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 */
	Map processAssetEntityImport(Long projectId, Long userLoginId, String batchId, Object session, tzId) {
		boolean performance=true
		def startedAt = new Date()
		String methodName='processAssetEntityImport()'
		String batchStatusCode

		Project project 
		UserLogin userLogin

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
		def assetsList = new ArrayList()
		def assetEntityErrorList = []
		def existingAssetsList = new ArrayList()
		int assetCount
		Map data = [:]
		Long dtbId

		// Get Room and Rack counts for stats at the end
		def counts = [:]
		Map mfgModelMap = [:]

		try {			
			project = Project.get(projectId)
			userLogin = UserLogin.get(userLoginId)

			DataTransferBatch dataTransferBatch = processValidation(project, userLogin, AssetClass.DEVICE, batchId)
			if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

			batchStatusCode = dataTransferBatch.statusCode
			dtbId = dataTransferBatch.id
			
			boolean canCreateMfgAndModel = securityService.hasPermission(userLogin, 'NewModelsFromImport')

			// Fetch all of the common data shared by all of the import processes
			def now = new Date()
			data = loadBatchData(dataTransferBatch)
			if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

			def eavAttributeSet = data.eavAttributeSet
			def staffList = data.staffList
			List dataTransferValueRowList = data.dataTransferValueRowList
			assetCount = dataTransferValueRowList.size()

			initProgress(session, assetCount)

			def nullProps = GormUtil.getDomainPropertiesWithConstraint( AssetEntity, 'nullable', true )
			def blankProps = GormUtil.getDomainPropertiesWithConstraint( AssetEntity, 'blank', true )

			// Get a Device Type Map used to verify that device type are valid
			Map deviceTypeMap = getDeviceTypeMap()

			counts.room = Room.countByProject(project)
			counts.rack = Rack.countByProject(project)

			// 
			// Iterate over the rows
			//
			def dtvList

			for ( int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++ ) {
				now = new Date()
				if (dataTransferValueRow.mod(25) == 0) {
					log.info "Processing DEVICE ($dataTransferValueRow rows of ${assetCount+1}) for batch id $batchId"

					def hibernateSession = sessionFactory.getCurrentSession()
					hibernateSession.flush()
					hibernateSession.clear()

					// Re-fetch a few objects that we need around
					project = Project.get(projectId)
					userLogin = UserLogin.get(userLoginId)
					dataTransferBatch = DataTransferBatch.get(dtbId)

					//if (dataTransferValueRow > 10)
					//	throw new RuntimeException("Just wanted to bail")
				}

				def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
				def rowNum = rowId+1

				// Remove the previous domain objects
				if (dtvList)
					dtvList.clear()

				// Get all of the property values imported for a given row
				dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch,rowId)

				def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
				def isNewValidate = false
				def isFormatError = 0

				def assetEntity = assetEntityAttributeLoaderService.findAndValidateAsset( AssetEntity, assetEntityId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets)
				if (assetEntity == null)
					continue

				if ( assetEntity.id ) {
					existingAssetsList << assetEntity
				} else {
					isNewValidate = true
					// Initialize extra properties for new asset
				}

				// This will hold any of the source/target location, room and rack information
				def locRoomRack = [source: [:], target: [:] ]

				// Vars caught in the each loop below to be used to create mfg/model appropriately
				String mfgName, modelName, usize, deviceType

				// Iterate over the attributes to update the asset with
				dtvList.each {
					def attribName = it.eavAttribute.attributeCode

					// If trying to set to NULL - call the closure to update the property and move on
					if (it.importValue == "NULL") {
						// Set the property to NULL appropriately
						newVal = assetEntityAttributeLoaderService.setToNullOrBlank(assetEntity, attribName, it.importValue, nullProps, blankProps)
						if (newVal) {
							// Error messages are returned otherwise it updated
							warnings << "$newVal for row $rowNum, asset $assetEntity"
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
							break;
						case 'model':
							modelName = it.correctedValue ?: it.importValue
							break;
						case "assetType":
							/*
							if (assetEntity.model) { 
								//if model already exist considering model's asset type and ignoring imported asset type.
								assetEntity[attribName] = assetEntity.model.assetType
							} else {
								assetEntity[attribName] = it.correctedValue ?: it.importValue
							}
							//Storing imported asset type in EavAttributeOptions table if not exist
							assetEntityAttributeLoaderService.findOrCreateAssetType(it.importValue, true)
							*/
							deviceType = it.correctedValue ?: it.importValue
							break
						case "usize":
							usize = it.correctedValue ?: it.importValue
							break

						case "sourceChassis":
							appendBladeToChassis(project, assetEntity, it.importValue, true, warnings, rowNum)
							break

						case "targetChassis":
							appendBladeToChassis(project, assetEntity, it.importValue, false, warnings, rowNum)
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
							assetEntityAttributeLoaderService.setCommonProperties(project, assetEntity, it, rowNum, warnings, errorConflictCount)
					}

				}

				//
				// Process the Mfg / Model assignment by utilizing a cache of the various mfg/model names
				//
				mfgName = StringUtil.defaultIfEmpty(mfgName, '')
				modelName = StringUtil.defaultIfEmpty(modelName, '')
				deviceType = StringUtil.defaultIfEmpty(deviceType, '')
				String mmKey = "${mfgName}::${modelName}::${deviceType}::$usize::${isNewValidate ? 'new' : 'existing'}"
				def mfg, model

				if (mfgModelMap.containsKey(mmKey)) {
					// We've already processed this mfg/model/type/usize/new|existing combination before so work from the cache
					mfgModelMap[mmKey].refCount++
					if (! mfgModelMap.errorMsg) {
						assetEntity.manufacturer = mfgModelMap[mmKey].mfg
						assetEntity.model = mfgModelMap[mmKey].model
					}
				} else {
					// We got a new combination so we have to do the more expensive lookup and possibly create mfg and model if user has perms
					Map results = assetEntityAttributeLoaderService.assignMfgAndModelToDevice(userLogin, assetEntity, mfgName, modelName, deviceType, deviceTypeMap, usize, canCreateMfgAndModel)
					mfgModelMap[mmKey] = [
						errorMsg: results.errorMsg, 
						warningMsg: results.warningMsg, 
						mfg: assetEntity.manufacturer, 
						model: assetEntity.model, 
						refCount: 1
					]
				}

				if (mfgModelMap.errorMsg) {
					warnings << "ERROR: $assetName (row $rowNum) - ${mfgModelMap.errorMsg}"
				}
				if (mfgModelMap.warningMsg) {
					warnings << "WARNING: $assetName (row $rowNum) - ${mfgModelMap.warningMsg}"
				}

				// Assign the Source/Target Location/Room/Rack properties for the asset
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
						if (assetEntity.isaBlade()) {
							def chassis = assetEntity["${disposition}Chassis"]
							if (chassis) {
								def roomProp = ((disposition == 'source')? 'roomSource' : 'roomTarget')
								if (chassis[roomProp]) {
									def room = chassis[roomProp]
									if (!room.roomName.equals(d["${disposition}Room"])) {
										validChassisRoom = false
										warnings << "Chassis room and device room don't match (row $rowNum)"
									}
								}
							}
						}

						if (validChassisRoom) {
							errors = deviceService.assignDeviceToLocationRoomRack(
								assetEntity, 
								d["${disposition}Location"],
								d["${disposition}Room"],
								d["${disposition}Rack"],
								(disposition == 'source') )
						}
						if (errors) {
							warnings << "Unable to set $disposition Loc/Room/Rack (row $rowNum) : $errors"
						}
					}
				}

				// log.debug "$methodName asset $assetEntity ${assetEntity.sourceLocation}/${assetEntity.sourceRoom}/${assetEntity.sourceRack}"

				// Save the asset if it was changed or is new
				(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
					assetEntity, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

				updateProgress(session, dataTransferValueRow)

				// Update status and clear hibernate session
				// TODO : JPM : Need to re-enable the clear Hibernate Session 
				// assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, null)

				if (performance) log.debug "$methodName Updated/Adding DEVICE() took ${TimeUtil.elapsed(now)}"

			} // for loop
				
			dataTransferBatch.statusCode = STATUS_COMPLETE
			if ( ! dataTransferBatch.save(flush:true)) {
				log.error "$methodName unable to update DataTransferBatch ${GormUtil.allErrorsString(dataTransferBatch)}"
				throw new DomainUpdateException("Unable to update the transfer batch status to COMPLETED")
			}
			batchStatusCode = dataTransferBatch.statusCode
			
			// Update assets racks, cabling data once process done
			// TODO : JPM 9/2014 : updateCablingOfAssets was commented out until we figure out what to do with this function (see TM-3308)
			// assetEntityService.updateCablingOfAssets( modelAssetsList )
	
			// Update Room and Rack counts for stats
			counts.room = Room.countByProject(project) - counts.room
			counts.rack = Rack.countByProject(project) - counts.rack

		} catch (Exception e) {
			insertCount = 0
			updateCount = 0
			counts.room = 0
			counts.rack = 0
			log.error "$methodName " + e.getMessage()
			log.error ExceptionUtil.stackTraceToString(e,80)

			throw new RuntimeException(e.getMessage())
		}
		// END OF TRY

		def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets

		def sb = new StringBuilder(
			"<b>Process Results for Batch ${batchId}:</b><ul>" + 
			"<li>Assets in Batch: ${data.assetsInBatch}</li>" + 
			"<li>Records Inserted: ${insertCount}</li>"+
			"<li>Records Updated: ${updateCount}</li>" + 
			"<li>Rooms Created: ${counts.room}</li>" + 
			"<li>Racks Created: ${counts.rack}</li>" + 
			"<li>Asset Errors: ${errorCount} </li> "+
			"<li>Attribute Errors: ${errorConflictCount}</li>" +
			"<li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li>" + 
			"</ul>"
		)

		if ( warnings || missingMfgModel || ignoredAssets) {
			sb.append("<b>Warnings:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (missingMfgModel) 
				appendIssueList(sb, 'Unable to assign Mfg/Model to device', missingMfgModel)

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}


		def elapsedTime = TimeUtil.elapsed(startedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"
		sb.append("<br>Elapsed time to process batch: $elapsedTime")

		return [elapsedTime: elapsedTime.toString(), batchStatus: batchStatusCode, info: sb.toString()] 
	}


	/**	
	 * This process will iterate over the assets imported into the specified batch and update the Application appropriately
	 * @param projectId - the id of the project that the user is logged into and the batch is associated with
	 * @param userLoginID - the id of user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param session - the controller session that invoked this method (used to update session variables used for progress updates)
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 */
	Map processApplicationImport(Long projectId, Long userLoginId, String batchId, Object session, tzId) {

		// Flag if we want performance information throughout the method
		boolean performance=true
		def startedAt = new Date()

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
		def existingAssetsList = new ArrayList()
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def application
		int assetCount
		String batchStatusCode
		Long dtbId

		try {

			//
			// Load initial data for method
			//
			Project project = Project.get(projectId)
			UserLogin userLogin = UserLogin.get(userLoginId)

			DataTransferBatch dataTransferBatch = processValidation(project, userLogin, AssetClass.APPLICATION, batchId)
			if (performance) log.debug "processValidation() took ${TimeUtil.elapsed(startedAt)}"

			batchStatusCode = dataTransferBatch.statusCode
			dtbId = dataTransferBatch.id

			// Fetch all of the common data shared by all of the import processes
			def now = new Date()
			Map data = loadBatchData(dataTransferBatch)
			if (performance) log.debug "loadBatchData() took ${TimeUtil.elapsed(now)}"

			def eavAttributeSet = data.eavAttributeSet
			def staffList = data.staffList
			List dataTransferValueRowList = data.dataTransferValueRowList
			assetCount = dataTransferValueRowList.size()

			initProgress(session, assetCount)

			if (log.isDebugEnabled()) {
				def fubar = new StringBuilder("Staff List\n")
				staffList.each { fubar.append( "   $it.id $it\n") }
				log.debug fubar.toString()
			}

			def nullProps = GormUtil.getDomainPropertiesWithConstraint( Application, 'nullable', true )
			def blankProps = GormUtil.getDomainPropertiesWithConstraint( Application, 'blank', true )
			def dtvList

			//
			// Main loop that will iterate over the row ids from the import batch
			//
			for ( int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++ ) {

				def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
				def rowNum = rowId+1

				if (dataTransferValueRow.mod(25) == 0) {
					log.info "Processing APPLICATION ($dataTransferValueRow rows of ${assetCount+1}) for batch id $batchId"

					def hibernateSession = sessionFactory.getCurrentSession()
					hibernateSession.flush()
					hibernateSession.clear()

					// Re-fetch a few objects that we need around that get funky after the session flush/clear
					project = Project.get(projectId)
					userLogin = UserLogin.get(userLoginId)
					dataTransferBatch = DataTransferBatch.get(dtbId)
				}

				// Release Hibernate objects when we can
				if (dtvList)
					dtvList.clear()

				// Get all of the property values imported for a given row					
				dtvList = DataTransferValue.findAllByDataTransferBatchAndRowId(dataTransferBatch, rowId)

				def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
				def flag = 0
				def isNewValidate = true
				def isFormatError = 0

				application = assetEntityAttributeLoaderService.findAndValidateAsset(Application, assetEntityId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets)

				if (application == null)
					continue

				if ( ! application.id ) {
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

					switch (attribName) {
						case ~/sme|sme2|appOwner/:
							if( it.importValue ) {
								// Substitute owner for appOwner
								def propName = attribName 
								def results = personService.findOrCreatePerson(it.importValue, project, staffList)
								def warnMsg = ''
								if (results?.person) {
									application[propName] = results.person

									// Now check for warnings
									if (results.isAmbiguous) {
										warnMsg = " $attribName (${it.importValue}) was ambiguous for App ${application.assetName} on row $rowNum. Name set to ${results.person}"
										warnings << warnMsg
										log.warn warnMsg
										errorConflictCount++
									}

									if (results.isNew) 
										personsAdded++

								} else if ( results?.error ) {
									warnMsg = "$attribName (${it.importValue}) had an error '${results.error}'' for App ${application.assetName} on row $rowNum"
									warnings << warnMsg
									log.info warnMsg
									errorConflictCount++
								}
							}
							break
						case ~/shutdownBy|startupBy|testingBy/:
							if (it.importValue.size()) {
								if(it.importValue[0] in ['@', '#']){
									application[attribName] = it.importValue
								} else {
									def resultMap = personService.findOrCreatePerson(it.importValue, project, staffList)
									application[attribName] = resultMap?.person?.id
									if(it.importValue && resultMap?.isAmbiguous){
										def warnMsg = "Ambiguity in ${attribName} (${it.importValue}) for ${application.assetName}"
										log.warn warnMsg
										warnings << warnMsg
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
							assetEntityAttributeLoaderService.setCommonProperties(project, application, it, rowNum, warnings, errorConflictCount)

					} // switch(attribName)

				}	// dtvList.each						

				// Save the asset if it was changed or is new
				(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
					application, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

				updateProgress(session, rowNum)
				
				// Update status and clear hibernate session
				// assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, session)

			} // for
			
			dataTransferBatch.statusCode = 'COMPLETED'
			if (! dataTransferBatch.save(flush:true) ) {
				log.error "$methodName unable to update DataTransferBatch ${GormUtil.allErrorsString(dataTransferBatch)}"
				throw new DomainUpdateException("Unable to update the transfer batch status to COMPLETED")
			}
			batchStatusCode = dataTransferBatch.statusCode
				
		} catch (Exception e) {
			insertCount = 0
			updateCount = 0
			log.error "$methodName " + e.getMessage()
			log.error ExceptionUtil.stackTraceToString(e,80)

			throw new RuntimeException(e.getMessage())			
		}

		def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets

		def sb = new StringBuilder(
			"<b>Process Results for Batch ${batchId}:</b><ul>" +
			"<li>Assets in Batch: ${assetCount}</li>" + 
			"<li>Records Inserted: ${insertCount}</li>"+
			"<li>Records Updated: ${updateCount}</li>" + 
			"<li>Asset Errors: ${errorCount}</li> "+
			"<li>Persons Added: $personsAdded</li>" +
			"<li>Attribute Errors: ${errorConflictCount}</li>" +
			"<li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> "
		)

		if (warnings || ignoredAssets) {
			sb.append("<b>Warning:</b><ul>")

			if (warnings)
				sb.append(WebUtil.getListAsli(warnings))

			if (ignoredAssets)
				appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
		}

		def elapsedTime = TimeUtil.elapsed(startedAt).toString()
		log.info "$methodName Import process of $assetCount assets took $elapsedTime"

		sb.append("<br>Process batch took $elapsedTime to complete")

		return [elapsedTime: elapsedTime, batchStatusCode: batchStatusCode, info: sb.toString()] 
	}	

	/**
	 * Used to append a list of ignored assets if any to a StringBuilder buffer
	 * @param StringBuilder the message buffer
	 * @param title - the title of the UL
	 * @param List<AssetEntity> list of ignored assets
	 */
	private void appendIgnoredAssets(StringBuilder sb, List assets ) {
		int count = assets.size()
		if (count) {
			String title = "${count} asset${count != 1 ? 's where' : ' was'} skipped due to having been updated since the export was done:"
			sb.append("<li>$title<ul>")
			assets.each { sb.append("<li>${it.id} ${it.assetName}</li>") }
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
	private void appendIssueList(StringBuilder sb, String title, List issues ) {
		if (issues.size()) {
			sb.append("<li>$title<ul>")
			issues.each { sb.append("<li>${it}</li>") }
			sb.append('</ul></li>')
		}
		sb.append('</ul></li>')
	}

	void appendBladeToChassis(project, assetEntity, importValue, isSource, warnings, rowNum) {
		def result = false
		if (!StringUtils.isBlank(importValue) && assetEntity.isaBlade()) {
			def chassisType = isSource?'source':'target'
			if (importValue.startsWith("id:")) {
				// Proccess chassis id
				def chassisKey = null
				def chassisName = null
				// Check if there is a chassis name in the cell too
				if (importValue.indexOf(' ') > 0) {
					chassisKey = importValue.substring(0, importValue.indexOf(' '))
					chassisName = importValue.substring(importValue.indexOf(' ') + 1, importValue.size())
				} else {
					chassisKey = importValue
				}
				def chassisId = chassisKey.substring(3, chassisKey.size())
				def chassis = AssetEntity.get(chassisId.toLong())
				if (chassis) {
					// Validates that the chassis name match with the chassis found
					if (chassisName != null && (!chassisName.equals(chassis.assetName))) {
						warnings << "Chassis $chassisType with id $chassisId don't have name $chassisName (row $rowNum)"
					}
					assetEntityService.assignBladeToChassis(project, assetEntity, chassis.id.toString(), isSource)
					result = true
				} else {
					warnings << "No chassis $chassisType found with id $chassisId (row $rowNum)"
				}

			} else {
				// Proccess chassis name
				def chassis = AssetEntity.findAllByAssetName(importValue)
				if (chassis.size() > 0) {
					// Check if we found more than one chassis
					if (chassis.size() > 1) {
						warnings << "Non-unique blade chassis name ($importValue) was referenced"
					} else  {
						def sChassis = chassis[0]
						assetEntityService.assignBladeToChassis(project, assetEntity, sChassis.id.toString(), isSource)	
						result = true
					}				
				} else {
					warnings << "No chassis $chassisType found with name $importValue (row $rowNum)"
				}
			}
		}
	}

}