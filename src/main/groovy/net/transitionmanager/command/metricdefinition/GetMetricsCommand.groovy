package net.transitionmanager.command.metricdefinition

import net.transitionmanager.command.CommandObject


/**
 * A command object that contains the parameters to get metrics:
 *

 * startDate The start date, defaulted to the previous date.
 * endDate The end date, defaulted to the previous date.
 * projectGuid A project Guid to filter on.
 * metricCodes A csv delimited list of metrics codes to filter on.
 * format The format to return the metrics results CSV/JSON, defaulting to CSV.
 */

class GetMetricsCommand implements CommandObject{
	Date startDate = new Date().clearTime() - 1
	Date endDate = new Date().clearTime() - 1
	String projectGuid
	String metricCodes
	String format = 'csv'

	List<String> codes(){
		if (metricCodes) {
			return metricCodes.split(',')*.trim()
		}

		return []
	}

	static constraints = {
		projectGuid nullable: true
		metricCodes nullable: true
		format nullable: true, inList: ['csv','json']
	}
}
