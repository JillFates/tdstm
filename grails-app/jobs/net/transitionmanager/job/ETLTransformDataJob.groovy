package net.transitionmanager.job

import groovy.util.logging.Slf4j
import net.transitionmanager.imports.DataTransformService
import net.transitionmanager.security.UserLogin
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * ETL Transform Data Job
 */
@Slf4j
class ETLTransformDataJob {
    static group = 'tdstm-etl-transform-data'
    static triggers = {}

    DataTransformService dataTransformService
    /**
     * Launch a ETL transform data process with given job execution context parameters
     * @param context
     */
    void execute(JobExecutionContext context) {

        JobDataMap dataMap = context.mergedJobDataMap
        long projectId = dataMap.getLongValue('projectId')
        String filename = dataMap.getString('filename')
        Boolean sendNotification = dataMap.getBooleanValue('sendNotification')
        String progressKey = dataMap.getString('progressKey')
        UserLogin userLogin = (UserLogin) dataMap.get('userLogin')
        long dataScriptId = dataMap.getLongValue('dataScriptId')

        log.info('ETLTransformDataJob started for dataScriptId: {}', dataScriptId)
        Map result = dataTransformService.transformEtlData(userLogin, projectId, dataScriptId, filename, sendNotification, progressKey)
        log.info('ETL transform data execution result: {}', result)
    }
}
