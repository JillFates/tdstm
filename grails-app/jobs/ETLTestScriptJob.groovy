import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.etl.ETLProcessor
import groovy.util.logging.Slf4j
import net.transitionmanager.common.ProgressService
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.imports.ScriptProcessorService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

@Slf4j
class  ETLTestScriptJob {
	static group = 'tdstm-etl-test-script'
	static triggers = {}

	ProgressService progressService
	ScriptProcessorService scriptProcessorService

	/**
	 * Launch a ETL Test Script with given job execution context parameters
	 * @param context
	 */
	void execute(JobExecutionContext context) {

		JobDataMap dataMap = context.mergedJobDataMap
		long projectId = dataMap.getLongValue('projectId')
		String filename = dataMap.getString('filename')
		String progressKey = dataMap.getString('progressKey')

		try {
			// test script temporary filename
			String scriptFilename = dataMap.getString('scriptFilename')

			log.info('ETLTransformDataJob started for test script: {}', scriptFilename)
			Map result = scriptProcessorService.testScript(projectId, scriptFilename, filename, progressKey)
			log.info('ETL transform data execution result: {}', result)
		} catch (Throwable e) {
			log.error "execute() received exception ${e.getMessage()}"
			progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage(), null, ETLProcessor.getErrorMessage(e))

		}
	}

}
