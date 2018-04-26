import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.metricdefinition.GetMetricsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.MetricReportingService
import org.springframework.http.HttpStatus

@Secured('isAuthenticated()')
class MetricController implements ControllerMethods {
	static namespace = 'v1'
	static responseFormats = ['csv', 'json']

	MetricReportingService metricReportingService

	@HasPermission(Permission.AdminUtilitiesAccess)
	def index(GetMetricsCommand params) {
		if (params.hasErrors()) {
			response.setStatus(HttpStatus.BAD_REQUEST.value())
			return renderAsJson(params.errors)
		}

		List<Map> metricData = metricReportingService.getMetrics(params.startDate,
																 params.endDate,
																 params.projectGuid,
																 params.metricCodes)

		withFormat {
			csv {
				renderASCSV(metricData)
			}

			json {
				renderAsJson(metricData)
			}
		}
	}
}
