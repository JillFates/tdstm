package net.transitionmanager.service

import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StopWatch
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import net.transitionmanager.metric.ProjectDailyMetric
import net.transitionmanager.command.metricdefinition.MetricDefinitionCommand
import net.transitionmanager.command.metricdefinition.MetricDefinitionsCommand
import net.transitionmanager.metric.MetricResult
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomUtils
import org.grails.web.json.JSONObject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
/**
 * A service for dealing with reporting metrics.
 */
class MetricReportingService {
	SettingService             settingService
	LicenseAdminService        licenseAdminService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

	private static String MetricDefinitions = 'METRIC_DEFINITIONS'

	private static String DateFormat = 'yyyy-MM-dd'

	private static String ImagesUsedLabel      = 'used'
	private static String ImagesIssuedLabel    = 'issued'
	private static String ImagesAvailableLabel = 'available'

	/**
	 * emum for the metric mode
	 */
	enum MetricMode {
		query,
		sql,
		function

		/**
		 * Looks up a MetricMode based on it's string code.
		 *
		 * @param code The code of the MetricMode to lookup.
		 *
		 * @return The MetricMode Looked up, by it's code.
		 */
		static MetricMode lookup(String code) {
			return MetricMode.enumConstantDirectory().get(code)
		}

		/**
		 * Gets a list of the values of the modes.
		 * @return
		 */
		static List<String> list() {
			return values()*.name()
		}
	}

	Date getMetricCollectionDate() {
		new Date().clearTime() - 1
	}

	/**
	 * A map of functions that can be run by gatherMetric if the mode is function.
	 */
	private Map functions = [
			testMetric   : { List<Long> projectIds, String metricCode -> testMetricFunction(projectIds, metricCode) },
			licenseMetric: { List<Long> projectIds, String metricCode -> licenseMetricFunction(projectIds, metricCode) }
	]

	/**
	 * Gathers metric data based a list of project ids and a metric definition, which has a mode query, sql and function.
	 *
	 * Query: Database queries described through JSON meta-data
	 * Function: For metric that can not be directly queried (e.g. Licensing details from the Licensing engine)
	 * SQL: Meant as a temporary solution using hand-crafted SQL until Query can address the assembling of a query from the meta-data
	 *
	 * @param projectIds a list of projects to run metrics for.
	 * @param metricCode the code of the metric being run.
	 * @param metricDefinition the JSON that represents the metric.
	 * @param logError When running in batch mode we'll want to log errors and not prevent other metrics from running,
	 * but when testing the metric we will want to send back errors.
	 *
	 * @return a list of maps, each containing metric data example:
	 * 		[
	 * 			projectId : row.projectId,
	 * 			metricCode: row.metricCode,
	 * 			date      : row.date,
	 * 			label     : row.label,
	 * 			value     : row.value
	 * 		]
	 */
	List<Map> gatherMetric(List<Long> projectIds, String metricCode, JSONObject metricDefinition) {
		MetricMode mode = MetricMode.lookup((String) metricDefinition.mode)

		switch (mode) {
			case MetricMode.query:
				runQuery((JSONObject) metricDefinition.query, projectIds, metricCode)
				break
			case MetricMode.sql:
				return runSql((String) metricDefinition.sql, projectIds)
				break
			case MetricMode.function:
				Closure function = functions[(String) metricDefinition.function]

				if (!function) {
					throw new InvalidParamException('Function for metric not implemented.')
				}

				return function(projectIds, metricCode)
				break
			default:
				throw new InvalidParamException("Mode $mode is an invalid mode for GatherMetric")
		}
	}

	/**
	 * Runs a Query based on JSON meta data
	 *
	 * @param query the JSON meta data to create the query.
	 * @param projectIds A list of project Ids to run metrics for.
	 * @param metricCode The code the represents the metric being run.
	 *
	 * @return a list of maps, each containing metric data example:
	 * 		[
	 * 			projectId : row.projectId,
	 * 			metricCode: row.metricCode,
	 * 			date      : row.date,
	 * 			label     : row.label,
	 * 			value     : row.value
	 * 		]
	 */
	private List<Map> runQuery(JSONObject query, List<Long> projectIds, String metricCode) {
		String date = metricCollectionDate.format(DateFormat)
		List results

		if (query.groupBy && query.groupBy.size > 1) {
			// the colon is in the parameters because any colon in the query will be seen an a parameter, so this is workaround.
			results = MetricResult.executeQuery(getQuery(query), [projectIds: projectIds, colon: ':'])
		} else {
			results = MetricResult.executeQuery(getQuery(query), [projectIds: projectIds])
		}

		results.collect { Object[] row ->
			[
					projectId : row[0],
					metricCode: metricCode,
					date      : date,
					label     : row[1],
					value     : row[2]
			]
		}
	}

