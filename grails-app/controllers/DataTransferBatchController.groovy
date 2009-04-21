import com.tdssrc.eav.EavAttributeSet
class DataTransferBatchController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
    /*
     *   Return list of dataTransferBatchs for associated Project and Mode = Import
     */
    def list = {
    		def projectId = params.projectId
    		def projectInstance = Project.findById( projectId )
    		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( projectInstance, "I" );
            return [ dataTransferBatchList:dataTransferBatchList, projectId:projectId ]
    }
    //Process DataTransfervalues Corresponding to DataTransferBatch
    def process = { 
    	def projectId = params.projectId
    	def projectInstance = Project.findById( projectId )
    	def dataTransferBatch = DataTransferBatch.get(params.batchId)
    	try{
	    	if(dataTransferBatch){
		    	def dataTransferValueRowList = DataTransferValue.findAll(" From DataTransferValue d where d.dataTransferBatch = $dataTransferBatch.id and d.dataTransferBatch.statusCode = 'PENDING' group by rowId")
		    	for(int dataTransferValueRow =0; dataTransferValueRow < dataTransferValueRowList.size(); dataTransferValueRow ++) {
		    		def rowId =dataTransferValueRowList[dataTransferValueRow].rowId
		    		def dtvList = DataTransferValue.findAllByRowIdAndDataTransferBatch( rowId, dataTransferBatch )
		    		def  assetEntityId = dataTransferValueRowList[dataTransferValueRow].assetEntityId
		    		def assetEntity
		    		if(assetEntityId == null) {
		    			assetEntity = new AssetEntity()
		    			assetEntity.attributeSet = EavAttributeSet.findById(1)
		    		}else {
		    			assetEntity = AssetEntity.findById(assetEntityId)
		    		}
		    		if(assetEntity){
			    		assetEntity.project = projectInstance 
			    		dtvList.each {
			    			def attribName = it.eavAttribute.attributeCode
			    			if ( attribName == "moveBundle" ) {
			    				if( it.importValue != null && it.correctedValue != null ) {
			    					def importMoveBundleInstance = MoveBundle.findByName(it.importValue)
			        				def exportMoveBundleInstance = MoveBundle.findByName(it.correctedValue)
			        				assetEntity."$attribName" = exportMoveBundleInstance ? exportMoveBundleInstance : importMoveBundleInstance
			    				}
			    			}else if( it.eavAttribute.backendType == "int" ){
			    				def importPos
			    				def correctedPos
			    				if(it.importValue != null && it.importValue != "") {
			    					importPos = Integer.parseInt(it.importValue)
			    				}
			    				if(it.correctedValue != null && it.correctedValue != "") {
			    					correctedPos = Integer.parseInt(it.correctedValue)
			    				}
			    				
			    				correctedPos = it.correctedValue
			    				assetEntity."$attribName" = correctedPos ? correctedPos : importPos
			    			}else {
			    				assetEntity."$attribName" = it.correctedValue ? it.correctedValue : it.importValue
			    			}
			    		}
			    		assetEntity.save(flush:true)
		    		}
		    	}  
		    	def dataTransferCommentRowList = DataTransferComment.findAll(" From DataTransferComment dtc where dtc.dataTransferBatch = $dataTransferBatch.id and dtc.dataTransferBatch.statusCode = 'PENDING'")
		    	if(dataTransferCommentRowList){
		    		dataTransferCommentRowList.each{
		    			def assetComment
		    			def assetEntity = AssetEntity.findById(it.assetId)
		    			if(assetEntity){
		    			if(it.commentId){
		    				assetComment = AssetComment.findById(it.commentId)
		    			} 
		    			if(!assetComment){
			    			assetComment = new AssetComment()
			    			assetComment.mustVerify = 0
		    			}
			    		assetComment.comment = it.comment  		
			        	assetComment.commentType = it.commentType
			        	assetComment.assetEntity = assetEntity        		
			        	assetComment.save(flush:true)
		    			}
		    		}
		    		
		    	}
		    dataTransferBatch.statusCode = 'COMPLETED'
		    dataTransferBatch.save(flush:true)
	    	}
    	}catch (Exception e) {
			flash.message = "Import Batch process failed"
		}
    	redirect (action:list, params:[projectId:projectId])
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
