import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil

class ProjectDailyMetricsJob {

	// def concurrent = false
	// Configured to run daily at 00:05hs
	static triggers = {
		cron name: 'projectDailyMetricsJob', cronExpression: "0 5 0 * * ?"

		// This is to test running the jobs 2 minutes after starting the application
		// cron name: 'projectDailyMetricsJob', cronExpression: "15 0/2 * * * ?"
	}

	// Quartz Properties
	def group = 'tdstm-project-daily-metrics'

	// IOC services
	def projectService

	/**
	 * executes the AssetEntityController.basicExport
	 * @param context
	 * @return void
	 */
	 def execute(context) {
	 	try {
			def dataMap = context.mergedJobDataMap
			projectService.activitySnapshot(dataMap)
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
