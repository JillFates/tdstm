package com.tdsops.common.sql

import grails.test.*
import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.AssetEntity

class SqlUtilTests extends GrailsUnitTestCase {
	
	public void testAppendToWhere() {
		
		assertEquals 'name="Tim"', SqlUtil.appendToWhere('', 'name="Tim"')
		assertEquals 'age=5 and name="Tim"', SqlUtil.appendToWhere('age=5', 'name="Tim"')
		assertEquals 'age=5 or name="Tim"', SqlUtil.appendToWhere('age=5', 'name="Tim"', 'or')

	}
	
	public void testWhereExpression() {
		def map
		
		// map = whereExpression(property, criteria, paramName, isNot=false)
		
		// Test the default EQUALs expression
		map = SqlUtil.whereExpression('name', 'jack', 'np')
		assertEquals 'name = :np', map.sql
		assertEquals 'jack', map.param
		
		// Test with expression in the criteria
		map = SqlUtil.whereExpression('name', '>=5', 'np')
		assertEquals 'name >= :np', map.sql
		assertEquals '5', map.param
		
		// Test LIKE clause
		map = SqlUtil.whereExpression('name', 'j%', 'np')
		assertEquals 'name LIKE :np', map.sql
		assertEquals 'j%', map.param

		// Test LIKE with NOT in the clause
		map = SqlUtil.whereExpression('name', 'j%', 'np', true)
		assertEquals 'name NOT LIKE :np', map.sql
		assertEquals 'j%', map.param
		
		// Test IN clause with an array
		def a = ['a','b','c']
		map = SqlUtil.whereExpression('name', a, 'np', false)
		assertEquals 'name IN (:np)', map.sql
		assertTrue (map.param instanceof List)
		assertEquals a[0], map.param[0]

		// Test NOT IN clause with an array
		map = SqlUtil.whereExpression('name', a, 'np', true)
		assertEquals 'name NOT IN (:np)', map.sql
		assertTrue (map.param instanceof List)
		assertEquals a[0], map.param[0]

		// Test an Enum
		map = SqlUtil.whereExpression('name', AssetClass.safeValueOf('DEVICE'), 'assetClass')
		assertEquals 'Testing of Enum', 'name = :assetClass', map.sql
		assertTrue 'Testing of Enum', (map.param instanceof java.lang.Enum)

		// Test Domain Object
		def domainObj = new AssetEntity()
		map = SqlUtil.whereExpression('a', domainObj, 'asset')
		assertEquals 'Testing of Domain', 'a = :asset', map.sql
		assertTrue 'Testing of Domain', (map.param instanceof AssetEntity)


	}

	public void testMatchWords() {
		assertEquals 'case 1', '(a like ? and a like ? and a like ?)', SqlUtil.matchWords('a', ['x','y','z'], true, false)
		assertEquals 'case 2', '(a like ? or a like ? or a like ?)', SqlUtil.matchWords('a', ['x','y','z'], false, false)
		assertEquals 'case 3', '(a=? or a=? or a=?)', SqlUtil.matchWords('a', ['x','y','z'], false, true)
		assertEquals 'case 4', '(a=? or a=? or a=?)', SqlUtil.matchWords('a', ['x','y','z'], false, true)
		assertEquals 'empty array', '(1=1)', SqlUtil.matchWords('a', [], false, true)
	}	
}