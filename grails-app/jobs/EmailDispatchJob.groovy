import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.EmailDispatchService
import org.quartz.JobExecutionContext

class EmailDispatchJob {

	def group = 'tdstm-send-email'
	static triggers = {}

	EmailDispatchService emailDispatchService

	/**
	 * Calls emailDispatchService.sendEmail().
	 */
	void execute(JobExecutionContext context) {
		try {
			emailDispatchService.sendEmail(context.mergedJobDataMap)
		}
		catch (e) {
			log.error "execute() received exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
		}
		finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