	/**
	 * Generates the query from the JSON query definition.
	 *
	 * @param query the JSON query definition.
	 *
	 * @return and hql string that can be run to get metric data.
	 */
	String getQuery(JSONObject query) {
		ETLDomain etlDomain = ETLDomain.lookup((String) query.domain)

		if(!etlDomain){
			throw new InvalidParamException("Invalid domain (${query.domain}) specified in Metric definition (code ${query.metricCode})")
		}

		Class domainClass = etlDomain.clazz
		String domain = domainClass.simpleName
		String aggregation = processAggregation((String) query.aggregation)
		List groupBys = query?.groupBy?.clone() ?: []
		String label = getLabel(groupBys, aggregation)
		String projectReference = getProjectReference(domainClass)

		groupBys << projectReference
		String groupBy = getGroupBy(groupBys)

		List wheres = query?.where?.clone() ?: []
		processWheres(wheres, domainClass, etlDomain)
		String where = getWhere(wheres)

		return """
			select $projectReference,
					$label as label,
					$aggregation as value
			from $domain
			where $projectReference in (:projectIds) $where
			$groupBy
		""".stripIndent()
	}

	/**
	 * Adds additional where definitions, based on the domainClass used.
	 *
	 * @param whereDefinitions The list of where definitions.
	 * @param domainClass the domain class being used.
	 *
	 * @return The list of where definitions.
	 */
	List<Map> processWheres(List<Map> whereDefinitions, Class domainClass, ETLDomain originalDomain) {
		if (originalDomain == ETLDomain.Device) {
			// Need to exclude Application, Database and Storage (aka Files)
			whereDefinitions << [column: 'assetClass', expression: "= '${AssetClass.DEVICE.name()}'"]
		}

		switch (domainClass) {
			case {it in AssetEntity}:
				whereDefinitions << [column: 'moveBundle.useForPlanning', expression: '= 1']
				break
			case {it in AssetDependency}:
				whereDefinitions << [column: 'asset.moveBundle.useForPlanning', expression: '= 1']
				whereDefinitions << [column: 'dependent.moveBundle.useForPlanning', expression: '= 1']
				break
			case {it in AssetComment}:
				whereDefinitions << [column: 'isPublished', expression: '= 1']
				whereDefinitions << [column: 'commentType', expression: "= 'issue'"]
				break
		}

		return whereDefinitions
	}

	/**
	 * Applies rules to the aggregation, making them more useful. For now we just have the one rule for sum, that wraps the
	 * domain with a COALESCE ( domain, 0), so that nulls don't screw up the sum. If the aggregation is not a sum it just
	 * returns the aggregation, as is.
	 *
	 * @param aggregation The aggregation string to process.
	 *
	 * @return An updated aggregation string.
	 */
	String processAggregation(String aggregation){
		if(aggregation.toLowerCase().contains('sum')){
			String domain = aggregation.substring(aggregation.indexOf('(') + 1, aggregation.indexOf(')'))
			return("SUM (COALESCE( $domain, 0 ) )")
		}

		return aggregation
	}

	/**
	 * Generates the hql that will be run to generate the label. By default it will be the groupBys
	 * delimited by a colon. Example groupBys = ['planStatus', 'assetType', 'os'] would produce:
	 * 'concat(planStatus, :colon, assetType, :colon, os)'
	 *
	 * if there are no groupBys then the label will be the aggregation.
	 *
	 * @param groupBy a list of columns to group by
	 * @param aggregation the expression for aggregating.
	 *
	 * @return The hql that will generate the label
	 */
	String getLabel(List groupBy, String aggregation) {
		if (groupBy) {
			List sanitizedGroupBy = groupBy.collect { String group ->
				return "COALESCE($group, 'Unknown')"
			}
			return "concat(${sanitizedGroupBy.join(', :colon, ')})"
		}

		return "'${aggregation.split(/\(/)[0]}'"
	}

	String getProjectReference(Class domain) {
		if (domain in AssetDependency) {
			return 'asset.project.id'
		}

		return 'project.id'
	}

	/**
	 * Generates the hql for groupBys
	 *
	 * @param groupBys A list of the groupBys.
	 *
	 * @return the hql for the groupBys, e.g. group by column1, column2, column3 or
	 * an empty string of there are no groupBys.
	 */
	String getGroupBy(List groupBys) {
		if (groupBys) {
			return "group by ${groupBys.join(', ')}"
		}

		return ''
	}

	/**
	 * Generates the hql for the where section of the query.
	 *
	 * @param wheres the JSON that represents the wheres, e.g.:
	 * [column: 'validation', expression: "in ('PlanReady', 'Confirmed')"]
	 *
	 * @return the wheres, if there are any joined by an AND, and proceeded by an AND
	 */
	String getWhere(List wheres) {
		if (wheres) {
			return 'and ' + wheres.collect { Map where ->
				"${where.column} ${where.expression}"
			}.join(' and ')
		}

		return ''
	}

