import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.JobExecutionException;

/**
 * 
 * A Quartz Job that is used to send email messages by invoking thecommentService.sendTaskEmail(params) with the context parameters. This 
 * uses the TDSTM-Email Quartz group that will allow several emails to be sent concurrently. 
 */
// class SendTaskEmailJob implements Job {
class SendTaskEmailJob {
    
    def group = 'tdstm'
	def concurrent = false
    def commentService 
	
    static triggers = { }

    /**
     * @param context
     * @return call commentService.sendTaskEmail based on the params set to context
     * @throws JobExecutionException
     */
	// TODO - change job so that it will retry if email fails to send
 	def execute(context) {
		def dataMap = context.mergedJobDataMap
        def taskId = dataMap.getLongValue('taskId');
        def tzId = dataMap.get('tzId').toString();
        def isNew = dataMap.getBooleanValue('isNew')
		long tries = dataMap.getLongValue('tries') + 1
		//log.info "execute: taskId=$taskId, tzId=$tzId, isNew=$isNew"
		
		def result = commentService.sendTaskEMail(taskId, tzId, isNew)
		
		if(result=='reschedule'){
			if(tries<3){
				// Reschedule the job for 10s
				long nextFiring = System.currentTimeMillis() + 10000   // in 10s
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
			} else {
				log.error "Gave up on waiting for task $taskId to create"
				return
			}
		}
    }
}