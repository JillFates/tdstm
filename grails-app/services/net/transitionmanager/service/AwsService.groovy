package net.transitionmanager.service

// import com.amazonaws.auth.BasicAWSCredentials
import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.GrailsApplication
import groovy.json.JsonOutput

import groovy.util.logging.Slf4j

/**
 * Methods to interact with Camel components
 */
@Slf4j(value='logger')
// class CamelService implements ServiceMethods, InitializingBean {
class AwsService implements InitializingBean {

	AuditService auditService
	GrailsApplication grailsApplication

	static transactional=false

	// Queue name used for inbound responses to method invocations
	static final String responseQueueName = 'TransitionManager_Response_Queue'

	String accessKey
	String secretKey

	void afterPropertiesSet() {
		def config = grailsApplication.config

		// Settings that should now be in
		// accessKey='AKIAJQVV5RZ45K6T5GRA'
		// secretKey='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'

		// TODO : The credentials should be loaded based on the project
		accessKey = config?.tdstm?.credentials?.aws?.accessKey ?: null
		secretKey = config?.tdstm?.credentials?.aws?.secretKey ?: null

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
		//sendMessage(sqsUrl(topicName), message)
		log.debug "sendSqsMessage() sent message to the $topicName queue"
	}

	/**
	 * Returns the URL endpoint to the SNS Simple Notification Service for a given topic name
	 * @param topicName - the unique name of the queue
	 * @return the URL to the endpoint
	 */
	String snsUrl(String topicName) {
		StringBuilder url = serviceUrl('sns', topicName)
		// log.debug "URL = $url"
		url ? url.toString() : null
	}

	/**
	 * Returns the URL endpoint to the SQS Simple Queuing Service for a given topic name
	 * @param topicName - the unique name of the queue
	 * @return the URL to the endpoint
	 */
	String sqsUrl(String topicName) {
		StringBuilder url = serviceUrl('sqs', topicName)
		// log.debug "URL = $url"
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


	/**
	 * Temporary function to demostrate the method routing
	 * This will update the task specified in the message
	 * @param message - the Map that came from the message
	 */
	void updateTransportStatus(Map message) {
		log.debug "updateTransportStatus() called with message=$message"
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