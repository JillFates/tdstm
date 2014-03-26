package com.tds.asset

import com.tdssrc.grails.GormUtil

class Files extends AssetEntity{
	String fileFormat
	String LUN
	
    static constraints = {
		fileFormat( blank:false, nullable:false )
		LUN( blank:true, nullable:true )
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
		modifiedBy = Person.loggedInPerson
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
		modifiedBy = Person.loggedInPerson
	}
	String toString(){
		"id:$id name:$assetName "
	}

}
