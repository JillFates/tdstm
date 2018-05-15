package net.transitionmanager.service

import com.tdsops.event.ImportBatchJobSchedulerEvent
import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationListener

/**
 * This service class is in charge of attend import batch jobs application events.
 * When an ImportBatchJobSchedulerEvent event is received, it calls ImportBatchService.scheduleJob(...)
 * to queue schedule a quartz job
 *
 * For more details see:
 * http://grails.org/plugin/spring-events
 */
@Slf4j
class ImportBatchJobSchedulerResponderService implements ApplicationListener<ImportBatchJobSchedulerEvent> {

	SecurityService securityService
	ImportBatchService importBatchService

	@Override
	void onApplicationEvent(ImportBatchJobSchedulerEvent event) {
		log.info('Want to schedule a new job: {}', event)

		securityService.assumeUserIdentity(event.source.username, true)
		importBatchService.scheduleJob(event.source.project, event.source.batchId)
	}

}
