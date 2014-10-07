//import org.apache.commons.lang.math.NumberUtils
//import org.apache.commons.lang.StringUtils
//import org.codehaus.groovy.grails.commons.GrailsClassUtils
//import com.tds.asset.AssetCableMap
//import com.tdssrc.eav.EavAttribute
//import com.tdssrc.eav.EavAttributeOption
//import com.tdssrc.eav.EavEntityAttribute
//import com.tdssrc.eav.EavEntityType
//import com.tdsops.tm.enums.domain.SizeScale
//import com.tdsops.tm.enums.domain.AssetCableStatus
//import com.tdssrc.grails.DateUtil

import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttributeSet
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil

import java.util.regex.Matcher

class ImportService {

	boolean transactional = true
	
	def assetEntityAttributeLoaderService
	def assetEntityService
	def deviceService
	def rackService
	def roomService
	def securityService
	def partyRelationshipService
	
	static final STATUS_COMPLETE = 'COMPLETED'
	static final STATUS_PENDING = 'PENDING'
	// The number of assets to process before clearing the hibernate session 
	static final int CLEAR_SESSION_AFTER = 100

	/** 
	 * Used by the process methods to peform the common validation checks before processing
	 * @param project - the project that the user is logged into and the batch is associated with
	 * @param userLogin - the user login object of whom invoked the process
	 * @param assetClass - the asset class type
	 * @param batchId - the id number of the batch to be processed
	 */
	private DataTransferBatch processValidation(Project project, UserLogin userLogin, AssetClass assetClass, String batchId) {
		String warn = "SECURITY : User $userLogin attempted to invoke processServerImport"

		if (! securityService.hasPermission(userLogin, 'Import')) {
			log.warn "$warn without permission, project:$project, batchId:$batchId"
			throw new UnauthorizedException('User account does not have permission to process asset imports')
		}

		if (! (batchId && batchId.isLong()) ) {
			log.warn "$warn with invalid batchId ($batchId)"
			throw new InvalidParamException("Invalid batch id number received")
		}

		def dataTransferBatch = DataTransferBatch.findByIdAndProject(batchId, project)
		if (! dataTransferBatch) {
			log.warn "$warn with invalid batchId $batchId, project:$project"
			throw new RuntimeException('Unable to find the batch the specified batch')
		}

		if ( dataTransferBatch.eavEntityType?.domainName != AssetClass.domainNameFor(assetClass)) {
			throw new InvalidParamException('Specified batch is not for asset classs $assetClass')
		}

		if ( dataTransferBatch.statusCode == STATUS_COMPLETE ) {
			log.warm "$warn for previously processed batch $batchId, project:$project"
			throw new InvalidParamException('Specified batch has already been processed')
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
		data.dataTransferValues = DataTransferValue.findAllByDataTransferBatch(dtb)
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
	 * This process will iterate over the assets imported into the specified batch and update the devices appropriately
	 * @param project - the project that the user is logged into and the batch is associated with
	 * @param userLogin - the user login object of whom invoked the process
	 * @param batchId - the id number of the batch to be processed
	 * @param tzId - the timezone of the user whom is logged in to compute dates based on their TZ
	 */
	String processDeviceImport(Project project, UserLogin userLogin, String batchId, Object session, tzId) {
		DataTransferBatch dataTransferBatch = processValidation(project, userLogin, AssetClass.DEVICE, batchId)

		// Fetch all of the common data shared by all of the import processes
		Map data = loadBatchData(dataTransferBatch)
		def dataTransferValueRowList = data.dataTransferValueRowList
		def dataTransferValues = data.dataTransferValues
		def eavAttributeSet = data.eavAttributeSet
		def staffList = data.staffList

		def assetCount = dataTransferValueRowList.size()
		initProgress(session, assetCount)

		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def nullProps = GormUtil.getDomainPropertiesWithConstraint( AssetEntity, 'nullable', true )
		def blankProps = GormUtil.getDomainPropertiesWithConstraint( AssetEntity, 'blank', true )
		def newVal
		def warnings = []
		def ignoredAssets = []
		def insertCount = 0
		def errorConflictCount = 0
		def updateCount = 0
		def errorCount = 0
		def unknowAssetIds = 0
		def unknowAssets = ""
		def modelAssetsList = new ArrayList()
		def existingAssetsList = new ArrayList()

		// Get Room and Rack counts for stats at the end
		def counts = [:]
		counts.room = Room.countByProject(project)
		counts.rack = Rack.countByProject(project)

		try {			

			// 
			// Iterate over the rows
			//
			for ( int dataTransferValueRow=0; dataTransferValueRow < assetCount; dataTransferValueRow++ ) {
				def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
				def rowNum = rowId+1

				def dtvList = dataTransferValues.findAll{ it.rowId== rowId } //DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
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
						case "manufacturer":
							def manufacturerName = it.correctedValue ? it.correctedValue : it.importValue
							def manufacturerInstance = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName ) 
							if( assetEntity[attribName] != manufacturerInstance || isNewValidate ) {
								assetEntity[attribName] = manufacturerInstance 
							}
							break
						case "model":
							def modelInstance = assetEntityAttributeLoaderService.getdtvModel(it, dtvList, assetEntity) 
							if( assetEntity[attribName] != modelInstance || isNewValidate ) {
								assetEntity[attribName] = modelInstance 
								modelAssetsList.add(assetEntity)
							}
							break
						case "assetType":
							if(assetEntity.model){ 
								//if model already exist considering model's asset type and ignoring imported asset type.
								assetEntity[attribName] = assetEntity.model.assetType
							} else {
								assetEntity[attribName] = it.correctedValue ?: it.importValue
							}
							//Storing imported asset type in EavAttributeOptions table if not exist
							assetEntityAttributeLoaderService.findOrCreateAssetType(it.importValue, true)
							break
						case "usize":
							// Skip the insertion
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

				// Assign the Source/Target Location/Room/Rack properties for the asset
				def errors
				['source', 'target'].each { disposition ->
					def d = locRoomRack[disposition]
					if (d?.size()) {
						// Check to see if they are trying to clear the fields with the NULL setting
						if (d.values().contains('NULL')) {
							warnings << "NULL not supported for unsetting Loc/Room/Rack in $disposition (row $rowNum)"
							return
						}

						// Need to capitalize the disposition to form the property names correctly
						//disposition = disposition.capitalize()

						errors = deviceService.assignDeviceToLocationRoomRack(
							assetEntity, 
							d["${disposition}Location"],
							d["${disposition}Room"],
							d["${disposition}Rack"],
							(disposition == 'source') )
						if (errors) {
							warnings << "Unable to set $disposition Loc/Room/Rack (row $rowNum) : $errors"
						}
					}
				}

				log.debug "processServerImport() asset $assetEntity ${assetEntity.sourceLocation}/${assetEntity.sourceRoom}/${assetEntity.sourceRack}"

				// Save the asset if it was changed or is new
				(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
					assetEntity, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

				updateProgress(session, rowNum)

				// Update status and clear hibernate session
				// TODO : JPM : Need to re-enable the clear Hibernate Session 
				// assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, null)

			} // for loop
				
			dataTransferBatch.statusCode = STATUS_COMPLETE
			if (!dataTransferBatch.save(flush:true)) {
				GormUtil.allErrorsString(dataTransferBatch)
				throw new RuntimeException("Unable to update the transfer batch status to COMPLETED")
			}
			
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
			log.error "serverProcess() Unexpected error - rolling back : " + e.getMessage()
			log.error ExceptionUtil.stackTraceToString(e,80)

			warnings << "Encounted unexpected error: ${e.getMessage()}"
			warnings << "<b>The Import was NOT processed</b>"
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
			"</ul><b>Warnings:</b><ul>" + 
			WebUtil.getListAsli(warnings)
		)

		appendIgnoredAssets(sb, ignoredAssets)
		sb.append('</ul>')
		sb = sb.toString()

		return sb
	}

	/**
	 * Used to append a list of ignored assets if any to a StringBuilder buffer
	 * @param StringBuilder the message buffer
	 * @param List<Asset> list of ignored assets
	 */
	void appendIgnoredAssets(StringBuilder sb, List assets ) {
		if (assets.size()) {
			sb.append("<li>${assets.size()} assets where skipped due to being updated since export<ul>")
			assets.each { sb.append("<li>${it.id} ${it.assetName}</li>") }
			sb.append('</ul></li>')
		}
		sb.append('</ul></li>')
	}	

}