	/**
	 * Meant as a temporary solution using hand-crafted SQL until Query can address the assembling of a query from the meta-data
	 *
	 * @param query the sql query to run.
	 * @param projectIds the project ids that will be supplied as a parameter to the query.
	 *
	 * @return a list of maps, each containing metric data example:
	 * 		[
	 * 			projectId : row.projectId,
	 * 			metricCode: row.metricCode,
	 * 			date      : row.date,
	 * 			label     : row.label,
	 * 			value     : row.value
	 * 		]
	 */
	private List<Map> runSql(String query, List<Long> projectIds) {
		return namedParameterJdbcTemplate.queryForList(query, [projectIds: projectIds])
	}

	/**
	 * This is a test function to generate data, for when a metric is running in function mode.
	 *
	 * @param projectIds A list of ids to create metric data for.
	 * @param metricCode the metric code to use for the generated data.
	 *
	 * @return a list of maps, each containing metric data example:
	 * 		[
	 * 			projectId : row.projectId,
	 * 			metricCode: row.metricCode,
	 * 			date      : row.date,
	 * 			label     : row.label,
	 * 			value     : row.value
	 * 		]
	 */
	private List<Map> testMetricFunction(List<Long> projectIds, String metricCode) {
		String date = metricCollectionDate.format(DateFormat)

		projectIds.collect { Long projectId ->
			[
					projectId : projectId,
					metricCode: metricCode,
					date      : date,
					label     : 'count',
					value     : RandomUtils.nextInt(0, 499) + 1
			]
		}
	}

	/**
	 * Gets License metrics for images issued, used and available
	 * .
	 * @param projectIds The project Ids go get license metrics for.
	 * @param metricCode the metric code to use for the license metrics.
	 *
	 * @return A List of maps containing the license metrics.
	 */
	private List<Map> licenseMetricFunction(List<Long> projectIds, String metricCode) {
		Date date = metricCollectionDate
		List<Map> licenseMetrics = []
		long imagesIssued
		long imagesUsed
		Project project


		projectIds.each { Long id ->
			project = Project.get(id)
			imagesIssued = licenseAdminService.getLicenseStateMap(project)?.numberOfLicenses ?: 0
			imagesUsed = ProjectDailyMetric.findByProjectAndMetricDate(project, date)?.planningServers ?: 0

			Map metrics = [
					(ImagesUsedLabel)     : imagesUsed,
					(ImagesIssuedLabel)   : imagesIssued,
					(ImagesAvailableLabel): imagesIssued - imagesUsed
			]

			metrics.each { String label, Long value ->
				licenseMetrics << [
						projectId : project.id,
						metricCode: metricCode,
						date      : date.format(DateFormat),
						label     : label,
						value     : value
				]
			}
		}

		return licenseMetrics
	}

	/**
	 * Gets the definitions JSON for the GSP page, pretty printed with the version.
	 *
	 * @return A Map containing the definitions JSON and their version.
	 */
	Map getDefinitions() {
		Map definitions = getMetricDefinitions()
		int version = definitions?.version ?: 0
		definitions.remove('version')
		String definition = definitions.definitions ?  (definitions.definitions as JSON).toString(true) : '[]'

		return [definitions: definition, version: version]
	}

	/**
	 *
	 * @return
	 */
	Map getMetricDefinitions() {
		settingService.getAsMap(SettingType.METRIC_DEF, MetricDefinitions) ?: [:]
	}

	/**
	 *
	 * @param code
	 * @return
	 */
	JSONObject getDefinition(String code, MetricDefinitionsCommand definitions) {

		Map metricDefinition = definitions.definitions.find { MetricDefinitionCommand definition ->
			if (definition.metricCode == code) {
				return definition
			}
		}.toMap()

		if (!metricDefinition) {
			throw new InvalidParamException("Metric definition doesn't exist for $code")
		}

		return new JSONObject(metricDefinition)
	}

	/**
	 * Saves the Definitions as a Stringified JSON to the setting table.
	 *
	 * @param definitions The definitions command object that contains the metrics JSON.
	 * @param version The version of the metrics for the Settings table.
	 *
	 * @return A Map of the saved JSON, and the version of the metrics.
	 */
	Map saveDefinitions(MetricDefinitionsCommand definitions, Integer version) {
		settingService.save(
				SettingType.METRIC_DEF,
				MetricDefinitions,
				(definitions.toMap() as JSON).toString(),
				version
		)

		return getDefinitions()
	}

