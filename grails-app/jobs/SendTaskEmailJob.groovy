import org.quartz.JobExecutionContext;
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
		// log.info "execute: taskId=$taskId, tzId=$tzId, isNew=$isNew"
        commentService.sendTaskEMail(taskId, tzId, isNew)
    }
}