import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Application
import com.tds.asset.Files
import com.tds.asset.Database
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil
class DataTransferBatchController {
    def sessionFactory
    def assetEntityAttributeLoaderService
    def jdbcTemplate
	def securityService
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static bundleTeamRoles = ['sourceTeamMt':'MOVE_TECH','targetTeamMt':'MOVE_TECH',
										'sourceTeamLog':'CLEANER','targetTeamLog':'CLEANER',
										'sourceTeamSa':'SYS_ADMIN','targetTeamSa':'SYS_ADMIN',
										'sourceTeamDba':'DB_ADMIN','targetTeamDba':'DB_ADMIN'
										]
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [save:'POST', update:'POST']
    /* --------------------------------------------------------------------------
     * Return list of dataTransferBatchs for associated Project and Mode = Import
     * @param projectId
     * @author Lokanath
     * @return dataTransferBatchList
     * -------------------------------------------------------------------------- */
    def list = {
    	if(params.message){
    		flash.message = params.message
    	}
    	def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		
		def project = Project.findById( projectId )
		if( !params.max ) params.max = 10
		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( project, "I", 
				[sort:"dateCreated", order:"desc",max:params.max,offset:params.offset ? params.offset : 0] )
		return [ dataTransferBatchList:dataTransferBatchList, projectId:projectId ]
    }

