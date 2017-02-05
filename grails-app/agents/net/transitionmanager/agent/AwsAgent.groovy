package net.transitionmanager.agent


import groovy.util.logging.Slf4j

/**
 * Methods to interact with RiverMeadow 3rd Party Application API
 */
@Slf4j(value='logger')
class AwsAgent implements AgentTrait {

	private static Map topicParam() { [type:String, description: 'The name of the topic/queue to send message to'] }
	private static Map messageParam() { [type:Object, description: 'The data to pass to the message'] }
	private static Map queueParams() { [topicName: topicParam(), message: messageParam()] }

	private static final AgentClass agentClass = AgentClass.AWS

	static final String name='Amazon AWS API'
	static final String description='llll'

	static List dict = [
		new DictionaryItem([
			name: 'sendSnsMessage',
			description: 'Used to generate Simple Notification Service (SNS) messages',
			method: 'sendSns',
			params: queueParams(),
			results: invokeResults()
		]),
		new DictionaryItem([
			name: 'sendSqsMessage',
			description: 'Used to generate Simple Queue Service (SQS) messages',
			method: 'sendSqs',
			params: queueParams(),
			results: invokeResults()
		])
	]

	/**
	 * Used to invoke the transport process on a groupd of servers
	 * @param tokenName - the name of the token/queue to send the message to
	 * @param message - the message object to send to the queue
	 * @return a map with the invocation results
	 */
	static Map sendSqs(String tokenName, Object message) {
		return notImplementedResults()
	}

	/**
	 * Used to get the status of the an individual server that is being transported in a group
	 * @param groupId - the group id reference
	 * @param serverGuild - the unique id of the server being transported
	 * @return a map containing the current status (status, progress and cause)
	 */
	static Map getTransportStatus(String groupId, String serverGuid) {
		return notImplementedResults()
	}

	/**
	 * Used to perform a Preflight Check on a group of servers that will be eventually transported
	 * @param groupId - the id of the group to start transporting
	 * @return A list of server issues or concerns to be addressed
	 */
	static Map executePreflightCheck(String groupdId) {
		return notImplementedResults()
	}

}