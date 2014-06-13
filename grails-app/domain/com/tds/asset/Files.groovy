package com.tds.asset

//import org.codehaus.groovy.grails.orm.hibernate.cfg.IdentityEnumType

import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil

class Files extends AssetEntity {

	AssetClass assetClass = AssetClass.STORAGE
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
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
		// modifiedBy = Person.loggedInPerson
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
		// modifiedBy = Person.loggedInPerson
	}
	String toString(){
		"id:$id name:$assetName "
	}

}
