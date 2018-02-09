package net.transitionmanager.service

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import groovy.util.logging.Slf4j
import net.transitionmanager.command.IdsCommandObject
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.Project
import net.transitionmanager.i18n.Message
import org.apache.commons.lang3.BooleanUtils

@Slf4j
class ImportBatchService implements ServiceMethods{

	/**
	 * Return a list with the existing batches for the given project and with
	 * the given status (optional).
	 * @param project - if null, the user's current project will be used.
	 * @param batchStatus - param for filtering by that status.
	 * @return all the batches for the project.
	 */
	List<ImportBatch> listBatches(Project project, ImportBatchStatusEnum batchStatus = null) {

		List<ImportBatch> importBatches = ImportBatch.where {
			project == project
			if (batchStatus) {
				status == batchStatus
			}
		}.list()
	}


	/**
	 * Delete a list of Import Batches, throwing an exception if not all of them
	 * are deleted because of invalid ids of because they're being processed.
	 * @param importBatchIds
	 * @param project
	 */
	void deleteImportBatch(IdsCommandObject idsCommand, Project project) {
		if (idsCommand.validate()) {
			List<Long> importBatchIds = idsCommand.ids
			// Query that will delete all not running jobs for this project from a list of ids.
			String hql = """
				DELETE FROM ImportBatch 
				WHERE project = :project AND id in (:batches) AND status != :status"""
			Map hqlParams = [project: project, status: ImportBatchStatusEnum.RUNNING, batches: importBatchIds]
			// Execute the query up keep and retrieve the number of records deleted.
			int deletedBatches = ImportBatch.executeUpdate(hql, hqlParams)
			// Throw an exception if not all the batches were deleted
			if (importBatchIds.size() > deletedBatches) {
				throw new DomainUpdateException(Message.ImportBatchBulkDelete)
			}
		} else {
			throw new InvalidRequestException(Message.RequestMissingIds)
		}

	}

	/**
	 * Set the archived property's value on the Import Batches received.
	 * @param importBatchId - the id of the batch
	 * @param project - current project
	 * @param archived - true: archive / false: unarchive
	 */
	void setArchivedFlagOnImportBatch(IdsCommandObject idsCommand, Project project, boolean archivedFlag) {
		if (idsCommand.validate()) {
			List<Long> importBatchIds = idsCommand.ids
			// Transform the boolean flag to 0: false, 1: true
			Integer archived = BooleanUtils.toInteger(archivedFlag)
			// Query that archives or unarchives a list of batches.
			String hql = "UPDATE ImportBatch SET archived = :archived WHERE project = :project AND id in (:batches)"
			Map params = [archived: archived, project: project, batches: importBatchIds]
			Integer updated = ImportBatch.executeUpdate(hql, params)
			// Throw an exception if the number of updated records doesn't match the number of batches received.
			if (updated < importBatchIds.size()) {
				throw new DomainUpdateException(Message.ImportBatchBulkUpdate)
			}
		} else {
			throw new InvalidRequestException(Message.RequestMissingIds)
		}

	}
}
