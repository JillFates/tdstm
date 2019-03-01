import net.transitionmanager.service.ProjectService
import org.quartz.JobExecutionContext

class ProjectDailyMetricsJob {

	// Configured to run daily at 00:05hs
	static triggers = {
		cron name: 'projectDailyMetricsJob', cronExpression: '0 5 0 * * ?'

		// This is to test running the jobs 2 minutes after starting the application
		// cron name: 'projectDailyMetricsJob', cronExpression: '15 0/2 * * * ?'
	}

	// Quartz Properties
	static group = 'tdstm-project-daily-metrics'

	// IOC services
	ProjectService projectService

	void execute(JobExecutionContext context) {
		projectService.activitySnapshot()
	}
}
