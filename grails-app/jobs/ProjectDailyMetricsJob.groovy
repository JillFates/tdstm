class ProjectDailyMetricsJob {

	// def concurrent = false
	// Configured to run daily at 00:05hs
	static triggers = {
		cron name: 'projectDailyMetricsJob', cronExpression: "0 5 0 * * ?"
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
		def dataMap = context.mergedJobDataMap
		projectService.activitySnapshot(dataMap)
	}
}
