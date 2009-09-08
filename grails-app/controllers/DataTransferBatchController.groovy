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
    		def projectId = params.projectId
    		def projectInstance = Project.findById( projectId )
    		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( projectInstance, "I", [sort:"dateCreated", order:"desc"] )
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
    		try{
    			dataTransferBatch = DataTransferBatch.get(params.batchId)
    			if(dataTransferBatch){
    				def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = $dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
    				def assetsSize = dataTransferValueRowList.size()
    				session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
    				for(int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow ++) {
    					def rowId =dataTransferValueRowList[dataTransferValueRow].rowId
    					def dtvList = DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
    					def  assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
    					def flag = 0
    					def isNewValidate
    					def isFormatError = 0
		    			def assetEntity
		    			if( assetEntityId ) {
		    				assetEntity = AssetEntity.findById(assetEntityId)
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
    								if( it.correctedValue && bundleInstance ) {
    									teamInstance = projectTeam.findByTeamCodeAndMoveBundle(it.correctedValue,bundleInstance)
    									if(!teamInstance){
    										teamInstance = new ProjectTeam(teamCode:it.correctedValue,moveBundle:bundleInstance).save()
    									}
    								} else if( it.importValue && bundleInstance ) {
    									teamInstance = ProjectTeam.findByTeamCodeAndMoveBundle(it.importValue,bundleInstance)
    									if(!teamInstance){
    										teamInstance = new ProjectTeam( name:it.importValue, teamCode:it.importValue, 
			    														moveBundle:bundleInstance ).save()
    									}
    								}
    								assetEntity."$attribName" = teamInstance
    							} else if ( attribName == "moveBundle" ) {
    								def moveBundleInstance
				    				/*if( it.importValue != null && it.correctedValue != null ) {
				    					importMoveBundleInstance = MoveBundle.findByName(it.importValue)
				        				exportMoveBundleInstance = MoveBundle.findByName(it.correctedValue)
				        				assetEntity."$attribName" = exportMoveBundleInstance ? exportMoveBundleInstance : importMoveBundleInstance
				    				}*/
				    				if(it.correctedValue){
				    					moveBundleInstance = MoveBundle.findByNameAndProject(it.correctedValue,projectInstance)
				    					if(!moveBundleInstance){
				    						moveBundleInstance = new MoveBundle(name:it.correctedValue,project:projectInstance,operationalOrder:1).save()
				    					}
				    				} else if(it.importValue){
				    					moveBundleInstance = MoveBundle.findByNameAndProject(it.importValue,projectInstance)
				    					if(!moveBundleInstance){
				    						moveBundleInstance = new MoveBundle(name:it.importValue,project:projectInstance,operationalOrder:1).save()
				    					}
				    				}
				    				assetEntity."$attribName" = moveBundleInstance 
    							} else if( it.eavAttribute.backendType == "int" ){
    								def correctedPos
    								try {
    									if( it.correctedValue ) {
    										correctedPos = Integer.parseInt(it.correctedValue)
    									} else if( it.importValue ) {
    										correctedPos = Integer.parseInt(it.importValue)
    									}
    									//correctedPos = it.correctedValue
    									assetEntity."$attribName" = correctedPos 
    								} catch ( Exception ex ) {
    									assetEntityErrorList << " ${attribName} at row ${dataTransferValueRow+1}"
    									it.hasError = 1
    									it.errorText = "format error"
    									it.save()
    									dataTransferBatch.hasErrors = 1
    									isFormatError = 1
    								}
    							} else {
    								assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
    							}
    						}
    						if ( isFormatError != 1 ) {
    							assetEntity.save()
    							if ( isNewValidate == "true" ) {
    								insertCount+=1
    							} else {
    								updateCount+=1
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
    				def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = $dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
    				if(dataTransferCommentRowList){
    					dataTransferCommentRowList.each{
    						def assetComment
    						def assetEntity = AssetEntity.findById(it.assetId)
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
    		//def errorCount = DataTransferValue.countByDataTransferBatchAndHasError(dataTransferBatch, 1)
    		if ( dataTransferBatch.dataTransferSet.id == 1 ) {
    			flash.message = " ${insertCount} Records Inserted; ${updateCount} Records Updated; ${errorCount} Asset Errors;  ${errorConflictCount} Attribute Erros " 
    		}
    		if ( assetEntityErrorList ) {
    			if ( flash.message ) {
    				flash.message = flash.message + " and ${assetEntityErrorList} is invalid format not updated "
    			} else {
    				flash.message = " ${errorCount} Asset Errors; ${assetEntityErrorList} is invalid format not updated "
    			}
    		}
    	}
    	redirect ( action:list, params:[projectId:projectId] )
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
        		currentValues = assetEntity.(it.attribute_code).name
        	} else {
        		currentValues = assetEntity.(it.attribute_code)
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
}
