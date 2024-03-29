package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil

class Files extends AssetEntity {

	// Override default value set by parent class
	String assetType = 'Logical Storage'

	AssetClass assetClass = AssetClass.STORAGE
	String fileFormat
	String LUN

	static constraints = {
		fileFormat nullable: true, size: 0..255
		LUN(nullable: true, size: 0..255)
	}

	static mapping = {
		autoTimestamp false
		tablePerHierarchy false
		id column: 'files_id'
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"id:$id name:$assetName"
	}
}
