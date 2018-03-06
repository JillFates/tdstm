import com.tdsops.common.security.spring.HasPermission
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
import net.transitionmanager.service.ImportBatchService

@Secured("isAuthenticated()")
@Slf4j
class WsImportBatchController implements ControllerMethods {

	ImportBatchService importBatchService

	/**
	 * Return all the Import Batches for the current project
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def listImportBatches() {
		Project project = getProjectForWs()
		List<ImportBatch> batches = importBatchService.listBatches(project)
		renderSuccessJson(batches*.toMap())
	}

	/**
	 * Find a single Import Batch by its ID
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatch(Long id) {
		ImportBatch importBatch = fetchDomain(ImportBatch, [id:id]) as ImportBatch
		renderSuccessJson(importBatch.toMap())
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
				impacted = importBatchService.queueBatchesForProcessing(project, actionCmd.ids)
				break

			case ImportBatchActionEnum.EJECT:
				impacted = importBatchService.ejectBatchesFromQueue(project, actionCmd.ids)
				break

			case ImportBatchActionEnum.STOP:
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
		PatchActionCommand actionCmd = populateCommandObject(PatchActionCommand) as PatchActionCommand
		validateCommandObject(actionCmd)

		ImportBatch importBatch = fetchDomain(ImportBatch, [id:id]) as ImportBatch

		switch (actionLookup(ImportRecordActionEnum, action)) {
			case ImportBatchActionEnum.IGNORE:
			case ImportBatchActionEnum.INCLUDE:
			case ImportBatchActionEnum.PROCESS:
			default:
				renderErrorJson( 'Currently not implemented' )
				return
		}

		renderSuccessJson((action.toString):true)
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
	def bulkDeleteImportBatches(IdsCommand idsCmd) {
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

}
