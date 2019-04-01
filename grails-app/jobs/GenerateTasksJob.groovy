import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.imports.TaskBatch
import net.transitionmanager.service.TaskService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.TriggerKey
import org.quartz.spi.MutableTrigger
/**
 * Generates tasks for a given task Batch.
 */
class GenerateTasksJob {

	static group = 'tdstm-generate-tasks'
	static triggers = {}

	TaskService taskService

	void execute(JobExecutionContext context) {
		try {
			JobDataMap dataMap = context.mergedJobDataMap
			long taskBatchId = dataMap.getLongValue('taskBatchId')
			boolean publishTasks = dataMap.getBoolean('publishTasks')

			log.info "GenerateTasksJob started for taskBatchId $taskBatchId"

			def taskBatch = TaskBatch.get(taskBatchId)
			if (taskBatch) {
				taskService.generateTasks(taskBatch, publishTasks)
			}
			else {
				int tries = dataMap.getIntValue('tries')
				if (tries > 10) {
					log.error "GenerateTasksJob - Gave up on waiting for taskBatchId $taskBatchId"
					return
				}

				// Reschedule the job for 2000ms
				long nextFiring = System.currentTimeMillis() + 2000 // 2 seconds
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
				log.info("Rescheduled job $trigger.key for ${nextFiringDate}, tries=$tries")
			}
		}catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			// TODO : JPM 5/2016 : execute() should update the progress to 100% if an exception occurs
			// progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage())
		}
	}
}
