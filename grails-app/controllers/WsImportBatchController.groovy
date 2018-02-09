import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.IdsCommandObject
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
		renderSuccessJson(batches*.toMap())
	}

	/**
	 * Find a single Import Batch by its ID.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def fetchImportBatch(Long ids) {
		ImportBatch importBatch = (ImportBatch)fetchDomain(ImportBatch, [id: ids])
		renderSuccessJson(importBatch.toMap())
	}

	/**
	 * Delete an ImportBatch.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def deleteImportBatch(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(cmd, project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Delete a given list of batches.
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def bulkDeleteImportBatches(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.deleteImportBatch(cmd, project)
		renderSuccessJson( [deleted: true] )
	}

	/**
	 * Mark the given Import Batch as archived.
	 * @param id - the id of the Import Batch
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def archiveImportBatch(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(cmd, project, true)
		//importBatchService.setArchivedFlagOnImportBatch(id, project, true)
		renderSuccessJson( [updated: true] )
	}

	/**
	 * Clear out the archived flag for the given Import Batch
	 * @param id - the ImportBatch id.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def unArchiveImportBatch(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(cmd, project, false)
		renderSuccessJson( [updated: true])
	}

	/**
	 * Set the archived flag to true for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkArchiveImportBatches(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(cmd, project, true)
		renderSuccessJson( [updated: true] )
	}

	/**
	 * Set the archived flag to false for a list of batches.
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def bulkUnArchiveImportBatches(IdsCommandObject cmd) {
		Project project = getProjectForWs()
		importBatchService.setArchivedFlagOnImportBatch(cmd, project, false)
		renderSuccessJson( [updated: true] )
	}

}
