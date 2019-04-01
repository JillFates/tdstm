import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.security.SecurityService
import net.transitionmanager.task.TaskNonTranService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.TriggerKey
import org.quartz.spi.MutableTrigger
/**
 * Invokes TaskService.sendTaskEmail(params) with the context parameters. This
 * uses the TDSTM-Email Quartz group that will allow several emails to be sent concurrently.
 */
class UpdateTaskSuccessorsJob {

	static group = 'tdstm-task-update'
	static concurrent = false
	static triggers = {}

	TaskNonTranService taskNonTranService
	SecurityService securityService

	/**
	 * Executes TaskService.updateTaskSuccessors().
	 */
	void execute(JobExecutionContext context) {
		try {
			JobDataMap dataMap = context.mergedJobDataMap
			long taskId = dataMap.getLongValue('taskId')
			long whomId = dataMap.getLongValue('whomId')
			String status = dataMap.getString('status')
			boolean isPM = dataMap.getBoolean('isPM')
			int tries = dataMap.getIntValue('tries') + 1

			log.info "updateTaskSuccessors Job started for task id $taskId (attempt #$tries)"

			// Invoke the service method
			def result = taskNonTranService.updateTaskSuccessors(taskId, status, whomId, isPM, tries)

			if (result == 'reschedule') {
				if (tries > 100) {
					log.error "Gave up on waiting for task $taskId status to update to '$status'"
					return
				}

				// Reschedule the job for 500ms
				long nextFiring = System.currentTimeMillis() + 2000    // in 100ms
				Date nextFiringDate = new Date(nextFiring)
				MutableTrigger trigger = (MutableTrigger) context.getTrigger()
				trigger.setStartTime(nextFiringDate)

				// Update the retries count
				JobDataMap map = context.jobDetail.jobDataMap
				map.tries = tries
				trigger.jobDataMap.putAll(map)
				log.info "JobDataMap = $map"

				//TM-6506 calling the 'getKey' accessor and assigning to a variable to enforce type and avoid groovy coersion
				TriggerKey triggerKey = trigger.getKey()

				// reschedule the job
				context.scheduler.rescheduleJob(triggerKey, trigger)
				log.info("Rescheduled job $trigger.key for ${nextFiringDate}")
			}
		}catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
		}
	}
}
