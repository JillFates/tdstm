import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * A Quartz Job that is used to invoking the TaskService.sendTaskEmail(params) with the context parameters. This 
 * uses the TDSTM-Email Quartz group that will allow several emails to be sent concurrently. 
 */
class UpdateTaskSuccessorsJob {

    // Quartz Properties
    def group = 'tdstm'
	def concurrent = false
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
		def taskId = dataMap.getLongValue('taskId')
		def whomId = dataMap.getLongValue('whomId')
		def status = dataMap.getString('status')
		taskService.updateTaskSuccessors(taskId, whomId, status)
	}
}
