import grails.converters.JSON

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import java.util.regex.Matcher
// import org.apache.shiro.SecurityUtils

import com.tds.asset.Application
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.WebUtil

class DataTransferBatchController {

	// Objects to be injected
	def sessionFactory
	def assetEntityService
	def assetEntityAttributeLoaderService
	def controllerService
	def importService
	def jdbcTemplate
	def securityService
	def partyRelationshipService
	def personService

	def messageSource

	// Data used within some of the controller methods static vars bundleMoveAndClientTeams and bundleTeamRoles
	// TODO : JPM 9/2014 : Need to remove the 
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static bundleTeamRoles = ['sourceTeamMt':'MOVE_TECH','targetTeamMt':'MOVE_TECH',
		'sourceTeamLog':'CLEANER','targetTeamLog':'CLEANER',
		'sourceTeamSa':'SYS_ADMIN','targetTeamSa':'SYS_ADMIN',
		'sourceTeamDba':'DB_ADMIN','targetTeamDba':'DB_ADMIN'
	]

	protected static formatter = new SimpleDateFormat("M-d-yyyy")

	/**
	 * The default index page loads the list page
	 */
	def index = { redirect(action:list, params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [save:'POST', update:'POST']


	/**
	 * Return list of dataTransferBatchs for associated Project and Mode = Import
	 * @param projectId
	 * @return dataTransferBatchList
	 */
	def list = {
		if(params.message){
			flash.message = params.message
		}
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view Manage Batches"
			redirect(controller:'project',action:'list')
			return
		}
		if( !params.max ) params.max = 10
		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( project, "I", 
			[sort:"dateCreated", order:"desc", max:params.max,offset:params.offset ? params.offset : 0] )
		
		def isMSIE = false
		def userAgent = request.getHeader("User-Agent")
		if (userAgent.contains("MSIE") || userAgent.contains("Firefox"))
			isMSIE = true
		return [ dataTransferBatchList:dataTransferBatchList, projectId:projectId, isMSIE:isMSIE ]
	}

// TODO : JPM : 10/2013 - remove this function after done testing
// Working on an improved version of the GormUtil function to get human readable error messages
	def public static allErrorsString = { domain, separator= " : " ->
		def text = new StringBuilder()
		def first = true
		domain.errors.allErrors.each { 
			println message(error: it)
//			text.append( (first ? '' : separator) + messageSource.getMessage(it, null) ) 
			text.append( (first ? '' : separator) + message(error:it) ) 
		}
		
		text.toString()
	}

	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for DEVICES
	 * @param dataTransferBach
	 * @return process the dataTransferBatch and return to datatransferBatchList
	 */
	def deviceProcess = {
		String message
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def userLogin = securityService.getUserLogin()
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ

		try {
			message = importService.processDeviceImport(project, userLogin, params.id, session, tzId)
			log.info "deviceProcess() for batchId ${params.id} by user $userLogin\n " + message
			render ServiceResults.success([results:message]) as JSON
		} catch (Exception e) {
			message = 'An error occurred while processing the import. Please contact support for assistance.'
			if (log.isDebugEnabled()) {
				message = "$message : ${e.getMessage()}"
			}
			log.error "deviceProcess() failed : ${e.getMessage()} : userLogin $userLogin : batchId ${params.batchId}"
			log.error ExceptionUtil.stackTraceToString(e)
			render ServiceResults.errors(message) as JSON
		}
	}
	
	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for Applications
	 * @param dataTransferBach
	 * @return process the dataTransferBatch and return to datatransferBatchList
	 */
	def appProcess = {
		session.setAttribute("TOTAL_BATCH_ASSETS",0)
		session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		// session.getAttribute(null)

		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project
		def nullProps = GormUtil.getDomainPropertiesWithConstraint( Application, 'nullable', true )
		def blankProps = GormUtil.getDomainPropertiesWithConstraint( Application, 'blank', true )
		def newVal
		def warnings = []
		def ignoredAssets = []
		def personMap = []
		
		DataTransferBatch.withTransaction { status ->
			project = securityService.getUserCurrentProject()			
			def staffList = partyRelationshipService.getAllCompaniesStaffPersons(project.client)
			def dataTransferBatch
			def insertCount = 0
			def personsAdded = 0
			def errorConflictCount = 0
			def updateCount = 0
			def errorCount = 0
			def batchRecords = 0
			def unknowAssetIds = 0
			def unknowAssets = ""
			def modelAssetsList = new ArrayList()
			def existingAssetsList = new ArrayList()
			def application
		
			def fubar = new StringBuilder("Staff List\n")
			staffList.each { fubar.append( "   $it.id $it\n") }
			log.debug fubar.toString()

			try {
				dataTransferBatch = DataTransferBatch.findByIdAndProject(params.batchId, project)
				if (! dataTransferBatch) 
					throw new RuntimeException('Unable to find the batch number for your current project')

				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = ${dataTransferBatch.id}")[0]
				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
					"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
				def assetsSize = dataTransferValueRowList.size()
				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
				def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
				def eavAttributeSet = EavAttributeSet.findById(2)
				
				for ( int dataTransferValueRow=0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
					def rowNum = rowId+1
					def dtvList = dataTransferValues.findAll{ it.rowId == rowId } 
					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
					def flag = 0
					def isNewValidate = true
					def isFormatError = 0

					application = assetEntityAttributeLoaderService.findAndValidateAsset(Application, assetEntityId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets)

					if (application == null)
						continue

					if ( ! application.id ) {
						// Initialize extra properties for new asset
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

					// Update status and clear hibernate session
					assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, session)

				} // for
				
				dataTransferBatch.statusCode = 'COMPLETED'
				dataTransferBatch.save(flush:true)
					
			} catch (Exception e) {
				status.setRollbackOnly()
				insertCount = 0
				updateCount = 0
				log.error "appProcess() Unexpected error - rolling back : " + e.getMessage()
				e.printStackTrace()
				warnings << "Encounted unexpected error: ${e.getMessage()}"
				warnings << "<b>The Import was NOT processed</b>"			}

			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets

			def sb = new StringBuilder(
				"Process Results for Batch ${params.batchId}:<ul><li>Assets in Batch: ${batchRecords}</li>" + 
				"<li>Records Inserted: ${insertCount}</li>"+
				"<li>Records Updated: ${updateCount}</li>" + 
				"<li>Asset Errors: ${errorCount}</li> "+
				"<li>Persons Added: $personsAdded</li>" +
				"<li>Attribute Errors: ${errorConflictCount}</li>" +
				"<li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> " +
				"<b>Warning:</b><ul>" + 
				WebUtil.getListAsli(warnings)
			)

			importService.appendIgnoredAssets(sb, ignoredAssets)
			sb.append('</ul>')
			sb = sb.toString() 

			log.info sb
			flash.message = sb
		}

		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:project.id] )	
	
	}

	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for Files (aka Storage)
	 * @param dataTransferBach
	 * @return process the dataTransferBatch and return to datatransferBatchList
	 */
	def fileProcess ={
		session.setAttribute("TOTAL_BATCH_ASSETS",0)
		session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		// session.getAttribute(null)

		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project
		def nullProps = GormUtil.getDomainPropertiesWithConstraint( Files, 'nullable', true )
		def blankProps = GormUtil.getDomainPropertiesWithConstraint( Files, 'blank', true )
		def newVal
		def warnings = []
		def ignoredAssets = []
		
		DataTransferBatch.withTransaction { status ->
			project = securityService.getUserCurrentProject()			
			def dataTransferBatch
			def insertCount = 0
			def errorConflictCount = 0
			def updateCount = 0
			def errorCount = 0
			def batchRecords = 0
			def unknowAssetIds = 0
			def unknowAssets = ""
			def existingAssetsList = []
			def files

			try {
				dataTransferBatch = DataTransferBatch.findByIdAndProject(params.batchId, project)
				if (! dataTransferBatch) 
					throw new RuntimeException('Unable to find the batch number for your current project')

				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
					"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
				def assetsSize = dataTransferValueRowList.size()
				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
				def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
				def eavAttributeSet = EavAttributeSet.findById(4)
				
				for( int dataTransferValueRow=0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
					def rowNum = rowId+1
					def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
					def flag = 0
					def isNewValidate
					def isFormatError

					files = assetEntityAttributeLoaderService.findAndValidateAsset(Files, assetEntityId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets)

					if (files == null)
						continue

					if ( ! files.id ) {
						// Initialize extra properties for new asset
						files.scale = SizeScale.GB
					}

					dtvList.each {
						def attribName = it.eavAttribute.attributeCode

						// If trying to set to NULL - call the closure to update the property and move on
						if (it.importValue == "NULL") {
							// Set the property to NULL appropriately
							assetEntityAttributeLoaderService.setToNullOrBlank(files, attribName, it.importValue, nullProps, blankProps)
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
								assetEntityAttributeLoaderService.setCommonProperties(project, files, it, rowNum, warnings, errorConflictCount)

						}

					} // dtvList.each

					// Save the asset if it was changed or is new
					(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
						files, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

					// Update status and clear hibernate session
					assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, session)

				} // for

				dataTransferBatch.statusCode = 'COMPLETED'
				dataTransferBatch.save(flush:true)

			} catch (Exception e) {
				status.setRollbackOnly()
				insertCount = 0
				updateCount = 0
				log.error "filesProcess() Unexpected error - rolling back : " + e.getMessage()
				e.printStackTrace()
				warnings << "Encounted unexpected error: ${e.getMessage()}"
				warnings << "<b>The Import was NOT processed</b>"			}
		
			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets

			def sb = new StringBuilder("Process Results for Batch ${params.batchId}:<ul><li>Assets in Batch: ${batchRecords}</li>" + 
				"<li>Records Inserted: ${insertCount}</li>"+
				"<li>Records Updated: ${updateCount}</li>" + 
				"<li>Asset Errors: ${errorCount} </li> "+
				"<li>Attribute Errors: ${errorConflictCount}</li>" + 
				"<li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li>" + 
				"</ul><b>Warnings:</b><ul>" + 
				WebUtil.getListAsli(warnings)
			)

			importService.appendIgnoredAssets(sb, ignoredAssets)

			sb.append('</ul>')
			sb = sb.toString() 

			log.info sb
			flash.message = sb
		}

		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[ projectId:project.id ] )
		
	}

	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for Databases
	 * @param dataTransferBach
	 * @return process the dataTransferBatch and return to datatransferBatchList
	 */	
	def dbProcess = {
		session.setAttribute("TOTAL_BATCH_ASSETS",0)
		session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		// session.getAttribute(null)

		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project
		def nullProps = GormUtil.getDomainPropertiesWithConstraint( Database, 'nullable', true )
		def blankProps = GormUtil.getDomainPropertiesWithConstraint( Database, 'blank', true )
		def newVal
		def warnings = []
		def ignoredAssets = []
		
		DataTransferBatch.withTransaction { status ->
			project = securityService.getUserCurrentProject()			
			log.info('Starting batch process for of Databases for project: ' + project)
			def staffList = partyRelationshipService.getAllCompaniesStaffPersons(project.client)
			
			def dataTransferBatch
			def insertCount = 0
			def errorConflictCount = 0
			def updateCount = 0
			def errorCount = 0
			def batchRecords = 0
			def unknowAssetIds = 0
			def unknowAssets = ""
			def modelAssetsList = new ArrayList()
			def existingAssetsList = new ArrayList()
			def database

			try {
				dataTransferBatch = DataTransferBatch.findByIdAndProject(params.batchId, project)
				if (! dataTransferBatch) 
					throw new RuntimeException('Unable to find the batch number for your current project')

				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
				def dataTransferValueRowList = DataTransferValue.findAll("From DataTransferValue d where d.dataTransferBatch = "+
					"$dataTransferBatch.id and d.dataTransferBatch.statusCode='PENDING' group by rowId")
				def assetsSize = dataTransferValueRowList.size()
				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
				def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
				def eavAttributeSet = EavAttributeSet.findById(3)
				log.info("Process batch ${dataTransferBatch}, assets count: ${assetsSize}")

				for ( int dataTransferValueRow=0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
					def rowNum = rowId+1
					def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
					def flag = 0
					def isFormatError = 0

					database = assetEntityAttributeLoaderService.findAndValidateAsset(Database, assetEntityId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets)

					if (database == null)
						continue

					if ( ! database.id ) {
						// Initialize extra properties for new asset
					}

					dtvList.each {
						def attribName = it.eavAttribute.attributeCode

						// If trying to set to NULL - call the closure to update the property and move on
						if (it.importValue == "NULL") {
							// Set the property to NULL appropriately
							assetEntityAttributeLoaderService.attribNameOrBlank(database, attribName, it.importValue, nullProps, blankProps)

							return
						}

						switch (attribName) {
							// case ?:

							default:
								// Try processing all common properties
								assetEntityAttributeLoaderService.setCommonProperties(project, database, it, rowNum, warnings, errorConflictCount)

						} // switch

					} // dtvList.each

					// Save the asset if it was changed or is new
					(insertCount, updateCount, errorCount) = assetEntityAttributeLoaderService.saveAssetChanges(
						database, assetsList, rowNum, insertCount, updateCount, errorCount, warnings)

					// Update status and clear hibernate session
					assetEntityAttributeLoaderService.updateStatusAndClear(project, dataTransferValueRow, sessionFactory, session)

				} // for

				dataTransferBatch.statusCode = 'COMPLETED'
				dataTransferBatch.save(flush:true)

			} catch (Exception e) {
				status.setRollbackOnly()
				insertCount = 0
				updateCount = 0
				log.error "dbProcess() Unexpected error - rolling back : " + e.getMessage()
				e.printStackTrace()
				warnings << "Encounted unexpected error: ${e.getMessage()}"
				warnings << "<b>The Import was NOT processed</b>"			}

			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets

			def sb = new StringBuilder(" Process Results for Batch ${params.batchId}:<ul><li>Assets in Batch: ${batchRecords}" + 
				"<li>Records Inserted: ${insertCount}</li>" +
				"<li>Records Updated: ${updateCount}</li>" + 
				"<li>Asset Errors: ${errorCount}</li>"+
				"<li>Attribute Errors: ${errorConflictCount}</li>" + 
				"<li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li>" +
				"</ul><b>Warnings:</b><ul>" + 
				WebUtil.getListAsli(warnings)
			)

			importService.appendIgnoredAssets(sb, ignoredAssets)
			sb.append('</ul>')
			sb = sb.toString() 
			
			log.info sb
			flash.message = sb
		
		}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:project.id] )
		
	}
	
	/**
	 * Called by the Asset Post page via Ajax in order to show progress of how many assets have been posted
	 * 	@param  : processed and total assts from session 
	 *	@return : processed data for Batch progress bar
	 */
	def getProgress = {
		def progressData = []
		def total = session.getAttribute("TOTAL_BATCH_ASSETS") 
		def processed = session.getAttribute("TOTAL_PROCESSES_ASSETS")
		progressData << [processed:processed, total:total]
		render progressData as JSON
	 }
	
	/* --------------------------------------
	 * 	@author : Mallikarjun
	 *	@return : data transfer batch error list
	 * -------------------------------------- */
	def errorsListView = {
		def dataTransferBatchInstance = DataTransferBatch.get( params.id )
		def query = new StringBuffer(" select d.asset_entity_id,d.import_value,d.row_id,a.attribute_code,d.error_text FROM data_transfer_value d ")
		query.append(" left join eav_attribute a on (d.eav_attribute_id = a.attribute_id) where d.data_transfer_batch_id = ${dataTransferBatchInstance.id} ")
		query.append(" and has_error = 1 ")
		def dataTransferErrorList = jdbcTemplate.queryForList( query.toString() )
		def completeDataTransferErrorList = []
		def currentValues
		dataTransferErrorList.each {
			def assetNameId = EavAttribute.findByAttributeCode("assetName")?.id
			def assetTagId = EavAttribute.findByAttributeCode("assetTag")?.id
			def assetName = DataTransferValue.find("from DataTransferValue where rowId=$it.row_id and eavAttribute=$assetNameId "+
													"and dataTransferBatch=$dataTransferBatchInstance.id")?.importValue
			def assetTag = DataTransferValue.find("from DataTransferValue where rowId=$it.row_id and eavAttribute=$assetTagId "+
													"and dataTransferBatch=$dataTransferBatchInstance.id")?.importValue
			def assetEntity = AssetEntity.find("from AssetEntity where id=${it.asset_entity_id}")
			if( bundleMoveAndClientTeams.contains(it.attribute_code) ) {
				currentValues = assetEntity?.(it.attribute_code).name
			} else {
				currentValues = assetEntity?.(it.attribute_code)
			}
			completeDataTransferErrorList << ["assetName":assetName, "assetTag":assetTag, "attribute":it.attribute_code, "error":it.error_text,  
											  "currentValue":currentValues, "importValue":it.import_value]
		}
		completeDataTransferErrorList.sort{it.assetTag + it.attribute}
		return [ completeDataTransferErrorList : completeDataTransferErrorList ]
	 }

	/**
	 * Update Asset Racks once import batch process done.
	 */