	/**
	 * Gets a list of project ids for metrics, that have collectMetrics set to 1.
	 *
	 * @return A list of Long project ids, that have collectMetrics enabled.
	 */
	List<Long> projectIdsForMetrics() {
		Date collectionDate = metricCollectionDate

		List<Long> projectIds = Project.where {
			collectMetrics == 1 && startDate <= collectionDate && completionDate >= collectionDate
		}.projections {
			property 'id'
		}.list()

		if (!projectIds) {
			throw new EmptyResultException('No projects found with collect metrics enabled that are currently active.')
		}

		return projectIds
	}

	/**
	 * This runs all the metrics enabled against all the projects that have collectMetrics enabled.
	 */
	Map generateDailyMetrics() {
		List<Long> projectIds = projectIdsForMetrics()
		List definitions = getMetricDefinitions().definitions ?: []
		int errors = 0
		int metrics = 0
		StopWatch stopwatch = new StopWatch()

		log.info 'generateDailyMetrics started'
		stopwatch.start()

		definitions.each { Map definition ->
			if (definition.enabled) {
				try {
					List <Map> metricsData = gatherMetric(projectIds, (String) definition.metricCode, new JSONObject(definition))

					metricsData.each { Map data ->
						writeMetricData(data)
						metrics++
					}
				} catch (Exception e) {
					errors++
					log.error("$definition.metricCode failed", e)
				}
			}

		}

		log.info("generateDailyMetrics completed, duration ${stopwatch.endDuration()}, metrics created $metrics, errors encountered $errors")
		return [metrics: metrics, errors: errors]
	}

	/**
	 * Writes metric data to the metric result table.
	 *
	 * @param data The metric result data to write to the db.
	 */
	@Transactional
	void writeMetricData(Map<String, ?> data) {
		MetricResult result = MetricResult.findOrCreateWhere(
				project: Project.load(data.projectId),
				metricDefinitionCode: data.metricCode,
				date: Date.parse(DateFormat, data.date),
				label: data.label
		)

		result.value = data.value
		result.save()
	}

	/**
	 * This runs a test for a metric based on a metric code, against projects that have collectMetrics set to 1.
	 *
	 * @param metricCode the code of the metric to run.
	 *
	 * @return A list of maps containing the metric data gathered.
	 */
	List<Map> testMetric(String metricCode, MetricDefinitionsCommand definitions) {
		List<Long> projectIds = projectIdsForMetrics()
		JSONObject metricDefinition = getDefinition(metricCode, definitions)

		if (!metricDefinition) {
			throw new InvalidParamException("Metric definition doesn't exist for $metricCode")
		}

		return gatherMetric(projectIds, metricCode, metricDefinition)
	}

	/**
	 * Queries metrics based on start/end date, guid, metric code and projectId
	 *
	 * @param startDate The start date range to query for.
	 * @param endDate The end date range to query for.
	 * @param projectGuid Optional to query for the guid
	 * @param metricCodes Optional to query for metrics codes, will be split on a ,
	 * @param projectId Optional to query based on the project.
	 *
	 * @return A list of maps containing the metric results with
	 * projectGuid, metricCode, date, label, value  if not filtering by project or
	 * metricCode, date, label, value if filtering by project.
	 */
	List<Map> getMetrics(Date startDate, Date endDate, String projectGuid, List<String> metricCodes, Long projectId = null) {
		DetachedCriteria metrics = MetricResult.where {
			date >= startDate && date <= endDate

			// Filter on the projectGuid, projectId or all projects that have collectMetrics set
			if (projectGuid) {
				project.guid == projectGuid
			} else if (projectId) {
				project.id == projectId
			} else {
				project.collectMetrics == 1
			}

			// Filter on a list of metric codes
			if (metricCodes) {
				metricDefinitionCode in metricCodes
			}
		}

		List<MetricResult> results = metrics.list()

		if (projectId) {
			return metricResultsProjectMap(results)
		}

		return metricResultsMap(results)
	}

	/**
	 * Collects the metric results for not filtering by project.
	 *
	 * @param results A list of metrics results.
	 *
	 * @return A list of maps of metrics results that can be rendered as JSON/
	 */
	private List<Map> metricResultsMap(List<MetricResult> results) {
		return results.collect { MetricResult result ->
			[
					projectGuid: result.project.guid,
					metricCode : result.metricDefinitionCode,
					date       : result.date.format(DateFormat),
					label      : result.label,
					value      : result.value,
			]
		}
	}

	/**
	 * Collects the metric results for filtering by project.
	 *
	 * @param results A list of metrics results.
	 *
	 * @return A list of maps of metrics results that can be rendered as JSON/
	 */
	private List<Map> metricResultsProjectMap(List<MetricResult> results) {
		return results.collect { MetricResult result ->
			[
					metricCode: result.metricDefinitionCode,
					date      : result.date.format(DateFormat),
					label     : result.label,
					value     : result.value,
			]
		}
	}
}
