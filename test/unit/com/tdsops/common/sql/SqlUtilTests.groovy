package com.tdsops.common.sql

import grails.test.*

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

	}
	
}