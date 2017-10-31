package net.transitionmanager.routes

import com.tdsops.common.security.spring.CamelHostnameIdentifier
import org.apache.camel.builder.RouteBuilder
import net.transitionmanager.service.AwsService
import groovy.util.logging.Slf4j

/**
 * Route to handle inbound messages coming from AWS SQS service for the TM Response Queue
 */
@Slf4j(value='logger')
class AwsSqsRoute extends RouteBuilder {
	AwsService awsService
	CamelHostnameIdentifier camelHostnameIdentifier

	@Override
	void configure() {
		String url = awsService.sqsUrl(awsService.responseQueueName)
		if (url) {
			logger.info '**** Initializing the AwsSqsRoute'
			from(url)
			.filter {
				it.in.body.contains(camelHostnameIdentifier.hostnameIdentifierDigest)
			}
			.log('AwsSqsRoute received a message and forwarded it')
			.to('bean:routingService?method=processMessage')
		} else {
			logger.info '**** AwsSqsRoute initialization was skipped'
		}
	}

}
