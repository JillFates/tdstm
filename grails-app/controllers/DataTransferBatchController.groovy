import grails.converters.JSON

import org.jsecurity.SecurityUtils

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
    	def projectId = params.projectId
		if(!projectId){
			projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ 
		}
		def projectInstance = Project.findById( projectId )
		if( !params.max ) params.max = 10
		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( projectInstance, "I", 
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
		def assetEntityErrorList = []
    	def projectId
		def assetsList = new ArrayList()
		DataTransferBatch.withTransaction { status ->
			projectId = params.projectId
			def projectInstance = Project.findById( projectId )
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
    		try{
    			dataTransferBatch = DataTransferBatch.get(params.batchId)
				if(dataTransferBatch.eavEntityType?.domainName == "AssetEntity"){
    				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
    				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
    															"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
    				def assetsSize = dataTransferValueRowList.size()
    				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
					def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
					def eavAttributeSet = EavAttributeSet.findById(1)
					
    				for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
    					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
    					def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
    					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
    					def flag = 0
    					def isModified = "false"
    					def isNewValidate
    					def isFormatError = 0
		    			def assetEntity
		    			if( assetEntityId && AssetEntity.get(assetEntityId).project.id == projectInstance.id) {
		    				assetEntity = AssetEntity.get(assetEntityId)
							if(assetEntity?.id != null && assetEntity.project.id == projectInstance.id ){
								existingAssetsList.add( assetEntity )
			    				if ( dataTransferBatch.dataTransferSet.id == 1 ) {
			    					def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, assetEntity, dtvList, projectInstance )
			    					flag = validateResultList[0]?.flag
			    					errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount 
			    					if( flag == 0 ) {
			    						isNewValidate = "false"
			    					}else {
			    						errorCount+=1
			    					}
			    				} else {
			    					flag = 0;
			    				}
							} 
		    			} else if(!assetEntityId || AssetEntity.get(assetEntityId).project.id != projectInstance.id) {
		    				assetEntity = new AssetEntity()
		    				assetEntity.attributeSet = eavAttributeSet
		    				isNewValidate = "true"
		    			}
		    		
    					if( assetEntity && flag == 0 ) {
    						assetEntity.project = projectInstance
    						assetEntity.owner = projectInstance.client
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
	    								def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, projectInstance ) 
					    				if( assetEntity."$attribName" != moveBundleInstance || isNewValidate == "true" ) {
	    									isModified = "true"
	    									assetEntity."$attribName" = moveBundleInstance 
	    								}
										break;
									case "manufacturer":
										def manufacturerInstance = assetEntityAttributeLoaderService.getdtvManufacturer( it ) 
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
										if(assetEntity.model){
											assetEntity."$attribName" = assetEntity.model.assetType
										} else {
	    									assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
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
												ex.printStackTrace()
		    									errorConflictCount+=1
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
															def lastAssetId = projectInstance.lastAssetId
															if(!lastAssetId){
																lastAssetId = jdbcTemplate.queryForInt("select max(asset_entity_id) FROM asset_entity WHERE project_id = ${projectInstance.id}")
															}
															while(AssetEntity.findByAssetTagAndProject("TDS-${lastAssetId}",projectInstance)){
																lastAssetId = lastAssetId+1
															}
															assetEntity."$attribName" = "TDS-${lastAssetId}"
															projectInstance.lastAssetId = lastAssetId + 1
														}
													} else {
														assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
													}
			    								}
		    								} catch ( Exception ex ) {
											ex.printStackTrace()
		    									errorConflictCount+=1
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
        									insertCount+=1
        								}
        							} else {
        								if( assetEntity.save(flush:true) ) {
        									updateCount+=1
        								}
        							}
									if(assetEntity?.id)
										assetsList.add(assetEntity?.id)
    							}
    						} else {
    							errorCount+=1
    							assetEntity.discard()
    						}
    						if(dataTransferValueRow % 50 == 0) {
    							sessionFactory.getCurrentSession().flush();
    							sessionFactory.getCurrentSession().clear();
    						}
    						session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
    					}
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
					
					if(!projectInstance.save(flush:true)){
						println"Error while updating project.lastAssetId : ${projectInstance}"
						projectInstance.errors.each { println it }
					}
					/* update assets racks, cabling data once process done */
					updateAssetsCabling( modelAssetsList, existingAssetsList )
    			}
				}catch (Exception e) {
    			status.setRollbackOnly()
				flash.message = "Import Batch process failed"
    		}
    		def assetIdErrorMess = unknowAssets ? "(${unknowAssets.substring(0,unknowAssets.length()-1)})" : unknowAssets
    		flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
    							"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
    							"<li>Attribute Errors: ${errorConflictCount}</li><li>AssetId Errors: ${unknowAssetIds}${assetIdErrorMess}</li></ul> " 
    	}
		session.setAttribute("IMPORT_ASSETS", assetsList)
		redirect ( action:list, params:[projectId:projectId, message:flash.message ] )
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
    	def projectId
		def assetsList = new ArrayList()
		DataTransferBatch.withTransaction { status ->
			projectId = params.projectId
			def projectInstance = Project.findById( projectId )
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
		    			if( assetEntityId && AssetEntity.get(assetEntityId).project.id == projectInstance.id ) {
		    				application = Application.get(assetEntityId)
							if(application?.id != null && application.project.id == projectInstance.id ){
								existingAssetsList.add( application )
			    				if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
			    					def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, application, dtvList, projectInstance )
			    					flag = validateResultList[0]?.flag
			    					errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount 
			    					if( flag == 0 ) {
			    						isNewValidate = "false"
			    					}else {
			    						errorCount+=1
			    					}
			    				} else {
			    					flag = 0;
			    				}
							} else {
								unknowAssetIds += 1
								unknowAssets += assetEntityId+"," 
							}
		    			} else if(!assetEntityId || AssetEntity.get(assetEntityId).project.id != projectInstance.id){
		    				application = new Application()
		    				application.attributeSet = eavAttributeSet
		    				isNewValidate = "true"
		    			}
		    		
    					if( application && flag == 0 ) {
    						application.project = projectInstance
    						dtvList.each {
    							def attribName = it.eavAttribute.attributeCode
									switch(attribName){
										case "moveBundle":
											def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, projectInstance )
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
														def validation = "Discovery"
														application."$attribName" = validation
												}else{
												        application."$attribName" = it.importValue
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
												println"=================================="
												ex.printStackTrace()
		    									errorConflictCount+=1
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
										    	ex.printStackTrace()
		    									errorConflictCount+=1
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
                                   											
        									insertCount+=1
        								}
        							} else {
        								if( application.save() ) {
        									updateCount+=1
        								}
        							}
									if(application?.id)
										assetsList.add(assetEntity?.id)
    							}
    						} else {
    							errorCount+=1
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
		redirect ( action:list, params:[projectId:projectId, message:flash.message ] )
     
		
		
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
		def projectId
		def assetsList = new ArrayList()
		DataTransferBatch.withTransaction { status ->
			projectId = params.projectId
			def projectInstance = Project.findById( projectId )
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
			try{
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
						if( assetEntityId && AssetEntity.get(assetEntityId).project.id == projectInstance.id ) {
							files = Files.get(assetEntityId)
							if(files?.id != null && files.project.id == projectInstance.id ){
								existingAssetsList.add( files )
								if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
									def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, files, dtvList, projectInstance )
									flag = validateResultList[0]?.flag
									errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount
									if( flag == 0 ) {
									}else {
										errorCount+=1
									}
								} else {
									flag = 0;
								}
							} else {
								unknowAssetIds += 1
								unknowAssets += assetEntityId+","
							}
						} else if(!assetEntityId || AssetEntity.get(assetEntityId).project.id != projectInstance.id){
							files = new Files()
							files.attributeSet = eavAttributeSet
							isNewValidate = "true"
						}
					
						if( files && flag == 0 ) {
							files.project = projectInstance
							dtvList.each {
								def attribName = it.eavAttribute.attributeCode
									switch(attribName){
										case "moveBundle":
											def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, projectInstance )
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
												ex.printStackTrace()
												errorConflictCount+=1
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
												ex.printStackTrace()
												errorConflictCount+=1
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
											
											insertCount+=1
										}
									} else {
										if( files.save() ) {
											updateCount+=1
										}
									}
									if(files?.id)
										assetsList.add(assetEntity?.id)
								}
							} else {
								errorCount+=1
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
		redirect ( action:list, params:[projectId:projectId, message:flash.message ] )
	 
		
		
		
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
		def projectId
		def assetsList = new ArrayList()
		DataTransferBatch.withTransaction { status ->
			projectId = params.projectId
			def projectInstance = Project.findById( projectId )
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
			try{
				dataTransferBatch = DataTransferBatch.get(params.batchId)
					batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
					def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
																"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
					def assetsSize = dataTransferValueRowList.size()
					session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
					def dataTransferValues = DataTransferValue.findAllByDataTransferBatch( dataTransferBatch )
					def eavAttributeSet = EavAttributeSet.findById(3)
					
					for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
						def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
						def dtvList = dataTransferValues.findAll{ it.rowId== rowId }//DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
						def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
						def flag = 0
						def isModified = "false"
						def isNewValidate
						def isFormatError = 0
						def assetEntity
						if( assetEntityId && AssetEntity.get(assetEntityId).project.id == projectInstance.id ) {
							dbInstance = Database.get(assetEntityId)
							if(dbInstance?.id != null && dbInstance.project.id == projectInstance.id ){
								existingAssetsList.add( dbInstance )
								if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
									def validateResultList = assetEntityAttributeLoaderService.importValidation( dataTransferBatch, dbInstance, dtvList, projectInstance )
									flag = validateResultList[0]?.flag
									errorConflictCount = errorConflictCount+validateResultList[0]?.errorConflictCount
									if( flag == 0 ) {
									}else {
										errorCount+=1
									}
								} else {
									flag = 0;
								}
							} else {
								unknowAssetIds += 1
								unknowAssets += assetEntityId+","
							}
						} else if(!assetEntityId || AssetEntity.get(assetEntityId).project.id != projectInstance.id){
							dbInstance = new Database()
							dbInstance.attributeSet = eavAttributeSet
							isNewValidate = "true"
						}
					
						if( dbInstance && flag == 0 ) {
							dbInstance.project = projectInstance
							//application.owner = projectInstance.client
							dtvList.each {
								def attribName = it.eavAttribute.attributeCode
									switch(attribName){
										case "moveBundle":
										if(it.importValue){
											def moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, projectInstance )
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
												println"=================================="
												ex.printStackTrace()
												errorConflictCount+=1
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
												ex.printStackTrace()
												errorConflictCount+=1
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
											insertCount+=1
										}
									} else {
										if( dbInstance.save() ) {
											updateCount+=1
										}
									}
									if(dbInstance?.id)
										assetsList.add(assetEntity?.id)
								}
							} else {
								errorCount+=1
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
			}catch (Exception e) {
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
		redirect ( action:list, params:[projectId:projectId, message:flash.message ] )
	 
		
		
		
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
    	progressData<<[processed:processed,total:total]
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
    /*
    def show = {
        def dataTransferBatchInstance = DataTransferBatch.get( params.id )

        if(!dataTransferBatchInstance) {
            flash.message = "DataTransferBatch not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ dataTransferBatchInstance : dataTransferBatchInstance ] }
    }

    def delete = {
        def dataTransferBatchInstance = DataTransferBatch.get( params.id )
        if(dataTransferBatchInstance) {
            dataTransferBatchInstance.delete()
            flash.message = "DataTransferBatch ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "DataTransferBatch not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def dataTransferBatchInstance = DataTransferBatch.get( params.id )

        if(!dataTransferBatchInstance) {
            flash.message = "DataTransferBatch not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ dataTransferBatchInstance : dataTransferBatchInstance ]
        }
    }

    def update = {
        def dataTransferBatchInstance = DataTransferBatch.get( params.id )
        if(dataTransferBatchInstance) {
            dataTransferBatchInstance.properties = params
            if(!dataTransferBatchInstance.hasErrors() && dataTransferBatchInstance.save()) {
                flash.message = "DataTransferBatch ${params.id} updated"
                redirect(action:show,id:dataTransferBatchInstance.id)
            }
            else {
                render(view:'edit',model:[dataTransferBatchInstance:dataTransferBatchInstance])
            }
        }
        else {
            flash.message = "DataTransferBatch not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def dataTransferBatchInstance = new DataTransferBatch()
        dataTransferBatchInstance.properties = params
        return ['dataTransferBatchInstance':dataTransferBatchInstance]
    }

    def save = {
        def dataTransferBatchInstance = new DataTransferBatch(params)
        if(!dataTransferBatchInstance.hasErrors() && dataTransferBatchInstance.save()) {
            flash.message = "DataTransferBatch ${dataTransferBatchInstance.id} created"
            redirect(action:show,id:dataTransferBatchInstance.id)
        }
        else {
            render(view:'create',model:[dataTransferBatchInstance:dataTransferBatchInstance])
        }
    }
    */
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
	/*
     *     Delete the Data Transfer Batch Instance
	 */
	
	def delete={
		try{
			def dataTransferBatchInstance = DataTransferBatch.get(params.batchId)
	        if(dataTransferBatchInstance) {
			    dataTransferBatchInstance.delete(flush:true,failOnError:true)
				// TODO to check why instance is not deleting ?
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
}

	