/*	 
	def updateAssetRacks = {
		def assetsList = session.getAttribute("IMPORT_ASSETS")
		assetsList.each { assetId ->
			AssetEntity.get(assetId)?.updateRacks()
		}
		session.setAttribute("IMPORT_ASSETS",null)
		render ""
	}
*/
	/**
	 *     Delete the Data Transfer Batch Instance
	 */
	def delete = {
		try{
			def dataTransferBatchInstance = DataTransferBatch.get(params.batchId)
			if(dataTransferBatchInstance) {
				dataTransferBatchInstance.delete(flush:true,failOnError:true)
				flash.message = "DataTransferBatch ${params.batchId} deleted"
				redirect(action:list)
			}else {
				flash.message = "DataTransferBatch not found with id ${params.batchId}"
				redirect(action:list)
		   }
		}catch(Exception e){
			   e.printStackTrace()
		}
	}
	
	/**
	 * This action used to review batch and find error in excel import if any 
	 * @param : id- data transfer batch id
	 * @return map containing error message if any and import permission  (NewModelsFromImport)
	 */
	def reviewBatch = {
		String errorMsg = ''
		boolean importPerm = true

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this, 'NewModelsFromImport')
		if (!project) {
			errorMsg = flash.message
			flash.message = null
			importPerm = false
		} else {
			Long batchId = NumberUtil.toLong(params.id)
			if (batchId) {
				errorMsg = importService.reviewImportBatch(project.id, userLogin.id, batchId, session)
			} else {
				log.error "reviewBatch() called with invalid batch id ($batchId) by user $userLogin while assigned to project $project"
				errorMsg = "The batch id was missing or an invalid value"
			}
		}

		def returnMap = [errorMsg : errorMsg, importPerm:importPerm]
		render returnMap as JSON
	}

}