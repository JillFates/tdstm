import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.event.ImportBatchJobSchedulerEventDetails
import grails.events.EventPublisher
import grails.transaction.NotTransactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataImportService
import org.quartz.JobExecutionContext

@Slf4j
class ImportBatchJob extends SecureJob implements EventPublisher {
	static final String NEXT_BATCH_READY = 'NEXT_BATCH_READY'
	static String group = 'tdstm-import-batch'
	static triggers = {}

	DataImportService dataImportService

	/**
	 * Invokes an asset import process job to post assets to the inventory.
	 */
	@NotTransactional()
	void execute(JobExecutionContext context) {
		Map dataMap = initialize(context)

		long projectId = dataMap.getLongValue('projectId')

		try {
			long batchId = dataMap.getLongValue('batchId')
			long userLoginId = dataMap.getLongValue('userLoginId')

			log.debug('execute() batchId={}, projectId={}, userLoginId={}', batchId, projectId, userLoginId)

			if (batchId) {
				log.info('execute() is about to invoke dataImportService.processBatch(...) to start processing batch ({})', batchId)

				// call process batch
				Integer processedRows = dataImportService.processBatch(Project.get(projectId), batchId)

				log.info('execute() return from dataImportService.processBatch(...) : processed rows={}', processedRows)
			} else {
				log.info('execute() did not find a import batch candidate to start processing.')
			}
		} catch (e) {
			log.error('execute() received exception {}\n{}', e.message, ExceptionUtil.stackTraceToString(e))
		} finally {
			// find if there are more queued jobs to start processing different than the current one
			// Long nextBatchId = dataImportService.getNextBatchToProcess(projectId)
			Map<String, ?> nextBatch = dataImportService.getNextBatchToProcess(projectId)
			if (nextBatch) {
				// if there is a next batch to process, then trigger an application event to schedule it
				notify(NEXT_BATCH_READY, new ImportBatchJobSchedulerEventDetails(projectId, nextBatch.batchId, nextBatch.queuedBy))
			}
		}
	}

}
