import grails.converters.JSON
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.ObjectAlreadyExistsException

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdsops.common.lang.ExceptionUtil

class ImportController {
	
	def controllerService
	def importService
	def progressService
	def securityService

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
	 * This action used to review batch and find error in excel import if any 
	 * @param : id- data transfer batch id
	 * @return map containing error message if any and import permission  (NewModelsFromImport)
	 */
	def invokeAssetImportReview = {
		String methodName = 'invokeAssetReviewProcess()'
		Map results = [info:'', hasPerm:false]
		String errorMsg
		String progressKey
		Project project
		UserLogin userLogin
		String triggerName
		String triggerGroup=null
		boolean progressStarted

		while (true) {
			try {

				(project, userLogin) = controllerService.getProjectAndUserForPage(this, 'import')
				if (!project) {
					errorMsg = flash.message
					flash.message = null
					break
				}
				DataTransferBatch dtb 
				(dtb, errorMsg) = importService.getAndValidateBatch(params.id, project.id)
				if (errorMsg)
					break
				
				// Update the batch status to POSTING and save the progress key 
				progressKey = "AssetImportProcess-" + UUID.randomUUID().toString()
				dtb.progressKey = progressKey
				if (!dtb.save(flush:true, failOnError:true)) {
					log.error "$methodName error occurred while trying to update the DataTransferBatch ${dtb.id} ${GormUtil.allErrorsString(dtb)}" 
					errorMsg = "Unable to update batch record : ${GormUtil.allErrorsString(dtb)}"
					break
				}

				// Start the progress service with a new key
				progressService.create(progressKey)
				progressStarted=true

				//
				// Setup the Quartz job that will execute the actual posting process
				//

				// The triggerName/Group will allow us to controller on import
				Date startTime = new Date(System.currentTimeMillis() + 2000) // Delay 2 seconds to allow this current transaction to commit before firing off the job

				triggerName = "TM-AssetImportReview-${dtb.id}"
				Trigger trigger = new SimpleTrigger(triggerName, null, startTime)

				//trigger.jobDataMap.putAll(results)
				trigger.jobDataMap.put('batchId', dtb.id)
				trigger.jobDataMap.put('progressKey', progressKey)
				trigger.jobDataMap.put('userLoginId', userLogin.id)
				trigger.jobDataMap.put('projectId', project.id)
				trigger.jobDataMap.put('timeZoneId', getSession().getAttribute( "CURR_TZ" )?.CURR_TZ)

				trigger.setJobName('AssetImportReviewJob')			// Please note that the JobName must matche the class file name
				trigger.setJobGroup('tdstm-asset-import-review')	// and that the group should be specifed in the Job

				quartzScheduler.scheduleJob(trigger)

				log.info "$methodName $userLogin kicked of an asset import process for batch (${dtb.id}), progressKey=$progressKey"

				// Need to set the progress into the 'In progress' status so that the modal window will work correctly
				progressService.update(progressKey, 1, progressService.STARTED)

			} catch (ObjectAlreadyExistsException e) {
				errorMsg = 'It appears that someone else is currently reviewing this batch.'
				log.error ExceptionUtil.stackTraceToString(e)
				if (log.isDebugEnabled())
					errorMsg = "$errorMsg ${e.getMessage()}"

			} catch (e) {
				if (progressStarted)
					progressService.update(progressKey, 100I, progressService.FAILED)
				errorMsg = controllerService.getDefaultErrorMessage()
				if (log.isDebugEnabled())
					errorMsg = "$errorMsg ${e.getMessage()}"
			}

			break
		}

		if (errorMsg) {
			render ServiceResults.errors(errorMsg) as JSON
		} else {
			results.progressKey = progressKey
			results.batchStatusCode = DataTransferBatch.PENDING
			render ServiceResults.success([results:results]) as JSON
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
		String triggerName
		String triggerGroup=null
		boolean progressStarted = false
		Long batchId

		while (true) {
			try {
				(project, userLogin) = controllerService.getProjectAndUserForPage(this, 'import')
				if (!project) {
					errorMsg = flash.message
					flash.message = null
					break
				}

				batchId = NumberUtil.toLong(params.id)

				errorMsg = importService.validateImportBatchCanBeProcessed(project.id, batchId)
				if (errorMsg)
					break

				DataTransferBatch dtb = DataTransferBatch.get(batchId)

				// Update the batch and save the progress key 
				progressKey = "AssetImportProcess-" + UUID.randomUUID().toString()
				dtb.progressKey = progressKey
				if (!dtb.save(flush:true, failOnError:true)) {
					errorMsg = "Unable to update batch status : ${GormUtil.allErrorsString(dtb)}"
					break
				}

				// Start the progress service with a new key
				progressService.create(progressKey)
				progressStarted = true

				//
				// Setup the Quartz job that will execute the actual posting process
				//

				// The triggerName/Group will allow us to controller on import
				Date startTime = new Date(System.currentTimeMillis() + 2000) // Delay 2 seconds to allow this current transaction to commit before firing off the job

				triggerName = "TM-AssetImportPosting-${project.id}"
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

				// Need to set the progress into the 'In progress' status so that the modal window will work correctly
				progressService.update(progressKey, 1, progressService.STARTED)

			} catch (ObjectAlreadyExistsException e) {
				errorMsg = 'It appears that someone else is currently posting assets for this project.'
				log.error ExceptionUtil.stackTraceToString(e)
				if (log.isDebugEnabled())
					errorMsg = "$errorMsg ${e.getMessage()}"

			} catch (e) {
				log.error ExceptionUtil.stackTraceToString(e)
				if (progressStarted)
					progressService.update(progressKey, 100I, progressService.FAILED)

				errorMsg = controllerService.getDefaultErrorMessage()
				if (log.isDebugEnabled())
					errorMsg = "$errorMsg ${e.getMessage()}"
			}

			break
		}
		
		if (errorMsg) {
			render ServiceResults.errors(errorMsg) as JSON
		} else {
			results.progressKey = progressKey
			results.batchStatusCode = DataTransferBatch.POSTING

			render ServiceResults.success( [results:results] ) as JSON
		}
	}

	/**
	 * Used to retrieve the current import results of a batch
	 * @param params.id - the id number of the batch
	 * @return The standard ServiceResults object with the value in var results (JSON)
	 */
	def importResults = {
		String errorMsg
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

				Long id = NumberUtil.toLong( params.id )

				if (id == null || id < 1) {
					errorMsg = 'Invalid batch id submitted'
				}

				DataTransferBatch dtb = DataTransferBatch.read(id)
				if (!dtb) {
					errorMsg = 'Unable to find import batch specified'
					break
				}
				if (dtb.project.id != project.id) {
					securityService.reportViolation("attempted to access data import batch ($id) not associated to project (${project.id})", userLogin)
					errorMsg = 'Unable to find import batch specified'
					break
				}
				results.results = dtb.importResults
				results.batchStatusCode = dtb.statusCode
				results.hasErrors = (dtb.hasErrors > 0)

			} catch (e) {
				log.error "getImportResults() received error ${e.getMessage()}"
				errorMsg = log.isDebugEnabled() ? e.getMessage() : 'An error occured while attempting to lookup the results'
			}

			break
		}
		
		if (errorMsg) {
			//if (progressKey)
			//	progressService.remove(progressKey)
			render ServiceResults.errors(errorMsg) as JSON
		} else {
			render(ServiceResults.success(results) as JSON)
		}

	}

}