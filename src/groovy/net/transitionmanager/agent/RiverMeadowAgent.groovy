package net.transitionmanager.agent

import groovy.util.logging.Slf4j

/**
 * Methods to interact with RiverMeadow 3rd Party Application API
 */
@Slf4j(value='logger')
@Singleton(strict=false)
class RiverMeadowAgent extends AbstractAgent {

	private static Map groupIdParam() { [type:String, description: 'The id of the group to transport'] }

	/*
	 * Constructor
	 */
	RiverMeadowAgent() {
		setInfo(AgentClass.RIVER_MEADOW, 'River Meadow APIs')

		setDictionary( [
			initiateTransport: new DictionaryItem([
				name: 'initiateTransport',
				description: 'Initiates the transport process for a group of servers',
				params: [
					groupId: groupIdParam()
				],
				results: invokeResults()
			]),
			getTransportStatus: new DictionaryItem([
				name: 'getTransportStatus',
				description: 'Used to retrieve the status of the transport of a particular server',
				params: [
					groupId: groupIdParam(),
					serverGuid: [type:String, description: 'The unique server reference']
				],
				results: invokeResults() + [progress:'progressResult()']
			]),
			preflightCheck: new DictionaryItem([
				name: 'preflightCheck',
				description: 'Performs a series of checks on a group of servers prior to a transport',
				method: 'executePreflightCheck',
				params: [
					groupId: groupIdParam(),
					serverGuid: [type:String, description: 'The unique server reference']
				],
				results: invokeResults() + [type:List, description: 'A list of Map objects describing issues for servers in the group']
			])
		] )
	}

	/**
	 * Used to invoke the transport process on a groupd of servers
	 * @param groupId - the id of the group to start transporting
	 * @return a map with the invocation results
	 */
	Map initiateTransport(String groupId) {
		return notImplementedResults()
	}

	/**
	 * Used to get the status of the an individual server that is being transported in a group
	 * @param groupId - the group id reference
	 * @param serverGuild - the unique id of the server being transported
	 * @return a map containing the current status (status, progress and cause)
	 */
	Map getTransportStatus(String groupId, String serverGuid) {
		return notImplementedResults()
	}

	/**
	 * Used to perform a Preflight Check on a group of servers that will be eventually transported
	 * @param groupId - the id of the group to start transporting
	 * @return A list of server issues or concerns to be addressed
	 */
	Map executePreflightCheck(String groupdId) {
		return notImplementedResults()
	}

}