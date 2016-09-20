package com.tdsops.common.sql

import spock.lang.Specification

class SqlUtilTests extends Specification {

	void testAppendToWhere() {
		expect:
		'name="Tim"' == SqlUtil.appendToWhere('', 'name="Tim"')
		'age=5 and name="Tim"' == SqlUtil.appendToWhere('age=5', 'name="Tim"')
		'age=5 or name="Tim"' == SqlUtil.appendToWhere('age=5', 'name="Tim"', 'or')
	}

	void testWhereExpression() {
		def map

		// map = whereExpression(property, criteria, paramName, isNot=false)

		// Test the default EQUALs expression
		when:
		map = SqlUtil.whereExpression('name', 'jack', 'np')
		then:
		'name = :np' == map.sql
		'jack' == map.param

		// Test with expression in the criteria
		when:
		map = SqlUtil.whereExpression('name', '>=5', 'np')
		then:
		'name >= :np' == map.sql
		'5' == map.param

		// Test LIKE clause
		when:
		map = SqlUtil.whereExpression('name', 'j%', 'np')
		then:
		'name LIKE :np' == map.sql
		'j%' == map.param

		// Test LIKE with NOT in the clause
		when:
		map = SqlUtil.whereExpression('name', 'j%', 'np', true)
		then:
		'name NOT LIKE :np' == map.sql
		'j%' == map.param

		// Test IN clause with an array
		when:
		def a = ['a', 'b', 'c']
		map = SqlUtil.whereExpression('name', a, 'np', false)
		then:
		'name IN (:np)' == map.sql
		map.param instanceof List
		a[0] == map.param[0]

		// Test NOT IN clause with an array
		when:
		map = SqlUtil.whereExpression('name', a, 'np', true)
		then:
		'name NOT IN (:np)' == map.sql
		map.param instanceof List
		a[0] == map.param[0]
	}
}