    /* -----------------------------------------------------------------------
     * Process DataTransfervalues Corresponding to DataTransferBatch
     * @param dataTransferBach
     * @author Lokanath
     * @return process the dataTransferBatch and return to datatransferBatchList
     * -------------------------------------------------------------------------    */
    def serverProcess = {
    	sessionFactory.getCurrentSession().flush();
    	sessionFactory.getCurrentSession().clear();
    	session.setAttribute("TOTAL_BATCH_ASSETS",0)
    	session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		def formatter = new SimpleDateFormat("yyyy-MM-dd")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project

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
			def modelAssetsList = new ArrayList()
    		def existingAssetsList = new ArrayList()
    		try {
    			dataTransferBatch = DataTransferBatch.get(params.batchId)
				if (dataTransferBatch.eavEntityType?.domainName == "AssetEntity") {
    				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
    				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
    					"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
    				def assetsSize = dataTransferValueRowList.size()
    				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
					def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
					def eavAttributeSet = EavAttributeSet.findById(1)
					
    				for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
    					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
    					def dtvList = dataTransferValues.findAll{ it.rowId== rowId } //DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
    					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
    					def flag = 0
    					def isModified = "false"
    					def isNewValidate
    					def isFormatError = 0
		    			def assetEntity
		    			if( assetEntityId ) {
		    				assetEntity = AssetEntity.get(assetEntityId)
							if(assetEntity && assetEntity.project?.id == project.id ){
								existingAssetsList.add( assetEntity )
			    				if ( dataTransferBatch.dataTransferSet.id == 1 ) {
			    					def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, assetEntity, dtvList, project )
			    					flag = validateResultList[0]?.flag
			    					errorConflictCount += validateResultList[0]?.errorConflictCount 
			    					if( flag == 0 ) {
			    						isNewValidate = "false"
			    					}else {
			    						errorCount++
			    					}
			    				} else {
			    					flag = 0;
			    				}
							} else if(assetEntity && assetEntity.project?.id != project.id){
								assetEntity = new AssetEntity()
								assetEntity.attributeSet = eavAttributeSet
								isNewValidate = "true"
						    } 
		    			} else if(!assetEntityId) {
		    				assetEntity = new AssetEntity()
		    				assetEntity.attributeSet = eavAttributeSet
		    				isNewValidate = "true"
							log.info "serverProcess - creating new asset for rowId $rowId"
		    			}
		    		
    					if( assetEntity && flag == 0 ) {
    						assetEntity.project = project
    						assetEntity.owner = project.client
    						dtvList.each {
    							def attribName = it.eavAttribute.attributeCode
								switch(attribName){
									case "sourceTeamMt":
									case "targetTeamMt":
									case "sourceTeamLog":
									case "targetTeamLog":
									case "sourceTeamSa":
									case "targetTeamSa":
									case "sourceTeamDba":
									case "targetTeamDba":
										def bundleInstance = assetEntity.moveBundle 
	    								def teamInstance = assetEntityAttributeLoaderService.getdtvTeam(it, bundleInstance, bundleTeamRoles.get(attribName) ) 
	    								if( assetEntity."$attribName" != teamInstance || isNewValidate == "true" ) {
	    									isModified = "true"
	    									assetEntity."$attribName" = teamInstance
	    								}
										break;
									case "moveBundle":
	    								def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, project ) 
					    				if( assetEntity."$attribName" != moveBundleInstance || isNewValidate == "true" ) {
	    									isModified = "true"
	    									assetEntity."$attribName" = moveBundleInstance 
	    								}
										break;
									case "manufacturer":
										def manufacturerName = it.correctedValue ? it.correctedValue : it.importValue
	 									def manufacturerInstance = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName ) 
					    				if( assetEntity."$attribName" != manufacturerInstance || isNewValidate == "true" ) {
	    									isModified = "true"
	    									assetEntity."$attribName" = manufacturerInstance 
	    								}
										break;
									case "model":
										def modelInstance = assetEntityAttributeLoaderService.getdtvModel(it, dtvList, assetEntity) 
					    				if( assetEntity."$attribName" != modelInstance || isNewValidate == "true" ) {
	    									isModified = "true"
	    									assetEntity."$attribName" = modelInstance 
											modelAssetsList.add(assetEntity)
	    								}
										break;
									case "assetType":
										if(assetEntity.model){ //if model already exist considering model's asset type and ignoring imported asset type.
											assetEntity."$attribName" = assetEntity.model.assetType
										} else {
	    									assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
	    								}
										//Storing imported asset type in EavAttributeOptions table if not exist
										assetEntityAttributeLoaderService.findOrCreateAssetType(it.importValue, true)
										break;
									case "validation":
										if(!it.importValue){
											assetEntity."$attribName" = 'Discovery'
										} else {
											assetEntity."$attribName" = it.importValue
										}
										break;
									case "planStatus":
										if(!it.importValue){
											assetEntity."$attribName" = 'Unassigned'
										} else {
											assetEntity."$attribName" = it.importValue
										}
										break;
									case "retireDate":
										if(it.importValue){
											def retireDate = it.importValue
												isModified = "true"
												assetEntity."$attribName" = GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
										}
										break;
									case "maintExpDate":
										if(it.importValue){
											def maintExpDate = it.importValue
												isModified = "true"
												assetEntity."$attribName" = GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
											 }
										break;
									case "usize":
										// Skip the insertion
										break;
									
									default:
										if( it.eavAttribute.backendType == "int"){
		    								def correctedPos
		    								try {
		    									if( it.correctedValue ) {
		    										correctedPos = Integer.parseInt(it.correctedValue.trim())
		    									} else if( it.importValue ) {
		    										correctedPos = Integer.parseInt(it.importValue.trim())
		    									}
		    									//correctedPos = it.correctedValue
												if( assetEntity."$attribName" != correctedPos || isNewValidate == "true" ) {
													isModified = "true"
													assetEntity."$attribName" = correctedPos 
		    	        						}
		    								} catch ( Exception ex ) {
												log.error "serverProcess - unexpected exception 1 - " + ex.toString()
		    									errorConflictCount++
		    									it.hasError = 1
		    									it.errorText = "format error"
		    									it.save()
		    									dataTransferBatch.hasErrors = 1
		    									isFormatError = 1
		    								}
		    							} else {
		    								try{
			    								if( ( ( it.correctedValue == null || assetEntity."$attribName" != it.correctedValue ) && assetEntity."$attribName" != it.importValue) || isNewValidate == "true"  ) {
			    									isModified = "true"
													if(("$attribName" == "assetTag" || "$attribName" == "assetName" ) && !it.importValue){
														if(assetEntity?.id){
															assetEntity."$attribName" = "TDS-${assetEntity?.id}"
														} else {
															def lastAssetId = project.lastAssetId
															if(!lastAssetId){
																lastAssetId = jdbcTemplate.queryForInt("select max(asset_entity_id) FROM asset_entity WHERE project_id = ${project.id}")
															}
															while(AssetEntity.findByAssetTagAndProject("TDS-${lastAssetId}",project)){
																lastAssetId++
															}
															assetEntity."$attribName" = "TDS-${lastAssetId}"
															project.lastAssetId = lastAssetId + 1
														}
													} else {
														assetEntity."$attribName" = it.correctedValue ?: it.importValue
													}
			    								}
		    								} catch ( Exception ex ) {
												log.error "serverProcess - unexpected exception 2 - " + ex.toString()
		    									errorConflictCount++
		    									it.hasError = 1
		    									it.errorText = "Asset Tag should not be blank"
		    									it.save()
		    									dataTransferBatch.hasErrors = 1
		    									isFormatError = 1
		    								}
		    							}
										break
								}
    						}
    						if ( isFormatError != 1 ) {
    							if( isModified == "true" ) {
        							if ( isNewValidate == "true" ) {
        								if( assetEntity.save(flush:true) ) {
        									insertCount++
        								}
        							} else {
        								if( assetEntity.save(flush:true) ) {
        									updateCount++
        								}
        							}
									if(assetEntity?.id)
										assetsList.add(assetEntity?.id)
    							}
    						} else {
    							errorCount++
								log.warn "serverProcess - performing discard for rowId $rowId"
    							assetEntity.discard()
    						}
    						if(dataTransferValueRow % 50 == 0) {
    							sessionFactory.getCurrentSession().flush();
    							sessionFactory.getCurrentSession().clear();
    						}
    						session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
    					}
    				} // for loop
  
    				def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = "+
    																	"$dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
    				if (dataTransferCommentRowList){
    					dataTransferCommentRowList.each{
    						def assetComment
    						def assetEntity = AssetEntity.get(it.assetId)
    						if(assetEntity){
    							def principal = SecurityUtils.subject.principal
    							def loginUser = UserLogin.findByUsername(principal)
    							if(it.commentId){
    								assetComment = AssetComment.findById(it.commentId)
    							} 
    							if(!assetComment){
    								assetComment = new AssetComment()
    								assetComment.mustVerify = 0
    							}
    							assetComment.comment = it.comment
    							assetComment.commentType = it.commentType
    							assetComment.createdBy = loginUser.person
    							assetComment.assetEntity = assetEntity
    							assetComment.save()
    						}
    					}
    				}
					
					sessionFactory.getCurrentSession().flush();
					sessionFactory.getCurrentSession().clear();
					project = project.merge()
					
					if(!project.save(flush:true)){
						println"Error while updating project.lastAssetId : ${project}"
						project.errors.each { println it }
					}
					
    				dataTransferBatch.statusCode = 'COMPLETED'
					if(!dataTransferBatch.save(flush:true)){
						dataTransferBatch.errors.allErrors.each { println it}
					}
					
					/* update assets racks, cabling data once process done */
					updateAssetsCabling( modelAssetsList, existingAssetsList )
    			}
			} catch (Exception e) {
				log.error "serverProcess - Unexpected error, rolling back - " + e.printStackTrace()
				insertCount = 0
				updateCount = 0
    			status.setRollbackOnly()
				flash.message = "Import Batch process failed"
    		}
 			// END OF TRY

   			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets
    		flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
				"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
				"<li>Attribute Errors: ${errorConflictCount}</li><li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> " 
    	}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:project.id, message:flash.message ] )
     }
	
	def appProcess ={
    	sessionFactory.getCurrentSession().flush();
    	sessionFactory.getCurrentSession().clear();
    	session.setAttribute("TOTAL_BATCH_ASSETS",0)
    	session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		session.getAttribute(null)
		def formatter = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
    	def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project
		
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
			def modelAssetsList = new ArrayList()
    		def existingAssetsList = new ArrayList()
			def application
    		try{
    			dataTransferBatch = DataTransferBatch.get(params.batchId)
    				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
    				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
    															"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
    				def assetsSize = dataTransferValueRowList.size()
    				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
					def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
					def eavAttributeSet = EavAttributeSet.findById(2)
					
    				for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
    					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
    					def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
    					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
    					def flag = 0
    					def isModified = "false"
    					def isNewValidate
    					def isFormatError = 0
		    			def assetEntity
		    			if( assetEntityId ) {
		    				application = Application.get(assetEntityId)
							if(application && application.project?.id == project.id ){
								existingAssetsList.add( application )
			    				if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
			    					def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, application, dtvList, project )
			    					flag = validateResultList[0]?.flag
			    					errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount 
			    					if( flag == 0 ) {
			    						isNewValidate = "false"
			    					}else {
			    						errorCount++
			    					}
			    				} else {
			    					flag = 0;
			    				}
							} else if(application && application.project?.id != project.id){
								application = new Application()
			    				application.attributeSet = eavAttributeSet
			    				isNewValidate = "true"
							}
		    			} else if(!assetEntityId){
		    				application = new Application()
		    				application.attributeSet = eavAttributeSet
		    				isNewValidate = "true"
		    			}
		    		
    					if( application && flag == 0 ) {
    						application.project = project
    						dtvList.each {
    							def attribName = it.eavAttribute.attributeCode
									switch(attribName){
										case "moveBundle":
											def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, project )
												isModified = "true"
												application."$attribName" = moveBundleInstance
											break;
										case "retireDate":
										if(it.importValue){
											def retireDate = it.importValue
												isModified = "true"
												application."$attribName" = GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
										}
											break;
										case "maintExpDate":
										if(it.importValue){
											def maintExpDate = it.importValue
												isModified = "true"
												application."$attribName" = GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
										     }
											break;
										case "owner":
											   application."$attribName" = application.owner
											break;
										case "validation":
												if(!it.importValue){
													application."$attribName" = "Discovery"
												}else{
											        application."$attribName" = it.importValue
												}
												break;
										case "planStatus":
												if(!it.importValue){
													application."$attribName" = 'Unassigned'
												} else {
													application."$attribName" = it.importValue
												}
												break;
										case "sme":
												if(it.importValue){
													def person = assetEntityAttributeLoaderService.findOrCreatePerson(it.importValue, true)
													application."$attribName" = person
												} 
												break;
										case "sme2":
												if(it.importValue){
													def person = assetEntityAttributeLoaderService.findOrCreatePerson(it.importValue, true)
													application."$attribName" = person
												} 
												break;
										case "appOwner":
												if(it.importValue){
													def person = assetEntityAttributeLoaderService.findOrCreatePerson(it.importValue, true)
													application."$attribName" = person
												} 
												break;
										default:
										if( it.eavAttribute.backendType == "int"){
		    								def correctedPos
		    								try {
		    									if( it.correctedValue ) {
		    										correctedPos = Integer.parseInt(it.correctedValue.trim())
		    									} else if( it.importValue ) {
		    										correctedPos = Integer.parseInt(it.importValue.trim())
		    									}
		    									//correctedPos = it.correctedValue
												if( application."$attribName" != correctedPos || isNewValidate == "true" ) {
													isModified = "true"
													application."$attribName" = correctedPos 
		    	        						}
		    								} catch ( Exception ex ) {
												log.error "appProcess - unexpected exception 1 - " + ex.toString()
		    									errorConflictCount++
		    									it.hasError = 1
		    									it.errorText = "format error"
		    									it.save()
		    									dataTransferBatch.hasErrors = 1
		    									isFormatError = 1
		    								}
		    							} else {
		    								try{
												application."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
		    								} catch ( Exception ex ) {
												log.error "appProcess - unexpected exception 2 - " + ex.toString()
		    									errorConflictCount++
		    									it.hasError = 1
		    									it.save()
		    									dataTransferBatch.hasErrors = 1
		    									isFormatError = 1
		    								}
		    							}
								}
    						}
    						}
    						if ( isFormatError != 1 ) {
    							if( isModified == "true" ) {
									application.assetType='Application'
        							if ( isNewValidate == "true" ) {
        								if( application.save() ) {
                                   											
        									insertCount++
        								}
        							} else {
        								if( application.save() ) {
        									updateCount++
        								}
        							}
									if(application?.id)
										assetsList.add(assetEntity?.id)
    							}
    						} else {
    							errorCount++
    							application.discard()
    						}
    						if(dataTransferValueRow % 50 == 0) {
    							sessionFactory.getCurrentSession().flush();
    							sessionFactory.getCurrentSession().clear();
    						}
    						session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
    					}
    				def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = "+
    																	"$dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
    				if(dataTransferCommentRowList){
    					dataTransferCommentRowList.each{
    						def assetComment
    						def assetEntity = AssetEntity.get(it.assetId)
    						if(assetEntity){
    							def principal = SecurityUtils.subject.principal
    							def loginUser = UserLogin.findByUsername(principal)
    							if(it.commentId){
    								assetComment = AssetComment.findById(it.commentId)
    							} 
    							if(!assetComment){
    								assetComment = new AssetComment()
    								assetComment.mustVerify = 0
    							}
    							assetComment.comment = it.comment
    							assetComment.commentType = it.commentType
    							assetComment.createdBy = loginUser.person
    							assetComment.assetEntity = assetEntity
    							assetComment.save()
    						}
    					}
    				
						}
					
    				dataTransferBatch.statusCode = 'COMPLETED'
    				dataTransferBatch.save(flush:true)
					
    		}catch (Exception e) {
				log.error "appProcess - Unexpected error, rolling back - " + e.printStackTrace()
				status.setRollbackOnly()
				e.printStackTrace()
				flash.message = "Import Batch process failed"
    		}
    		def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets
    		flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
    							"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
    							"<li>Attribute Errors: ${errorConflictCount}</li><li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> " 
    	}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:project.id, message:flash.message ] )
		
	}
	
	def fileProcess ={
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		session.setAttribute("TOTAL_BATCH_ASSETS",0)
		session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		session.getAttribute(null)
		def formatter = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project
		
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
			def modelAssetsList = new ArrayList()
			def existingAssetsList = new ArrayList()
			def files
			try {
				dataTransferBatch = DataTransferBatch.get(params.batchId)
					batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
					def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
																"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
					def assetsSize = dataTransferValueRowList.size()
					session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
					def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
					def eavAttributeSet = EavAttributeSet.findById(4)
					
					for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
						def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
						def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
						def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
						def flag = 0
						def isModified = "false"
						def isNewValidate
						def isFormatError = 0
						def assetEntity
						if( assetEntityId ) {
							files = Files.get(assetEntityId)
							if(files && files.project?.id == project.id ){
								existingAssetsList.add( files )
								if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
									def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, files, dtvList, project )
									flag = validateResultList[0]?.flag
									errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount
									if( flag == 0 ) {
									}else {
										errorCount++
									}
								} else {
									flag = 0;
								}
							} else if(files && files.project?.id != project.id){
								files = new Files()
								files.attributeSet = eavAttributeSet
								isNewValidate = "true"
							}
						} else if(!assetEntityId){
							files = new Files()
							files.attributeSet = eavAttributeSet
							isNewValidate = "true"
						}
					
						if( files && flag == 0 ) {
							files.project = project
							files.sizeUnit = "GB"
							dtvList.each {
								def attribName = it.eavAttribute.attributeCode
									switch(attribName){
										case "moveBundle":
											def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, project )
												isModified = "true"
												files."$attribName" = moveBundleInstance
											break;
										case "retireDate":
										if(it.importValue){
											def retireDate = it.importValue
												isModified = "true"
												files."$attribName" = GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
										}
											break;
										case "maintExpDate":
										if(it.importValue){
											def maintExpDate = it.importValue
												isModified = "true"
												files."$attribName" = GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
											 }
											break;
										case "owner":
												files."$attribName" = application.owner
											break;
										case "fileSize":
											files."$attribName" = it.importValue ? Integer.parseInt(it.importValue) : 0
										    break;
										case "validation":
											if(!it.importValue){
												files."$attribName" = "Discovery"
											}else{
												files."$attribName" = it.importValue
											}
											break;
										case "planStatus":
											if(!it.importValue){
												files."$attribName" = 'Unassigned'
											} else {
												files."$attribName" = it.importValue
											}
											break;
										default:
										if( it.eavAttribute.backendType == "int"){
											def correctedPos
											try {
												if( it.correctedValue ) {
													correctedPos = Integer.parseInt(it.correctedValue.trim())
												} else if( it.importValue ) {
													correctedPos = Integer.parseInt(it.importValue.trim())
												}
												if( files."$attribName" != correctedPos || isNewValidate == "true" ) {
													isModified = "true"
													files."$attribName" = correctedPos
												}
											} catch ( Exception ex ) {
												log.error "fileProcess - unexpected exception 1 - " + ex.toString()
												errorConflictCount++
												it.hasError = 1
												it.errorText = "format error"
												it.save()
												dataTransferBatch.hasErrors = 1
												isFormatError = 1
											}
										} else {
											try{
												files."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
											} catch ( Exception ex ) {
												log.error "fileProcess - unexpected exception 2 - " + ex.toString()
												errorConflictCount++
												it.hasError = 1
												it.save()
												dataTransferBatch.hasErrors = 1
												isFormatError = 1
											}
										}
									}
								}
							}
							if ( isFormatError != 1 ) {
								if( isModified == "true" ) {
									files.assetType = 'Files'
									if ( isNewValidate == "true" ) {
										if( files.save() ) {
											
											insertCount++
										}
									} else {
										if( files.save() ) {
											updateCount++
										}
									}
									if(files?.id)
										assetsList.add(assetEntity?.id)
								}
							} else {
								errorCount++
								files.discard()
							}
							if(dataTransferValueRow % 50 == 0) {
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}
							session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
						}
					def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = "+
																		"$dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
					if(dataTransferCommentRowList){
						dataTransferCommentRowList.each{
							def assetComment
							def assetEntity = AssetEntity.get(it.assetId)
							if(assetEntity){
								def principal = SecurityUtils.subject.principal
								def loginUser = UserLogin.findByUsername(principal)
								if(it.commentId){
									assetComment = AssetComment.findById(it.commentId)
								}
								if(!assetComment){
									assetComment = new AssetComment()
									assetComment.mustVerify = 0
								}
								assetComment.comment = it.comment
								assetComment.commentType = it.commentType
								assetComment.createdBy = loginUser.person
								assetComment.assetEntity = assetEntity
								assetComment.save()
							}
						}
					
					}
					
					dataTransferBatch.statusCode = 'COMPLETED'
					dataTransferBatch.save(flush:true)
			}catch (Exception e) {
				log.error "fileProcess - Unexpected error, rolling back - " + e.printStackTrace()
				status.setRollbackOnly()
				e.printStackTrace()
				flash.message = "Import Batch process failed"
			}
			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets
			flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
								"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
								"<li>Attribute Errors: ${errorConflictCount}</li><li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> "
		}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[ projectId:project.id, message:flash.message ] )
		
	}
	
	def dbProcess={
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		session.setAttribute("TOTAL_BATCH_ASSETS",0)
		session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
		session.getAttribute(null)
		def formatter = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetEntityErrorList = []
		def assetsList = new ArrayList()
		def project

		DataTransferBatch.withTransaction { status ->
			project = securityService.getUserCurrentProject()			
			log.info('Starting batch process for project: ' + project)
			
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
			def dbInstance
			try {
				dataTransferBatch = DataTransferBatch.get(params.batchId)
				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
				def dataTransferValueRowList = DataTransferValue.findAll("From DataTransferValue d where d.dataTransferBatch = "+
					"$dataTransferBatch.id and d.dataTransferBatch.statusCode='PENDING' group by rowId")
				def assetsSize = dataTransferValueRowList.size()
				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
				def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
				def eavAttributeSet = EavAttributeSet.findById(3)
				log.info("Process batch ${dataTransferBatch}, assets count: ${assetsSize}")
				for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
					def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
					def flag = 0
					def isModified = "false"
					def isNewValidate
					def isFormatError = 0
					def assetEntity
					log.info("Processing rowId=${rowId}, assetId=${assetEntityId}")
					if( assetEntityId ) {
						dbInstance = Database.get(assetEntityId)
						if(dbInstance && dbInstance.project.id == project.id ){
							existingAssetsList.add( dbInstance )
							if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
								def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, dbInstance, dtvList, project )
								flag = validateResultList[0]?.flag
								errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount
								if( flag == 0 ) {
								}else {
									errorCount++
								}
							} else {
								flag = 0;
							}
						} else if(dbInstance && dbInstance.project?.id != project.id){
							dbInstance = new Database()
							dbInstance.attributeSet = eavAttributeSet
							isNewValidate = "true"
						}
					} else if(!assetEntityId || AssetEntity.get(assetEntityId).project.id != project.id){
						dbInstance = new Database()
						dbInstance.attributeSet = eavAttributeSet
						isNewValidate = "true"
					}
				
					if( dbInstance && flag == 0 ) {
						dbInstance.project = project
						//application.owner = project.client
						dtvList.each {
							def attribName = it.eavAttribute.attributeCode
								switch(attribName){
									case "moveBundle":
									if(it.importValue){
										def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, project)
										isModified = "true"
										dbInstance."$attribName" = moveBundleInstance
									}
										break;
									case "retireDate":
										if(it.importValue){
											    def retireDate = it.importValue
												isModified = "true"
												dbInstance."$attribName" = GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
										}
										break;
									case "maintExpDate":
										if(it.importValue){
											def maintExpDate = it.importValue
											isModified = "true"
											dbInstance."$attribName" = GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
										 }
										break;
									case "dbSize":
										    dbInstance."$attribName" = it.importValue ? Integer.parseInt(it.importValue) : 0
									    break;
									case "validation":
										if(!it.importValue){
											dbInstance."$attribName" = "Discovery"
										}else{
											dbInstance."$attribName" = it.importValue
										}
										break;
									case "planStatus":
										if(!it.importValue){
											dbInstance."$attribName" = 'Unassigned'
										} else {
											dbInstance."$attribName" = it.importValue
										}
										break;
									default:
									if( it.eavAttribute.backendType == "int"){
										def correctedPos
										try {
											if( it.correctedValue ) {
												correctedPos = Integer.parseInt(it.correctedValue.trim())
											} else if( it.importValue ) {
												correctedPos = Integer.parseInt(it.importValue.trim())
											}
											//correctedPos = it.correctedValue
											if( dbInstance."$attribName" != correctedPos || isNewValidate == "true" ) {
												isModified = "true"
												dbInstance."$attribName" = correctedPos
											}
										} catch ( Exception ex ) {
											log.error "dbProcess - unexpected exception 1 - " + ex.toString()
											errorConflictCount++
											it.hasError = 1
											it.errorText = "format error"
											it.save()
											dataTransferBatch.hasErrors = 1
											isFormatError = 1
										}
									} else {
										try{
											dbInstance."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
										} catch ( Exception ex ) {
											log.error "dbProcess - unexpected exception 2 - " + ex.toString()
											errorConflictCount++
											it.hasError = 1
											it.save()
											dataTransferBatch.hasErrors = 1
											isFormatError = 1
										}
									}
								}
							}
						}
						if ( isFormatError != 1 ) {
							if( isModified == "true" ) {
								if ( isNewValidate == "true" ) {
									dbInstance.assetType ='Database'
									if( dbInstance.save(flush:true) ) {
										insertCount++
									}
								} else {
									if( dbInstance.save() ) {
										updateCount++
									}
								}
								if(dbInstance?.id)
									assetsList.add(assetEntity?.id)
							}
						} else {
							errorCount++
							dbInstance.discard()
						}
						if(dataTransferValueRow % 50 == 0) {
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}
						session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
					}

					def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = "+
																	"$dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
				if(dataTransferCommentRowList){
					dataTransferCommentRowList.each{
						def assetComment
						def assetEntity = AssetEntity.get(it.assetId)
						if(assetEntity){
							def principal = SecurityUtils.subject.principal
							def loginUser = UserLogin.findByUsername(principal)
							if(it.commentId){
								assetComment = AssetComment.findById(it.commentId)
							}
							if(!assetComment){
								assetComment = new AssetComment()
								assetComment.mustVerify = 0
							}
							assetComment.comment = it.comment
							assetComment.commentType = it.commentType
							assetComment.createdBy = loginUser.person
							assetComment.assetEntity = assetEntity
							assetComment.save()
						}
					}
				}
				
				dataTransferBatch.statusCode = 'COMPLETED'
				dataTransferBatch.save(flush:true)
			} catch (Exception e) {
				log.error("dbProcess - Unexpected error, rolling back - ", e)
				status.setRollbackOnly()
				flash.message = "Import Batch process failed"
			}
			def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets
			flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
				"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
				"<li>Attribute Errors: ${errorConflictCount}</li><li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> "
		}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:project.id, message:flash.message ] )
		
	}
	
    /* --------------------------------------
     * 	@author : Lokanada Reddy
     * 	@param  : processed and total assts from session 
     *	@return : processed data for Batch progress bar
     * -------------------------------------- */
    def getProgress = {
    	def progressData = []
        def total = session.getAttribute("TOTAL_BATCH_ASSETS") 
        def processed = session.getAttribute("TOTAL_PROCESSES_ASSETS")
    	progressData << [processed:processed,total:total]
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

	/*=========================================================
	 * Update Asset Racks once import batch process done.
	 *========================================================*/
	def updateAssetRacks = {
		def assetsList = session.getAttribute("IMPORT_ASSETS")
		assetsList.each { assetId ->
			AssetEntity.get(assetId)?.updateRacks()
		}
		session.setAttribute("IMPORT_ASSETS",null)
		render ""
    }
    /*
     *  Update assets cabling data for selected list of assets 
     */
    def updateAssetsCabling( modelAssetsList, existingAssetsList ){
    	modelAssetsList.each{ assetEntity->
    		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
								toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
								where toAsset = ? """,[assetEntity])
			
			AssetCableMap.executeUpdate("delete from AssetCableMap where fromAsset = ?",[assetEntity])
			assetEntityAttributeLoaderService.createModelConnectors( assetEntity )
    	}
		existingAssetsList.each{ assetEntity->
			AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='${assetEntity.targetRack}',
					toAssetUposition=${assetEntity.targetRackPosition} where toAsset = ? """,[assetEntity])
		}
    }
    
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
		
		def dtBatch = DataTransferBatch.read(params.id)
		def errorMsg = ""
		def importPerm = RolePermissions.hasPermission("NewModelsFromImport")
		if(dtBatch){
			def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
				"$dtBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
			def assetsSize = dataTransferValueRowList.size()
			def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dtBatch )
			def eavAttributeSet = EavAttributeSet.findById(1)
			def assetIdList = []
			def project = securityService.getUserCurrentProject()
			def assetIds = AssetEntity.findAllByProject(project)?.id
			def dupAssetIds = []
			def notExistedIds = []
			for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
				def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
				def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
				
				def dtvList= dataTransferValues.findAll{ it.rowId == rowId  }
				if(dtBatch.eavEntityType?.domainName == "AssetEntity"){
					def importedModel 
					def importedManu 
					dtvList.each{
						def attribName = it.eavAttribute.attributeCode
						if(attribName == 'model')
							importedModel  = it.importValue
							
						if(attribName == 'manufacturer')
							importedManu  = it.importValue
					}
					// Verifying Model and manufacturer's pair in database if it's asset type is Server
					errorMsg += verifyModelAndManuPair(importedModel, importedManu)
				}
				
				// Checking for duplicate asset ids
				if(assetEntityId && assetIdList.contains(assetEntityId))
					dupAssetIds << assetEntityId
				
				// Checking for asset ids which does not exist in database
				if(assetEntityId && !assetIds.contains((Long)(assetEntityId)))
					notExistedIds << assetEntityId
					
				assetIdList << assetEntityId
			}
			if(dupAssetIds.size() > 0)
				errorMsg += "Duplicate assetIDs #$dupAssetIds  <br/>"
				
			if(notExistedIds.size() > 0)
				errorMsg += "No match found for assetIDs #$notExistedIds   <br/>"
		} else{
			errorMsg+=" ${params.id} does not exist for DataTransferBatch"
		}
		def returnMap = [errorMsg : errorMsg, importPerm:importPerm]
		render returnMap as JSON
	}
	
	/**
	 * To Verify Manufacturer and Model pair from database
	 * @param importedModel  : Imported Model from excel
	 * @param importedManu   : Imported Manufacturer from excel
	 * @return : if pair not found return error message
	 */
	def verifyModelAndManuPair(importedModel, importedManu){
		 
		def errorMsg = ''
		def manu = Manufacturer.findByName(importedManu)
		if( !manu ){
			manu = ManufacturerAlias.findByName( importedManu )?.manufacturer
		}
		def modelName = Model.findByModelName(importedModel)?.modelName
		if(!modelName){
			modelName = ModelAlias.findByNameAndManufacturer(importedModel,manu)?.model?.modelName
			if(!modelName)
				modelName = importedModel
		}
	    def pairExist = Model.findByModelNameAndManufacturer(modelName, manu)
		if(!pairExist && importedManu && importedModel)
		    errorMsg = "No match found for $importedManu / $importedModel <br/>"
			
		return errorMsg
	}
}

	