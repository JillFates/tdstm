import net.transitionmanager.service.MetricReportingService
import org.quartz.JobExecutionContext
/**
 * A job to run the daily reporting metrics from the metricReportingService.
 */
class ReportingDailyMetricsJob {

	// Configured to run daily at 00:20hs
	static triggers = {
		cron name: 'reportingDailyMetricsJob', cronExpression: '0 20 0 * * ?'

		// This is to test running the jobs 2 minutes after starting the application
		//cron name: 'reportingDailyMetricsJob', cronExpression: '15 0/2 * * * ?'
	}

	// Quartz Properties
	static group = 'tdstm-reporting-daily-metrics'

	// IOC services
	MetricReportingService metricReportingService

	void execute(JobExecutionContext context) {
		metricReportingService.generateDailyMetrics()
	}
}
