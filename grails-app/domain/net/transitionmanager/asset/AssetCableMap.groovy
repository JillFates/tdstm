package net.transitionmanager.asset

import net.transitionmanager.model.ModelConnector

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
		assetLoc nullable: true, inList: ['S', 'T']
		assetTo nullable: true
		assetToPort nullable: true
		cable blank: false
		cableColor nullable: true, inList: ['White', 'Grey', 'Green', 'Yellow', 'Orange',
		                                    'Red', 'Blue', 'Purple', 'Black']
		cableComment nullable: true
		cableLength nullable: true
		cableStatus blank: false
		toPower nullable: true
	}

	static mapping = {
		version false
		autoTimestamp false
		id column: 'asset_cable_map_id'
	}

	String toString() {
		"$cable : from $assetFrom to $assetTo"
	}
}
