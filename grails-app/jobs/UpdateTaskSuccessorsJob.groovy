import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.JobExecutionException;

/**
 * 
 * A Quartz Job that is used to invoking the TaskService.sendTaskEmail(params) with the context parameters. This 
 * uses the TDSTM-Email Quartz group that will allow several emails to be sent concurrently. 
 */
class UpdateTaskSuccessorsJob {

    // Quartz Properties
    def group = 'tdstm-task-update'
	def concurrent = false
    static triggers = { }

	// IOC services
    def taskNonTranService 
	def securityService

    /**
	 * executes the TaskService.updateTaskSuccessors
     * @param context
     * @return void
     */
 	def execute(context) {

		def dataMap = context.mergedJobDataMap
		def taskId = dataMap.getLongValue('taskId')
		def whomId = dataMap.getLongValue('whomId')
		def status = dataMap.getString('status')
		def isPM = dataMap.getBoolean('isPM')
		long tries = dataMap.getLongValue('tries') + 1
		
		log.info "updateTaskSuccessors Job started for task id $taskId (attempt #$tries)"

		// Invoke the service method
		def result = taskNonTranService.updateTaskSuccessors(taskId, status, whomId, isPM, tries)

		if (result == 'reschedule') {
			if (tries > 100) {
				log.error "Gave up on waiting for task $taskId status to update to '$status'"
				return
			}

			// Reschedule the job for 500ms
			long nextFiring = System.currentTimeMillis() + 100    // in 100ms
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
			log.info("Rescheduled job ${triggerName} for ${nextFiringDate}")
		}
	}
}
