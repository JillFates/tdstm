package net.transitionmanager.service

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdssrc.grails.GormUtil
import groovy.util.logging.Slf4j
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
	 * Find a batch for the given project with the id provided.
	 *
	 * @param importBatchId - the id of the batch.
	 * @param project - the project
	 * @return the batch, null if it doesn't exist.
	 */
	ImportBatch findImportBatchById(Long importBatchId, Project project) {
		return GormUtil.findInProject(project, ImportBatch, importBatchId, true)
	}

	/**
	 * Delete a batch if it exists for this project, throwing an exception if it doesn't
	 * or if it's being processed.
	 *
	 * @param importBatchId - the batch id
	 * @param project
	 */
	void deleteImportBatch(Long importBatchId, Project project) {
		ImportBatch importBatch = findImportBatchById(importBatchId, project)
		if (! importBatch) {
			throw new EmptyResultException(Message.ImportBatchDoesntExist)
		}
		if (importBatch.status == ImportBatchStatusEnum.RUNNING) {
			throw new EmptyResultException(Message.ImportBatchRunning)
		}
		importBatch.delete()
	}

	/**
	 * Delete a list of Import Batches, throwing an exception if not all of them
	 * are deleted because of invalid ids of because they're being processed.
	 * @param importBatchIds
	 * @param project
	 */
	void bulkDeleteImportBatches(List<Long> importBatchIds, Project project) {
		/* Even though the ids are received as a List<Long>, internally they are handled as Integers in the query,
		causing a casting exception. */
		String idsList = importBatchIds.join(", ")
		String hql = """
				DELETE FROM ImportBatch 
				WHERE project = :project AND id in ($idsList) AND status != :status"""
		Map hqlParams = [project: project, status: ImportBatchStatusEnum.RUNNING]
		int deletedBatches = ImportBatch.executeUpdate(hql, hqlParams)
		// Throw an exception if not all the batches were deleted
		if (importBatchIds.size() > deletedBatches) {
			throw new EmptyResultException(Message.ImportBatchBulkDelete)
		}
	}

	/**
	 * Set the archived property's value on the Import Batch to that of the parameter received.
	 * This method will throw an EmptyResultException if the given import batch doesn't exist.
	 * @param importBatchId - the id of the batch
	 * @param project - current project
	 * @param archived - true: archive / false: unarchive
	 */
	void setArchivedFlagOnImportBatch(Long importBatchId, Project project, boolean archived) {
		ImportBatch importBatch = findImportBatchById(importBatchId, project)
		if (! importBatch) {
			throw new EmptyResultException(Message.ImportBatchDoesntExist)
		}
		importBatch.archived = BooleanUtils.toInteger(archived)
		importBatch.save(failOnError: true)
	}

	/**
	 * Set the 'archived' flag on a list of import batches.
	 * @param importBatchIds
	 * @param project
	 * @param archivedFlag
	 */
	void bulkSetArchivedFlagOnImportBatches(List<Long> importBatchIds, Project project, boolean archivedFlag) {
		// Transform the boolean flag to 0: false, 1: true
		Integer archived = BooleanUtils.toInteger(archivedFlag)
		/* Even though the ids are received as a List<Long>, internally they are handled as Integers in the query,
		causing a casting exception. */
		String idsList = importBatchIds.join(", ")
		String hql = "UPDATE ImportBatch SET archived = :archived WHERE project = :project AND id in ($idsList)"
		Map params = [archived: archived, project: project]
		Integer updated = ImportBatch.executeUpdate(hql, params)
		// Throw an exception if the number of updated records doesn't match the number of batches received.
		if (updated < importBatchIds.size()) {
			throw new EmptyResultException(Message.ImportBatchBulkUpdate)
		}
	}
}
