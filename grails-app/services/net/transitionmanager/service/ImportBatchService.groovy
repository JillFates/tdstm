package net.transitionmanager.service

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ImportBatchRecordStatusEnum
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ImportBatchRecordFieldUpdateCommand

import java.text.DateFormat
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Project
import net.transitionmanager.i18n.Message
import org.apache.commons.lang3.BooleanUtils
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

@Slf4j
class ImportBatchService implements ServiceMethods {

	DataImportService dataImportService
	UserPreferenceService userPreferenceService
	Scheduler quartzScheduler

	/**
	 * Find all batches and include the records summary information. If the batchId or batchStatus
	 * parameters are specified then the results will filtered down appropriately.
	 * @param project - the project to retrieve batches for
	 * @param batchId - optional parameter to filter results to a single batch
	 * @param batchStatus - optional parameter to filter results to a specified Status
	 * @return a Map a map of each batch where the key is the ordinal position in the query (why not a list???)
	 */
	Map findBatchesWithSummary(Project project, Long batchId = null, ImportBatchStatusEnum batchStatus = null) {

		// Query the info batch and the number of records grouped by status in order to
		// create a list of [batch, status, number of records]
		List batchStatusList = ImportBatchRecord.createCriteria().list {
			createAlias('importBatch', 'batch')
			if (batchStatus){
				eq('batch.status', batchStatus)
			}

			if (batchId) {
				eq('batch.id', batchId)
			}

			eq('batch.project', project)
			projections{
				property('importBatch')
				property('status')
				count('id', 'statusCount')

			}
			groupProperty('importBatch')
			groupProperty('status')
		}

		// Map that will contain the result that will be returned.
		Map results = [:]
		// Iterate over the result of the query.
		for (batchInfo in batchStatusList) {
			// Map that will contain the summary for the batch (total of erred records, etc.).
			Map batchMap = null
			// Fetch the corresponding batch
			ImportBatch batch = (ImportBatch) batchInfo[0]
			if (results.containsKey(batch.id)) {
				batchMap = results[batch.id]
			} else {
				batchMap = batch.toMap()
				batchMap['recordsSummary'] = [count:0, erred: 0, ignored:0, pending:0, processed: 0]

				results[batch.id] = batchMap
			}

			Long statusCount = NumberUtil.toPositiveLong(batchInfo[2])
			ImportBatchStatusEnum status = batchInfo[1]
			switch(status) {
				case ImportBatchStatusEnum.IGNORED:
					batchMap['recordsSummary']['ignored'] = statusCount
					break
				case ImportBatchStatusEnum.PENDING:
					batchMap['recordsSummary']['pending'] = statusCount
					break
				case ImportBatchStatusEnum.COMPLETED:
					batchMap['recordsSummary']['processed'] = statusCount
					break
			}

			// Update the number of records for this batch
			batchMap['recordsSummary']['count'] += statusCount
		}

		// Query for the info about erred records.
		List batchErrorsList = ImportBatchRecord.createCriteria().list {
			createAlias('importBatch', 'batch')
			if (batchStatus){
				eq('batch.status', batchStatus)
			}

			if (batchId) {
				eq('batch.id', batchId)
			}

			gt('errorCount', (long)0)
			eq('batch.project', project)
			projections {
				property('importBatch')
				count('id', 'errorCount')
			}
			groupProperty('importBatch')
		}

		// Update the results with the erred number of records for each batch
		for (batchErrorInfo in batchErrorsList) {
			ImportBatch batch = (ImportBatch) batchErrorInfo[0]
			Map batchMap = results[batch.id]
			batchMap['recordsSummary']['erred'] = batchErrorInfo[1]
		}

		return results
	}

	/**
	 * Return a list with the existing batches for the given project and with
	 * the given status (optional).
	 * @param project - if null, the user's current project will be used.
	 * @param batchStatus - param for filtering by that status.
	 * @return all the batches for the project.
	 */
	Collection listBatches(Project project, ImportBatchStatusEnum batchStatus = null) {
		Map results = findBatchesWithSummary(project)
		return results.values()

	}

