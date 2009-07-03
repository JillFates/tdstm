import com.tdssrc.eav.EavAttributeSet
import grails.converters.JSON
import org.jsecurity.SecurityUtils
class DataTransferBatchController {
    def sessionFactory
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
    		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( projectInstance, "I" );
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
    	DataTransferBatch.withTransaction { status ->
    	def projectId = params.projectId
    	def projectInstance = Project.findById( projectId )
    	try{
    		def dataTransferBatch = DataTransferBatch.get(params.batchId)
	    	if(dataTransferBatch){
		    	def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = $dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
		    	def assetsSize = dataTransferValueRowList.size()
		    	session.setAttribute("TOTAL_BATCH_ASSETS",assetsSize)
		    	for(int dataTransferValueRow =0; dataTransferValueRow < assetsSize; dataTransferValueRow ++) {
		    		def rowId =dataTransferValueRowList[dataTransferValueRow].rowId
		    		def dtvList = DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
		    		def  assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
		    		def assetEntity
		    		if( assetEntityId ) {
		    			assetEntity = AssetEntity.findById(assetEntityId)
		    		}else {
		    			assetEntity = new AssetEntity()
		    			assetEntity.attributeSet = EavAttributeSet.findById(1)
		    		}
		    		if(assetEntity){
			    		assetEntity.project = projectInstance
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
			    			}else if( it.eavAttribute.backendType == "int" ){
			    				def correctedPos
			    				if( it.correctedValue ) {
			    					correctedPos = Integer.parseInt(it.correctedValue)
			    				} else if( it.importValue ) {
			    					correctedPos = Integer.parseInt(it.importValue)
			    				}
			    				
			    				//correctedPos = it.correctedValue
			    				assetEntity."$attribName" = correctedPos 
			    			}else {
			    				assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
			    			}
			    		}
			    		assetEntity.save()
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
    	redirect (action:list, params:[projectId:projectId])
    	}
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
