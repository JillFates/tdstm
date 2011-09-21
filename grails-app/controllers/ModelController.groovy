import com.tdssrc.grails.GormUtil;
import grails.converters.JSON
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import org.jsecurity.SecurityUtils

class ModelController {
	
	//Initialize services
    def jdbcTemplate
	def assetEntityAttributeLoaderService 
    def sessionFactory 
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		boolean filter = params.filter
		if(filter){
			session.modelFilters.each{
				if(it.key.contains("tag")){
					request.parameterMap[it.key] = [session.modelFilters[it.key]]
				}
			}
		} else {
			session.modelFilters = params
		}
       // params.max = Math.min(params.max ? params.int('max') : 25, 100)
		if(!params.sort) params.sort = 'modelName'
    	if(!params.order) params.order = 'asc'
		def modelsList
		if(params.sort == 'connector'){
			String hql = '''
				SELECT m.id
				FROM Model m LEFT JOIN m.modelConnectors AS connector 
				GROUP BY m.id
				ORDER BY COUNT(connector)
			'''
			hql += params.order
			def offset = params.offset ? Integer.parseInt( params.offset ) : 0
			def ids = Model.executeQuery(hql,[ max : params.max, offset: offset ])
			modelsList = Model.getAll(ids)
		} else {
			modelsList = Model.list(params)
		}
        TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = modelsList
        Limit limit = tableFacade.limit
		if(limit.isExported()){
            tableFacade.setExportTypes(response,limit.getExportType())
            tableFacade.setColumnProperties("modelName","manufacturer","description","assetType","powerUse","noOfConnectors")
            tableFacade.render()
        }else
            return [modelsList : modelsList]
        
