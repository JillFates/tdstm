import com.tds.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.MetricReportingService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.jdbc.BadSqlGrammarException
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class MetricReportingServiceIntegrationSpec extends IntegrationSpec {
	MetricReportingService metricReportingService
	@Shared
	AssetEntityTestHelper  assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	DataImportService dataImportService

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Project otherProject = projectTestHelper.createProject()

	@Shared
	MoveBundle moveBundle

	@Shared
	AssetEntity device

	@Shared
	AssetEntity device2

	@Shared
	AssetEntity otherProjectDevice

	@Shared
	Map context

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject,
																	 moveBundleTestHelper.createBundle(otherProject, null))
		context = dataImportService.initContextForProcessBatch(project, ETLDomain.Dependency)
		context.record = new ImportBatchRecord(sourceRowId: 1)

		device.assetType = 'Server'
		device.validation = 'BundleReady'
		device.save(flush: true, failOnError: true)

		device2.assetType = 'Server'
		device2.validation = 'Discovery'
		device2.save(flush: true, failOnError: true)

		// Create a second project with a device with the same name and type as device above
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.validation = 'Discovery'
		otherProjectDevice.save(flush: true, failOnError: true)
	}


	void "test gatherMetric for query mode"() {
		setup: 'giving a metric definition for a query, that will have results'
			String date = new Date().format('yyyy-MM-dd')
			JSONObject metricDefinition = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "query",
					"query"      : [
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
											"expression": "in ('Discovery', 'BundleReady')"
									]
							]
					]
			] as JSONObject
		when: 'running gatherMetrics on query mode'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'We get a list of map results'
			results == [
					[
							projectId : project.id,
							metricCode: 'APP-VPS',
							date      : date,
							label     : 'Unassigned:Server',
							value     : 2
					],
					[
							projectId : otherProject.id,
							metricCode: 'APP-VPS',
							date      : date,
							label     : 'Unassigned:Server',
							value     : 1
					]
			]
	}

	void "test gatherMetric for query mode no results"() {
		setup: 'giving metric definition with a query that will not return any results'
			JSONObject metricDefinition = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "query",
					"query"      : [
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
											"expression": "in ('Diasdfscovery', 'sadfsdf')"
									]
							]
					]
			] as JSONObject
		when: 'running gatherMetrics on query mode'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'We get an empty list'
			results == []
	}

	void "test gatherMetric for sql mode"() {
		setup: 'giving sql metric definition'
			String date = new Date().format('yyyy-MM-dd')
			JSONObject metricDefinition = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "sql",
					sql          : """
						select a.project_id as projectId,
								'APP-VPS' as metricCode, 
								DATE_FORMAT(NOW(), "%Y-%m-%d") as date,
								concat(a.plan_status, ':', a.asset_type) as label,
								count(*) as value
						from asset_entity a
						join move_bundle m on m.move_bundle_id=a.move_bundle_id
						where a.project_id in (:projectIds) and a.validation in ('Discovery', 'BundleReady') and m.use_for_planning = true 
						group by a.plan_status, a.asset_type, a.project_id;
					""".stripIndent().toString()
			]
		when: 'running gatherMetrics on sql query'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'Get Results for the query'
			results == [
					[
							projectId : project.id,
							metricCode: 'APP-VPS',
							date      : date,
							label     : 'Unassigned:Server',
							value     : 2
					],
					[
							projectId : otherProject.id,
							metricCode: 'APP-VPS',
							date      : date,
							label     : 'Unassigned:Server',
							value     : 1
					]
			]
	}

	void "test gatherMetric for sql mode invalid sql"() {
		setup: 'giving sql metric definition with errors in the SQL'
			String date = new Date().format('yyyy-MM-dd')
			JSONObject metricDefinition = [
					"metricCode" : "APP-VPS",
					"description": "Application counts metrics for Validation/PlanStatus",
					"enabled"    : true,
					"mode"       : "sql",
					sql          : """
							select a.project_id as projectId,
									'APP-VPS' as metricCode 
									DATE_FORMAT(NOW(), "%Y-%m-%d") as date,
									concat(a.plan_status, ':', a.asset_type) as label,
									count(*) as value
							from asset_entity a
							join move_bundle m on m.move_bundle_id=a.move_bundle_id
							where a.project_id in (:projectIds) and a.validation in ('Discovery', 'BundleReady') and m.use_for_planning = true 
							group by a.plan_status, a.asset_type, a.project_id;
						""".stripIndent().toString()
			]
		when: 'running gatherMetrics on sql query'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'Get exception for bad sql grammer'
			thrown BadSqlGrammarException
	}
}
