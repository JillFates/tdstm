package net.transitionmanager.service

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.Project
import net.transitionmanager.i18n.Message
import org.apache.commons.lang3.BooleanUtils

@Slf4j
class ImportBatchService implements ServiceMethods {

	/**
	 * Return a list with the existing batches for the given project and with
	 * the given status (optional).
	 * @param project - if null, the user's current project will be used.
	 * @param batchStatus - param for filtering by that status.
	 * @return all the batches for the project.
	 */
	List<ImportBatch> listBatches(Project project, ImportBatchStatusEnum batchStatus = null) {
		return ImportBatch.where {
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
	void deleteImportBatch(List<Long> importBatchIds, Project project) {
		int deletedBatches = ImportBatch.where {
			project == project
			id in importBatchIds
			status != ImportBatchStatusEnum.RUNNING
		}.deleteAll()

		// Throw an exception if not all the batches were deleted
		if (importBatchIds.size() > deletedBatches) {
			String domainTitle = 'Import Batch' + (importBatchIds.size() > 1 ? 'es' : '')			
			throwException(DomainUpdateException, Message.DomainFailureBulk, ['Delete', domainTitle], "Deleting of $domainTitle failed")
		}
	}

	/**
	 * Set the archived property's value on the Import Batches received.
	 * @param importBatchIds - the id of the batch
	 * @param project - current project
	 * @param archived - true: archive / false: unarchive
	 */
	void setArchivedFlagOnImportBatch(List<Long> importBatchIds, Project project, boolean archivedFlag) {
		// Transform the boolean flag to 0: false, 1: true
		Integer archived = BooleanUtils.toInteger(archivedFlag)
		Integer idCount = importBatchIds.size()

		// Query that archives or unarchives a list of batches.
		String hql = 'UPDATE ImportBatch SET archived = :archived WHERE project = :project AND id in (:batches)'

		Map params = [archived: archived, project: project, batches: importBatchIds]
		Integer updated = ImportBatch.executeUpdate(hql, params)
		
		if (updated < idCount) {
			String actionName = archivedFlag ? 'Archive' : 'Unarchive'
			String domainTitle = 'Import Batch' + (idCount > 1 ? 'es' : '')
			throwException(DomainUpdateException, Message.DomainFailureBulk, [actionName, domainTitle], "$actionName of $domainTitle failed")
		}
	}
}
