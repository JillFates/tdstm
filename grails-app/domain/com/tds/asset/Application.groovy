package com.tds.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.Person

class Application extends AssetEntity {
	static final String       UNKNOWN     = "Unknown"
	static final List<String> CRITICALITY = [
		'Mission Critical',
		'Business Critical',
		'Business Operational',
		'Administrative Services',
		'Critical',
		'Major',
		'Important',
		'Minor'
	]

	// Override default value set by parent class
	String assetType = 'Application'

	AssetClass assetClass = AssetClass.APPLICATION
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

		appAccess nullable: true, size: 0..255
		appFunction nullable: true, size: 0..255
		appSource nullable: true, size: 0..255
		appTech nullable: true, size: 0..255
		appVendor nullable: true, size: 0..255
		appVersion nullable: true, size: 0..255
		businessUnit nullable: true, size: 0..255
		criticality nullable: true, size: 0..25, inList: CRITICALITY
		drRpoDesc nullable: true, size: 0..255
		drRtoDesc nullable: true, size: 0..255
		latency nullable: true, size: 0..255
		license nullable: true, size: 0..255
		moveDowntimeTolerance nullable: true, size: 0..255
		shutdownBy nullable: true, size: 0..255
		shutdownDuration nullable: true
		shutdownFixed nullable: true
		sme nullable: true
		sme2 nullable: true
		startupBy nullable: true, size: 0..255
		startupDuration nullable: true
		startupFixed nullable: true
		startupProc nullable: true, size: 0..255
		testingBy nullable: true, size: 0..255
		testingDuration nullable: true
		testingFixed nullable: true
		testProc nullable: true, size: 0..255
		url nullable: true, size: 0..255
		useFrequency nullable: true, size: 0..255
		userCount nullable: true, size: 0..255
		userLocations nullable: true, size: 0..255
	}

	static mapping = {
		autoTimestamp false
		tablePerHierarchy false
		id column: 'app_id'
		sme column: 'sme_id'
		sme2 column: 'sme2_id'
		columns {
			shutdownFixed sqltype: 'tinyint(1)'
			startupFixed sqltype: 'tinyint(1)'
			testingFixed sqltype: 'tinyint(1)'
		}
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"id:$id name:$assetName tag:$appVendor"
	}

	boolean belongsToClient(client) {
		ownerId == client?.id
	}
}
