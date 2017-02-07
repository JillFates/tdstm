package net.transitionmanager.agent

import net.transitionmanager.domain.ApiAction
import com.tds.asset.AssetComment
import net.transitionmanager.service.AwsService
import net.transitionmanager.service.InvalidRequestException
import com.tdsops.common.grails.ApplicationContextHolder

import groovy.util.logging.Slf4j

/**
 * Methods to interact with the Amazon AWS services
 */
@Slf4j(value='logger')
@Singleton(strict=false)
class AwsAgent extends AbstractAgent {

	public AwsService awsService

	/*
	 * Constructor
	 */
	AwsAgent() {

		setInfo(AgentClass.AWS, 'Amazon AWS API')

		setDictionary( [
			sendSnsNotification: new DictionaryItem([
				name: 'sendSnsNotification',
				description: 'Used to generate Simple Notification Service (SNS) messages',
				method: 'sendSns',
				params: queueParams(),
				results: invokeResults()
			]),
			sendSqsMessage: new DictionaryItem([
				name: 'sendSqsMessage',
				description: 'Used to generate Simple Queue Service (SQS) messages',
				method: 'sendSqs',
				params: queueParams(),
				results: invokeResults()
			])
		].asImmutable() )

		awsService = ApplicationContextHolder.getBean('awsService')
	}

	/**
	 * Used to invoke the transport process on a groupd of servers
	 * @param topicName - the name of the token/queue to send the message to
	 * @param message - the message object to send to the queue
	 * @return a map with the invocation results
	 */
	Map sendSnsNotification(String topicName, Object message) {
		awsService.sendSnsMessage(topicName, message)
	}

	/**
	 * Used to invoke the transport process on a groupd of servers
	 * @param topicName - the name of the token/queue to send the message to
	 * @param message - the message object to send to the queue
	 * @return a map with the invocation results
	 */
	Map sendSqsMessage(String topicName, Object message) {
		awsService.sendSqsMessage(topicName, message)
	}


}