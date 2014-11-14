import org.quartz.JobExecutionContext
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobExecutionException
import com.tdsops.common.lang.ExceptionUtil

class AssetImportProcessJob {

	// Quartz Properties
	def group = 'tdstm-asset-import-process'

	// def concurrent = false
	static triggers = { }

	// IOC services
	def importService
	def progressService

	/**
	 * Used to invoke an asset import process job that will post assets to the inventory 
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

			results = importService.invokeAssetImportProcess(projectId, userLoginId, batchId, progressKey, timeZoneId)

			log.info "execute() return from importService.invokeAssetImportProcess() : results=$results"
			
		} catch (e) {
			ExceptionUtil.stackTraceToString(e)
		}
 	}
}
