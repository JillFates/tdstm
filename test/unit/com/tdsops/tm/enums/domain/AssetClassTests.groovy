package com.tdsops.tm.enums.domain

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import spock.lang.Specification

import com.tdsops.tm.enums.domain.AssetClass

class AssetClassTests extends Specification {

	// Validate that domainNameFor returns the expected GORM domain class name for each AssetClass
	void testDomainNameFor() {
		expect:
		"Application" == AssetClass.domainNameFor(AssetClass.APPLICATION)
		"AssetEntity" == AssetClass.domainNameFor(AssetClass.DEVICE)
		"Database" == AssetClass.domainNameFor(AssetClass.DATABASE)
		"Files" == AssetClass.domainNameFor(AssetClass.STORAGE)
	}

	// Validate that domainClassFor returns the appropriate GORM domain classes as expected for each AssetClass
	void testDomainClassFor() {
		expect:
		Application == AssetClass.domainClassFor(AssetClass.APPLICATION)
		AssetEntity == AssetClass.domainClassFor(AssetClass.DEVICE)
		Database == AssetClass.domainClassFor(AssetClass.DATABASE)
		Files == AssetClass.domainClassFor(AssetClass.STORAGE)
	}

	// Test that getClassOptions returns a map with some of the expected keys
	void testGetClassOptions() {
		given:
		Map map = AssetClass.classOptions

		expect:
		// Contains App
		map.containsKey('APPLICATION')
		// Contains SERVER-DEVICE
		map.containsKey('SERVER-DEVICE')
		// Contains STORAGE-DEVICE
		map.containsKey('STORAGE-DEVICE')
		// Contains OTHER-DEVICE
		map.containsKey('OTHER-DEVICE')
	}
}
