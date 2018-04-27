package net.transitionmanager.command.metricdefinition

import grails.validation.Validateable
import net.transitionmanager.service.MetricReportingService.MetricMode

/**
 * A Command object to bind the individual metric definition JSON, and validate it.
 */
@Validateable
class MetricDefinitionCommand {
	String       metricCode
	String       description
	Integer      enabled
	String       mode
	QueryCommand query
	String       function
	String       sql

	static constraints = {
		sql nullable: true
		function nullable: true
		query nullable: true, cascade: true
		mode inList: MetricMode.list()
		enabled range: 0..1

		/**
		 * A validator for validating that the modes are required to have a field to describe
		 * The logic of the mode. For example sql mode, requires an sql field.
		 */
		mode validator: { String mode, MetricDefinitionCommand definition ->
			MetricMode metricMode = MetricMode.lookup(mode)

			if (metricMode == MetricMode.sql && !definition.sql) {
				return 'metric.validation.sql.null'
			}

			if (metricMode == MetricMode.function && !definition.function) {
				return 'metric.validation.function.null'
			}

			if (metricMode == MetricMode.query && !definition.query) {
				if (!definition.query) {
					return 'metric.validation.query.null'
				}
			}
		}
	}

	/**
	 * Converts the Command object to a map, so that it can be serialized back to JSON.
	 *
	 * @return A Map of the MetricDefinition fields, without the properties introduced by @Validateable.
	 */
	Map toMap() {
		return [
				metricCode : metricCode,
				description: description,
				enabled    : enabled,
				mode       : mode,
				query      : query ? query.toMap() : null,
				function   : function,
				sql        : sql,
		]
	}
}
