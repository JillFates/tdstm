package com.tds.asset

//import org.codehaus.groovy.grails.orm.hibernate.cfg.IdentityEnumType

import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil

class Database extends AssetEntity {

	AssetClass assetClass = AssetClass.DATABASE
	String dbFormat
	
    static constraints = {
		dbFormat( blank:false, nullable:false )
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
