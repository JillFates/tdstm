package net.transitionmanager.reporting

import com.tdssrc.grails.GormUtil
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.metricdefinition.GetMetricsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

/**
 * An API controller for getting metric results.
 */
@Secured('isAuthenticated()')
class MetricController implements ControllerMethods {
	static namespace = 'v1'

	MetricReportingService metricReportingService

	/**
	 * Gets the metric results based on a GetMetricsCommand object
	 *
	 * @param params a GetMetricsCommand object which includes stateDate, endDate,
	 * projectGuid, metricCodes(csv delimited) and format (csv|json, default csv)
	 *
	 * @return the metric data formated as csv or json based on format specified
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def index() {
		GetMetricsCommand commandObj = populateCommandObject(GetMetricsCommand)
		if (commandObj.hasErrors()) {
			sendInvalidInput( renderAsJson( GormUtil.validateErrorsI18n(commandObj) ) )
			return
		}

		List<Map> metricData = metricReportingService.getMetrics(
			commandObj.startDate,
			commandObj.endDate,
			commandObj.projectGuid,
			commandObj.codes())

		withFormat {
			csv {
				renderAsCSV(metricData)
			}

			json {
				renderAsJson(metricData)
			}
		}
	}
}
