package com.tds.asset

import com.tdssrc.grails.GormUtil

class Database extends AssetEntity{
	String dbFormat
	Integer dbSize
	
    static constraints = {
		dbFormat( blank:false, nullable:false )
		dbSize( nullable:false )
    }
	static mapping  = {
		table "data_base"
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'db_id'
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	String toString(){
		"id:$id name:$assetName "
	}

}
