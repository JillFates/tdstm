import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.metricdefinition.GetMetricsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.MetricReportingService
import org.springframework.http.HttpStatus

/**
 * An API controller for getting metric results.
 */
@Secured('isAuthenticated()')
class MetricController implements ControllerMethods {
	static namespace = 'v1'

	MetricReportingService metricReportingService

	/**
	 * Gets the metric results based on a GetMetricsCommand object.
	 *
	 * @param params a GetMetricsCommand object which incduled stateDate, endDate,
	 * projectGuid, and metricCodes(csv delimited). it also includes a format,
	 * csv/json defaulting to csv
	 *
	 * @return if the format is csv, or default, then this returns a csv file,
	 * with the metrics results queried for, else it returns JSON with the
	 * metrics results queried for.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def index(GetMetricsCommand params) {
		if (params.hasErrors()) {
			response.setStatus(HttpStatus.BAD_REQUEST.value())
			return renderAsJson(params.errors)
		}

		List<Map> metricData = metricReportingService.getMetrics(
				params.startDate,
				params.endDate,
				params.projectGuid,
				params.codes())

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
