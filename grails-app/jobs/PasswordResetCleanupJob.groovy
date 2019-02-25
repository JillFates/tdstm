import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.SecurityService
import org.quartz.JobExecutionContext

class PasswordResetCleanupJob {

	// Configured to run daily at 01:00hs
	static triggers = {
		cron name: 'passwordResetCleanupJob', cronExpression: "0 0 1 * * ?"
		// This is used to test running the job 2 minutes after the application starts
		// cron name: 'passwordResetCleanupJob', cronExpression: "15 0/2 * * * ?"
	}

	static group = 'tdstm-password-reset-cleanup'

	SecurityService securityService

	/**
	 * Cleanups FMP expired entries
	 */
	void execute(JobExecutionContext context) {
		try {
			securityService.cleanupPasswordReset(context.mergedJobDataMap)
		}
		catch (e) {
			log.error "execute() encountered exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
			// progressService.fail(progressKey, e.message))
		}
	}
}
