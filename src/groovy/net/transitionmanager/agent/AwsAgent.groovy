package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.service.AwsService
import groovy.transform.CompileStatic

/**
 * Methods to interact with the Amazon AWS services
 */
@Slf4j(value='logger')
@Singleton(strict=false)
@CompileStatic
class AwsAgent extends AbstractAgent {

	public AwsService awsService

	/*
	 * Constructor
	 */
	AwsAgent() {

		setInfo(AgentClass.AWS, 'Amazon AWS API')
		setDictionary( [
			PublishSNS: new DictionaryItem( [
				agentMethod: 'PublishSNS',
				name: 'Publish SNS Notification',
				description: 'Used to publish Simple Notification Service (SNS) messages',
				endpointUrl: 'https://sns.{{region}}.amazonaws.com/',
				docUrl: 'https://docs.aws.amazon.com/sns/latest/api/Welcome.html',
				method: 'publishSnsNotification',
				providesData: 0,
				results: invokeResults(),
				params: [
					[
						paramName: 'region',
						desc: 'The AWS Region to publish the SNS notifications (e.g. us-east-1, us-west-2)',
						type: 'String',
						context: ContextType.USER_DEF,
						fieldName: null,
						value: '',
						required:1,
						readonly:0,
						encoded: 1
					]
				] + queueParams()
			] ),
			SendSQS: new DictionaryItem([
				agentMethod: 'SendSQS',
				name: 'Send SQS Message',
				description: 'Used to send Simple Queue Service (SQS) messages',
				endpointUrl: 'https://sqs.{{region}}.amazonaws.com/',
				docUrl: '',
				method: 'sendSqsMessage',
				providesData: 0,
				results: invokeResults(),
				params: [
					[
						paramName: 'region',
						desc: 'The AWS Region to publish the SQS message (e.g. us-east-1, us-west-2)',
						type: 'String',
						context: ContextType.USER_DEF,
						fieldName: null,
						value: '',
						required:1,
						readonly:0,
						encoded: 1
					]
				] + queueParams()
			])
		] )

		awsService = (AwsService) ApplicationContextHolder.getBean('awsService')
	}

	/**
	 * Used to invoke the transport process on a groupd of servers
	 * @param topicName - the name of the token/queue to send the message to
	 * @param message - the message object to send to the queue
	 * @return a map with the invocation results
	 */
	Map publishSnsNotification(String topicName, Object message) {
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