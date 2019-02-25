import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.ImportService
import net.transitionmanager.service.ProgressService
import org.quartz.JobExecutionContext

class AssetImportProcessJob extends SecureJob {

	static group = 'tdstm-asset-import-process'
	static triggers = {}

	ImportService importService
	ProgressService progressService

	/**
	 * Invokes an asset import process job to post assets to the inventory.
	 */
	void execute(JobExecutionContext context) {
		String progressKey
		try {
			// Map dataMap = context.mergedJobDataMap
			Map dataMap = initialize(context)

			progressKey = dataMap.getString('progressKey')

			long batchId = dataMap.getLongValue('batchId')
			long projectId = dataMap.getLongValue('projectId')

			log.debug "userLoginId = ${dataMap.userLoginId}"
			long userLoginId = dataMap.getLongValue('userLoginId')
			String timeZoneId = dataMap.getString('timeZoneId')
			String dtFormat = dataMap.getString('dtFormat')

			log.debug "execute() batchId=$batchId, projectId=$projectId, userLoginId=$userLoginId, timeZoneId=$timeZoneId, progressKey=$progressKey, dtFormat=$dtFormat"

			log.info "execute() is about to invoke importService.invokeAssetImportProcess to start processing batch ($batchId)"

			Map results = importService.invokeAssetImportProcess(projectId, userLoginId, batchId, progressKey, timeZoneId, dtFormat)

			log.info "execute() return from importService.invokeAssetImportProcess() : results=$results"
		}
		catch (e) {
			log.error "execute() received exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.update(progressKey, 100, ProgressService.FAILED, e.message)
		}
	}
}
