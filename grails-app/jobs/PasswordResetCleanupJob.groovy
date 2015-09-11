class PasswordResetCleanupJob {

	// def concurrent = false
	// Configured to run daily at 01:00hs
	static triggers = {
		cron name: 'passwordResetCleanupJob', cronExpression: "0 0 1 * * ?"
	}

	// Quartz Properties
	def group = 'tdstm-password-reset-cleanup'

	// IOC services
	def securityService

	/**
	 * Cleanups FMP expired entries
	 */
	 def execute(context) {
		def dataMap = context.mergedJobDataMap
		securityService.cleanupPasswordReset(dataMap)
	}
}
