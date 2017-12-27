package net.transitionmanager.routes

import com.tdsops.camel.ArrayListAggregationStrategy
import com.tdsops.common.security.spring.CamelHostnameIdentifier
import org.apache.camel.builder.RouteBuilder
import net.transitionmanager.service.AwsService
import groovy.util.logging.Slf4j
import org.apache.camel.processor.aggregate.AggregationStrategy
import org.springframework.context.annotation.Bean

/**
 * Route to handle inbound messages coming from AWS SQS service for the TM Response Queue
 */
@Slf4j(value='logger')
class AwsSqsRoute extends RouteBuilder {
	//private static final long BATCH_TIME_OUT = 5000L;
	//private static final int MAX_RECORDS = 10;

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
			//.convertBodyTo(JSON)
			.aggregate(constant(true), batchAggregationStrategy())
			.completionFromBatchConsumer()
			//.completionSize(MAX_RECORDS)
			//.completionTimeout(BATCH_TIME_OUT)
			.to('bean:routingService?method=processMessages')
			.end()
		} else {
			logger.info '**** AwsSqsRoute initialization was skipped'
		}
	}

	@Bean
	AggregationStrategy batchAggregationStrategy() {
		return new ArrayListAggregationStrategy();
	}

}
