package com.tds.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil

class Database extends AssetEntity {

	// Override default value set by parent class
	String assetType = 'Database'

	AssetClass assetClass = AssetClass.DATABASE
	String dbFormat

	static constraints = {
		dbFormat nullable: true, size: 0..255
	}

	static mapping = {
		table 'data_base'
		autoTimestamp false
		tablePerHierarchy false
		id column: 'db_id'
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"id:$id name:$assetName"
	}
}
