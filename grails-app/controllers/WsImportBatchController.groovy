import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.IdsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ImportBatchService
import net.transitionmanager.service.InvalidRequestException

@Secured("isAuthenticated()")
@Slf4j
class WsImportBatchController implements ControllerMethods{

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
	 * @param idsCmd
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatch(Long id) {
		ImportBatch importBatch = (ImportBatch)fetchDomain(ImportBatch, [id:id])
		renderSuccessJson(importBatch.toMap())
	}

	/**
	 * Delete a single ImportBatch by id
	 * @param idsCmd
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def deleteImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch([id], project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Delete a given list of batches.
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def bulkDeleteImportBatches(IdsCommand idsCmd) {
		validateIdsCommand(idsCmd)
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(idsCmd.ids, project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Mark the given Import Batch as archived
	 * @param id - the id of the Import Batch
	 * @return a Map of the ImportBatch after it has been updated
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def archiveImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch([id], project, true)
		ImportBatch ib = fetchDomain(ImportBatch, [id:id])
		renderSuccessJson( ib.toMap() )
	}

	/**
	 * Clear out the archived flag for the given Import Batch
	 * @param id - the ImportBatch id.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def unarchiveImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch([id], project, false)
		ImportBatch ib = fetchDomain(ImportBatch, [id:id])
		renderSuccessJson( ib.toMap() )
	}

	/**
	 * Set the archived flag to true for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkArchiveImportBatches(IdsCommand idsCmd) {
		validateIdsCommand(idsCmd)
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(idsCmd.ids, project, true)
		renderSuccessJson( [updated: true] )
	}

	/**
	 * Set the archived flag to false for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkUnarchiveImportBatches(IdsCommand idsCmd) {
		validateIdsCommand(idsCmd)
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(idsCmd.ids, project, false)
		renderSuccessJson( [updated: true] )
	}

	/** 
	 * Used by controller methods that use the IdsCommand class for params that will throw exception 
	 * if there were no IDs or that an ID was less than 1
	 * @param idsCmd - the IdsCommand object to validate
	 * @throws InvalidRequestException
	 */
	private void validateIdsCommand(IdsCommand idsCmd) {
		if (! idsCmd.validate()) {
			throw new InvalidRequestException(Message.ValidationMissingIds)
		}
	}

}
