class AssetCableMap {
	String cable
	AssetEntity fromAsset
	AssetEntity toAsset
	Integer fromConnectorNumber
	Integer toConnectorNumber
	Integer state
	
	static constraints = {
		cable( nullable:false, blank:false )
		fromAsset( nullable:false, blank:false )
		toAsset( nullable:false, blank:false )
		fromConnectorNumber( nullable:true, blank:true )
		toConnectorNumber( nullable:true, blank:true )
		state( nullable:true, blank:true )
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