       // [modelInstanceList: modelsList, modelInstanceTotal: Model.count()]
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
		def powerType = session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE
        return [modelInstance: modelInstance, modelConnectors : modelConnectors, 
				otherConnectors:otherConnectors, modelTemplate:modelTemplate, powerType : powerType ]
    }

    def save = {
    	def modelId = params.modelId
		def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
		def powerType = params.powerType
		if( powerType == "Amps"){
			powerUsed = powerUsed * 110
        }
	    def modelTemplate 
		if(modelId)
			modelTemplate = Model.get(modelId)
    	params.useImage = params.useImage == 'on' ? 1 : 0
    	params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
    	params.powerUse = powerUsed
        def  modelInstance = new Model(params)
		modelInstance.powerUse = powerUsed
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
        	} else {
				def powerConnector = new ModelConnector(model : modelInstance,
														connector : 1,
														label : "Pwr1",
														type : "Power",
														labelPosition : "Right",
														connectorPosX : 0,
														connectorPosY : 0,
														status: "missing"
														)
				
				if (!powerConnector.save(flush: true)){
					def etext = "Unable to create Power Connectors for ${modelInstance}" +
					GormUtil.allErrorsString( powerConnector )
					println etext
				}
			}
			
        	modelInstance.sourceTDSVersion = 1
        	modelInstance.save(flush: true)
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
        	def modelConnectors = ModelConnector.findAllByModel( modelInstance,[sort:"id"] )
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
			def nextConnector
			try{
				nextConnector = modelConnectors.size() > 0 ? Integer.parseInt(modelConnectors[modelConnectors.size()-1]?.connector) : 0
			} catch( NumberFormatException ex){
				nextConnector = modelConnectors.size()+1
			}
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
			def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
			def powerType = params.powerType
			if( powerType == "Amps"){
				powerUsed = powerUsed * 110
			}
        	params.useImage = params.useImage == 'on' ? 1 : 0
        	params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
			params.powerUse = powerUsed
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
		
			modelInstance.height = params.modelHeight != "" ? Integer.parseInt(params.modelHeight):0 
			modelInstance.weight = params.modelWeight != "" ? Integer.parseInt(params.modelWeight):0 
			modelInstance.depth  = params.modelDepth  != "" ? Integer.parseInt(params.modelDepth):0 
			modelInstance.width  = params.modelWidth  != "" ? Integer.parseInt(params.modelWidth):0
            modelInstance.properties = params
            modelInstance.rearImage = rearImage
            modelInstance.frontImage = frontImage
            if (!modelInstance.hasErrors() && modelInstance.save(flush: true)) {
            	def connectorCount = Integer.parseInt(params.connectorCount)
				if(connectorCount > 0){
		        	for(int i=1; i<=connectorCount; i++){
						def connector = params["connector"+i]
		        		def modelConnector = connector ? ModelConnector.findByModelAndConnector(modelInstance,connector) : null
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
	        	} else {
				
					def powerConnector = new ModelConnector(model : modelInstance,
															connector : 1,
															label : "Pwr1",
															type : "Power",
															labelPosition : "Right",
															connectorPosX : 0,
															connectorPosY : 0,
															status: "missing"
															)
					
					if (!powerConnector.save(flush: true)){
						def etext = "Unable to create Power Connectors for ${modelInstance}" +
						GormUtil.allErrorsString( powerConnector )
						println etext
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
							
						}
						if(assetEntity?.rackTarget && connector.type == "Power" && 
							connector.label?.toLowerCase() == 'pwr1' && !assetCableMap.toPower){
							assetCableMap.toAsset = assetEntity
							assetCableMap.toAssetRack = assetEntity?.rackTarget?.tag
							assetCableMap.toAssetUposition = 0
							assetCableMap.toConnectorNumber = null
							assetCableMap.toPower = "A"
						}
						if ( !assetCableMap.validate() || !assetCableMap.save() ) {
							def etext = "Unable to create assetCableMap for assetEntity ${assetEntity}" +
							GormUtil.allErrorsString( assetCableMap )
							println etext
							log.error( etext )
						}
    				}
					def assetCableMaps = AssetCableMap.findAllByFromAsset( assetEntity )
					assetCableMaps.each{assetCableMap->
						if(!assetConnectors.id?.contains(assetCableMap.fromConnectorNumber?.id)){
							AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
														toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
														where toConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}""")
							AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}")
						}
					}
            	}
            	def updateAssetsQuery = "update asset_entity set asset_type = '${modelInstance.assetType}' where model_id='${modelInstance.id}'"
            	jdbcTemplate.update(updateAssetsQuery)
                
				if(modelInstance.sourceTDSVersion){
	        		modelInstance.sourceTDSVersion ++
	    		} else {
	    			modelInstance.sourceTDSVersion = 1
	    		}
	        	modelInstance.save(flush: true)
				
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
			models = manufacturerInstance ? Model.findAllByManufacturer( manufacturerInstance,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetType } : null
		}
    	def modelsList = []
    	if(models.size() > 0){
    		models.each{
    			modelsList << [id:it.id, modelName:it.modelName]
    		}
    	}
		render modelsList as JSON
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
			assetCableMap = AssetCableMap.findAll("from AssetCableMap where status in ('empty','cabled','cabledDetails') and (fromConnectorNumber = ? or toConnectorNumber = ? )",[modelConnector,modelConnector])
		}
    	render assetCableMap as JSON
    }
    /*
     *  TEMP method to redirect to action : show
     */
    def cancel = {
    		 redirect(action: "show", id: params.id)
    }
    /*
     *  When the user clicks on an item do the following actions:
     *	1. Add to the AKA field list in the target record
	 *	2. Revise Asset, and any other records that may point to this model
	 *	3. Delete model record
	 *	4. Return to model list view with the flash message "Merge completed."
     */
	def merge = {
    	// Get the Model instances for params ids
		def toModel = Model.get(params.id)
		def fromModel = Model.get(params.fromId)
		
		//	Revise Asset, and any other records that may point to this model
		def fromModelAssets = AssetEntity.findAllByModel( fromModel )
		fromModelAssets.each{ assetEntity->
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			assetEntity.save(flush:true)
			assetEntityAttributeLoaderService.updateModelConnectors( assetEntity )
		}
    	// Delete model associated record
		AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber in (from ModelConnector where model = ${fromModel.id})")
		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
												toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
												where toConnectorNumber in (from ModelConnector where model = ${fromModel.id})""")
    	ModelConnector.executeUpdate("delete ModelConnector where model = ?",[fromModel])
		
		// Add to the AKA field list in the target record
		if(!toModel.aka?.contains(fromModel.modelName)){
			def aka = new StringBuffer(toModel.aka ? toModel.aka+"," : "")
			aka.append(fromModel.modelName)
			aka.append(fromModel.aka ? ","+fromModel.aka : "")
			
			// Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
			
			toModel.aka = aka 
			if(!toModel.hasErrors())
				toModel.save(flush:true)
		} else {
			//	Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
		}
		// Return to model list view with the flash message "Merge completed."
    	flash.message = "Merge completed."
    	redirect(action:list)
    }
    /*
     * 
     */
	def importExport = {
		if( params.message ) {
			flash.message = params.message
		}
		
		def batchCount = jdbcTemplate.queryForInt("select count(*) from ( select * from manufacturer_sync group by batch_id ) a")
		[batchCount:batchCount]
    }
    /*
     * Use excel format with the manufacturer,model and connector sheets. 
     * The file name should be of the format TDS-Sync-Data-2011-05-02.xls with the current date.
     */
    def export = {
        //get template Excel
        try {
			File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Sync_model_template.xls" ).getFile()
			WorkbookSettings wbSetting = new WorkbookSettings()
			wbSetting.setUseTemporaryFileDuringWrite(true)
			def workbook = Workbook.getWorkbook( file, wbSetting )
			//set MIME TYPE as Excel
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			def filename = 	"TDS-Sync-Data-"+formatter.format(new Date())+".xls"
					filename = filename.replace(" ", "_")
			response.setContentType( "application/vnd.ms-excel" )
			response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
			
			def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
			
			def manuSheet = book.getSheet("manufacturer")
			def manufacturers = Model.findAll("FROM Model where sourceTDS = 1 GROUP BY manufacturer").manufacturer
			
			for ( int r = 0; r < manufacturers.size(); r++ ) {
				manuSheet.addCell( new Label( 0, r+1, String.valueOf(manufacturers[r].id )) )
				manuSheet.addCell( new Label( 1, r+1, String.valueOf(manufacturers[r].name )) )
				manuSheet.addCell( new Label( 2, r+1, String.valueOf(manufacturers[r].aka ? manufacturers[r].aka : "" )) )
				manuSheet.addCell( new Label( 3, r+1, String.valueOf(manufacturers[r].description ? manufacturers[r].description : "" )) )
			}
			
			def modelSheet = book.getSheet("model")
			def models = Model.findAllBySourceTDS(1)
			
			for ( int r = 0; r < models.size(); r++ ) {
				modelSheet.addCell( new Label( 0, r+1, String.valueOf(models[r].id )) )
				modelSheet.addCell( new Label( 1, r+1, String.valueOf(models[r].modelName )) )
				modelSheet.addCell( new Label( 2, r+1, String.valueOf(models[r].aka ? models[r].aka : "" )) )
				modelSheet.addCell( new Label( 3, r+1, String.valueOf(models[r].description ? models[r].description : "" )) )
				modelSheet.addCell( new Label( 4, r+1, String.valueOf(models[r].manufacturer.id )) )
				modelSheet.addCell( new Label( 5, r+1, String.valueOf(models[r].manufacturer.name )) )
				modelSheet.addCell( new Label( 6, r+1, String.valueOf(models[r].assetType )) )
				modelSheet.addCell( new Label( 7, r+1, String.valueOf(models[r].bladeCount ? models[r].bladeCount : "" )) )
				modelSheet.addCell( new Label( 8, r+1, String.valueOf(models[r].bladeLabelCount ? models[r].bladeLabelCount : "" )) )
				modelSheet.addCell( new Label( 9, r+1, String.valueOf(models[r].bladeRows ? models[r].bladeRows : "" )) )
				modelSheet.addCell( new Label( 10, r+1, String.valueOf(models[r].sourceTDS == 1 ? "TDS" : "" )) )
				modelSheet.addCell( new Label( 11, r+1, String.valueOf(models[r].powerUse ? models[r].powerUse : "" )) )
				modelSheet.addCell( new Label( 12, r+1, String.valueOf(models[r].sourceTDSVersion ? models[r].sourceTDSVersion : 1 )) )
				modelSheet.addCell( new Label( 13, r+1, String.valueOf(models[r].useImage == 1 ? "yes" : "no" )) )
				modelSheet.addCell( new Label( 14, r+1, String.valueOf(models[r].usize)) )
			}
			def connectorSheet = book.getSheet("connector")
			def connectors = ModelConnector.findAll("FROM ModelConnector where model.sourceTDS = 1 order by model.id")
			
			for ( int r = 0; r < connectors.size(); r++ ) {
				connectorSheet.addCell( new Label( 0, r+1, String.valueOf(connectors[r].id )) )
				connectorSheet.addCell( new Label( 1, r+1, String.valueOf(connectors[r].connector )) )
				connectorSheet.addCell( new Label( 2, r+1, String.valueOf(connectors[r].connectorPosX )) )
				connectorSheet.addCell( new Label( 3, r+1, String.valueOf(connectors[r].connectorPosY )) )
				connectorSheet.addCell( new Label( 4, r+1, String.valueOf(connectors[r].label ? connectors[r].label : "" )) )
				connectorSheet.addCell( new Label( 5, r+1, String.valueOf(connectors[r].labelPosition )) )
				connectorSheet.addCell( new Label( 6, r+1, String.valueOf(connectors[r].model.id )) )
				connectorSheet.addCell( new Label( 7, r+1, String.valueOf(connectors[r].model.modelName )) )
				connectorSheet.addCell( new Label( 8, r+1, String.valueOf(connectors[r].option ? connectors[r].option : "" )) )
				connectorSheet.addCell( new Label( 9, r+1, String.valueOf(connectors[r].status )) )
				connectorSheet.addCell( new Label( 10, r+1, String.valueOf(connectors[r].type )) )
			}
			book.write()
			book.close()
		} catch( Exception ex ) {
			flash.message = "Exception occurred while exporting data"+ex
			redirect( controller:'model', action:"importExport")
			return;
		}
    }
    /*
     *1. On upload the system should put the data into temporary tables and then perform validation to make sure the data is proper and ready.
	 *2. Step through each imported model:
	 *2a if it's SourceTDSVersion is higher than the one in the database, update the database with the new model and connector data.
	 *2b If it is lower, skip it.
	 *3. Report the number of Model records updated.
     */
    def upload = {
		DataTransferBatch.withTransaction { status ->
			//get user name.
			def subject = SecurityUtils.subject
			def principal = subject.principal
			def userLogin = UserLogin.findByUsername( principal )
	        // get File
	        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
	        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")
	        def modelSyncBatch = new ModelSyncBatch(userLogin:userLogin).save()
	        // create workbook
	        def workbook
	        def sheetNameMap = new HashMap()
	        //get column name and sheets
			sheetNameMap.put( "manufacturer", ["manufacturer_id", "name", "aka", "description"] )
			sheetNameMap.put( "model", ["model_id", "name","aka","description","manufacturer_id","manufacturer_name","asset_type","blade_count","blade_label_count","blade_rows","sourcetds","power_use","sourcetdsversion","use_image","usize"] )
			sheetNameMap.put( "connector", ["model_connector_id", "connector", "connector_posx", "connector_posy", "label", "label_position", "model_id", "model_name", "connector_option", "status", "type"] )
			
	        try {
	            workbook = Workbook.getWorkbook( file.inputStream )
	            List sheetNames = workbook.getSheetNames()
				def sheets = sheetNameMap.keySet()
				def missingSheets = []
	            def flag = 1
	            def sheetsLength = sheets.size()
				
				sheets.each{
					if ( !sheetNames.contains( it ) ) {
	                    flag = 0
						missingSheets<< it
	                }
				}
	            if( flag == 0 ) {
	                flash.message = "${missingSheets} sheets not found, Please check it."
	                redirect( action:importExport, params:[message:flash.message] )
	                return;
	            } else {
	            	def manuAdded = 0
					def manuSkipped = []
	            	def sheetColumnNames = [:]
	                //check for column
					def manuSheet = workbook.getSheet( "manufacturer" )
	                def manuCol = manuSheet.getColumns()
	                for ( int c = 0; c < manuCol; c++ ) {
	                    def cellContent = manuSheet.getCell( c, 0 ).contents
	                    sheetColumnNames.put(cellContent, c)
	                }
	                def missingHeader = checkHeader( sheetNameMap.get("manufacturer"), sheetColumnNames )
	                // Statement to check Headers if header are not found it will return Error message
	                if ( missingHeader != "" ) {
	                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
	                    redirect( action:importExport, params:[message:flash.message] )
	                    return;
	                } else {
	                    def sheetrows = manuSheet.rows
	                    for ( int r = 1; r < sheetrows ; r++ ) {
	                		def valueList = new StringBuffer("(")
	                    	for( int cols = 0; cols < manuCol; cols++ ) {
	                    		valueList.append("'"+manuSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
	                        }
	                		try{
	                			jdbcTemplate.update("insert into manufacturer_sync( manufacturer_temp_id, name,aka, description, batch_id) values "+valueList.toString()+"${modelSyncBatch.id})")
								manuAdded = r
	                		} catch (Exception e) {
	                			manuSkipped += ( r +1 )
	                		}
	                    }
	
	                }
	                /*
	                 *  Import Model Information
	                 */
					def modelSheetColumnNames = [:]
					def modelAdded = 0
					def modelSkipped = []
	                 //check for column
	 				def modelSheet = workbook.getSheet( "model" )
					def modelCol = modelSheet.getColumns()
					for ( int c = 0; c < modelCol; c++ ) {
						def cellContent = modelSheet.getCell( c, 0 ).contents
						modelSheetColumnNames.put(cellContent, c)
					}
	                missingHeader = checkHeader( sheetNameMap.get("model"), modelSheetColumnNames )
					// Statement to check Headers if header are not found it will return Error message
					if ( missingHeader != "" ) {
						flash.message = " Column Headers : ${missingHeader} not found, Please check it."
						redirect( action:importExport, params:[ message:flash.message] )
						return;
					} else {
						def sheetrows = modelSheet.rows
						for ( int r = 1; r < sheetrows ; r++ ) {
							def valueList = new StringBuffer("(")
		             		def manuId
		                 	for( int cols = 0; cols < modelCol; cols++ ) {
		                 		
								switch(modelSheet.getCell( cols, 0 ).contents){
								case "manufacturer_name" : 
									def manuName = modelSheet.getCell( cols, r ).contents
									manuId = ManufacturerSync.findByNameAndBatch(manuName,modelSyncBatch)?.id
									valueList.append("'"+modelSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								case "blade_count" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "blade_label_count" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "blade_rows" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "use_image" : 
									int useImage = 0
									if(modelSheet.getCell( cols, r ).contents.toLowerCase() != "no"){
										useImage = 1
									}
									valueList.append(useImage+",")
									break;
								case "power_use" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "usize" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "sourcetds" : 
									int isTDS = 0
									if(modelSheet.getCell( cols, r ).contents.toLowerCase() == "tds"){
										isTDS = 1
									}
									valueList.append(isTDS+",")
									break;
								case "sourcetdsversion" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								default : 
									valueList.append("'"+modelSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								}
		                 									
		                 	}
		             		try{
		             			if(manuId){
			             			jdbcTemplate.update("insert into model_sync( model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,front_image,power_use,sourcetdsversion,use_image,usize,batch_id,manufacturer_id ) values "+valueList.toString()+"${modelSyncBatch.id}, $manuId)")
									modelAdded = r
		             			} else {
		             				modelSkipped += ( r +1 )
		             			}
		             		} catch (Exception e) {
		             			modelSkipped += ( r +1 )
		             		}
		                }
					}
	                /*
	                 *  Import Model Information
	                 */
					def connectorSheetColumnNames = [:]
					def connectorAdded = 0
					def connectorSkipped = []
	                 //check for column
	 				def connectorSheet = workbook.getSheet( "connector" )
					def connectorCol = connectorSheet.getColumns()
					for ( int c = 0; c < connectorCol; c++ ) {
						def cellContent = connectorSheet.getCell( c, 0 ).contents
						connectorSheetColumnNames.put(cellContent, c)
					}
	                missingHeader = checkHeader( sheetNameMap.get("connector"), connectorSheetColumnNames )
					// Statement to check Headers if header are not found it will return Error message
					if ( missingHeader != "" ) {
						flash.message = " Column Headers : ${missingHeader} not found, Please check it."
						redirect( action:importExport, params:[message:flash.message] )
						return;
					} else {
						def sheetrows = connectorSheet.rows
						for ( int r = 1; r < sheetrows ; r++ ) {
							def valueList = new StringBuffer("(")
		             		def modelId
		                 	for( int cols = 0; cols < connectorCol; cols++ ) {
		                 		
								switch(connectorSheet.getCell( cols, 0 ).contents){
								case "model_name" : 
									def modelName = connectorSheet.getCell( cols, r ).contents
									modelId = ModelSync.findByModelNameAndBatch(modelName,modelSyncBatch)?.id
									valueList.append("'"+connectorSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								case "connector_posx" : 
									valueList.append((connectorSheet.getCell( cols, r ).contents ? connectorSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "connector_posy" :
									valueList.append((connectorSheet.getCell( cols, r ).contents ? connectorSheet.getCell( cols, r ).contents : null)+",")
									break;
								default : 
									valueList.append("'"+connectorSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								}
		                 									
		                 	}
		             		try{
		             			if(modelId){
			             			jdbcTemplate.update("insert into model_connector_sync( connector_temp_id,connector,connector_posx,connector_posy,label,label_position,model_temp_id,model_name,connector_option,status,type,batch_id,model_id ) values "+valueList.toString()+"${modelSyncBatch.id}, $modelId)")
									connectorAdded = r
		             			} else {
		             				connectorSkipped += ( r +1 )
		             			}
		             		} catch (Exception e) {
		             			connectorSkipped += ( r +1 )
		             		}
		                }
					}                
	                workbook.close()
	                if (manuSkipped.size() > 0 || modelSkipped.size() > 0 || connectorSkipped.size() > 0) {
	                    flash.message = " File Uploaded Successfully with Manufactures:${manuAdded},Model:${modelAdded},Connectors:${connectorAdded} records. and  Manufactures:${manuSkipped},Model:${modelSkipped},Connectors:${connectorSkipped} Records skipped Please click the Manage Batches to review and post these changes."
	                } else {
	                    flash.message = " File uploaded successfully with Manufactures:${manuAdded},Model:${modelAdded},Connectors:${connectorAdded} records.  Please click the Manage Batches to review and post these changes."
	                }
	                redirect( action:importExport, params:[message:flash.message] )
		            return;  
		        }
	        } catch( NumberFormatException ex ) {
	            flash.message = ex
	            status.setRollbackOnly()
	            redirect( action:importExport, params:[message:flash.message] )
	            return;
	        } catch( Exception ex ) {
	        	ex.printStackTrace()
				status.setRollbackOnly()
	            flash.message = ex
	            redirect( action:importExport, params:[message:flash.message] )
	            return;
	        } 
		}
    }
    def checkHeader( def list, def sheetColumnNames  ) {  
    	def missingHeader = ""
        def listSize = list.size()
        for ( int coll = 0; coll < listSize; coll++ ) {
            if( !sheetColumnNames.containsKey( list[coll] ) ) {
                missingHeader = missingHeader + ", " + list[coll]
            }
        }
    	return missingHeader
    }
    def manageImports = {
    	[modelSyncBatch:ModelSyncBatch.list()]
    }
    /*
     *  Send Model details as JSON object
     */
	def getModelAsJSON = {
    	def id = params.id
    	def model = Model.get(params.id)
		def modelMap = [id:model.id,
						modelName:model.modelName,
						description:model.description,
						assetType:model.assetType,
						powerUse:model.powerUse,
						aka:model.aka,
						usize:model.usize,
						frontImage:model.frontImage ? model.frontImage : '',
						rearImage:model.rearImage ? model.rearImage : '',
						useImage:model.useImage,
						bladeRows:model.bladeRows,
						bladeCount:model.bladeCount,
						bladeLabelCount:model.bladeLabelCount,
						bladeHeight:model.bladeHeight,
						bladeHeight:model.bladeHeight,
						sourceTDSVersion:model.sourceTDSVersion,
						]
    	render modelMap as JSON
    }
}
