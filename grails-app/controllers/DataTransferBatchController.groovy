import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavAttribute
import grails.converters.JSON
import org.jsecurity.SecurityUtils
class DataTransferBatchController {
    def sessionFactory
    def assetEntityAttributeLoaderService
    def jdbcTemplate
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
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
    def process = {
    	sessionFactory.getCurrentSession().flush();
    	sessionFactory.getCurrentSession().clear();
    	session.setAttribute("TOTAL_BATCH_ASSETS",0)
    	session.setAttribute("TOTAL_PROCESSES_ASSETS",0)
    	def assetEntityErrorList = []
    	def projectId
    	DataTransferBatch.withTransaction { status ->
    		projectId = params.projectId
    		def projectInstance = Project.findById( projectId )
    		def dataTransferBatch
    		def insertCount = 0
    		def errorConflictCount = 0
    		def updateCount = 0
    		def errorCount = 0
    		def batchRecords = 0
    		try{
    			dataTransferBatch = DataTransferBatch.get(params.batchId)
    			if(dataTransferBatch){
    				batchRecords = DataTransferValue.executeQuery("select count( distinct rowId  ) from DataTransferValue where dataTransferBatch = $dataTransferBatch.id ")[0]
    				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = "+
    															"$dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
    				def assetsSize = dataTransferValueRowList.size()
    				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
    				for( int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow++ ) {
    					def rowId = dataTransferValueRowList[dataTransferValueRow].rowId
    					def dtvList = DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
    					def assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
    					def flag = 0
    					def isModified = "false"
    					def isNewValidate
    					def isFormatError = 0
		    			def assetEntity
		    			if( assetEntityId ) {
		    				assetEntity = AssetEntity.get(assetEntityId)
							errorCount += assetEntity ? 0 : 1 
							if(!assetEntity){
								def obj = dataTransferValueRowList[dataTransferValueRow]
								obj.hasError = 1
								obj.errorText = "Unknown AssetID"
								obj.save(flush:true)
								dataTransferBatch.hasErrors = 1
							}
		    				if ( assetEntity && dataTransferBatch.dataTransferSet.id == 1 ) {
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
		    			} else {
		    				assetEntity = new AssetEntity()
		    				assetEntity.attributeSet = EavAttributeSet.findById(1)
		    				isNewValidate = "true"
		    			}
		    		
    					if( assetEntity && flag == 0 ) {
    						assetEntity.project = projectInstance
    						assetEntity.owner = projectInstance.client
    						dtvList.each {
    							def attribName = it.eavAttribute.attributeCode
    							//sourceteam and targetTeam assignment to assetEntity
    							if( attribName == "sourceTeam" || attribName == "targetTeam" ) {
    								def bundleInstance = assetEntity.moveBundle 
    								def teamInstance
    								teamInstance = assetEntityAttributeLoaderService.getdtvTeam(it, bundleInstance ) 
    								if( assetEntity."$attribName" != teamInstance || isNewValidate == "true" ) {
    									isModified = "true"
    									assetEntity."$attribName" = teamInstance
    								}
    							} else if ( attribName == "moveBundle" ) {
    								def moveBundleInstance
				    				moveBundleInstance = assetEntityAttributeLoaderService.getdtvMoveBundle(it, projectInstance ) 
				    				if( assetEntity."$attribName" != moveBundleInstance || isNewValidate == "true" ) {
    									isModified = "true"
    									assetEntity."$attribName" = moveBundleInstance 
    								}
    							} else if ( attribName == "manufacturer" ) {
									def manufacturerInstance = assetEntityAttributeLoaderService.getdtvManufacturer( it ) 
				    				if( assetEntity."$attribName" != manufacturerInstance || isNewValidate == "true" ) {
    									isModified = "true"
    									assetEntity."$attribName" = manufacturerInstance 
    								}
    							} else if ( attribName == "model" ) {
    								def modelInstance
    								modelInstance = assetEntityAttributeLoaderService.getdtvModel(it, dtvList) 
				    				if( assetEntity."$attribName" != modelInstance || isNewValidate == "true" ) {
    									isModified = "true"
    									assetEntity."$attribName" = modelInstance 
    								}
    							} else if( it.eavAttribute.backendType == "int"){
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
												assetEntity."$attribName" = assetEntity?.id ? "TDS${assetEntity?.id}" :"TDS${projectId}${rowId+1}"
											} else {
												assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
											}
	    								}
    								} catch ( Exception ex ) {
    									errorConflictCount+=1
    									it.hasError = 1
    									it.errorText = "Asset Tag should not be blank"
    									it.save()
    									dataTransferBatch.hasErrors = 1
    									isFormatError = 1
    								}
    							}
    						}
    						if ( isFormatError != 1 ) {
    							if( isModified == "true" ) {
        							if ( isNewValidate == "true" ) {
        								if( assetEntity.save() ) {
        									insertCount+=1
        								}
        							} else {
        								if( assetEntity.save() ) {
        									updateCount+=1
        								}
        							}
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
    				dataTransferBatch.save()
    			}
    		}catch (Exception e) {
    			status.setRollbackOnly()
				flash.message = "Import Batch process failed"
    		}
    		flash.message = " Process Results:<ul><li>	Assets in Batch: ${batchRecords} <li>Records Inserted: ${insertCount}</li>"+
    							"<li>Records Updated: ${updateCount}</li><li>Asset Errors: ${errorCount} </li> "+
    							"<li>Attribute Errors: ${errorConflictCount}</li></ul> " 
    	}
    	/* update assets racks once process done */
		updateAssetRacks()
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
        	if( it.attribute_code == "sourceTeam" || it.attribute_code == "targetTeam") {
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
	def updateAssetRacks(){
    	def sourceRackQuery = "select project_id as `project.id`, source_location as location, source_room as room, source_rack as tag, 1 as source " +
							"from asset_entity where asset_type <> 'Blade' and source_rack != '' and source_rack is not null and " +
							"project_id is not null and rack_source_id is null group by source_location, source_rack, source_room"

		def targetRackQuery = "select project_id as `project.id`, target_location as location, target_room as room, target_rack as tag, 0 as source " +
							"from asset_entity where asset_type <> 'Blade' and target_rack != '' and target_rack is not null and " +
							"project_id is not null and rack_target_id is null group by target_location, target_rack, target_room"
	
		def sourceRacks = jdbcTemplate.queryForList(sourceRackQuery)
		def targetRacks = jdbcTemplate.queryForList(targetRackQuery)
	
		(sourceRacks + targetRacks).each { rackFields ->
			def rack = Rack.findOrCreateWhere(rackFields)
			
			// Update all assets with this rack info to point to this rack
			if(rack.id == null)
				println "Unable to create rack: ${rack.errors}"
			else {
				def source = rack.source ? 'source' : 'target'
				
				def updateQuery = "update asset_entity set rack_${source}_id='${rack.id}' where project_id='${rack.project.id}' AND rack_${source}_id is null AND "
				if(rackFields.location == null)
					updateQuery += "${source}_location is null AND "
				else
					updateQuery += "${source}_location=\"${rackFields.location}\" AND "
				
				if(rackFields.room == null)
					updateQuery += "${source}_room is null AND "
				else
					updateQuery += "${source}_room=\"${rackFields.room}\" AND "
				updateQuery += "${source}_rack=\"${rackFields.tag}\""
				
				def updated = jdbcTemplate.update(updateQuery)
				println "Updated ${source} rack to ${rack.id} for ${updated} assets"
			}
		}
    }
}
