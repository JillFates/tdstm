import com.tds.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MetricResult
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
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(
				AssetClass.DEVICE,
				otherProject,
				moveBundleTestHelper.createBundle(otherProject, null)
		)

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

	void 'test projectIdsForMetrics'() {

		setup: 'Given 3 new projects, 1 that has a start/completion date in the range for projectIdsForMetrics'
			test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
			Project project1 = projectTestHelper.createProject()
			Project project2 = projectTestHelper.createProject()
			Project project3 = projectTestHelper.createProject()

			project1.collectMetrics = 1
			project1.startDate = new Date() - 5
			project1.save(flush: true, failOnError: true)

			project2.collectMetrics = 1
			project2.startDate = project2.startDate + 5
			project2.save(flush: true, failOnError: true)

			project3.collectMetrics = 1
			project3.completionDate = new Date() - 1
			project3.save(flush: true, failOnError: true)

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
			List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate  + 1, null, ['someCode'])
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
				List<Map> metricData = metricReportingService.getMetrics(startDate - 1, startDate  + 1, null, ['invalid'])
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