package com.tds.asset

import PartyGroup

import com.tdssrc.grails.GormUtil

class Application extends AssetEntity{
	String appVendor
	String appVersion
	PartyGroup owner
	String sme
	String sme2
	String url
	String appTech
	String appAccess
	String appSource
	String license
	String supportType
	String businessUnit
	String criticality
	String appFunction
	String environment
	String useFrequency
	String userLocations
	String userConcurrent
	String userCount
	
	String drRpoDesc
	String drRtoDesc
	String moveDowntimeTolerance
	
    static constraints = {
		appVendor( blank:true, nullable:true )
		appVersion( blank:true, nullable:true )
		appOwner( blank:true, nullable:true )
		sme( blank:true, nullable:true )
		sme2( blank:true, nullable:true )
		url( blank:true, nullable:true )
		appTech( blank:true, nullable:true )
		appAccess( blank:true, nullable:true )
		appSource( blank:true, nullable:true )
		license( blank:true, nullable:true )
		supportType( blank:true, nullable:true )
		businessUnit( blank:true, nullable:true )
		criticality( blank:true, nullable:true )
		appFunction( blank:true, nullable:true )
		environment( blank:true, nullable:true )
		useFrequency( blank:true, nullable:true )
		userLocations( blank:true, nullable:true )
		userConcurrent( blank:true, nullable:true )
		userCount( blank:true, nullable:true )
		
		drRpoDesc( blank:true, nullable:true )
		drRtoDesc( blank:true, nullable:true )
		moveDowntimeTolerance( blank:true, nullable:true )
    }
	static mapping  = {
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'app_id'
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
		"id:$id name:$assetName tag:$appVendor"
	}

}
