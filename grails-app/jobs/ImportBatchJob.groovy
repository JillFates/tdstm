import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.ImportBatchService
import net.transitionmanager.service.ProgressService
import org.quartz.JobExecutionContext

@Slf4j
class ImportBatchJob extends SecureJob {
	String group = 'tdstm-import-batch'
	static triggers = {}

	ImportBatchService importBatchService
	DataImportService dataImportService
	ProgressService progressService

	/**
	 * Invokes an asset import process job to post assets to the inventory.
	 */
	void execute(JobExecutionContext context) {
		Map dataMap = initialize(context)

		long batchId = dataMap.getLongValue('batchId')
		long projectId = dataMap.getLongValue('projectId')

		try {
			log.debug('userLoginId = {}', dataMap.userLoginId)
			long userLoginId = dataMap.getLongValue('userLoginId')

			log.debug('execute() batchId={}, projectId={}, userLoginId={}', batchId, projectId, userLoginId)

			dataImportService.setBatchToQueued(batchId)
			Long nextBatchId = dataImportService.getNextBatchToProcess(projectId)
			if (nextBatchId) {
				log.info('execute() is about to invoke dataImportService.processBatch(...) to start processing batch ({})', batchId)

				// call process batch
				dataImportService.processBatch(Project.get(projectId), batchId)

				log.info('execute() return from dataImportService.processBatch(...) : results={}', null)
			} else {
				log.info('execute() did not find a import batch candidate to start processing.')
			}
		} catch (e) {
			log.error('execute() received exception {}\n{}', e.message, ExceptionUtil.stackTraceToString(e))
		} finally {
			// find if there are more queued jobs to start processing different than the current one
			Long nextBatchId = dataImportService.getNextBatchToProcess(projectId)
			if (nextBatchId && batchId != nextBatchId) {
				// if there is a next batch to process, then launch it by scheduling it
				importBatchService.scheduleJob(Project.get(projectId), nextBatchId)
			}

			GormUtil.releaseLocalThreadMemory()
		}
	}

}
