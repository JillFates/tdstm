package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.SettingType
import grails.converters.JSON
import net.transitionmanager.command.metricdefinition.MetricDefinitionsCommand
import net.transitionmanager.domain.Person
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * A service for dealing with reporting metrics.
 */
class MetricReportingService {
	Random                     randomNumberGenerator
	SettingService             settingService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

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

	/**
	 * sets up a random instance with a seed.
	 *
	 * @param seed a seed for random, for testing.If null the seed becomes currentTimeMillis + freeMemory
	 */
	void setUpRandom(Long seed = null) {
		if (seed == null) {
			seed = System.currentTimeMillis() + Runtime.runtime.freeMemory()
		}

		if (randomNumberGenerator == null) {
			randomNumberGenerator = new Random(seed)
		}
	}

	/**
	 * A random number generator I copied from my Groovy talk on Meta programming:
	 * https://github.com/virtualdogbert/MetaProgrammingMagicRevealed/blob/master/Dgenerator.groovy
	 *
	 * @param min Lower bound for the generated number.
	 * @param max Upper bound for the generated number.
	 * @param seed An optional seed for the generator, using for testing.
	 *
	 * @return A random integer between min and max, using a seed for the generator.
	 */
	int generateRandomInt(int min, int max, Long seed = null) {
		setUpRandom(seed)
		return min + ((max - min) * randomNumberGenerator.nextDouble()) as int
	}

	/**
	 * A map of functions that can be run by gatherMetric if the mode is function.
	 */
	private Map functions = [
			testMetric: { List<Long> projectIds, String metricCode -> testMetric(projectIds, metricCode) }
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
	List<Map> gatherMetric(List<Long> projectIds, String metricCode, JSONObject metricDefinition, boolean logError = true) {
		MetricMode mode = MetricMode.lookup((String) metricDefinition.mode)

		try {
			if (mode == MetricMode.query) {
				runQuery((JSONObject) metricDefinition.query, projectIds, metricCode)
			} else if (mode == MetricMode.sql) {
				return runSql((String) metricDefinition.sql, projectIds)
			} else if (mode == MetricMode.function) {
				Closure function = functions[(String) metricDefinition.function]
				if (!function) {
					throw new InvalidParamException('Function for metric not implemented.')
				}

				return function(projectIds, metricCode)
			} else {
				throw new InvalidParamException("Mode $mode is an invalid mode for GatherMetric")
			}
		} catch (Exception e) {
			if (logError) {
				log.error(e)
				return []
			} else {
				throw e
			}
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
		String date = new Date().format('yyyy-MM-dd')
		List results = Person.executeQuery(getQuery(query), [projectIds: projectIds, colon: ':'])

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
		String domain = ETLDomain.lookup((String) query.domain).clazz.simpleName
		String aggregation = query.aggregation
		List groupBys = query?.groupBy?.clone() ?: []
		String label = getLabel(groupBys, aggregation)
		groupBys << 'project.id'
		String groupBy = getGroupBy(groupBys)
		groupBys << 'project.id'
		List wheres = query?.where?.clone() ?: []

		if (domain == AssetEntity.class.simpleName) {
			wheres << [column: 'moveBundle.useForPlanning', expression: '= 1']
		}

		String where = getWhere(wheres)

		return """
			select project.id,
					$label as label,
					$aggregation as value
			from $domain
			where project.id in (:projectIds) $where 
			$groupBy
		""".stripIndent()
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
			return "concat(${groupBy.join(', :colon, ')})"
		}

		return "'${aggregation.split(/\(/)[0]}'"
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
	 * [column: 'validation', expression: "in ('BundleReady', 'Confirmed')"]
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
	private List<Map> testMetric(List<Long> projectIds, String metricCode) {
		String date = new Date().format('yyyy-MM-dd')
		setUpRandom()

		projectIds.collect { Long projectId ->
			[
					projectId : projectId,
					metricCode: metricCode,
					date      : date,
					label     : 'count',
					value     : generateRandomInt(1, 500)
			]
		}
	}

	/**
	 * Gets the definitions JSON for the GSP page, pretty printed with the version.
	 *
	 * @return A Map containing the definitions JSON and their version.
	 */
	Map getDefinitions() {
		Map definitions = settingService.getAsMap(SettingType.METRIC_DEF, 'MetricDefinitions') ?: [:]
		int version = definitions?.version ?: 0
		definitions.remove('version')
		String definition = (definitions.definitions as JSON).toString(true) ?: ''

		return [definitions: definition, version: version]
	}

	/**
	 * Saves the Definitions as a Stringified JSON to the setting table.
	 *
	 * @param definitions The definitions command object that contains the metrics JSON.
	 * @param version The version of the metrics for the Settings table.
	 *
	 * @return A Map of the saved JSON, and the version of the metrics.
	 */
	Map saveDefinitions(MetricDefinitionsCommand definitions, Integer version){
		settingService.save(SettingType.METRIC_DEF, 'MetricDefinitions', (definitions.toMap() as JSON).toString(), version)
		return getDefinitions()
	}
}
