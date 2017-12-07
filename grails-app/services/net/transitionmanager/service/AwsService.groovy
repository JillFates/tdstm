package net.transitionmanager.service

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.GrailsApplication
import groovy.json.JsonOutput

import groovy.util.logging.Slf4j

/**
 * Methods to interact with Camel components
 */
@Slf4j
// class CamelService implements ServiceMethods, InitializingBean {
class AwsService implements InitializingBean {
	// http://camel.apache.org/aws-sqs.html
	static final int MAX_MESSAGES_PER_POLL = 10
	static final int WAIT_TIME_SECONDS = 5
	static final boolean DELETE_IF_FILTERED = false

	AuditService auditService
	GrailsApplication grailsApplication

	static transactional=false

	// Queue name used for inbound responses to method invocations
	// static final String responseQueueName = 'TransitionManager_Response_Queue'

	String accessKey
	String secretKey
	String responseQueueName

	void afterPropertiesSet() {
		def config = grailsApplication.config

		// Settings that should now be in
		// accessKey='AKIAJQVV5RZ45K6T5GRA'
		// secretKey='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'

		// TODO : The credentials should be loaded based on the project
		accessKey = config?.tdstm?.credentials?.aws?.accessKey ?: null
		secretKey = config?.tdstm?.credentials?.aws?.secretKey ?: null
		responseQueueName = config?.tdstm?.jms?.aws?.responseQueueName ?: null
	}

	/**
	 * Used to send a message object to the SNS service
	 * @param topicName - the name of the queue configured in AWS SNS
	 * @param message - an object to be sent with the message. This will be a JSON payload.
	 */
	void sendSnsMessage(String topicName, Object message) {
		String jsonMessage = JsonOutput.toJson(message)
		sendMessage(snsUrl(topicName), jsonMessage)
		log.debug "sendSnsMessage() sent message to the $topicName queue"
	}

	/**
	 * Used to send a message object to the SQS service
	 * @param topicName - the name of the queue configured in AWS SQS
	 * @param message - an object to be sent with the message. This will be a JSON payload.
	 */
	void sendSqsMessage(String topicName, Object message) {
		String jsonMessage = JsonOutput.toJson(message)
		sendMessage(sqsUrl(topicName), jsonMessage)
		log.debug "sendSqsMessage() sent message to the $topicName queue"
	}

	/**
	 * Returns the URL endpoint to the SNS Simple Notification Service for a given topic name
	 * @param topicName - the unique name of the queue
	 * @return the URL to the endpoint
	 */
	String snsUrl(String topicName) {
		StringBuilder url = serviceUrl('sns', topicName)
		url ? url.toString() : null
	}

	/**
	 * Returns the URL endpoint to the SQS Simple Queuing Service for a given topic name
	 * @param topicName - the unique name of the queue
	 * @return the URL to the endpoint
	 */
	String sqsUrl(String topicName) {
		StringBuilder url = serviceUrl('sqs', topicName)
		if (url) {
			//url.append('&defaultVisibilityTimeout=5000&deleteIfFiltered=false')
			url.append('&maxMessagesPerPoll=')
			url.append(MAX_MESSAGES_PER_POLL)
			url.append('&waitTimeSeconds=')
			url.append(WAIT_TIME_SECONDS)
			url.append('&deleteIfFiltered=')
			url.append(DELETE_IF_FILTERED)
		}
		url ? url.toString() : null
	}

	/**
	 * Used to construct the base URL for the service with the credentials
	 * @param service - the name of the service (sns, sqs, wfs)
	 * @param topicName - the topic or queue name to communicate with
	 * @param The StringBuffer with the initial URL
	 */
	private StringBuilder serviceUrl(String service, String topicName) {
		StringBuilder url
		if (accessKey && secretKey) {
			url = new StringBuilder("aws-${service}://")
			url.append(topicName)
			url.append('?accessKey=')
			url.append(URLEncoder.encode(accessKey, 'UTF-8'))
			url.append('&secretKey=')
			url.append(URLEncoder.encode(secretKey, 'UTF-8'))
		}
		return url
	}

	// Test methods to play with
	void sendAlert(String message) {
		sendMessage('seda:queue', message)
		log.debug "sendAlert() sent $message"
	}

	void receiveAlert(message) {
		log.debug "receiveAlert() occurred with message=$message"
	}

}