	/**
	 * Find a Batch given its id.
	 * @param project
	 * @param batchId
	 * @return
	 */
	Map findBatch(Project project, Long batchId) {
		Map batchMap = findBatchesWithSummary(project, batchId)
		return batchMap[batchId]
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
		ImportBatchRecord record = ImportBatchRecord.get(recordId)
		if (! record || record.importBatch.id != batchId || record.importBatch.project != project ) {
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
	 * Used to set or clear the Ignore flag on individual batch records. The logic will be that
	 * when setToIgnore is false, then it will explicitely clear the flag of all records. When set to
	 * true then it will work as a group toggle with the following rules:
	 *		When mix of ignored and not, then toggle to ignored
	 *		When all not ignored then toggle to ignored
	 * 		When all ignored the toggle to not ignored
	 *
	 * @param batchId - the Id of the ImportBatch for which the records being updated are assigned to
	 * @param recordIds - the ids of the ImportBatchRecords to update
	 * @param setToIgnore - a flag if set to true will update the Records to be ignored otherwise include them in the batch process
	 * @return the count of ImportBatchRecords that were update
	 */
	Integer toggleIgnoreOnRecords(ImportBatch batch, List<Long> recordIds, Boolean setToIgnore) {

		// Fail this if the batch is COMPLETED or RUNNING
		// if (batch.status == ImportBatchStatusEnum.COMPLETED) {
		// 	throw new DomainUpdateException('Can not update a batch that has been completed')
		// }
		if (batch.status == ImportBatchStatusEnum.RUNNING) {
			throw new DomainUpdateException('Can not update a batch that is being processed')
		}

		String hql = '''UPDATE ImportBatchRecord SET status=:status
			WHERE importBatch.id=:batchId AND id in (:recordIds) AND status != :status AND status != :completed'''

		Map params = [
			batchId: batch.id,
			recordIds: recordIds,
			status: determineIgnoreStatus(batch, recordIds),
			// TODO : JPM 3/2018 : Change to use ImportBatchRecordStatusEnum
			// completed: ImportBatchRecordStatusEnum.COMPLETED
			completed: ImportBatchStatusEnum.COMPLETED
		]

		Integer affected = ImportBatch.executeUpdate(hql, params)

		return affected
	}

	/**
	 * Used to determine the status that should be used to set the ImportBatchRecords based on the current status of the
	 * list of records. The logic will be that when setToIgnore is false, then it will explicitely clear the flag of all
	 * records. When set to true then it will work as a group toggle with the following rules:
	 *		When mix of ignored and not, then toggle to ignored
	 *		When all not ignored then toggle to ignored
	 * 		When all ignored the toggle to not ignored
	 *
	 * @param batchId - the Id of the ImportBatch for which the records being updated are assigned to
	 * @param recordIds - the ids of the ImportBatchRecords to update
	 * @return status to set the records to
	 */
	private ImportBatchRecordStatusEnum determineIgnoreStatus(ImportBatch batch,  List<Long> recordIds) {
		Boolean setToIgnore
		// Need to determine what the operation should be based on the three rules described in the javadoc
		List results = ImportBatchRecord.executeQuery(
			'select status, count(*) from ImportBatchRecord where importBatch.id = :batchId and id in :ids group by status',
			[batchId: batch.id, ids:recordIds] )

		int ignored = 0, included = 0
		results.each { row ->
			if (row[0] == ImportBatchStatusEnum.PENDING) {
				included = row[1]
			} else if ( row[0] == ImportBatchStatusEnum.IGNORED ) {
				ignored = row[1]
			}
		}

		if (ignored > 0 && included > 0) {
			setToIgnore = true
		} else if (ignored == 0 && included > 0) {
			setToIgnore = true
		} else {
			setToIgnore = false
		}
		return (setToIgnore ? ImportBatchRecordStatusEnum.IGNORED : ImportBatchRecordStatusEnum.PENDING )
	}

	/**
	 * Ejects or removes one or more batches from the processing queue. It will only update batches that are
	 * associated to the user's current project and the status is QUEUED.
	 * @param project - current project
	 * @param importBatchIds - the id of the batch
	 * @return the count of batches that were queued
	 */
	Integer ejectBatchesFromQueue(Project project, List<Long> batchIds) {

		String hql = '''UPDATE ImportBatch
			SET status = :status, queuedBy = NULL, queuedAt = NULL
			WHERE project = :project AND status=:currentStatus AND id in (:batches)'''

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
		Integer updated = 0
		List remainingBatches = ids

		// Look for stalled batches	where the progress hasn't budged in past 2 minutes
		Date noProgressInPast2Min = new Date()
		use( groovy.time.TimeCategory ) {
			noProgressInPast2Min = noProgressInPast2Min - 2.minutes
		}

		List<Long> stalledBatches = ImportBatch.where {
			project == project
			id in ids
			status == ImportBatchStatusEnum.RUNNING
			processLastUpdated < noProgressInPast2Min
		}.projections {
			property('id')
		}.list()

		if (stalledBatches) {
			updated = stalledBatches.size()
			log.warn "signalStopProcessing() discovered $updated batches for project $project"

			// Now set those batches to the proper status based on what was completed before the
			// batch stalled (a.k.a. runtime error encountered and stopped)
			stalledBatches.each {
				dataImportService.updateBatchStatus(it)
			}
			remainingBatches = ids - stalledBatches
		}

		// Now update any remaining job that was not processed above by setting the stop flag
		if (remainingBatches) {
			log.debug "signalStopProcessing() Flagging batch(es) to stop: $remainingBatches for project $project"
			updated += ImportBatch.where {
				project == project
				id in ids
				status == ImportBatchStatusEnum.RUNNING
			}.updateAll([processStopFlag: 1])
		}

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
		// TODO JPM -- this is a hack because the frontend is passing the Label now
		Map fieldNameToLabels = batch.fieldLabelMapAsJsonMap()
		Map labelsToFieldName = [:]
		fieldNameToLabels.each { field, label -> labelsToFieldName.put(label, field) }

		Closure calculateFieldName = { ImportBatchRecordFieldUpdateCommand field ->
			String fieldName = ( field.fieldName in validFields ? field.fieldName : null)
			if (!fieldName) {
				fieldName = ( field.fieldName in labelsToFieldName ? labelsToFieldName[field.fieldName] : null)
			}
			if (!fieldName){
				throw new InvalidParamException("Encountered unspecified field name (${field.fieldName}) for batch")
			}
			return fieldName
		}
		Map<String, ImportBatchRecordFieldUpdateCommand> importBatchRecordMap = command.fieldsInfo.collectEntries { [( calculateFieldName(it)): it] }

		// Iterate over all the keys in the map of fields to be updated, copying the values to the json in the object.
		for (field in command.fieldsInfo) {
			// Check that the fieldName provided is in the list of fields defined for the batch
			String fieldName = calculateFieldName(field)
			Map fieldMap = fieldsMap[fieldName]
			fieldMap["value"] = field.value

			// Updates find command values
			for (query in fieldMap.find?.query) {

				for (criteria in query.criteria){
					ImportBatchRecordFieldUpdateCommand importBatchRecordFieldUpdateCommand = importBatchRecordMap[criteria.propertyName]
					if (importBatchRecordFieldUpdateCommand){
						criteria.value = importBatchRecordFieldUpdateCommand.value
					}
				}
			}

			//create whenNotFound values
			for (createMap in fieldMap.create){
				ImportBatchRecordFieldUpdateCommand importBatchRecordFieldUpdateCommand = importBatchRecordMap[createMap.key]
				if (importBatchRecordFieldUpdateCommand) {
					createMap.value = importBatchRecordFieldUpdateCommand.value
				}
			}

			//updates whenFound values
			for (updateMap in fieldMap.update){
				ImportBatchRecordFieldUpdateCommand importBatchRecordFieldUpdateCommand = importBatchRecordMap[updateMap.key]
				if (importBatchRecordFieldUpdateCommand) {
					updateMap.value = importBatchRecordFieldUpdateCommand.value
				}
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
					infoMap = [
						status: [code: importBatch.status.getKey(), label:importBatch.status.toString()],
						progress: importBatch.processProgress,
						lastUpdated: importBatch.processLastUpdated
					]
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

	/**
	 * Schedule a quartz job for the batch import process
	 * @param project - user current project
	 * @param batchId - the id of the batch to be queued
	 */
	void scheduleJob(Long projectId, Long batchId) {
		// fetch project
		Project project = Project.get(projectId)
		// fetch batch
		ImportBatch importBatch = fetchBatch(project, batchId)

		// Set batch to queued if this is the first time the user tries to schedule the import batch
		if (importBatch.status == ImportBatchStatusEnum.PENDING) {
			dataImportService.setBatchToQueued(importBatch.id)
			importBatch.refresh()
		}

		// Setup the Quartz job that will execute the actual posting process

		// The triggerName/Group will allow us to controller on import
		Date startTime = new Date(System.currentTimeMillis() + 2000) // Delay 2 seconds to allow this current transaction to commit before firing off the job

		// making import batch trigger name this way to let quartz to fail if there are multiple scheduled jobs going to run at the same time
		String triggerName = 'TM-ImportBatch-' + project.id
		Trigger trigger = new SimpleTriggerImpl(triggerName, null, startTime)

		trigger.jobDataMap.batchId = importBatch.id
		trigger.jobDataMap.username = importBatch.queuedBy
		trigger.jobDataMap.userLoginId = securityService.currentUserLoginId
		trigger.jobDataMap.projectId = project.id
		trigger.setJobName('ImportBatchJob') 			// Please note that the JobName must match the class file name
		trigger.setJobGroup('tdstm-import-batch') 		// and that the group should be specified in the Job

		try {
			quartzScheduler.scheduleJob(trigger)
		} catch (Exception e) {
			log.info('Could not schedule import batch. {}\\n{}', e.message, ExceptionUtil.stackTraceToString(e))
		}

		log.info('scheduleJob() {} kicked of an batch import process for batch ({})', securityService.currentUsername, batchId)
	}
}