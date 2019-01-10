package com.tdsops.common.sql

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.SizeScale
import net.transitionmanager.search.FieldSearchData
import spock.lang.Specification
import spock.lang.Unroll

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

	void 'Test parseParameter for a numeric field with some simple expressions'() {
		expect: "the sql expression is built correctly and also the param"
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "id",
				filter: filter
			])
			SqlUtil.parseParameter(fsd)
			fsd.sqlSearchExpression == expression
			fsd.sqlSearchParameters.size() == 1
			fsd.sqlSearchParameters["id"] == parameter
		where:
			filter | expression  | parameter
			"-10"  | "id <> :id" | 10
			"<>10" | "id <> :id" | 10
			"<10"  | "id < :id"  | 10
			">10"  | "id > :id"  | 10
			"<=10" | "id <= :id" | 10
			">=10" | "id >= :id" | 10
			"=10"  | "id = :id"  | 10
	}

	void 'Test parseParameter for a string field with some simple expressions'() {
		expect: "the sql expression is built correctly and also the param"
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "assetName",
				filter: filter
			])
			SqlUtil.parseParameter(fsd)
			fsd.sqlSearchExpression == expression
			fsd.sqlSearchParameters.size() == 1
			fsd.sqlSearchParameters["assetName"] == parameter
		where:
			filter      | expression                      | parameter
			"-alpha"    | "assetName NOT LIKE :assetName" | "%alpha%"
			"<>alpha"   | "assetName NOT LIKE :assetName" | "%alpha%"
			"\"alpha\"" | "assetName = :assetName"        | "alpha"
			"!alpha"    | "assetName <> :assetName"       | "alpha"
	}

	void 'Test parseParameter on a string field for IN/NOT IN LIST scenarios'() {
		expect: "an IN or NOT IN expression is built."
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "assetName",
				filter: filter
			])
			SqlUtil.parseParameter(fsd)
			fsd.sqlSearchExpression == expression
			fsd.sqlSearchParameters.size() == 2
			fsd.sqlSearchParameters["assetName__0"] == param0
			fsd.sqlSearchParameters["assetName__1"] == param1
		where:
			filter   | expression                                        | param0 | param1
			"ab|bc"  | "assetName IN (:assetName__0, :assetName__1)"     | "ab"   | "bc"
			"-ab|bc" | "assetName NOT IN (:assetName__0, :assetName__1)" | "ab"   | "bc"
			"!ab|bc" | "assetName NOT IN (:assetName__0, :assetName__1)" | "ab"   | "bc"

	}

	void 'Test parseParameter on a string field with multiple LIKEs'() {
		expect: "a LIKE expression is built."
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "assetName",
				filter: filter
			])
			SqlUtil.parseParameter(fsd)
			fsd.sqlSearchExpression == expression
			fsd.sqlSearchParameters.size() == 2
			fsd.sqlSearchParameters["assetName__0"] == param0
			fsd.sqlSearchParameters["assetName__1"] == param1
		where:
			filter   | expression                                                                | param0 | param1
			"ab:bc"  | "(assetName LIKE :assetName__0 OR assetName LIKE :assetName__1)"          | "%ab%" | "%bc%"
			"!ab:bc" | "(assetName NOT LIKE :assetName__0 AND assetName NOT LIKE :assetName__1)" | "%ab%" | "%bc%"
			"-ab:bc" | "(assetName NOT LIKE :assetName__0 AND assetName NOT LIKE :assetName__1)" | "%ab%" | "%bc%"

	}

	void 'Test parseParameter for a string field with custom wildcards'() {
		expect: "a LIKE expression is constructed recognizing the wildcards introduced by the user."
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "assetName",
				filter: filter
			])
			SqlUtil.parseParameter(fsd)
			fsd.sqlSearchExpression == expression
			fsd.sqlSearchParameters.size() == 1
			fsd.sqlSearchParameters["assetName"] == parameter
		where:
			filter    | expression                  | parameter
			"alpha%"  | "assetName LIKE :assetName" | "alpha%"
			"alpha*"  | "assetName LIKE :assetName" | "alpha%"
			"%alpha"  | "assetName LIKE :assetName" | "%alpha"
			"*alpha"  | "assetName LIKE :assetName" | "%alpha"
			"%alpha%" | "assetName LIKE :assetName" | "%alpha%"
			"*alpha*" | "assetName LIKE :assetName" | "%alpha%"

	}

	void 'Test parseParameter using column alias'() {
		when: "creating a FieldSearchData using column alias"
			FieldSearchData fsd = new FieldSearchData([
				domain     : Application,
				column     : "assetName",
				filter     : "!alpha",
				columnAlias: "appName"
			])
			SqlUtil.parseParameter(fsd)
		then: "the alias is used for creating the parameters"
			fsd.sqlSearchExpression == "assetName <> :appName"
		and: "there's only one parameter"
			fsd.sqlSearchParameters.size() == 1
		and: "the parameter is using the alias"
			fsd.sqlSearchParameters["appName"] == "alpha"
	}

	void "Test parseParameter with invalid parameters"() {
		when: "passing no filter"
			FieldSearchData fsd = new FieldSearchData([
				domain: Application,
				column: "assetName"
			])
			SqlUtil.parseParameter(fsd)
		then:
			thrown RuntimeException

		when: 'giving no domain'
			fsd = new FieldSearchData([
				column: "assetName",
				filter: "!alpha"
			])
			SqlUtil.parseParameter(fsd)
		then:
			thrown RuntimeException

		when: 'giving no filter'
			fsd = new FieldSearchData([
				column: "assetName",
				domain: Application
			])
			SqlUtil.parseParameter(fsd)
		then:
			thrown RuntimeException

		when: 'giving an empty filter'
			fsd = new FieldSearchData([
				column: "assetName",
				domain: Application,
				filter: ""
			])
			SqlUtil.parseParameter(fsd)
		then:
			thrown RuntimeException
	}

	@Unroll
	void 'test can filtering Enum fields with filter {#filter} with HQL expression {#sqlSearchExpression}'() {

		setup: 'a FieldSearchData request'
			FieldSearchData fsd = new FieldSearchData([
				domain     : AssetEntity,
				column     : 'AE.scale',
				filter     : filter,
				type       : SizeScale,
				columnAlias: 'scale'
			])

		expect:
			SqlUtil.parseParameter(fsd)

		and: "contains an hql query"
			fsd.sqlSearchExpression == sqlSearchExpression

		and: 'contains parameters'
			fsd.sqlSearchParameters?.scale == sqlSearchParameters

		where:
			filter           || sqlSearchExpression      || sqlSearchParameters
			'Mega'           || 'AE.scale IN :scale'     || [SizeScale.MB]
			'%Mega%'         || 'AE.scale IN :scale'     || [SizeScale.MB]
			'=Mega'          || 'AE.scale IN :scale'     || [SizeScale.MB]
			'!Mega'          || 'AE.scale NOT IN :scale' || [SizeScale.MB]
			'-Mega'          || 'AE.scale NOT IN :scale' || [SizeScale.MB]
			'Mega|Peta|Giga' || 'AE.scale IN :scale'     || [SizeScale.MB, SizeScale.GB, SizeScale.PB]
			'Mega:Peta'      || 'AE.scale IN :scale'     || [SizeScale.MB, SizeScale.PB]
			'Mega&Peta'      || ' 1 = 0'                 || null
			'byte'           || 'AE.scale IN :scale'     || [SizeScale.KB, SizeScale.MB, SizeScale.GB, SizeScale.TB, SizeScale.PB]
			'FUBAR'          || ' 1 = 0'                 || null
			'%ga%'           || 'AE.scale IN :scale'     || [SizeScale.MB, SizeScale.GB]
			'*FUBAR*'        || ' 1 = 0'                 || null
			'Peta&by'        || 'AE.scale IN :scale'     || [SizeScale.PB]
			'P%'             || 'AE.scale IN :scale'     || [SizeScale.PB] // starts with P
			'%obyte'         || 'AE.scale IN :scale'     || [SizeScale.KB] // ends with obyte
			'%obyt*'         || 'AE.scale IN :scale'     || [SizeScale.KB] // contains obyt
			'%ob*t*'         || 'AE.scale IN :scale'     || [SizeScale.KB] // contains ob*t
	}

	@Unroll
	void 'test can convert filter {#filter} to regex content {#regexPattern}'() {

		expect:
			SqlUtil.convertFilterToRegex(filter) == regexPattern

		where:
			filter           || regexPattern
			'Mega'           || '.*Mega.*'
			'P%'             || '^P.*'
			'P$'             || '^P\\$'
			'(Mega)'         || '\\(Mega\\)'
			'[Mega]'         || '\\[Mega\\]'
			'{Mega}'         || '\\{Mega\\}'
			'P?'             || '^P\\?'
			'%obyte'         || '.*obyte$'
			'%obyte%'        || '.*obyte.*'
			'%obyte*'        || '.*obyte.*'
			'%ob%te%'        || '.*ob.*te.*'
			'%ob*te*'        || '.*ob.*te.*'
			'Mega|Peta'      || '.*(Mega|Peta).*'
			'Mega|Peta|Giga' || '.*(Mega|Peta|Giga).*'
			'Mega:Peta'      || '.*(Mega|Peta).*'
			'Mega:Peta:Giga' || '.*(Mega|Peta|Giga).*'
			'Mega&Peta'      || '(?=.*Mega)(?=.*Peta).*'
			'Peta&by'        || '(?=.*Peta)(?=.*by).*'
			'by&Peta'        || '(?=.*by)(?=.*Peta).*'
	}

}
