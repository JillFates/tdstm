package com.tds.asset

import Person
import Project

import com.tdsops.tm.enums.domain.ContextType
import com.tdssrc.grails.TimeUtil

class Application extends AssetEntity {
	
	static ContextType getContextType() {
		return ContextType.A
	}
	
	String appVendor
	String appVersion
	Person sme
	Person sme2
	String url
	String appTech
	String appAccess
	String appSource
	String license
	String businessUnit
	String criticality
	String appFunction
	String useFrequency
	String userLocations
	String userCount
	String latency
	String testProc
	String startupProc
	
	String drRpoDesc
	String drRtoDesc
	String moveDowntimeTolerance
	
	String shutdownBy
	Integer shutdownFixed = 0 
	Integer shutdownDuration 
	
	String startupBy
	Integer startupFixed = 0
	Integer startupDuration
	
	String testingBy
	Integer testingFixed = 0
	Integer testingDuration
	
    static constraints = {
		appVendor( blank:true, nullable:true )
		appVersion( blank:true, nullable:true )
		sme( nullable:true )
		sme2( nullable:true )
		url( blank:true, nullable:true )
		appTech( blank:true, nullable:true )
		appAccess( blank:true, nullable:true )
		appSource( blank:true, nullable:true )
		license( blank:true, nullable:true )
		businessUnit( blank:true, nullable:true )
		criticality( blank:true, nullable:true ,inList:['Critical','Major','Important','Minor'] )
		appFunction( blank:true, nullable:true )
		useFrequency( blank:true, nullable:true )
		userLocations( blank:true, nullable:true )
		userCount( blank:true, nullable:true )
		latency( blank:true, nullable:true )
		testProc( blank:true, nullable:true )
		startupProc( blank:true, nullable:true )
		
		drRpoDesc( blank:true, nullable:true )
		drRtoDesc( blank:true, nullable:true )
		moveDowntimeTolerance( blank:true, nullable:true )
		
		shutdownBy (nullable:true )
		shutdownFixed (nullable:true )
		shutdownDuration (nullable:true )
		
		startupBy (nullable:true )
		startupFixed (nullable:true )
		startupDuration (nullable:true )
		
		testingBy (nullable:true )
		testingFixed (nullable:true )
		testingDuration (nullable:true )
    }
	static mapping  = {
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'app_id'
		sme column:'sme_id'
		sme2 column:'sme2_id'
		columns {
			shutdownFixed sqltype: 'tinyint(1)'
			startupFixed sqltype: 'tinyint(1)'
			testingFixed sqltype: 'tinyint(1)'
		}
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
		modifiedBy = Person.loggedInPerson
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
		modifiedBy = Person.loggedInPerson
	}
	String toString(){
		"id:$id name:$assetName tag:$appVendor"
	}

	boolean belongsToClient(aClient) {
		return this.owner.equals(aClient)
	}
}
