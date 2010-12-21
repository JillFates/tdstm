class AssetCableMap {
	String cable
	AssetEntity fromAsset
	AssetEntity toAsset
	ModelConnector fromConnectorNumber
	ModelConnector toConnectorNumber
	String status
	
	static constraints = {
		cable( nullable:false, blank:false )
		fromAsset( nullable:false, blank:false )
		toAsset( nullable:true, blank:true )
		fromConnectorNumber( nullable:false, blank:false )
		toConnectorNumber( nullable:true, blank:true )
		status( nullable:false, blank:false, inList: ['missing','empty','cabled','cabledDetails'] )
	}
	
	static mapping = {
		version false
		autoTimestamp false
		id column: 'asset_cable_map_id'
	}

	String toString() {
		"${cable} : from ${fromAsset} to ${toAsset}"
	}
}
