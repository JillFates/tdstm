package com.tdsops.common.sql

import grails.test.*
import spock.lang.Specification

/**
 * Unit test cases for the SqlUtil class
*/
class SqlUtilTests extends Specification {
	
	public void testAppendToWhere() {

		expect:		
			'name="Tim"'.equals(SqlUtil.appendToWhere('', 'name="Tim"'))
			'age=5 and name="Tim"'.equals(SqlUtil.appendToWhere('age=5', 'name="Tim"'))
			'age=5 or name="Tim"'.equals(SqlUtil.appendToWhere('age=5', 'name="Tim"', 'or'))

	}
	
	public void testWhereExpression() {
		def map
		
		// map = whereExpression(property, criteria, paramName, isNot=false)
		
		// Test the default EQUALs expression
		when:
			map = SqlUtil.whereExpression('name', 'jack', 'np')
		then:
			'name = :np' == map.sql
			'jack'.equals(map.param)
		
		// Test with expression in the criteria
		when:
			map = SqlUtil.whereExpression('name', '>=5', 'np')
		then:
			'name >= :np' == map.sql
			'5'.equals(map.param)
		
		// Test LIKE clause
		when:
			map = SqlUtil.whereExpression('name', 'j%', 'np')
		then:
			'name LIKE :np' == map.sql
			'j%'.equals(map.param)

		// Test LIKE with NOT in the clause
		when:
			map = SqlUtil.whereExpression('name', 'j%', 'np', true)
		then:
			'name NOT LIKE :np' == map.sql
			'j%'.equals(map.param)
		
		// Test IN clause with an array
		when:
			def a = ['a','b','c']
			map = SqlUtil.whereExpression('name', a, 'np', false)
		then:
			'name IN (:np)' == map.sql
			(map.param instanceof List)
			a[0].equals(map.param[0])

		// Test NOT IN clause with an array
		when:
			map = SqlUtil.whereExpression('name', a, 'np', true)
		then:			
			'name NOT IN (:np)' == map.sql
			(map.param instanceof List)
			a[0].equals(map.param[0])

	}
	
}