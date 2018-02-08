import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ImportBatchService

@Secured("isAuthenticated()")
@Slf4j
class WsImportBatchController implements ControllerMethods{

	ImportBatchService importBatchService

	/**
	 * Return all the Import Batches for the current project.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def listImportBatches() {
		Project project = getProjectForWs()
		List<ImportBatch> batches = importBatchService.listBatches(project)
		renderSuccessJson(batches*.toMap()) // TODO ImportBatch::toMap needs to be implemented
	}

	/**
	 * Find a single Import Batch by its ID.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatch(Long id) {
		Project project = getProjectForWs()
		ImportBatch importBatch = importBatchService.findImportBatchById(id, project)
		renderSuccessJson(importBatch.toMap()) // TODO ImportBatch::toMap needs to be implemented
	}

	/**
	 * Delete an ImportBatch.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def deleteImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(id, project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Delete a given list of batches.
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def bulkDeleteImportBatches() {
		Project project = getProjectForWs()
		List<Long> batches = (List<Long>)request.JSON.batches
		importBatchService.bulkDeleteImportBatches(batches, project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Mark the given Import Batch as archived.
	 * @param id - the id of the Import Batch
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def archiveImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(id, project, true)
		renderSuccessJson( [updated: true] )
	}

	/**
	 * Clear out the archived flag for the given Import Batch
	 * @param id - the ImportBatch id.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def unArchiveImportBatch(Long id) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(id, project, false)
		renderSuccessJson( [updated: true])
	}

	/**
	 * Set the archived flag to true for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkArchiveImportBatches() {
		Project project = getProjectForWs()
		List importBatchIds = (List<Long>) request.JSON.batches
		importBatchService.bulkSetArchivedFlagOnImportBatches(importBatchIds, project, true)
		renderSuccessJson( [updated: true] )
	}

	/**
	 * Set the archived flag to false for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkUnArchiveImportBatches() {
		Project project = getProjectForWs()
		List<Long> importBatchIds = (List<Long>) request.JSON.batches
		importBatchService.bulkSetArchivedFlagOnImportBatches(importBatchIds, project, false)
		renderSuccessJson( [updated: true] )
	}

}
