package com.tds.asset
class AssetCableMap {
	String cable
	AssetEntity fromAsset
	AssetEntity toAsset
	ModelConnector fromConnectorNumber
	ModelConnector toConnectorNumber
	String toAssetRack
	Integer toAssetUposition
	String status
	String color
	String toPower
	
	static constraints = {
		cable( nullable:false, blank:false )
		fromAsset( nullable:false )
		toAsset( nullable:true )
		fromConnectorNumber( nullable:false )
		toConnectorNumber( nullable:true )
		toAssetRack( nullable:true, blank:true )
		toAssetUposition( nullable:true )
		status( nullable:false, blank:false, inList: ['missing','empty','cabled','cabledDetails'] )
		color( nullable:true, blank:true, inList: ['White', 'Grey', 'Green', 'Yellow', 'Orange', 'Red', 'Blue', 'Purple', 'Black'] )
		toPower( nullable:true, blank:true)
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
