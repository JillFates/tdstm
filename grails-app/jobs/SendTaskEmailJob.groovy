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
        def taskId = context.getMergedJobDataMap().getLongValue("taskId") as Object;
        def tzId = context.getMergedJobDataMap().get("tzId")  as Object;
        def isNew = context.getMergedJobDataMap().getBooleanValue("isNew") as Object
        
        commentService.sendTaskEmail(taskId, tzId, isNew)
    }
}
