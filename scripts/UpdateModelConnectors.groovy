import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetCableStatus


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
			AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
							assetToPort=null where assetTo = ${assetEntity.id}""")
			AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom = ${assetEntity.id}")
			def assetCableMap = new AssetCableMap(
													cable : "Cable"+powerConnector.connector,
													assetFrom: assetEntity,
													assetFromPort : powerConnector,
													cableStatus : powerConnector.status
													)
			if(assetEntity?.rackTarget ){
				assetCableMap.assetTo = assetEntity
				assetCableMap.assetToPort = null
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
				def assetCableMap = AssetCableMap.findByAssetFromAndAssetFromPort( assetEntity, connector )
				if( !assetCableMap ){
					assetCableMap = new AssetCableMap(
														cable : "Cable"+connector.connector,
														assetFrom: assetEntity,
														assetFromPort : connector,
														cableStatus : connector.status
														)
				}
				if(assetEntity?.rackTarget && connector.type == "Power" && connector.label?.toLowerCase() == 'pwr1' && !assetCableMap.toPower){
					assetCableMap.assetTo = assetEntity
					assetCableMap.assetToPort = null
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
				if(!modelConnectors.id?.contains(assetCableMap.assetFromPort?.id)){
					AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
												assetToPort=null where assetToPort = ${assetCableMap.assetFromPort?.id}""")
					AssetCableMap.executeUpdate("delete AssetCableMap where assetFromPort = ${assetCableMap.assetFromPort?.id}")
				}
			}
		}
	}
	 println "Updated Model: '${modelInstance}' id : ${modelInstance.id} connectors and PowerCable for ${assetEntityList.size()} assets"
}




