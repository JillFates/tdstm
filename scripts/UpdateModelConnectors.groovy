
def modelInstancesList = Model.list()

/*
 *  go through the table of models and if it doesn't have any connectors, add one "Pwr1" of type "power". 
 */
modelInstancesList.each { modelInstance ->
	 def powerConnector = ModelConnector.findByModel(modelInstance)
	 if( !powerConnector ){
		 powerConnector = new ModelConnector(model : modelInstance,
											connector : "Pwr1",
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
}


/*
 * go through all assets and if the pwr1 cable doesn't have a connection, 
 * create a connection to PowerA. 
 * If there isn't any target rack specified, leave it alone.
 */
def assetEntityList = AssetEntity.findAllByModelIsNotNull()
assetEntityList.each { assetEntity ->
	def modelConnectors = ModelConnector.findAllByModel( assetEntity.model )
	modelConnectors.each{connector->
		def assetCableMap = AssetCableMap.findByFromAssetAndFromConnectorNumber( assetEntity, connector )
		if( !assetCableMap ){
			assetCableMap = new AssetCableMap(
												cable : "Cable"+connector.connector,
												fromAsset: assetEntity,
												fromConnectorNumber : connector,
												status : connector.status
												)
		}
		if(assetEntity?.rackTarget && connector.type == "Power" && connector.label?.toLowerCase() == 'pwr1' && !assetCableMap.toPower){
			assetCableMap.toAsset = assetEntity
			assetCableMap.toAssetRack = assetEntity?.rackTarget?.tag
			assetCableMap.toAssetUposition = 0
			assetCableMap.toConnectorNumber = null
			assetCableMap.toPower = "A"
		}
		if ( !assetCableMap.validate() || !assetCableMap.save(flush: true) ) {
			def etext = "Unable to create assetCableMap for assetEntity ${assetEntity}" +
			GormUtil.allErrorsString( assetCableMap )
			println etext
		} else {
			println "Created PowerCable for assetEntity ${assetEntity}"
		}
	}
}

