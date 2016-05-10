import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.JobExecutionException
import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil

/**
 * 
 * A Quartz Job that will generate tasks for a given task Batch
 */
class GenerateTasksJob {

    // Quartz Properties
    def group = 'tdstm-generate-tasks'
	// def concurrent = false
    static triggers = { }

	// IOC services
	def taskService

    /**
	 * executes the TaskService.updateTaskSuccessors
     * @param context
     * @return void
     */
 	def execute(context) {
 		try { 
			def dataMap = context.mergedJobDataMap
			def taskBatchId = dataMap.getLongValue('taskBatchId')
			def publishTasks = dataMap.getBoolean('publishTasks')
			
			log.info "GenerateTasksJob started for taskBatchId $taskBatchId"

			def taskBatch = TaskBatch.get(taskBatchId)
			if (taskBatch) {
				taskService.generateTasks(taskBatch, publishTasks)
			} else {
				def tries = dataMap.getLongValue('tries')
				if (tries > 10) {
					log.error "GenerateTasksJob - Gave up on waiting for taskBatchId $taskBatchId"
					return
				}

				// Reschedule the job for 2000ms
				long nextFiring = System.currentTimeMillis() + 2000 // 2 seconds
				Date nextFiringDate = new Date(nextFiring)
				Trigger trigger = context.getTrigger()
				trigger.setStartTime(nextFiringDate)

				// Update the retries count
				def map = context.getJobDetail().getJobDataMap()
				map.put("tries", tries)
				trigger.jobDataMap.putAll(map)
				log.info "JobDataMap = $map"

				// reschedule the job
				String triggerName = trigger.getName()
				context.getScheduler().rescheduleJob(triggerName, trigger.getGroup(), trigger)
				//context.getScheduler().rescheduleJob(trigger.getKey(), trigger)
				log.info("Rescheduled job ${triggerName} for ${nextFiringDate}, tries=$tries")
			}
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"			
			// TODO : JPM 5/2016 : execute() should update the progress to 100% if an exception occurs
			// progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage())
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
