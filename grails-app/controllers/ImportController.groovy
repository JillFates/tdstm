import grails.converters.JSON
import org.quartz.SimpleTrigger
import org.quartz.Trigger

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil

class ImportController {
	
	def controllerService
	def importService
	def progressService

	def quartzScheduler

	def listJobs = {
		List jobs = progressService.list()
		if (jobs) {
			render jobs.toString()
		} else {
			render 'No jobs found'
		}
	}

	/**
	 * Used to kickoff the Asset Import Process task to perform the final step in importing assets into the system. This will setup a
	 * Quartz job and a progressSevice so that it may be able to be tracked once it has started.
	 * @param params.id - the DataTransferBatch id to be processed
	 * @return A JSON object with various data attributes in it
	 */
	def invokeAssetImportProcess = {
		String errorMsg
		String progressKey
		Project project
		UserLogin userLogin
		Map results = [:]

		while (true) {
			try {
				(project, userLogin) = controllerService.getProjectAndUserForPage(this, 'import')
				if (!project) {
					errorMsg = flash.message
					flash.message = null
					break
				}

				Long batchId = NumberUtil.toLong(params.id)

				errorMsg = importService.validateImportBatchCanBeProcessed(project.id, batchId)
				if (errorMsg)
					break

				log.debug "invokeAssetImportProcess() about to fetch DataTransferBatch.get($batchId)"

				DataTransferBatch dtb = DataTransferBatch.get(batchId)

				log.debug "invokeAssetImportProcess() about to set $dtb, current status of ${dtb.statusCode}"

				// Update the batch status to POSTING
				dtb.statusCode = DataTransferBatch.POSTING

				log.debug "invokeAssetImportProcess() about to save $dtb, status of ${dtb.statusCode}"

				if (!dtb.save(flush:true, failOnError:true)) {
					errorMsg = "Unable to update batch status : ${GormUtil.allErrorsString(dtb)}"
					break
				}
				log.debug "invokeAssetImportProcess() Updated the batch status to: ${dtb.statusCode} for: $dtb"


				// Start the progress service with a new key
				progressKey = "AssetImportProcess-" + UUID.randomUUID().toString()
				progressService.create(progressKey)

errorMsg = "We are intentionally stopping the process kickoff to test something"
break
				//
				// Setup the Quartz job that will execute the actual posting process
				//

				// The triggerName/Group will allow us to controller on import
				String triggerName = "TM-AssetImport-${project.id}"
				String triggerGroup = null
				Date startTime = new Date(System.currentTimeMillis() + 2000) // Delay 2 seconds to allow this current transaction to commit before firing off the job

				Trigger trigger = new SimpleTrigger(triggerName, null, startTime)
				//trigger.jobDataMap.putAll(results)
				trigger.jobDataMap.put('batchId', batchId)
				trigger.jobDataMap.put('progressKey', progressKey)
				trigger.jobDataMap.put('userLoginId', userLogin.id)
				trigger.jobDataMap.put('projectId', project.id)
				trigger.jobDataMap.put('timeZoneId', getSession().getAttribute( "CURR_TZ" )?.CURR_TZ)

				trigger.setJobName('AssetImportProcessJob')			// Please note that the JobName must matche the class file name
				trigger.setJobGroup('tdstm-asset-import-process')	// and that the group should be specifed in the Job

				quartzScheduler.scheduleJob(trigger)

				log.info "invokeAssetImportProcess() $userLogin kicked of an asset import process for batch ($batchId), progressKey=$progressKey"
				// progressService.update(progressKey, 1, 'In progress')
			} catch (e) {
				log.error "invokeAssetImportProcess() Initiate asset import process $triggerName failed to create Quartz job : ${e.getMessage()}"
				progressService.update(progressKey, 100I, progressService.FAILED)
				errorMsg = 'There was either an error or that someone else is currently importing assets for this project.'
				if (log.isDebugEnabled())
					errorMsg = "$errorMsg ${e.getMessage()}"
			}

			break
		}
		
		if (errorMsg) {
			progressService.remove(progressKey)
			render ServiceResults.errors(errorMsg) as JSON
		} else {
			results.progressKey = progressKey
			results.batchStatusCode = DataTransferBatch.POSTING

			render ServiceResults.success( [results:results] ) as JSON
		}
	}

}