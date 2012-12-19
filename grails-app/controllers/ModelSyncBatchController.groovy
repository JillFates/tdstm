import com.tdssrc.grails.GormUtil
class ModelSyncBatchController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def securityService
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        //params.max = Math.min(params.max ? params.int('max') : 20, 100)
        [modelSyncBatchInstanceList: ModelSyncBatch.list([sort:'id',order:'desc']), modelSyncBatchInstanceTotal: ModelSyncBatch.count()]
    }

    def process = {
    	def modelBatch = params.batchId
		def modelSyncBatch = ModelSyncBatch.get(modelBatch)
		def loggedUser = securityService.getUserLogin()
		try{
			if(modelSyncBatch){
				def manufacturersSync = ManufacturerSync.findAllByBatch( modelSyncBatch )
				def modelsSync = ModelSync.findAllByBatch( modelSyncBatch )
				def connectorsSync = ModelConnectorSync.findAllByBatch( modelSyncBatch )
				// Merge manufacturers
				def manuAdded = 0
				def manuUpdated = 0
				manufacturersSync.each{ manufacturerSync->
					def manufacturerInstance = Manufacturer.findByIdAndName( manufacturerSync.manufacturerTempId,manufacturerSync.name )
					if( !manufacturerInstance ){
						manufacturerInstance = Manufacturer.findByName( manufacturerSync.name )
						if(!manufacturerInstance){
							manufacturerInstance = new Manufacturer( name : manufacturerSync.name,  
																	 description : manufacturerSync.description
																	)
							if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
								def etext = "Unable to create manufacturerInstance" +
								GormUtil.allErrorsString( manufacturerInstance )
								println etext
							} else {
							    if(manufacturerSync.aka){
									def akas = manufacturerSync.aka?.split(",")
									akas.each{
										def manuExist = Manufacturer.findByName(it.trim())
										if(!manuExist){
											manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
										}
									}
							    }
								manuAdded ++
							}
						} else {
							//manufacturerInstance.aka = manufacturerSync.aka
							manufacturerInstance.description = manufacturerSync.description 
							manufacturerInstance.userLogin = loggedUser
							if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
								def etext = "Unable to create manufacturerInstance" +
								GormUtil.allErrorsString( manufacturerInstance )
								println etext
							} else {
								if(manufacturerSync.aka){
									def akas = manufacturerSync.aka?.split(",")
									akas.each{
										def manuExist = Manufacturer.findByName(it.trim())
										if(!manuExist){
											manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
										}
									}
							    }
								manuUpdated ++
							}
						
						}
					} else {
						//manufacturerInstance.aka = manufacturerSync.aka
						manufacturerInstance.description = manufacturerSync.description 
						if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
							def etext = "Unable to create manufacturerInstance" +
							GormUtil.allErrorsString( manufacturerInstance )
							println etext
						} else {
							if(manufacturerSync.aka){
								def akas = manufacturerSync.aka?.split(",")
								akas.each{
									def manuExist = Manufacturer.findByName(it.trim())
									if(!manuExist){
										manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
									}
									
								}
						    }
							manuUpdated ++
						}
					}
				}
				// Merge manufacturers
				def modelAdded = 0
				def modelUpdated = 0
				modelsSync.each{ modelSync->
					def manufacturer = Manufacturer.findByName(modelSync.manufacturerName)
					def modelInstance = Model.findWhere(id:modelSync.modelTempId,modelName : modelSync.modelName,assetType : modelSync.assetType )
					if( !modelInstance ){
						modelInstance = Model.findByModelNameAndAssetType( modelSync.modelName,modelSync.assetType )
						if(!modelInstance){
							modelInstance = new Model( 
							   modelName : modelSync.modelName, 
							   description : modelSync.description,
							   assetType : modelSync.assetType,
							   powerNameplate : modelSync.powerNameplate,
							   powerDesign : modelSync.powerDesign,
							   powerUse : modelSync.powerUse,
							   usize : modelSync.usize,
							   bladeRows : modelSync.bladeRows,
							   bladeCount : modelSync.bladeCount,
							   bladeLabelCount : modelSync.bladeLabelCount,
							   sourceTDSVersion : 1,
							   manufacturer : manufacturer,
							   height : modelSync?.height,
							   weight : modelSync?.weight,
							   depth : modelSync?.depth,
							   width : modelSync?.width,
							   layoutStyle : modelSync.layoutStyle,
							   productLine : modelSync.productLine,
							   modelFamily : modelSync.modelFamily,
							   endOfLifeDate : modelSync.endOfLifeDate,
							   endOfLifeStatus : modelSync.endOfLifeStatus,
							   createdBy : modelSync.createdBy,
							   sourceURL : modelSync.sourceURL,
							   modelStatus : modelSync.modelStatus,
							   modelScope : modelSync.modelScope
							)
							if ( !modelInstance.validate() || !modelInstance.save() ) {
								def etext = "Unable to create modelInstance" +
								GormUtil.allErrorsString( modelInstance )
								println etext
								modelInstance.errors.allErrors.each { println it }
							} else {
							    if(modelSync.aka){
									def akas = modelSync.aka?.split(",")
									akas.each{
										def akaExist = Model.findByName(it.trim())
										if( !akaExist ){
											modelInstance.findOrCreateAliasByName(it.trim(), true)
										}
									}
							    }
								modelAdded ++
							}
						} else {
							if (modelInstance.sourceTDSVersion < modelSync.sourceTDSVersion) {
								
								modelInstance.description = modelSync.description 
								modelInstance.powerNameplate = modelSync.powerNameplate
								modelInstance.powerDesign = modelSync.powerDesign
								modelInstance.powerUse = modelSync.powerUse
								modelInstance.usize = modelSync.usize
								modelInstance.bladeRows = modelSync.bladeRows
								modelInstance.bladeCount = modelSync.bladeCount
								modelInstance.bladeLabelCount = modelSync.bladeLabelCount
								modelInstance.sourceTDSVersion = modelSync.sourceTDSVersion
								modelInstance.manufacturer = manufacturer
								modelInstance.height = modelSync.height
								modelInstance.weight = modelSync.weight
								modelInstance.depth = modelSync.depth
								modelInstance.width = modelSync.width
								modelInstance.layoutStyle = modelSync.layoutStyle
								modelInstance.productLine = modelSync.productLine
								modelInstance.modelFamily = modelSync.modelFamily
								modelInstance.endOfLifeDate = modelSync.endOfLifeDate
								modelInstance.endOfLifeStatus = modelSync.endOfLifeStatus
								modelInstance.createdBy = modelSync.createdBy
								modelInstance.updatedBy = modelSync.updatedBy
								modelInstance.sourceURL = modelSync.sourceURL
								modelInstance.modelStatus = modelSync.modelStatus
								modelInstance.modelScope = modelSync.modelScope
								
								if ( !modelInstance.validate() || !modelInstance.save() ) {
									def etext = "Unable to create modelInstance" +
									GormUtil.allErrorsString( modelInstance )
									println etext
								} else {
									if(modelSync.aka){
										def akas = modelSync.aka?.split(",")
										akas.each{
											def akaExist = Model.findByModelName(it.trim())
											if( !akaExist ){
												modelInstance.findOrCreateAliasByName(it.trim(), true)
											}
										}
									}
									modelUpdated ++
								}
							}
						}
					} else {
						if(modelInstance.sourceTDSVersion < modelSync.sourceTDSVersion){
						    
							modelInstance.description = modelSync.description 
							modelInstance.powerNameplate = modelSync.powerNameplate
							modelInstance.powerDesign = modelSync.powerDesign
							modelInstance.powerUse = modelSync.powerUse
							modelInstance.usize = modelSync.usize
							modelInstance.bladeRows = modelSync.bladeRows
							modelInstance.bladeCount = modelSync.bladeCount
							modelInstance.bladeLabelCount = modelSync.bladeLabelCount
							modelInstance.sourceTDSVersion = modelSync.sourceTDSVersion
							modelInstance.manufacturer = manufacturer
							modelInstance.height = modelSync.height
							modelInstance.weight = modelSync.weight
							modelInstance.depth = modelSync.depth
							modelInstance.width = modelSync.width
							modelInstance.layoutStyle = modelSync.layoutStyle
							modelInstance.productLine = modelSync.productLine
							modelInstance.modelFamily = modelSync.modelFamily
							modelInstance.endOfLifeDate = modelSync.endOfLifeDate
							modelInstance.endOfLifeStatus = modelSync.endOfLifeStatus
							modelInstance.createdBy = modelSync.createdBy
							modelInstance.updatedBy = modelSync.updatedBy
							modelInstance.validatedBy = modelSync.validatedBy
							modelInstance.sourceURL = modelSync.sourceURL
							modelInstance.modelStatus = modelSync.modelStatus
							modelInstance.modelScope = modelSync.modelScope
							
							if ( !modelInstance.validate() || !modelInstance.save() ) {
								def etext = "Unable to create modelInstance" +
								GormUtil.allErrorsString( modelInstance )
								println etext
							} else {
								if(modelSync.aka){
									def akas = modelSync.aka?.split(",")
									akas.each{
										// TODO - THIS should be checking if model exists by model name AND manufacturer. The model name is NOT unique
										def modelExist = Model.findByModelName(it.trim())
										if( !modelExist ){
											modelInstance.findOrCreateAliasByName(it.trim(), true)
										}
									}
								}
								modelUpdated ++
							}
						}
					}
				}
				// Merge Model Connectors
				def connectorsAdded = 0
				def connectorsUpdated = 0
				connectorsSync.each{ connectorSync->
					def manufacturer = Manufacturer.findByName(connectorSync.model.manufacturerName)
					def model = Model.findWhere(manufacturer:manufacturer,modelName : connectorSync.model.modelName,assetType : connectorSync.model.assetType )
					if(model){
						def connectorInstance = ModelConnector.findByConnectorAndModel( connectorSync.connector ,model)
						if( !connectorInstance ){
							connectorInstance = new ModelConnector( 
								connector : connectorSync.connector,
								label : connectorSync.label,
								type : connectorSync.type,
								labelPosition : connectorSync.labelPosition,
								connectorPosX : connectorSync.connectorPosX,
								connectorPosY : connectorSync.connectorPosY,
								status : connectorSync.status,
								option : connectorSync.option,
								model : model
							)
							if ( !connectorInstance.validate() || !connectorInstance.save() ) {
								def etext = "Unable to create connectorInstance" +
								GormUtil.allErrorsString( connectorInstance )
								//println etext
							} else {
								connectorsAdded ++
							}
						} else {
							if(connectorInstance.model.sourceTDSVersion <= connectorSync.model.sourceTDSVersion){
								connectorInstance.label = connectorSync.label
								connectorInstance.type = connectorSync.type
								connectorInstance.labelPosition = connectorSync.labelPosition
								connectorInstance.connectorPosX = connectorSync.connectorPosX
								connectorInstance.connectorPosY = connectorSync.connectorPosY
								connectorInstance.status = connectorSync.status
								connectorInstance.option = connectorSync.option
								
								if ( !connectorInstance.validate() || !connectorInstance.save() ) {
									def etext = "Unable to create connectorInstance" +
									GormUtil.allErrorsString( connectorInstance )
									//println etext
								} else {
									connectorsUpdated ++
								}
							}
						}
					}
				}
				flash.message = """ Process Results:<ul>
						<li>Manufacturers in Batch: ${manufacturersSync.size()}</li>
						<li>Maufacturers Inserted: ${manuAdded}</li>
						<li>Manufacturers Updated: ${manuUpdated}</li>
					</ul> 
					<ul>
						<li>Models in Batch: ${modelsSync.size()}</li>
						<li>Models Inserted: ${modelAdded}</li>
						<li>Models Updated: ${modelUpdated}</li>
					</ul>
					<ul>
						<li> Connectors in Batch: ${connectorsSync.size()}</li>
						<li>Connectors Inserted: ${connectorsAdded}</li>
						<li>Connectors Updated: ${connectorsUpdated}</li>
					</ul>""" 
			}
			modelSyncBatch.statusCode = "COMPLETED"
			modelSyncBatch.save()
		} catch (Exception e) {
			flash.message = "Import Batch process failed"
			e.printStackTrace();
		}
    	redirect(action: "list", params: params)
    }
}
