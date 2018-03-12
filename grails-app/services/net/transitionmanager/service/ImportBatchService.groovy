package net.transitionmanager.service

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportBatchRecordStatusEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
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
	 * Return a list of the ImportBatchRecords for a given ImportBatch
	 * the given status (optional).
	 * @param batch - The batch to return a list of the records for
	 * @return all the batches for the project.
	 */
	List<ImportBatch> listBatchRecords(Project project, ImportBatch batch) {
		return ImportBatchRecord.where {
			importBatch == batch
		}.list()
	}

	/**
	 * Return an individual ImportBatchRecord by its ID number
	 * @param project - if null, the user's current project will be used.
	 * @param id  - Id of the batch record
	 * @return the ImportBatchRecord if found otherwise throws EmptyResultException exception
	 */
	ImportBatchRecord fetchImportBatchRecord(Project project, Long batchId, Long recordId) throws EmptyResultException {
		ImportBatch batch = fetchBatch(project, batchId)
		ImportBatchRecord record = ImportBatchRecord.get(recordId)
		if (! record || record.importBatch.id != batch.id ) {
			throw new EmptyResultException('Requested record was not found')
		}
		return record
	}

	/**
	 * Delete a list of Import Batches, throwing an exception if not all of them
	 * are deleted because of invalid ids of because they're being processed.
	 * @param importBatchIds
	 * @param project
	 */
	void deleteImportBatch(Project project, List<Long> importBatchIds) {
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
	Integer setArchivedFlagOnImportBatch(Project project, List<Long> importBatchIds, boolean archivedFlag) {
		// Transform the boolean flag to 0: false, 1: true
		Integer archived = BooleanUtils.toInteger(archivedFlag)
		Integer idCount = importBatchIds.size()

		// Query that archives or unarchives a list of batches.
		String hql = 'UPDATE ImportBatch SET archived = :archived WHERE project = :project AND id in (:batches)'

		Map params = [archived: archived, project: project, batches: importBatchIds]
		Integer updated = ImportBatch.executeUpdate(hql, params)

		return updated
	}

	/**
	 * Triggers the queuing of one or more batches to be processed if they haven't already been queued. It will only
	 * updating batches that are in the PENDING status for the user's current project.
	 * @param project - current project
	 * @param importBatchIds - the id of the batch
	 * @return the count of batches that were queued
	 */
	Integer queueBatchesForProcessing(Project project, List<Long> batchIds) {
		// Transform the boolean flag to 0: false, 1: true
		Integer idCount = batchIds.size()

		// Query that archives or unarchives a list of batches.
		String hql = 'UPDATE ImportBatch SET status = :status WHERE project = :project AND status=:currentStatus AND id in (:batches)'
		Map params = [
			currentStatus: ImportBatchStatusEnum.PENDING,
			status: ImportBatchStatusEnum.QUEUED,
			project: project,
			batches: batchIds ]

		Integer updated = ImportBatch.executeUpdate(hql, params)

		return updated
	}

	/**
	 * Used to set or clear the Ignore flag on individual batch records
	 * @param project - current project
	 * @param batchId - the Id of the ImportBatch for which the records being updated are assigned to
	 * @param recordIds - the ids of the ImportBatchRecords to update
	 * @param setToIgnore - a flag if set to true will update the Records to be ignored otherwise include them in the batch process
	 * @return the count of ImportBatchRecords that were update
	 */
	Integer toggleIgnoreOnRecords(ImportBatch batch, List<Long> recordIds, Boolean setToIgnore) {

		// Fail this if the batch is COMPLETED or RUNNING
		if (batch.status == ImportBatchStatusEnum.COMPLETED) {
			throw new DomainUpdateException('Can not update a batch that has been completed')
		}
		if (batch.status == ImportBatchStatusEnum.RUNNING) {
			throw new DomainUpdateException('Can not update a batch that is being processed')
		}

		String hql = '''UPDATE ImportBatchRecord SET ignored=:ignored, status=:status
			WHERE importBatch.id=:batchId AND id in (:recordIds) AND ignored != :ignored'''

		Map params = [
			batchId: batch.id,
			recordIds: recordIds,
			ignored: (setToIgnore ? 1 : 0),
			status: (setToIgnore ? ImportBatchRecordStatusEnum.IGNORED : ImportBatchRecordStatusEnum.PENDING )
		]

		return ImportBatch.executeUpdate(hql, params)
	}

	/**
	 * Ejects or removes one or more batches from the processing queue. It will only update batches that are
	 * associated to the user's current project and the status is QUEUED.
	 * @param project - current project
	 * @param importBatchIds - the id of the batch
	 * @return the count of batches that were queued
	 */
	Integer ejectBatchesFromQueue(Project project, List<Long> batchIds) {
		// Transform the boolean flag to 0: false, 1: true
		Integer idCount = batchIds.size()

		// Query that archives or unarchives a list of batches.
		String hql = 'UPDATE ImportBatch SET status = :status WHERE project = :project AND status=:currentStatus AND id in (:batches)'
		Map params = [
			currentStatus: ImportBatchStatusEnum.QUEUED,
			status: ImportBatchStatusEnum.PENDING,
			project: project,
			batches: batchIds ]
		Integer updated = ImportBatch.executeUpdate(hql, params)

		return updated
	}

	/**
	 * Set the Stop Processing flag to 1 for a list of Import Batch ids.
	 * @param project - user current project
	 * @param ids - ids of batches to be signaled.
	 * @return number of batches updated.
	 */
	Integer signalStopProcessing(Project project, List<Long> ids) {
		Integer updated = ImportBatch.where {
			project == project
			id in ids
		}.updateAll([processStopFlag: 1])
		return updated
	}

	/**
	 * Used to update an Import Batch Record
	 * @param project - current project
	 * @param importBatchId - Id of the batch the record belongs to
	 * @param recordId - the id of the record being updated
	 * @param command - ImportBatchRecordUpdateCommand with the fieldsInfo JSON from the request
	 * @return
	 */
	ImportBatchRecord updateBatchRecord(Project project, Long importBatchId, Long recordId, ImportBatchRecordUpdateCommand command) {

		ImportBatchRecord record = fetchImportBatchRecord(project, importBatchId, recordId)
		ImportBatch batch = record.importBatch

		// Get the fieldInfo from the record as a Json Map (create an empty map if fieldInfo is null)
		Map fieldsMap = record.fieldsInfoAsMap() ?: [:]

		// Retrieve the fields specified in the ImportBatch
		List<String> validFields = batch.fieldNameListAsList()

		// Iterate over all the keys in the map of fields to be updated, copying the values to the json in the object.
		for (field in command.fieldsInfo) {
			// Check that the fieldName provided is in the list of fields defined for the batch
			if (field.fieldName in validFields) {
				// The field for the id has a different structure
				fieldsMap[field.fieldName]["value"] = field.value
			} else {
				throw new InvalidParamException("Encountered unspecified field name ($field) for batch")
			}
		}

		record.fieldsInfo = JsonUtil.convertMapToJsonString(fieldsMap)
		record.save(failOnError: true)
		return record
	}

	/**
	 * Return a map with information about the corresponding Import Batch.
	 * @param project - user's current project
	 * @param batchId - the id of the batch
	 * @param info - a String with the type of information requested: only 'progress' supported at the moment.
	 * @return
	 */
	Map getImportBatchInfo(Project project, Long batchId, String info) {
		Map infoMap = [:]
		if (info) {
			ImportBatch importBatch = fetchBatch(project, batchId)
			info = info.toLowerCase()
			// Although only 'progress' is supported at the moment, I leave the code ready for future changes.
			switch(info) {
				case 'progress':
					infoMap = [progress: importBatch.processProgress, lastUpdated: importBatch.processLastUpdated]
					break
				default:
					throw new InvalidParamException("Unsupported info requested $info.")
					break
			}
			return infoMap
		}

	}

	/**
	 * Used by the class to fetch the ImportBatch
	 * @param project - user's current project
	 * @param batchId - the id of the batch
	 * @return the batch for the given id or exception if the batch doesn't exist or not associated with project
	 */
	ImportBatch fetchBatch(Project project, Long batchId) {
		return GormUtil.findInProject(project, ImportBatch, batchId, true)
	}
}
