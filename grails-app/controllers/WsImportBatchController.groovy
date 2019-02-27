import com.tdsops.common.security.spring.HasPermission
import com.tdsops.event.ImportBatchJobSchedulerEventDetails
import grails.events.EventPublisher
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.IdsCommand
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.command.PatchActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Project
import net.transitionmanager.enums.controller.ImportBatchActionEnum
import net.transitionmanager.enums.controller.ImportRecordActionEnum
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.ImportBatchService

@Secured("isAuthenticated()")
@Slf4j
class WsImportBatchController implements EventPublisher, ControllerMethods {

	ImportBatchService importBatchService
	DataImportService dataImportService

	/**
	 * Return all the Import Batches for the current project
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def listImportBatches() {
		Project project = getProjectForWs()
		Collection batches = importBatchService.listBatches(project)
		renderSuccessJson(batches)
	}

	/**
	 * Find a single Import Batch by its ID
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatch(Long id) {
		Project project = getProjectForWs()
		Map batchMap = importBatchService.findBatch(project, id)
		renderSuccessJson(batchMap)
	}

	/**
	 * Find a single ImportBatchRecord by its ID
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatchRecord( Long id, Long recordId ) {
		Project project = getProjectForWs()
		ImportBatchRecord record = importBatchService.fetchImportBatchRecord(project, id, recordId)
		renderSuccessJson(record.toMap())
	}

	/**
	 * Used to retrieve a list of ImportBatchRecords of the ImportBatch specified by the id
	 * @param id - the ID of the ImportBatch
	 * @return <List><Map> of ImportBatchRecord
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def listBatchRecords(Long id) {
		Project project = getProjectForWs()
		ImportBatch importBatch = fetchDomain(ImportBatch, [id:id]) as ImportBatch
		List<ImportBatchRecord> list = importBatchService.listBatchRecords(project, importBatch)
		renderSuccessJson( list*.toMap(true) )
	}

	/**
	 * Used to perform an Action on one or more ImportBatch domain objects
	 * @param PatchActionCommand - in request body
	 * @return a map with the action and the quantity that were changed
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def patchActionOnBatches() {
		PatchActionCommand actionCmd = populateCommandObject(PatchActionCommand) as PatchActionCommand
		validateCommandObject(actionCmd)

		Project project = getProjectForWs()

		Integer impacted = 0

		switch (actionCmd.actionLookup(ImportBatchActionEnum)) {
			case ImportBatchActionEnum.ARCHIVE:
				impacted = importBatchService.setArchivedFlagOnImportBatch(project, actionCmd.ids, true)
				break

			case ImportBatchActionEnum.UNARCHIVE:
				impacted = importBatchService.setArchivedFlagOnImportBatch(project, actionCmd.ids, false)
				break

			case ImportBatchActionEnum.QUEUE:
				notify(ImportBatchJob.NEXT_BATCH_READY, new ImportBatchJobSchedulerEventDetails(project.id, actionCmd.ids[0], securityService.currentUsername))
				impacted = 1
				break

			case ImportBatchActionEnum.EJECT:
				impacted = importBatchService.ejectBatchesFromQueue(project, actionCmd.ids)
				break

			case ImportBatchActionEnum.STOP:
				impacted = importBatchService.signalStopProcessing(project, actionCmd.ids)
				break
			default:
				renderErrorJson( 'Currently not implemented' )
				return
		}

		// ImportBatch ib = fetchDomain(ImportBatch, [id:id]) as ImportBatch
		renderSuccessJson( (actionCmd.action): impacted )
	}

	/**
	 * Used to perform an Action on one or more ImportBatchRecord domain objects of a specified ImportBatch
	 * @param id - the ImportBatch id
	 * @param PatchActionCommand - in request body
	 * @return success or error structure
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def patchActionOnBatchRecords(Long id) {
		Project project = getProjectForWs()

		PatchActionCommand actionCmd = populateCommandObject(PatchActionCommand) as PatchActionCommand
		validateCommandObject(actionCmd)

		ImportBatch importBatch = fetchDomain(ImportBatch, [id:id]) as ImportBatch
		Integer affected

		switch (actionCmd.actionLookup(ImportRecordActionEnum)) {
			case ImportRecordActionEnum.IGNORE:
				affected = importBatchService.toggleIgnoreOnRecords(importBatch, actionCmd.ids, true)
				break

			case ImportRecordActionEnum.INCLUDE:
				affected = importBatchService.toggleIgnoreOnRecords(importBatch, actionCmd.ids, false)
				break

			case ImportRecordActionEnum.PROCESS: // TODO: change to Queue when implementing TM-10242 Quartz-Task
				// Need to mark the batch as QUEUED before trying to Schedule because the client upon returning
				// from this call will immediately call back to get the details of the batch. Because the scheduling
				// is done async the client is getting the data quicker than the event invocation can happen and the
				// UI doesn't show the status change.
				importBatchService.queueBatchesForProcessing(project, [id])

				// Now schedule the background job to run
				affected = importBatchService.dataImportService.processBatch(importBatch.project, importBatch.id, actionCmd.ids)
				break

			default:
				renderErrorJson( 'Currently not implemented' )
				return
		}

		dataImportService.updateBatchStatus(importBatch.id)
		renderSuccessJson( (actionCmd.action.toString()) : affected )
	}

	/**
	 * Delete a single ImportBatch by id
	 * @param idsCmd
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def deleteImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(project, [id])
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Delete a given list of batches.
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def bulkDeleteImportBatches() {
		//Delete with a body doesn't get bound to command objects in Grails 3. In general from a REST perspective
		// Delete methods shouldn't have a body, because they are usually deleting a resource by id.
		IdsCommand idsCmd = populateCommandObject(IdsCommand)
		validateCommandObject(idsCmd)
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(project, idsCmd.ids)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Update the Import Batch Record with new values for the given fields.
	 * @param id: ImportBatch id
	 * @param recordId: ImportBatchRecord id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def updateImportBatchRecord(Long id, Long recordId) {
		Project project = getProjectForWs()
		ImportBatchRecordUpdateCommand command = populateCommandObject(ImportBatchRecordUpdateCommand)
		ImportBatchRecord record = importBatchService.updateBatchRecord(project, id, recordId, command)
		renderSuccessJson(record.toMap())
	}

	/**
	 * Used to fetch various sorts of information about a Batch
	 * @param id - id number of the batch of interest
	 * @param info - the type of information interested in (e.g. progress)
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def getInfoOfBatch(Long id, String info) {
		Project project = getProjectForWs()
		Map infoMap = importBatchService.getImportBatchInfo(project, id, info)
		renderSuccessJson(infoMap)
	}
}
