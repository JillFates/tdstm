import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.EmailDispatchService
import org.quartz.JobExecutionContext

class EmailDispatchJob extends SecureJob {

	def group = 'tdstm-send-email'
	static triggers = {}

	EmailDispatchService emailDispatchService

	/**
	 * Calls emailDispatchService.sendEmail().
	 */
	void execute(JobExecutionContext context) {
		try {
			Map dataMap = initialize(context)
			emailDispatchService.sendEmail(dataMap)
		}
		catch (e) {
			log.error "execute() received exception ${e.message}\n${ExceptionUtil.stackTraceToString(e)}"
		}
	}
}
