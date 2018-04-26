package net.transitionmanager.command.metricdefinition

import grails.validation.Validateable

/**
 * A command object that contains the parameters to get metrics:
 *

 * startDate The start date, defaulted to the previous date.
 * endDate The end date, defaulted to the previous date.
 * projectGuid A project Guid to filter on.
 * metricCodes A csv delimited list of metrics codes to filter on.
 * format The format to return the metrics results CSV/JSON, defaulting to CSV.
 */
@Validateable
class GetMetricsCommand {
	Date startDate = new Date().clearTime() -1
	Date endDate = new Date().clearTime() -1
	String projectGuid
	String metricCodes
	String format = 'csv'

	static constraints = {
		projectGuid nullable: true
		metricCodes nullable: true
		format nullable: true
	}
}
