package com.tds.asset

import com.tdssrc.grails.GormUtil

class Files extends AssetEntity{
	String fileFormat
	Integer fileSize
	String LUN
	String sizeUnit = "GB"
	
    static constraints = {
		fileFormat( blank:false, nullable:false )
		fileSize( nullable:false )
		LUN( blank:true, nullable:true )
		sizeUnit( blank:false, nullable:false, inList:['TB','GB','MB'])
    }
	static mapping  = {
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'files_id'
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
