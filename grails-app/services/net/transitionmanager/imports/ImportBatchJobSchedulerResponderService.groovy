package net.transitionmanager.imports


import com.tdsops.event.ImportBatchJobSchedulerEventDetails
import grails.events.annotation.Subscriber
import grails.events.bus.EventBusAware
import groovy.util.logging.Slf4j
import net.transitionmanager.security.SecurityService

/**
 * This service class is in charge of attend import batch jobs application events.
 * When an ImportBatchJobSchedulerEvent event is received, it calls ImportBatchService.scheduleJob(...)
 * to queue schedule a quartz job
 *
 * For more details see:
 * http://grails.org/plugin/spring-events
 */
@Slf4j
class ImportBatchJobSchedulerResponderService implements EventBusAware {
	SecurityService    securityService
	ImportBatchService importBatchService

	@Subscriber('NEXT_BATCH_READY')
	void onApplicationEvent(ImportBatchJobSchedulerEventDetails event) {
		log.info('Want to schedule a new job: {}', event)

		securityService.assumeUserIdentity(event.username, true)
		importBatchService.scheduleJob(event.projectId, event.batchId)
	}

}
