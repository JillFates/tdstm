package net.transitionmanager.service

import net.transitionmanager.asset.Application
import net.transitionmanager.common.SettingService
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.reporting.MetricReportingService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.ValidationType
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import net.transitionmanager.command.metricdefinition.MetricDefinitionCommand
import net.transitionmanager.command.metricdefinition.MetricDefinitionsCommand
import net.transitionmanager.command.metricdefinition.QueryCommand
import net.transitionmanager.command.metricdefinition.WhereCommand
import net.transitionmanager.metric.MetricResult
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyType
import net.transitionmanager.project.Project
import net.transitionmanager.common.Setting
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

class MetricReportingServiceSpec extends Specification implements ServiceUnitTest<MetricReportingService>, DataTest, GrailsWebUnitTest{

	void setupSpec() {
		mockDomains PartyGroup, PartyType, Project, Setting, MetricResult, Project
	}

	void 'Test gatherMetric for invalid mode'() {
		setup: 'Given a function JSON structure'
			JSONObject function = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "malfunction",
					"function"   : "testMetric2"
			]
		when: 'gatherMetric is called with the JSON'
			service.gatherMetric([1l, 2l, 3l], (String) function.metricCode, function)
		then: 'gatherMetric throw and InvalidParameterException'
			thrown InvalidParamException
	}

	void 'Test gatherMetric for function'() {
		setup: 'Given a function JSON structure'
			JSONObject function = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "function",
					"function"   : "testMetric"
			]
		when: 'gatherMetric is called with the JSON'
			List results = service.gatherMetric([1l, 2l, 3l], (String) function.metricCode, function)
		then: 'gatherMetric returns list of map values, for each project'
			results.size() == 3
			results[0].projectId == 1l
			results[0].metricCode == 'APP-VPS'
			results[0].date == (new Date() - 1).format('yyyy-MM-dd')
			results[0].label == 'count'
			results[0].value >= 0
			results[0].value <= 500
	}

	void 'Test gatherMetric for invalid function'() {
		setup: 'Given a function JSON structure'
			JSONObject function = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "function",
					"function"   : "testMetric2"
			]
		when: 'gatherMetric is called with the JSON'
			List results = service.gatherMetric([1l, 2l, 3l], (String) function.metricCode, function)
		then: 'gatherMetric returns an empty list'
			thrown InvalidParamException
	}

	void 'Test gatherMetric for invalid function log error false'() {
		setup: 'Given a function JSON structure'
			JSONObject function = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "function",
					"function"   : "testMetric2"
			]
		when: 'gatherMetric is called with the JSON'
			service.gatherMetric([1l, 2l, 3l], (String) function.metricCode, function)
		then: 'gatherMetric throw and InvalidParameterException'
			thrown InvalidParamException
	}

	void 'Test processAggregation count'() {
		setup: 'Given an aggregation string'
			String aggregation = 'count(*)'
		when: 'processAggregation is called with the aggregation'
			String processedAggregation = service.processAggregation(aggregation)
		then: 'processAggregation returns a process aggregation string'
			processedAggregation == 'count(*)'
	}

	void 'Test processAggregation sum'() {
		setup: 'Given an aggregation string'
			String aggregation = 'sum(project.id)'
		when: 'processAggregation is called with the aggregation'
			String processedAggregation = service.processAggregation(aggregation)
		then: 'processAggregation returns a process aggregation string'
			processedAggregation == "SUM (COALESCE( project.id, 0 ) )"
	}

	void 'Test getLabel'() {
		setup: 'Given a list of groupBys, and an aggregation string'
			List groupBys = ['planStatus', 'assetType', 'os']
			String aggregation = 'count(*)'
		when: 'getLabel is called with groupBys and aggregation'
			String label = service.getLabel(groupBys, aggregation)
		then: 'getLabel returns a hql string for generating the label'
			label == "concat(COALESCE(planStatus, 'Unknown'), :colon, COALESCE(assetType, 'Unknown'), :colon, COALESCE(os, 'Unknown'))"
	}

	void 'Test getLabel one groupBy'() {
		setup: 'Given a list of one groupBy, and an aggregation string'
			List groupBys = ['planStatus']
			String aggregation = 'count(*)'
		when: 'getLabel is called with groupBys and aggregation'
			String label = service.getLabel(groupBys, aggregation)
		then: 'getLabel returns a hql string for generating the label'
			label == "concat(COALESCE(planStatus, 'Unknown'))"
	}

	void 'Test getLabel no groupBy'() {
		setup: 'Given an empty list of one groupBys, and an aggregation string'
			List groupBys = []
			String aggregation = 'count(*)'
		when: 'getLabel is called with groupBys and aggregation'
			String label = service.getLabel(groupBys, aggregation)
		then: 'getLabel returns a hql string for generating the label'
			label == "'count'"
	}

	void 'Test getGroupBy'() {
		setup: 'Given a list of groupBys'
			List groupBys = ['planStatus', 'assetType', 'os']
		when: 'getGroupBy is called with groupBys'
			String groupBy = service.getGroupBy(groupBys)
		then: 'getGroupBy returns the hql for the group by'
			groupBy == 'group by planStatus, assetType, os'
	}

	void 'Test getGroupBy one group'() {
		setup: 'Given a list of one groupBy'
			List groupBys = ['planStatus']
		when: 'getGroupBy is called with groupBys'
			String groupBy = service.getGroupBy(groupBys)
		then: 'getGroupBy returns the hql for the group by'
			groupBy == 'group by planStatus'
	}

	void 'Test getGroupBy no groups'() {
		setup: 'Given an empty list of groupBys'
			List groupBys = []
		when: 'getGroupBy is called with groupBys'
			String groupBy = service.getGroupBy(groupBys)
		then: 'getGroupBy returns the hql for the group by'
			groupBy == ''
	}

	void 'Test getWhere'() {
		setup: 'Given a list of where JSON structure'
			List wheres = [
				[column: 'moveBundle.useForPlanning', expression: '= true'],
				[column: 'validation', expression: "in ('${ValidationType.PLAN_READY}', 'Confirmed')"],
				[column: 'moveBundle.useForPlanning', expression: '= true']
			]
		when: 'getWhere is called with wheres'
			String where = service.getWhere(wheres)
		then: 'getWhere generates the hql for the wheres'
			where == "and moveBundle.useForPlanning = true and validation in (\'${ValidationType.PLAN_READY}\', \'Confirmed\') and moveBundle.useForPlanning = true"
	}

	void 'Test getWhere one where in list'() {
		setup: 'Given a list of where JSON structure'
			List wheres = [
					[column: 'moveBundle.useForPlanning', expression: '= true']
			]
		when: 'getWhere is called with wheres'
			String where = service.getWhere(wheres)
		then: 'getWhere generates the hql for the wheres'
			where == 'and moveBundle.useForPlanning = true'
	}

	void 'Test getWhere no where in list'() {
		setup: 'Given a list of where JSON structure'
			List wheres = []
		when: 'getWhere is called with wheres'
			String where = service.getWhere(wheres)
		then: 'getWhere generates the hql for the wheres'
			where == ''
	}

	void 'test getQuery'() {

		setup: 'Given a query JSON structure'
			JSONObject query = [
					"groupBy"    : [
							"planStatus",
							"assetType"
					],
					"domain"     : "Device",
					"join"       : [
							[
									"domain": "Dependency",
									"on"    : "Dependency.asset.id = Application.id"
							]
					],
					"aggregation": "count(*)",
					"where"      : [
							[
									"column"    : "validation",
									"expression": "in ('${ValidationType.PLAN_READY}', 'Confirmed')"
							]
					]
			] as JSONObject
			String expected = """
				select project.id,
						concat(COALESCE(planStatus, 'Unknown'), :colon, COALESCE(assetType, 'Unknown')) as label,
						count(*) as value
				from AssetEntity
				where project.id in (:projectIds) and validation in ('${ValidationType.PLAN_READY}', 'Confirmed') and assetClass = 'DEVICE' and moveBundle.useForPlanning = 1
				group by planStatus, assetType, project.id
				""".stripIndent()

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query).stripIndent()
		then: 'getQuery returns an HQL string'
			expected == hql
	}

	void 'test getQuery dependency'() {

		setup: 'Given a query JSON structure'
			JSONObject query = [
					"groupBy"    : [
							"type"
					],
					"domain"     : "Dependency",
					"aggregation": "count(*)",
					"where"      : [
							[
									"column"    : "type",
									"expression": "in ('Validated')"
							]
					]
			] as JSONObject
			String expected = """
				select asset.project.id,
						concat(COALESCE(type, 'Unknown')) as label,
						count(*) as value
				from AssetDependency
				where asset.project.id in (:projectIds) and type in ('Validated') and asset.moveBundle.useForPlanning = 1 and dependent.moveBundle.useForPlanning = 1
				group by type, asset.project.id
				""".stripIndent()

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query).stripIndent()
		then: 'getQuery returns an HQL string'
			expected == hql
	}

	void 'test getQuery task'() {

		setup: 'Given a query JSON structure'
			JSONObject query = [
				"groupBy"    : [
					"status"
				],
				"domain"     : "Task",
				"aggregation": "count(*)",
				"where"      : [
					[
						"column"    : "status",
						"expression": "in ('Complete')"
					]
				]
			] as JSONObject
			String expected = """
					select project.id,
							concat(COALESCE(status, 'Unknown')) as label,
							count(*) as value
					from AssetComment
					where project.id in (:projectIds) and status in ('Complete') and isPublished = 1 and commentType = 'issue'
					group by status, project.id
					""".stripIndent()

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query).stripIndent()
		then: 'getQuery returns an HQL string'
			expected == hql
	}

	void 'test getQuery no where'() {

		setup: 'Given a query JSON structure no where'
			JSONObject query = [
					"groupBy"    : [
							"planStatus",
							"assetType"
					],
					"domain"     : "Device",
					"join"       : [
							[
									"domain": "Dependency",
									"on"    : "Dependency.asset.id = Application.id"
							]
					],
					"aggregation": "count(*)"
			] as JSONObject

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query)

		then: 'getQuery returns an HQL string'
			hql == """
				select project.id,
						concat(COALESCE(planStatus, 'Unknown'), :colon, COALESCE(assetType, 'Unknown')) as label,
						count(*) as value
				from AssetEntity
				where project.id in (:projectIds) and assetClass = 'DEVICE' and moveBundle.useForPlanning = 1
				group by planStatus, assetType, project.id
				""".stripIndent()
	}

	void 'test getQuery one groupBy'() {

		setup: 'Given a query JSON structure no where'
			JSONObject query = [
					"groupBy"    : [
							"planStatus"
					],
					"domain"     : "Device",
					"join"       : [
							[
									"domain": "Dependency",
									"on"    : "Dependency.asset.id = Application.id"
							]
					],
					"aggregation": "count(*)"
			] as JSONObject

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query)

		then: 'getQuery returns an HQL string'
			hql == """
					select project.id,
							concat(COALESCE(planStatus, 'Unknown')) as label,
							count(*) as value
					from AssetEntity
					where project.id in (:projectIds) and assetClass = 'DEVICE' and moveBundle.useForPlanning = 1
					group by planStatus, project.id
					""".stripIndent()
	}

	void 'test getQuery no groupBy'() {

		setup: 'Given a query JSON structure no where'
			JSONObject query = [
					"domain"     : "Device",
					"join"       : [
							[
									"domain": "Dependency",
									"on"    : "Dependency.asset.id = Application.id"
							]
					],
					"aggregation": "count(*)"
			] as JSONObject

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query)

		then: 'getQuery returns an HQL string'
			hql == """
				select project.id,
						'count' as label,
						count(*) as value
				from AssetEntity
				where project.id in (:projectIds) and assetClass = 'DEVICE' and moveBundle.useForPlanning = 1
				group by project.id
				""".stripIndent()
	}


	void 'TM-10850 test getQuery invalid domain in query'() {

		setup: 'Given a query JSON structure no where'
			JSONObject query = [
				"domain"     : "device",//invalid for ETLDomain
				"join"       : [
					[
						"domain": "Dependency",
						"on"    : "Dependency.asset.id = Application.id"
					]
				],
				"aggregation": "count(*)"
			] as JSONObject

		when: 'getQuery is called with the JSON query'
			service.getQuery(query)

		then: 'getQuery returns an HQL string'
			thrown InvalidParamException
	}

	void 'test processWheres'() {
		setup: 'Given a list of where definitions and a domainClass'
			List<Map> whereDefinitions = []
			Class domainClass = Application
			ETLDomain etlDomain = ETLDomain.Application
		when: 'processWheres is called on the definitions and the domain class'
			List<Map> processedWhereDefinitions = service.processWheres(whereDefinitions, domainClass, etlDomain)
		then: 'the resulting map will have'
			processedWhereDefinitions == [[column: 'moveBundle.useForPlanning', expression: '= 1']]
	}

	void 'test processWheres AssetEntity'() {
		setup: 'Given a list of where definitions and a domainClass'
			List<Map> whereDefinitions = []
			Class domainClass = AssetEntity
			ETLDomain etlDomain = ETLDomain.Asset
		when: 'processWheres is called on the definitions and the domain class'
			List<Map> processedWhereDefinitions = service.processWheres(whereDefinitions, domainClass, etlDomain)
		then: 'the resulting map will have'
			processedWhereDefinitions == [[column: 'moveBundle.useForPlanning', expression: '= 1']]
	}

	void 'test processWheres AssetDependency'() {
		setup: 'Given a list of where definitions and a domainClass'
			List<Map> whereDefinitions = []
			Class domainClass = AssetDependency
			ETLDomain etlDomain = ETLDomain.Dependency
		when: 'processWheres is called on the definitions and the domain class'
			List<Map> processedWhereDefinitions = service.processWheres(whereDefinitions, domainClass, etlDomain)
		then: 'the resulting map will have'
			processedWhereDefinitions == [
				[column: 'asset.moveBundle.useForPlanning', expression: '= 1'],
				[column: 'dependent.moveBundle.useForPlanning', expression: '= 1']
			]
	}

	void 'test processWheres AssetComment'() {
		setup: 'Given a list of where definitions and a domainClass'
			List<Map> whereDefinitions = []
			Class domainClass = AssetComment
			ETLDomain etlDomain = ETLDomain.Task
		when: 'processWheres is called on the definitions and the domain class'
			List<Map> processedWhereDefinitions = service.processWheres(whereDefinitions, domainClass, etlDomain)
		then: 'the resulting map will have'
			processedWhereDefinitions == [
				[column: 'isPublished', expression: '= 1'],
				[column: 'commentType', expression: "= 'issue'"]
			]
	}

	void 'test saveDefinitions sql definition'() {

		setup: 'Given a MetricDefinitionsCommand object with a SQL definition.'
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'sql'
				query = null
				function = null
				sql = 'Select * from Table'
			}

			metricDefinitions.definitions = [definition]

		when: 'The definition is saved using the service.'
			Map definitions = service.saveDefinitions(metricDefinitions, 0)

		then: 'The definition returned is the sql definition, as JSON.'
			definitions.definitions == """[{
   "mode": "sql",
   "query": null,
   "function": null,
   "description": "A description.",
   "metricCode": "The code...",
   "enabled": 1,
   "sql": "Select * from Table"
}]""".stripIndent()
	}

	void 'test saveDefinitions function definition'() {

		setup: 'Given a MetricDefinitionsCommand object with a function definition.'
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'function'
				query = null
				function = 'TestFunction'
				sql = null
			}

			metricDefinitions.definitions = [definition]

		when: 'The definition is saved using the service.'
			Map definitions = service.saveDefinitions(metricDefinitions, 0)

		then: 'The definition returned is the function definition, as JSON.'
			definitions.definitions == """[{
   "mode": "function",
   "query": null,
   "function": "TestFunction",
   "description": "A description.",
   "metricCode": "The code...",
   "enabled": 1,
   "sql": null
}]""".stripIndent()
	}

	void 'test saveDefinitions query definition'() {

		setup: 'Given a MetricDefinitionsCommand object with a query definition'
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			WhereCommand whereCommand = new WhereCommand()
			whereCommand.column = 'validation'
			whereCommand.expression = "in ('${ValidationType.PLAN_READY}', 'Confirmed')"

			QueryCommand queryCommand = new QueryCommand()
			queryCommand.with {
				domain = 'Device'
				aggregation = 'count(*)'
				groupBy = ['planStatus', 'assetType']
				where = [whereCommand]
			}


			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'query'
				query = queryCommand
				function = null
				sql = null
			}

			metricDefinitions.definitions = [definition]

		when: 'The definition is saved using the service'
			Map definitions = service.saveDefinitions(metricDefinitions, 0)

		then: 'The definition returned is the query definition, as JSON.'
			definitions.definitions == """[{
   "mode": "query",
   "query": {
      "domain": "Device",
      "aggregation": "count(*)",
      "where": [{
         "expression": "in ('${ValidationType.PLAN_READY}', 'Confirmed')",
         "column": "validation"
      }],
      "groupBy": [
         "planStatus",
         "assetType"
      ]
   },
   "function": null,
   "description": "A description.",
   "metricCode": "The code...",
   "enabled": 1,
   "sql": null
}]""".stripIndent()
	}


	void 'test getDefinitions function definition'() {

		setup: 'Given a function definition is saved to the DB'
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'function'
				query = null
				function = 'TestFunction'
				sql = null
			}

			metricDefinitions.definitions = [definition]
			service.saveDefinitions(metricDefinitions, 0)

		when: 'When getDefinitions is called.'
			Map definitions = service.getDefinitions()

		then: 'The function definition is returned, as JSON'
			definitions.definitions == """[{
   "mode": "function",
   "query": null,
   "function": "TestFunction",
   "description": "A description.",
   "metricCode": "The code...",
   "enabled": 1,
   "sql": null
}]""".stripIndent()
	}


	@ConfineMetaClassChanges([MetricReportingService])
	void 'test generateDailyMetrics'() {

		setup: 'Given a function definition is saved to the database.'
			int writeData = 0
			service.metaClass.projectIdsForMetrics = { -> [1, 2, 3] }
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
			service.metaClass.writeMetricData = { Map<String, ?> data ->  writeData++}
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'function'
				query = null
				function = 'testMetric'
				sql = null
			}

			metricDefinitions.definitions = [definition]
			service.saveDefinitions(metricDefinitions, 0)

		when: 'generateDailyMetrics is called'
			Map metrics = service.generateDailyMetrics()
			List<MetricResult> results = MetricResult.list()

		then: 'The results are 3 metrics run, and 3 results in the db.'
			metrics == [metrics:3, errorCount:0, errors: '']
			writeData == 3
	}

	@ConfineMetaClassChanges([MetricReportingService])
	void 'test testMetric'() {

		setup: 'Given a function metric definition.'
			service.metaClass.projectIdsForMetrics = { -> [1] }
			service.settingService = new SettingService()
			service.settingService.transactionManager = getTransactionManager()
		service
			MetricDefinitionsCommand metricDefinitions = new MetricDefinitionsCommand()
			MetricDefinitionCommand definition = new MetricDefinitionCommand()

			definition.with {
				metricCode = 'The code...'
				description = 'A description.'
				enabled = 1
				mode = 'function'
				query = null
				function = 'testMetric'
				sql = null
			}

			metricDefinitions.definitions = [definition]

		when: 'When that definition is tested using testMetric.'
			List<Map> metrics = service.testMetric('The code...',metricDefinitions)
		then: 'The results are returned.'
			metrics[0].projectId == 1
			metrics[0].metricCode == 'The code...'
			metrics[0].date == (new Date() - 1).format('yyyy-MM-dd')
			metrics[0].label == 'count'
			metrics[0].value >= 1
			metrics[0].value <= 500
	}
}
