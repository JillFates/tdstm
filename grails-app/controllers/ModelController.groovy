import com.tdssrc.grails.GormUtil;
import grails.converters.JSON

class ModelController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        [modelInstanceList: Model.list(params), modelInstanceTotal: Model.count()]
    }

    def create = {
    	def modelId = params.modelId
        def modelInstance = new Model()
    	def modelConnectors
		def modelTemplate
	    if(modelId){
	    	modelTemplate = Model.get( modelId )
			modelConnectors = ModelConnector.findAllByModel( modelTemplate )
	    }
    	def otherConnectors = []
    	def existingConnectors = modelConnectors ? modelConnectors.size()+1 : 1
		for(int i = existingConnectors ; i<51; i++ ){
			otherConnectors << i
		}
        return [modelInstance: modelInstance, modelConnectors : modelConnectors, 
				otherConnectors:otherConnectors, modelTemplate:modelTemplate ]
    }

    def save = {
    	def modelId = params.modelId
    	def modelTemplate 
		if(modelId)
			modelTemplate = Model.get(modelId)
    	params.useImage = params.useImage == 'on' ? 1 : 0
        def modelInstance = new Model(params)
		def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		def frontImage = request.getFile('frontImage')
        if( frontImage.bytes.size() > 0 ) {
			if( frontImage.getContentType() && frontImage.getContentType() != "application/octet-stream"){
				if (! okcontents.contains(frontImage.getContentType())) {
	        		flash.message = "Front Image must be one of: ${okcontents}"
	        		render(view: "create", model: [modelInstance: modelInstance])
	        		return;
	        	}
        	}
        } else if(modelTemplate){
        	modelInstance.frontImage = modelTemplate.frontImage
        } else {
        	modelInstance.frontImage = null
        }
        def rearImage = request.getFile('rearImage')
        if( rearImage.bytes.size() > 0 ) {
			if( rearImage.getContentType() && rearImage.getContentType() != "application/octet-stream"){
				if (! okcontents.contains(rearImage.getContentType())) {
	        		flash.message = "Rear Image must be one of: ${okcontents}"
	        		render(view: "create", model: [modelInstance: modelInstance])
	        		return;
	        	}
        	}
        } else if(modelTemplate){
        	modelInstance.rearImage = modelTemplate.rearImage
        } else {
        	modelInstance.rearImage = null
        }
        if (modelInstance.save(flush: true)) {
        	def connectorCount = Integer.parseInt(params.connectorCount)
			if(connectorCount > 0){
	        	for(int i=1; i<=connectorCount; i++){
	        		def modelConnector = new ModelConnector(model : modelInstance,
	        												connector : params["connector"+i],
															label : params["label"+i],
															type :params["type"+i],
															labelPosition : params["labelPosition"+i],
															connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
															connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
															status:params["status"+i] )
	        		
	        		if (!modelConnector.hasErrors() )
	        			modelConnector.save(flush: true)
	        	}
        	}
            flash.message = "${modelInstance.modelName} created"
            redirect(action: "show", id: modelInstance.id)
        }
        else {
        	//flash.message = modelInstance.errors.allErrors.each() {  it }
			def	modelConnectors = modelTemplate ? ModelConnector.findAllByModel( modelTemplate ) : null
	    	def otherConnectors = []
			def existingConnectors = modelConnectors ? modelConnectors.size()+1 : 1
			for(int i = existingConnectors ; i<51; i++ ){
				otherConnectors << i
			}
            render(view: "create", model: [modelInstance: modelInstance, modelConnectors:modelConnectors,
										   otherConnectors:otherConnectors, modelTemplate:modelTemplate ] )
        }
    }

    def show = {
        def modelInstance = Model.get(params.id)
        if (!modelInstance) {
        	flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
        else {
        	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
            return [ modelInstance : modelInstance, modelConnectors : modelConnectors ]
        }
    }

    def edit = {
        def modelInstance = Model.get(params.id)
        if (!modelInstance) {
            flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
        else {
        	def modelConnectors = ModelConnector.findAllByModel( modelInstance,[sort:"id"] )
			def nextConnector = modelConnectors.size() > 0 ? Integer.parseInt(modelConnectors[modelConnectors.size()-1]?.connector) : 0 
			def otherConnectors = []
			for(int i = nextConnector+1 ; i<51; i++ ){
				otherConnectors << i
			}
            return [ modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors, nextConnector:nextConnector ]
        }
    }

    def update = {
        def modelInstance = Model.get(params.id)
        if (modelInstance) {
        	params.useImage = params.useImage == 'on' ? 1 : 0
            def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
    		def frontImage = request.getFile('frontImage')
            if( frontImage ) {
    			if( frontImage.getContentType() && frontImage.getContentType() != "application/octet-stream"){
    				if (! okcontents.contains(frontImage.getContentType())) {
    	        		flash.message = "Front Image must be one of: ${okcontents}"
    	        		render(view: "create", model: [modelInstance: modelInstance])
    	        		return;
    	        	}
    				frontImage = frontImage.bytes
            	} else {
            		frontImage = modelInstance.frontImage
            	}
            }
            def rearImage = request.getFile('rearImage')
            if( rearImage ) {
    			if( rearImage.getContentType() && rearImage.getContentType() != "application/octet-stream"){
    				if (! okcontents.contains(rearImage.getContentType())) {
    	        		flash.message = "Rear Image must be one of: ${okcontents}"
    	        		render(view: "create", model: [modelInstance: modelInstance])
    	        		return;
    	        	}
    				rearImage = rearImage.bytes
            	} else {
                	rearImage = modelInstance.rearImage
                }
            }
            modelInstance.properties = params
            modelInstance.rearImage = rearImage
            modelInstance.frontImage = frontImage
            if (!modelInstance.hasErrors() && modelInstance.save(flush: true)) {
            	def connectorCount = Integer.parseInt(params.connectorCount)
				if(connectorCount > 0){
		        	for(int i=1; i<=connectorCount; i++){
		        		def modelConnector = ModelConnector.findByModelAndConnector(modelInstance,i)
						def connector = params["connector"+i]
						if( !connector && modelConnector ){
							AssetCableMap.executeUpdate("Delete from AssetCableMap where fromConnectorNumber = ? ", [modelConnector])
							def assetCables = AssetCableMap.findAll("from AssetCableMap where toConnectorNumber = ? ",[modelConnector])
							assetCables.each{ assetCableMap->
								assetCableMap.status = 'missing' 
								assetCableMap.toAsset = null
								assetCableMap.toConnectorNumber = null
								assetCableMap.toAssetRack = null
								assetCableMap.toAssetUposition = null
								if ( !assetCableMap.validate() || !assetCableMap.save(flush:true) ) {
		    						def etext = "Unable to Update assetCableMap : " +
		    		                GormUtil.allErrorsString( assetCableMap )
		    						println etext
		    					}
							}
							
							modelConnector.delete(flush:true)
							
						} else {
							if(modelConnector){
								modelConnector.connector = params["connector"+i]
								modelConnector.label = params["label"+i]
								modelConnector.type = params["type"+i]
								modelConnector.labelPosition = params["labelPosition"+i]
								modelConnector.connectorPosX = Integer.parseInt(params["connectorPosX"+i])
								modelConnector.connectorPosY = Integer.parseInt(params["connectorPosY"+i])
								modelConnector.status = params["status"+i]
								
							} else if(connector){
								modelConnector = new ModelConnector(model : modelInstance,
			        												connector : params["connector"+i],
																	label : params["label"+i],
																	type : params["type"+i],
																	labelPosition : params["labelPosition"+i],
																	connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
																	connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
																	status : params["status"+i] )
			        		
							}
			        		if (modelConnector && !modelConnector.hasErrors() )
			        			modelConnector.save(flush: true)
						}
		        	}
	        	}
            	def assetEntitysByModel = AssetEntity.findAllByModel( modelInstance )
				def assetConnectors = ModelConnector.findAllByModel( modelInstance )
				assetEntitysByModel.each{ assetEntity ->
            		assetConnectors.each{connector->
            			
    					def assetCableMap = AssetCableMap.findByFromAssetAndFromConnectorNumber( assetEntity, connector )
						
						if( !assetCableMap ){
	    					assetCableMap = new AssetCableMap(
	    														cable : "Cable"+connector.connector,
	    														fromAsset: assetEntity,
	    														fromConnectorNumber : connector,
	    														status : connector.status
	    														)
	    					if ( !assetCableMap.validate() || !assetCableMap.save() ) {
	    						def etext = "Unable to create assetCableMap for assetEntity ${assetEntity}" +
	    		                GormUtil.allErrorsString( assetCableMap )
	    						println etext
	    						log.error( etext )
	    					}
						}
    				}
            	}
            	
                flash.message = "${modelInstance.modelName} Updated"
                redirect(action: "show", id: modelInstance.id)
            }
            else {
            	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
				def otherConnectors = []
				for(int i = modelConnectors.size()+1 ; i<51; i++ ){
					otherConnectors << i
				}
                render(view: "edit", model: [modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors])
            }
        }
        else {
            flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }

    def delete = {
        def modelInstance = Model.get(params.id)
        if (modelInstance) {
            try {
            	AssetEntity.executeUpdate("update AssetEntity set model = null where model = ?",[modelInstance])
				
            	AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber in (from ModelConnector where model = ${modelInstance.id})")
				AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
														toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
														where toConnectorNumber in (from ModelConnector where model = ${modelInstance.id})""")
            	ModelConnector.executeUpdate("delete ModelConnector where model = ?",[modelInstance])
                modelInstance.delete(flush: true)
                flash.message = "${modelInstance} deleted"
                redirect(action: "list")
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
            	flash.message = "${modelInstance} not deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
        	flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }
    /*
     *  Send FrontImage as inputStream
     */
    def getFrontImage = {
		if( params.id ) {
    		def model = Model.findById( params.id )
     		def image = model?.frontImage
     		response.contentType = 'image/jpg'		
     		response.outputStream << image
		} else {
			return "";
		}
    }
    /*
     *  Send RearImage as inputStream
     */
    def getRearImage = {
		if( params.id ) {
    		def model = Model.findById( params.id )
     		def image = model?.rearImage
     		response.contentType = 'image/jpg'		
     		response.outputStream << image
		} else {
			return "";
		}
    }
    /*
     *  Send List of model as JSON object
     */
	def getModelsListAsJSON = {
    	def manufacturer = params.manufacturer
		def assetType = params.assetType
    	def models
		if(manufacturer){
			def manufacturerInstance = Manufacturer.get(manufacturer)
			models = manufacturerInstance ? Model.findAllByManufacturer( manufacturerInstance )?.findAll{it.assetType == assetType } : null
		}
    	if(!models)
    		models = []
		render models as JSON
    }
    /*
     *  check to see that if they were any Asset records exist for the selected model before deleting it
     */
    def checkModelDependency = {
    	def modelId = params.modelId
		def modelInstance = Model.findById(Integer.parseInt(modelId))
		def returnValue = false
		if( modelInstance ){
			if( AssetEntity.findByModel( modelInstance ) )
				returnValue = true
		}
    	render returnValue
    }
    /*
     *  Return AssetCables to alert the user while deleting the connectors
     */
	def getAssetCablesForConnector = {
    	def modelId = params.modelId
		def modelInstance = Model.get(modelId)
		def assetCableMap = []
		if(modelInstance){
			def connector = params.connector
			def modelConnector = ModelConnector.findByConnectorAndModel( connector, modelInstance )
			assetCableMap = AssetCableMap.findAll("from AssetCableMap where toConnectorNumber = ? ",[modelConnector])
		}
    	render assetCableMap as JSON
    }
}
