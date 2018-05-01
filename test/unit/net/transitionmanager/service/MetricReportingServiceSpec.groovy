package net.transitionmanager.service

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

@TestFor(MetricReportingService)
@TestMixin(GrailsUnitTestMixin)
class MetricReportingServiceSpec extends Specification {


	void setup() {}

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
			results[0].date == (new Date() -1).format('yyyy-MM-dd')
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
					[column: 'validation', expression: "in ('BundleReady', 'Confirmed')"],
					[column: 'moveBundle.useForPlanning', expression: '= true']
			]
		when: 'getWhere is called with wheres'
			String where = service.getWhere(wheres)
		then: 'getWhere generates the hql for the wheres'
			where == 'and moveBundle.useForPlanning = true and validation in (\'BundleReady\', \'Confirmed\') and moveBundle.useForPlanning = true'
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
									"expression": "in ('BundleReady', 'Confirmed')"
							]
					]
			] as JSONObject

		when: 'getQuery is called with the JSON query'
			String hql = service.getQuery(query)

		then: 'getQuery returns an HQL string'
			hql == """
			select project.id,
					concat(COALESCE(planStatus, 'Unknown'), :colon, COALESCE(assetType, 'Unknown')) as label,
					count(*) as value
			from AssetEntity
			where project.id in (:projectIds) and validation in ('BundleReady', 'Confirmed') and moveBundle.useForPlanning = 1 
			group by planStatus, assetType, project.id
			""".stripIndent()
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
				where project.id in (:projectIds) and moveBundle.useForPlanning = 1 
				group by planStatus, assetType, project.id
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
				where project.id in (:projectIds) and moveBundle.useForPlanning = 1 
				group by project.id
				""".stripIndent()
	}
}
