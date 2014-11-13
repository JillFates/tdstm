import org.quartz.JobExecutionContext
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobExecutionException

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

			log.info "AssetImportProcessJob is about to invoke invokeAssetImportProcess started for batchId $batchId"

			results = importService.invokeAssetImportProcess(projectId, userLoginId, batchId, progressKey, timeZoneId)
			
		} catch (e) {
			errorMsg = e.getMessage()
		}
 
		// Need to persist the data back to the batch and stop the job
		if (errorMsg) {
			results = [status:'error', errors: errorMsg]
			progressService.update(progressKey, 100I, progressService.FAILED)
			log.info "execute() received an error $errorMsg"
		} else {
			results.status = 'success'
			progressService.update(progressKey, 100I, progressService.DONE)
			log.info "execute() call to service was successful"

			// TODO : JPM 11/2014 : persist the info and other information
		}

		// Store the results so the the client can grab it afterward
		progressService.updateData(progressKey, 'results', results)

	}
}
