package net.transitionmanager.routes

import org.apache.camel.builder.RouteBuilder
import net.transitionmanager.service.AwsService
import net.transitionmanager.service.RoutingService
import groovy.util.logging.Slf4j

/**
 * Route to handle inbound messages coming from AWS SQS service for the TM Response Queue
 */
@Slf4j(value='logger')
class AwsSqsRoute extends RouteBuilder {
	RoutingService routingService
	AwsService awsService
	static final queueName = 'TransitionManager_Response_Queue'

	@Override
	void configure() {
		String url = awsService.sqsUrl(awsService.responseQueueName)
		if (url) {
			println '**** Intializing the AwsSqsRoute'
			from(url)
				.log('AwsSqsRoute received a message and forwarded it')
				.to('bean:routingService?method=processMessage')
		} else {
			println '**** AwsSqsRoute initialization was skipped'
			log.info '**** AwsSqsRoute initialization was skipped'
		}
	}
}
