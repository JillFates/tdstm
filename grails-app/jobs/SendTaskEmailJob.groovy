import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Invoke the current commentService.sendTaskEmail(params) with the params set to context
 */
class SendTaskEmailJob {
    
    def commentService 
    def group = "TDSTM"
    static triggers = { }
    /**
     * @param context
     * @return call commentService.sendTaskEmail based on the params set to context
     * @throws JobExecutionException
     */
    def execute( JobExecutionContext context )  throws JobExecutionException {
        def taskId = context.getMergedJobDataMap().getLongValue("taskId");
        def tzId = context.getMergedJobDataMap().get("tzId").toString();
        def isNew = context.getMergedJobDataMap().getBooleanValue("isNew")
        commentService.sendTaskEMail(taskId, tzId, isNew)
    }
}
