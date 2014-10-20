/**
 * Unit test cases for testing all of the Enum classes
 */
package com.tdsops.tm.enums.domain

import grails.test.GrailsUnitTestCase

import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files

class AssetClassTests extends GrailsUnitTestCase {

	// Validate that domainNameFor returns the expected GORM domain class name for each AssetClass
	void testDomainNameFor() {
		assertEquals "Application", AssetClass.domainNameFor(AssetClass.APPLICATION)
		assertEquals "AssetEntity", AssetClass.domainNameFor(AssetClass.DEVICE)
		assertEquals "Database", AssetClass.domainNameFor(AssetClass.DATABASE)
		assertEquals "Files", AssetClass.domainNameFor(AssetClass.STORAGE)
	}

	// Validate that domainClassFor returns the appropriate GORM domain classes as expected for each AssetClass
	void testDomainClassFor() {
		assertEquals Application, AssetClass.domainClassFor(AssetClass.APPLICATION)
		assertEquals AssetEntity, AssetClass.domainClassFor(AssetClass.DEVICE)
		assertEquals Database, AssetClass.domainClassFor(AssetClass.DATABASE)
		assertEquals Files, AssetClass.domainClassFor(AssetClass.STORAGE)
	}

	// Test that getClassOptions returns a map with some of the expected keys
	void testGetClassOptions() {
		Map map = AssetClass.getClassOptions()
		assertTrue 'Contains App', map.containsKey('APPLICATION')
		assertTrue 'Contains SERVER-DEVICE', map.containsKey('SERVER-DEVICE')
		assertTrue 'Contains STORAGE-DEVICE', map.containsKey('STORAGE-DEVICE')
		assertTrue 'Contains OTHER-DEVICE', map.containsKey('OTHER-DEVICE')
	}

}
