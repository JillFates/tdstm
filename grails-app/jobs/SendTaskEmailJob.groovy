import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.CommentService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.spi.MutableTrigger

/**
 * Sends email messages by invoking commentService.sendTaskEmail(params) with the
 * context parameters. This uses the TDSTM-Email Quartz group that will allow
 * several emails to be sent concurrently.
 */
class SendTaskEmailJob {

	static triggers = {}

	def group = 'tdstm'
	def concurrent = false

	CommentService commentService

	/**
	 * Calls commentService.sendTaskEmail based on the params set to context.
	 */
	// TODO - change job so that it will retry if email fails to send
	void execute(JobExecutionContext context) {
		try {
			JobDataMap dataMap = context.mergedJobDataMap
			long taskId = dataMap.getLongValue('taskId')
			String tzId = dataMap.get('tzId').toString()
			String userDTFormat = dataMap.get('userDTFormat').toString()
			boolean isNew = dataMap.getBooleanValue('isNew')
			int tries = dataMap.getIntValue('tries') + 1
			//log.info "execute: taskId=$taskId, tzId=$tzId, isNew=$isNew"

			def result = commentService.sendTaskEMail(taskId, tzId, userDTFormat, isNew)

			if (result == 'reschedule') {
				if (tries < 3) {
					// Reschedule the job for 10s
					long nextFiring = System.currentTimeMillis() + 10000   // in 10s
					Date nextFiringDate = new Date(nextFiring)
					MutableTrigger trigger = (MutableTrigger) context.getTrigger()
					trigger.setStartTime(nextFiringDate)

					// Update the retries count
					JobDataMap map = context.jobDetail.jobDataMap
					map.tries = tries
					trigger.jobDataMap.putAll(map)
					log.info "JobDataMap = $map"

					// reschedule the job
					context.scheduler.rescheduleJob(trigger.key, trigger)
					log.info("Rescheduled job $trigger.key for ${nextFiringDate}")
				}
				else {
					log.error "Gave up on waiting for task $taskId to create"
				}
			}
		}
		finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
