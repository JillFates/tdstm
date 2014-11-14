import org.quartz.JobExecutionContext
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobExecutionException
import com.tdsops.common.lang.ExceptionUtil

class AssetImportReviewJob {

	// Quartz Properties
	def group = 'tdstm-asset-import-review'

	// def concurrent = false
	static triggers = { }

	// IOC services
	def importService
	def progressService

	/**
	 * Used to invoke an asset import REVIEW process that will review the assets before they can be posted to inventory 
	 * @param context
	 * @return void
	 */
	 void execute(context) {
	 	String errorMsg
	 	String progressKey
	 	String timeZoneId 
	 	Map results
	 	Long projectId, userLoginId, batchId

	 	try {
			def dataMap = context.mergedJobDataMap

			batchId = dataMap.getLongValue('batchId')
			projectId = dataMap.getLongValue('projectId')

			log.debug "userLoginId = ${dataMap.userLoginId}"
			userLoginId = dataMap.getLongValue('userLoginId')
			progressKey = dataMap.getString('progressKey')
			timeZoneId = dataMap.getString('timeZoneId')

			log.debug "execute() batchId=$batchId, projectId=$projectId, userLoginId=$userLoginId, timeZoneId=$timeZoneId, progressKey=$progressKey"

			log.info "execute() is about to invoke importService.invokeAssetImportProcess to start processing batch ($batchId)"

			results = importService.reviewImportBatch(projectId, userLoginId, batchId, progressKey)

			log.info "execute() return from importService.invokeAssetImportProcess() : results=$results"
			
		} catch (e) {
			ExceptionUtil.stackTraceToString(e)
		} 
	}
}
