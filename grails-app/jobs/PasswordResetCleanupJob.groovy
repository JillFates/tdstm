import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil

class PasswordResetCleanupJob {

	// def concurrent = false
	// Configured to run daily at 01:00hs
	static triggers = {
		cron name: 'passwordResetCleanupJob', cronExpression: "0 0 1 * * ?"

		// This is used to test running the job 2 minutes after the application starts
		// cron name: 'passwordResetCleanupJob', cronExpression: "15 0/2 * * * ?"
	}

	// Quartz Properties
	def group = 'tdstm-password-reset-cleanup'

	// IOC services
	def securityService

	/**
	 * Cleanups FMP expired entries
	 */
	 def execute(context) {
	 	try {
			def dataMap = context.mergedJobDataMap
			securityService.cleanupPasswordReset(dataMap)
		} catch (e) {
			log.error "execute() encountered exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			// progressService.fail(progressKey, e.getMessage())
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
