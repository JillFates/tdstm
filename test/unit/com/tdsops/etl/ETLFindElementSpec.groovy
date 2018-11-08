package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdssrc.grails.JsonUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import net.transitionmanager.domain.Project
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@TestFor(AssetEntity)
@TestMixin([DomainClassUnitTestMixin])
@Mock([AssetOptions])
class ETLFindElementSpec extends ETLBaseSpec {

	ETLProcessor processor
	ETLFieldsValidator validator

	def setup() {
		validator = createDomainClassFieldsValidator()
		processor = new ETLProcessor(
			Mock(Project),
			Mock(DataSetFacade),
			Mock(DebugConsole),
			validator)
	}

	@Unroll
	void 'test can assign a #domainClass domain in a find element current domain'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, domainClass, 1)

		expect: 'find command creation assign a current'
			(find.currentDomain == domainClass) == isValid

		and:
			(find.mainSelectedDomain == domainClass) == isValid

		and:
			(find.currentFind.domain == domainClass.name()) == isValid

		where:
			domainClass           || isValid
			ETLDomain.Application || true
			ETLDomain.Device      || true
			ETLDomain.Room        || true
			ETLDomain.Rack        || true
	}

	void 'test can build a list of conditions from a JSON object'() {

		given: 'an instance on JSON Object'
			List json = JsonUtil.parseJsonList('''
				[
					{ "propertyName": "assetName", "operator":"notContains", "value": "prod"},
					{ "propertyName": "priority", "operator":"gt", "value": 4}
				]
			''')

		when: 'FindCondition is used to build a list of conditions'
			List<FindCondition> conditions = FindCondition.buildCriteria(json)

		then: 'a list of conditions was built'
			conditions.size() == 2
			conditions[0].propertyName == 'assetName'
			conditions[0].operator == FindOperator.notContains
			conditions[0].value == 'prod'

			conditions[1].propertyName == 'priority'
			conditions[1].operator == FindOperator.gt
			conditions[1].value == 4

	}

	@Unroll
	void 'test can assign a #anotherDomainClass domain in an elseFind element with #domainClass as a main domain'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, domainClass, 1)

		and: 'it assigns another domain class'
			find.elseFind anotherDomainClass

		expect: 'find command creation assign a current'
			(find.currentDomain == anotherDomainClass) == isValid

		and:
			(find.mainSelectedDomain == domainClass) == isValid

		and:
			(find.currentFind.domain == anotherDomainClass.name()) == isValid

		where:
			domainClass           | anotherDomainClass    || isValid
			ETLDomain.Application | ETLDomain.Application || true
			ETLDomain.Device      | ETLDomain.Device      || true
			ETLDomain.Room        | ETLDomain.Rack        || true
			ETLDomain.Application | ETLDomain.Asset       || true
	}

	@Unroll
	void 'test can add a find statement with #aPropertyName eq #aConditionValue'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, domainClass, 1)

		and: 'an Element class as a local variable'
			Element srcNameVar = new Element(originalValue: aConditionValue, value: aConditionValue, processor: processor)

		and: 'it adds an eq statement'
			find.by aPropertyName eq srcNameVar

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete() == isComplete
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], aPropertyName, FindOperator.eq, aConditionValue, isComplete)
			}

		where:
			domainClass           | aPropertyName | aConditionValue || isComplete
			ETLDomain.Application | 'assetName'   | 'zulu01'        || true
			ETLDomain.Application | 'assetName'   | null            || true
	}

	@Unroll
	void 'test can append a find statement with #aPropertyName eq #aConditionValue'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, domainClass, 1)

		and: 'an Element class as a local variable'
			Element srcIPVar = new Element(originalValue: aConditionValue, value: aConditionValue, processor: processor)

		and: 'it appends an eq statement'
			find.by 'assetName' eq 'zulu01' and aPropertyName eq srcIPVar

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete() == isComplete
				conditions.size() == 2
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.eq, 'zulu01')
				assertFindConditionComplete(conditions[1], aPropertyName, FindOperator.eq, aConditionValue, isComplete)
			}

		where:
			domainClass      | aPropertyName | aConditionValue || isComplete
			ETLDomain.Device | 'ipAddress'   | '192.168.1.100' || true
			ETLDomain.Device | 'ipAddress'   | null            || true
	}

	void 'test can throw an ETLProcessorException if find command was incorrectly prepared'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		when: 'it appends an eq statement'
			find.by 'assetName' \
 				    and 'IP Address' eq '192.168.1.100'   \
 				    into 'id'

		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == 'Incorrect structure for find command'
				startLine == null
				endLine == null
				startColumn == null
				endColumn == null
				fatal == true
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can add find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'it appends an eq statement'
			find.by 'assetName' eq 'zulu01'   \
 				    and 'IP Address' eq '192.168.1.100'   \
 				    into 'id'

		then:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 2
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.eq, 'zulu01')
				assertFindConditionComplete(conditions[1], 'ipAddress', FindOperator.eq, '192.168.1.100')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use ne in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'it appends an eq statement'
			find.by 'assetName' ne 'zulu01' into 'id'

		then:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.ne, 'zulu01')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use nseq in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'it appends an nseq statement'
			find.by 'assetName' nseq 'zulu01' into 'id'

		then:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.nseq, 'zulu01')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use lt in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'it appends an lt statement'
			find.by 'priority' lt 3 into 'id'

		then:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.lt, 3)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use le in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'it appends an le statement'
			find.by 'priority' le 3 into 'id'

		then:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.le, 3)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use gt in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an gt statement'
			find.by 'priority' gt 4 into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.gt, 4)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use ge in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an ge statement'
			find.by 'priority' ge 4 into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.ge, 4)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use like in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an like statement'
			find.by 'Name' like 'zulu%' into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.like, 'zulu%')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use notLike in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an notLike statement'
			find.by 'Name' notLike 'zulu%' into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.notLike, 'zulu%')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use contains in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an contains statement'
			find.by 'Name' contains 'zulu' into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.contains, 'zulu')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use notContains in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an notContains statement'
			find.by 'Name' notContains 'zulu' into 'id'

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'assetName', FindOperator.notContains, 'zulu')
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use inList in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an inList statement'
			find.by('Environment').inList(['QA', 'Development']).into('id')

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'environment', FindOperator.inList, ['QA', 'Development'])
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use notInList in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an notInList statement'
			find.by('Environment').notInList(['QA', 'Development']).into('id')

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'environment', FindOperator.notInList, ['QA', 'Development'])
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use between in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an between statement'
			find.by('priority').between(4..6).into('id')

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.between, 4..6)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use notBetween in a find statement in ETL Processor results'() {

		given: 'an instance of ETLFindElement class'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Device, 1)

		and: 'an instance of ETLProcessor correctly configured'
			processor.domain ETLDomain.Dependency
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			processor.pushIntoStack find

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		and: 'it appends an notBetween statement'
			find.by('priority').notBetween(4..6).into('id')

		expect:
			with(find.currentFind.statement, FindStatementBuilder) {
				currentCondition.isComplete()
				conditions.size() == 1
				assertFindConditionComplete(conditions[0], 'priority', FindOperator.notBetween, 4..6)
			}
	}
}
