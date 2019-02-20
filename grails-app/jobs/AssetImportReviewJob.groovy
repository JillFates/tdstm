import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.ImportService
import net.transitionmanager.service.ProgressService
import org.quartz.JobExecutionContext

class AssetImportReviewJob {

	def group = 'tdstm-asset-import-review'
	static triggers = {}

	ImportService importService
	ProgressService progressService

	/**
	 * Invokes an asset import REVIEW process to review the assets before they can be posted to inventory.
	 */
	void execute(JobExecutionContext context) {
		Long projectId, userLoginId, batchId

		String progressKey
		try {
			Map dataMap = context.mergedJobDataMap
			progressKey = dataMap.getString('progressKey')

			batchId = dataMap.getLongValue('batchId')
			projectId = dataMap.getLongValue('projectId')

			log.debug "userLoginId = ${dataMap.userLoginId}"
			userLoginId = dataMap.getLongValue('userLoginId')
			String timeZoneId = dataMap.getString('timeZoneId')

			log.debug "execute() batchId=$batchId, projectId=$projectId, userLoginId=$userLoginId, timeZoneId=$timeZoneId, progressKey=$progressKey"

			log.info "execute() is about to invoke importService.invokeAssetImportProcess to start processing batch ($batchId) for project $projectId"

			Map results = importService.reviewImportBatch(projectId, userLoginId, batchId, progressKey)

			log.info "execute() return from importService.invokeAssetImportProcess() : results=$results"

		}
		catch (e) {
			log.error "execute() received exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.fail(progressKey, e.message)
		}
	}
}
