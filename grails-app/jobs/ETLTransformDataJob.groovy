import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.ProgressService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * ETL Transform Data Job
 */
@Slf4j
class ETLTransformDataJob {
	def group = 'tdstm-etl-transform-data'
	static triggers = {}

	ProgressService progressService
	DataImportService dataImportService

	/**
	 * Launch a ETL transform data process with given job execution context parameters
	 * @param context
	 */
	void execute(JobExecutionContext context) {

		JobDataMap dataMap = context.mergedJobDataMap
		long dataScriptId = dataMap.getLongValue('dataScriptId')
		long projectId = dataMap.getLongValue('projectId')
		String filename = dataMap.getString('filename')
		String progressKey = dataMap.getString('progressKey')

		try {
			log.info "ETLTransformDataJob started for dataScriptId $dataScriptId"
			Map result = dataImportService.transformEtlData(projectId, dataScriptId, filename, progressKey)

			log.info('ETL transform data execution result: {}', result)
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.update(progressKey, 100I, ProgressService.FAILED, e.getMessage())
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
