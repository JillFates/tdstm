import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.etl.ETLProcessor
import groovy.util.logging.Slf4j
import net.transitionmanager.common.ProgressService
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * Cron job to execute {@link DataImportService#loadETLJsonIntoImportBatch(net.transitionmanager.project.Project, net.transitionmanager.security.UserLogin, java.lang.String)}
 *
 * Taking parameters from {@link net.transitionmanager.asset.WsAssetImportController#loadData(java.lang.String)} it initializes the import process.
 */
@Slf4j
class ETLImportDataJob {

    static group = 'tdstm-etl-import-data'
    static triggers = {}

    ProgressService progressService
    DataImportService dataImportService

    /**
     * Launch an Import process with a given job execution context parameters
     *
     * @param context
     */
    void execute(JobExecutionContext context) {

        JobDataMap dataMap = context.mergedJobDataMap
        Project project = (Project) dataMap.get('project')
        String filename = dataMap.getString('filename')
        Boolean isAutoProcess = dataMap.getBooleanValue('isAutoProcess')
        Boolean sendResultsByEmail = dataMap.getBooleanValue('sendResultsByEmail')
        String progressKey = dataMap.getString('progressKey')
        UserLogin userLogin = (UserLogin) dataMap.get('userLogin')

        try {
            log.info('ETLImportDataJob started for filename: {}', filename)
            Map importResults = dataImportService.loadETLJsonIntoImportBatch(project, userLogin, filename, isAutoProcess, sendResultsByEmail, progressKey)
            log.info('ETL import data execution result: {}', importResults)
        } catch (Throwable e) {
            log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
            progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage(), null, ETLProcessor.getErrorMessage(e))

        }


    }


}

