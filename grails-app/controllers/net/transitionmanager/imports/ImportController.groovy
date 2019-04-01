package net.transitionmanager.imports

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DataTransferBatchService
import net.transitionmanager.service.ImportService
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.UserPreferenceService
import org.quartz.ObjectAlreadyExistsException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ImportController implements ControllerMethods {

	ControllerService controllerService
	ImportService importService
	ProgressService progressService
	Scheduler quartzScheduler
	UserPreferenceService userPreferenceService
	DataTransferBatchService dataTransferBatchService

	@HasPermission(Permission.AssetImport)
	def listJobs() {
		List jobs = progressService.list()
		if (jobs) {
			render jobs.toString()
		} else {
			render 'No jobs found'
		}
	}

	/**
	 * Review batch and find error in Excel import if any.
	 * @param : id- data transfer batch id
	 * @return map containing error message if any and import permission  (ModelCreateFromImport)
	 */
	@HasPermission(Permission.AssetImport)
	def invokeAssetImportReview() {
		String methodName = 'invokeAssetReviewProcess()'
		Map results = [info:'', hasPerm:false]
		String errorMsg
		String progressKey
		String triggerName
		boolean progressStarted

		while (true) {
			try {
				Project project = controllerService.getProjectForPage(this)
				if (!project || !controllerService.checkPermission(this, Permission.AssetImport)) {
					errorMsg = flash.message
					flash.message = null
					break
				}

				DataTransferBatch dtb
				(dtb, errorMsg) = importService.getAndValidateBatch(params.id, project)
				if (errorMsg) break

				// Update the batch status to POSTING and save the progress key
				progressKey = "AssetImportReview-" + UUID.randomUUID()
				dtb.progressKey = progressKey
				if (!dataTransferBatchService.save(dtb)) {
					log.error "$methodName error occurred while trying to update the DataTransferBatch $dtb.id ${GormUtil.allErrorsString(dtb)}"
					errorMsg = "Unable to update batch record : ${GormUtil.allErrorsString(dtb)}"
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

				triggerName = 'TM-AssetImportReview-' + dtb.id
				Trigger trigger = new SimpleTriggerImpl(triggerName, null, startTime)

				//trigger.jobDataMap.putAll(results)
				trigger.jobDataMap.batchId = dtb.id
				trigger.jobDataMap.progressKey = progressKey
				trigger.jobDataMap.userLoginId = securityService.currentUserLoginId
				trigger.jobDataMap.projectId = project.id
				trigger.jobDataMap.timeZoneId = userPreferenceService.timeZone

				trigger.setJobName('AssetImportReviewJob')			// Please note that the JobName must matche the class file name
				trigger.setJobGroup('tdstm-asset-import-review')	// and that the group should be specifed in the Job

				quartzScheduler.scheduleJob(trigger)

				log.info "$methodName $securityService.currentUsername kicked of an asset import process for batch ($dtb.id), progressKey=$progressKey"

				// Need to set the progress into the 'In progress' status so that the modal window will work correctly
				progressService.update(progressKey, 1, progressService.STARTED)

			} catch (ObjectAlreadyExistsException e) {
				errorMsg = 'It appears that someone else is currently reviewing this batch.'
				log.error ExceptionUtil.stackTraceToString(e)
				if (log.debugEnabled) {
					errorMsg = "$errorMsg $e.message"
				}

			} catch (e) {
				if (progressStarted) {
					progressService.update(progressKey, 100I, progressService.FAILED)
				}
				errorMsg = controllerService.getDefaultErrorMessage()
				if (log.debugEnabled) {
					errorMsg = "$errorMsg $e.message"
				}
			}

			break
		}

		if (errorMsg) {
			renderErrorJson(errorMsg)
		} else {
			results.progressKey = progressKey
			results.batchStatusCode = DataTransferBatch.PENDING
			renderSuccessJson(results: results)
		}
	}

	/**
	 * Used to kickoff the Asset Import Process task to perform the final step in importing assets into the system. This will setup a
	 * Quartz job and a progressSevice so that it may be able to be tracked once it has started.
	 * @param params.id - the DataTransferBatch id to be processed
	 * @return A JSON object with various data attributes in it
	 */
	@HasPermission(Permission.AssetImport)
	def invokeAssetImportProcess() {
		String errorMsg
		String progressKey
		Map results = [:]
		String triggerName
		boolean progressStarted = false
		Long batchId

		while (true) {
			try {
				Project project = controllerService.getProjectForPage(this)
				if (!project || !controllerService.checkPermission(this, Permission.AssetImport)) {
					errorMsg = flash.message
					flash.message = null
					break
				}

				batchId = NumberUtil.toLong(params.id)

				errorMsg = importService.validateImportBatchCanBeProcessed(project.id, securityService.currentUserLoginId, batchId)
				if (errorMsg) {
					break
				}

				DataTransferBatch dtb = DataTransferBatch.get(batchId)

				// Update the batch and save the progress key
				progressKey = "AssetImportProcess-" + UUID.randomUUID()
				dtb.progressKey = progressKey
				if (!dataTransferBatchService.save(dtb)) {
					errorMsg = "Unable to update batch status : ${GormUtil.allErrorsString(dtb)}"
					break
				}

				// Start the progress service with a new key
				progressService.create(progressKey)
				progressStarted = true

				// Setup the Quartz job that will execute the actual posting process

				// The triggerName/Group will allow us to controller on import
				Date startTime = new Date(System.currentTimeMillis() + 2000) // Delay 2 seconds to allow this current transaction to commit before firing off the job

				triggerName = 'TM-AssetImportPosting-' + project.id
				Trigger trigger = new SimpleTriggerImpl(triggerName, null, startTime)

				//trigger.jobDataMap.putAll(results)
				trigger.jobDataMap.batchId = batchId
				trigger.jobDataMap.progressKey = progressKey
				trigger.jobDataMap.username = securityService.currentUsername
				trigger.jobDataMap.userLoginId = securityService.currentUserLoginId
				trigger.jobDataMap.projectId = project.id
				trigger.jobDataMap.timeZoneId = userPreferenceService.timeZone
				trigger.jobDataMap.dtFormat = userPreferenceService.dateFormat

				trigger.setJobName('AssetImportProcessJob')			// Please note that the JobName must matche the class file name
				trigger.setJobGroup('tdstm-asset-import-process')	// and that the group should be specifed in the Job

				quartzScheduler.scheduleJob(trigger)

				log.info "invokeAssetImportProcess() $securityService.currentUsername kicked of an asset import process for batch ($batchId), progressKey=$progressKey"

				// Need to set the progress into the 'In progress' status so that the modal window will work correctly
				progressService.update(progressKey, 1, progressService.STARTED)

			} catch (ObjectAlreadyExistsException e) {
				errorMsg = 'It appears that someone else is currently posting assets for this project.'
				log.error ExceptionUtil.stackTraceToString(e)
				if (log.debugEnabled) {
					errorMsg = "$errorMsg $e.message"
				}

			} catch (e) {
				log.error ExceptionUtil.stackTraceToString(e)
				if (progressStarted) {
					progressService.update(progressKey, 100I, progressService.FAILED)
				}

				errorMsg = controllerService.getDefaultErrorMessage()
				if (log.debugEnabled) {
					errorMsg = "$errorMsg $e.message"
				}
			}

			break
		}

		if (errorMsg) {
			renderErrorJson(errorMsg)
		} else {
			results.progressKey = progressKey
			results.batchStatusCode = DataTransferBatch.POSTING

			renderSuccessJson(results: results)
		}
	}

	/**
	 * Used to retrieve the current import results of a batch
	 * @param params.id - the id number of the batch
	 * @return The standard ServiceResults object with the value in var results (JSON)
	 */
	@HasPermission(Permission.AssetImport)
	def importResults() {
		String errorMsg
		Map results = [:]

		while (true) {
			try {
				Project project = controllerService.getProjectForPage(this)
				if (!project || !controllerService.checkPermission(this, Permission.AssetImport)) {
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
					securityService.reportViolation("attempted to access data import batch ($id) not associated to project ($project.id)")
					errorMsg = 'Unable to find import batch specified'
					break
				}
				results.results = dtb.importResults
				results.batchStatusCode = dtb.statusCode
				results.hasErrors = dtb.hasErrors > 0

			} catch (e) {
				log.error "getImportResults() received error $e.message"
				errorMsg = log.debugEnabled ? e.message : 'An error occured while attempting to lookup the results'
			}

			break
		}

		if (errorMsg) {
			//if (progressKey)
			//	progressService.remove(progressKey)
			renderErrorJson(errorMsg)
		} else {
			renderSuccessJson(results)
		}
	}
}
