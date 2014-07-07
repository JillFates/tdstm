import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.JobExecutionException;

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
			log.info("Rescheduled job ${triggerName} for ${nextFiringDate}, tries=$tries")
		}
	}
}
