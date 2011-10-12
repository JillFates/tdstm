import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil


def modelInstancesList = Model.list([sort:'modelName',order:'asc'])

/*
 *  go through the table of models and if it doesn't have any connectors, add one "Pwr1" of type "power". 
 */
modelInstancesList.each { modelInstance ->
	 def powerConnector = ModelConnector.findByModel(modelInstance)
	 def assetEntityList = AssetEntity.findAllByModel( modelInstance )
	 if( !powerConnector ){
		 powerConnector = new ModelConnector(model : modelInstance,
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
		assetEntityList.each{ assetEntity ->
			AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
							toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
							where toAsset = ${assetEntity.id}""")
			AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset = ${assetEntity.id}")
			def assetCableMap = new AssetCableMap(
													cable : "Cable"+powerConnector.connector,
													fromAsset: assetEntity,
													fromConnectorNumber : powerConnector,
													status : powerConnector.status
													)
			if(assetEntity?.rackTarget ){
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
			}
			
		}
	 } else { 
		 /*
		 * go through all assets and if the pwr1 cable doesn't have a connection,
		 * create a connection to PowerA.
		 * If there isn't any target rack specified, leave it alone.
		 */
	 
		assetEntityList.each { assetEntity ->
			def modelConnectors = ModelConnector.findAllByModel( modelInstance )
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
				}
			}
			def assetCableMaps = AssetCableMap.findAllByFromAsset( assetEntity )
			assetCableMaps.each{assetCableMap->
				if(!modelConnectors.id?.contains(assetCableMap.fromConnectorNumber?.id)){
					AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
												toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
												where toConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}""")
					AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}")
				}
			}
		}
	}
	 println "Updated Model: '${modelInstance}' id : ${modelInstance.id} connectors and PowerCable for ${assetEntityList.size()} assets"
}




