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
			log.error "GenerateTasksJob failed to find batch $taskBatchId"
		}
	}
}
