import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.ValidationType
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.imports.ImportBatchRecord
import net.transitionmanager.metric.MetricResult
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.service.DataImportService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.reporting.MetricReportingService
import org.grails.web.json.JSONObject
import org.springframework.jdbc.BadSqlGrammarException
import spock.lang.Shared
import spock.lang.See
import spock.lang.Specification
import test.helper.ApplicationTestHelper
import test.helper.AssetEntityTestHelper

@Integration
@Rollback
class MetricReportingServiceIntegrationSpec extends Specification {
	MetricReportingService metricReportingService
	@Shared
	AssetEntityTestHelper  assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	ApplicationTestHelper applicationTestHelper = new ApplicationTestHelper()

	@Shared
	DataImportService dataImportService

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project

	@Shared
	Project otherProject

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetEntity device3

	/**
	 * an application in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	Application application1

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	Application application2

	@Shared
	AssetEntity otherProjectDevice

	@Shared
	Map context

	/**
	 * a dependency that has it's asset and dependent in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetDependency dependency1

	/**
	 * a dependency that has it's asset in moveBundle2(usedForPlanning = 0) and dependent in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetDependency dependency2

	/**
	 * a dependency that has it's asset in moveBundle(usedForPlanning = 1) and dependent in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetDependency dependency3

	void setup() {
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()

		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(project, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle2)

		application1 = applicationTestHelper.createApplication(AssetClass.APPLICATION, project, moveBundle)
		application2 = applicationTestHelper.createApplication(AssetClass.APPLICATION, project, moveBundle2)

		otherProjectDevice = assetEntityTestHelper.createAssetEntity(
			AssetClass.DEVICE,
			otherProject,
			moveBundleTestHelper.createBundle(otherProject, null)
		)

		context = dataImportService.initContextForProcessBatch(project, ETLDomain.Dependency)
		context.record = new ImportBatchRecord(sourceRowId: 1)

		device.assetType = 'Server'
		device.validation = ValidationType.PLAN_READY
		device.save(flush: true)

		device2.assetType = 'Server'
		device2.validation = ValidationType.UNKNOWN
		device2.save(flush: true)

		moveBundle2.useForPlanning = 0
		moveBundle2.save(flush: true)

		// Create a second project with a device with the same name and type as device above
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.validation = ValidationType.UNKNOWN
		otherProjectDevice.save(flush: true)

		dependency1 = new AssetDependency(asset: application1, dependent: device, status: AssetDependencyStatus.VALIDATED).save(flush: true)
		dependency2 = new AssetDependency(asset: application2, dependent: device2, status: AssetDependencyStatus.VALIDATED).save(flush: true)
		dependency3 = new AssetDependency(asset: application1, dependent: device3, status: AssetDependencyStatus.VALIDATED).save(flush: true)
	}

	void "test gatherMetric for query mode"() {
		setup: 'giving a metric definition for a query, that will have results'
			String date = (new Date() - 1).format('yyyy-MM-dd')
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
							"expression": "in ('${ValidationType.UNKNOWN}', '${ValidationType.PLAN_READY}')"
						]
					]
				]
			] as JSONObject
		when: 'running gatherMetrics on query mode'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'We get a list of map results'
			results[0] == [
				projectId : project.id,
				metricCode: 'APP-VPS',
				date      : date,
				label     : 'Unassigned:Server',
				value     : 2
			]

			results[1] == [
				projectId : otherProject.id,
				metricCode: 'APP-VPS',
				date      : date,
				label     : 'Unassigned:Server',
				value     : 1
			]
	}

	@See('TM-10727')
	void 'test gatherMetric for query mode filtering of Device'() {
		setup: 'giving a metric definition for a query, that will have results'
			String date = (new Date() - 1).format('yyyy-MM-dd')
			JSONObject metricDefinition = [
				"metricCode" : "DEV-COUNT",
				"description": "Device counts metrics",
				"enabled"    : true,
				"mode"       : "query",
				"query"      : [
					"domain"     : "Device",
					"aggregation": "count(*)"
				]
			] as JSONObject
		when: 'running gatherMetrics using query mode definition'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'two map results with expect counts of JUST devices and no applications'
			results[0] == [
				projectId : project.id,
				metricCode: 'DEV-COUNT',
				date      : date,
				label     : 'count',
				value     : 2
			]

			results[1] == [
				projectId : otherProject.id,
				metricCode: 'DEV-COUNT',
				date      : date,
				label     : 'count',
				value     : 1
			]
	}

	@See('TM-10662')
	void "test gatherMetric for filtering out non-planning assets"() {
		setup: 'giving a metric definition for a query, that will have results'
			String date = (new Date() - 1).format('yyyy-MM-dd')
			JSONObject metricDefinition = [
				"metricCode" : "App-Count",
				"description": "count of applications",
				"enabled"    : 1,
				"mode"       : "query",
				"query"      : [
					"domain"     : "Application",
					"aggregation": "count(*)"
				]
			] as JSONObject
		when: 'running gatherMetrics on query mode'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'We get a list of map results'
			results == [
				[
					projectId : project.id,
					metricCode: 'App-Count',
					date      : date,
					label     : 'count',
					value     : 1
				]
			]
	}

	@See('TM-10647')
	void "test gatherMetric for query Dependency"() {
		setup: 'giving a metric definition for a query, that will have results'
			String date = (new Date() - 1).format('yyyy-MM-dd')
			JSONObject metricDefinition = [
				"metricCode" : "Dependency",
				"description": "Dependency counts metrics DependencyStatus",
				"enabled"    : 1,
				"mode"       : "query",
				"query"      : [
					"domain"     : "Dependency",
					"aggregation": "count(*)",
					"groupBy"    : ["status"]
				]
			] as JSONObject
		when: 'running gatherMetrics on query mode'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'We get a list of map results'
			results == [
				[
					projectId : project.id,
					metricCode: 'Dependency',
					date      : date,
					label     : AssetDependencyStatus.VALIDATED,
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
			String date = (new Date() - 1).format('yyyy-MM-dd')
			JSONObject metricDefinition = [
				metricCode : "APP-VPS",
				description: "Application counts metrics for Validation/PlanStatus",
				enabled    : true,
				mode       : "sql",
				sql        : """
					select a.project_id as projectId,
						'APP-VPS' as metricCode,
						DATE_FORMAT(SUBDATE(NOW(), 1), "%Y-%m-%d") as date,
						concat(a.plan_status, ':', a.asset_type) as label,
						count(*) as value
					from asset_entity a
					join move_bundle m on m.move_bundle_id=a.move_bundle_id
					where a.project_id in (:projectIds) and a.validation in ('${ValidationType.UNKNOWN}', '${ValidationType.PLAN_READY}') and m.use_for_planning = true
					group by a.plan_status, a.asset_type, a.project_id
					order by a.project_id, a.plan_status, a.asset_type;
					""".stripIndent().toString()
			]
		when: 'running gatherMetrics on sql query'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'the results should have three expected map sets of values'
			results[0] == [
				projectId : project.id,
				metricCode: 'APP-VPS',
				date      : date,
				label     : 'Unassigned:Application',
				value     : 1
			]

			results[1] == [
				projectId : project.id,
				metricCode: 'APP-VPS',
				date      : date,
				label     : 'Unassigned:Server',
				value     : 2
			]

			results[2] == [
				projectId : otherProject.id,
				metricCode: 'APP-VPS',
				date      : date,
				label     : 'Unassigned:Server',
				value     : 1
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
							where a.project_id in (:projectIds) and a.validation in ('${ValidationType.UNKNOWN}', '${
					ValidationType.PLAN_READY
				}') and m.use_for_planning = true
							group by a.plan_status, a.asset_type, a.project_id;
						""".stripIndent().toString()
			]
		when: 'running gatherMetrics on sql query'
			List results = metricReportingService.gatherMetric([project.id, otherProject.id], (String) metricDefinition.metricCode, metricDefinition)
		then: 'Get exception for bad sql grammer'
			thrown BadSqlGrammarException
	}

	void 'test projectIdsForMetrics'() {

		setup: 'Given 3 new projects, 1 that has a start/completion date in the range for projectIdsForMetrics'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Project project2 = projectTestHelper.createProject()
			Project project3 = projectTestHelper.createProject()

			project1.collectMetrics = 1
			project1.startDate = new Date() - 5
			project1.save(flush: true)

			project2.collectMetrics = 1
			project2.startDate = project2.startDate + 5
			project2.save(flush: true)

			project3.collectMetrics = 1
			project3.completionDate = new Date() - 1
			project3.save(flush: true)

		when: 'projectIdsForMetrics() is run'
			List<Long> projectIds = metricReportingService.projectIdsForMetrics()

		then: 'The resulting list will have the first project, but not the others.'

			projectIds.contains(project1.id)
			!projectIds.contains(project2.id)
			!projectIds.contains(project3.id)

	}


	void 'test writeMetricData'() {

		setup: 'Given a metric data map.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date date = new Date().clearTime() - 1

			Map metricData = [
				projectId : project1.id,
				metricCode: 'someCode',
				date      : date.format('yyyy-MM-dd'),
				label     : 'someLabel',
				value     : 5
			]
		when: 'writeMetricData is called on the map of data.'
			metricReportingService.writeMetricData(metricData)
			MetricResult result = MetricResult.findByProjectAndDate(project1, date)

		then: 'The results can be retrieved from the database.'
			result.project.id == project1.id
			result.metricDefinitionCode == 'someCode'
			result.label == 'someLabel'
			result.value == 5
	}

	void 'test getMetrics'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'get metrics is called filtering to just one of those records based on date.'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, null, null)
		then: 'The one metric is returned.'
			metricData.size() == 1
			metricData[0].projectGuid == project1.guid
			metricData[0].metricCode == 'someCode'
			metricData[0].date == startDate.format('yyyy-MM-dd')
			metricData[0].label == 'someLabel'
			metricData[0].value == 5
	}

	void 'test getMetrics filter guid'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'Calling getMetrics filtering by the project guid.'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, project1.guid, null)
		then: 'The metric data is returned.'
			metricData.size() == 1
			metricData[0].projectGuid == project1.guid
			metricData[0].metricCode == 'someCode'
			metricData[0].date == startDate.format('yyyy-MM-dd')
			metricData[0].label == 'someLabel'
			metricData[0].value == 5
	}

	void 'test getMetrics filter invalid guid'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'Calling getMetrics with an invalid project guid.'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, 'invalid', null)
		then: 'No metric data is returned.'
			metricData.size() == 0
	}

	void 'test getMetrics metric code'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'Calling getMetrics filtering with a metric code'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, null, ['someCode'])
		then: 'The metric data is returned.'
			metricData.size() == 1
			metricData[0].projectGuid == project1.guid
			metricData[0].metricCode == 'someCode'
			metricData[0].date == startDate.format('yyyy-MM-dd')
			metricData[0].label == 'someLabel'
			metricData[0].value == 5
	}

	void 'test getMetrics invalid metric code'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'Calling getMetrics filtering with an invalid metric code'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, null, ['invalid'])
		then: 'No data is returned.'
			metricData.size() == 0
	}

	void 'test getMetrics by project id'() {
		setup: 'Given a list of metrics saved to the DB with a range of dates.'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Date previousDate = new Date().clearTime() - 1
			Date startDate = new Date().clearTime() + 5
			Date beyondDate = new Date().clearTime() + 10

			List<Map> metrics = [
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : previousDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : startDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				],
				[
					projectId : project1.id,
					metricCode: 'someCode',
					date      : beyondDate.format('yyyy-MM-dd'),
					label     : 'someLabel',
					value     : 5
				]
			]

			metrics.each { Map metricData ->
				metricReportingService.writeMetricData(metricData)
			}
		when: 'Calling getMetrics filtering by project id'
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate + 1, null, null, project1.id)
		then: 'The metric data is returned without the project guid.'
			metricData.size() == 1
			metricData[0].projectGuid == null
			metricData[0].metricCode == 'someCode'
			metricData[0].date == startDate.format('yyyy-MM-dd')
			metricData[0].label == 'someLabel'
			metricData[0].value == 5
	}
}