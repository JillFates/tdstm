package com.tds.asset
class AssetCableMap {
	String cable
	String cableComment
	AssetEntity assetFrom
	AssetEntity assetTo
	ModelConnector assetFromPort
	ModelConnector assetToPort
	String cableStatus
	String cableColor
	Integer cableLength
	String toPower
	String assetLoc = 'S'
	
	static constraints = {
		cable( nullable:false, blank:false )
		cableComment( nullable:true, blank:true )
		assetFrom( nullable:false )
		assetTo( nullable:true )
		assetFromPort( nullable:false )
		assetToPort( nullable:true )
		cableStatus( nullable:false, blank:false)
		cableColor( nullable:true, blank:true, inList: ['White', 'Grey', 'Green', 'Yellow', 'Orange', 'Red', 'Blue', 'Purple', 'Black'] )
		cableLength( nullable:true )
		toPower( nullable:true, blank:true)
		assetLoc( nullable: true , inList:['S', 'T'])
	}
	
	static mapping = {
		version false
		autoTimestamp false
		id column: 'asset_cable_map_id'
	}

	String toString() {
		"${cable} : from ${assetFrom} to ${assetTo}"
	}
}
