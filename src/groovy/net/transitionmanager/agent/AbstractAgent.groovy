package net.transitionmanager.agent

import net.transitionmanager.domain.ApiAction
import net.transitionmanager.service.InvalidRequestException
import com.tds.asset.AssetComment

import groovy.util.logging.Slf4j

import java.util.regex.Matcher

/**
 * Methods to interact with RiverMeadow 3rd Party Application API
 */
@Slf4j(value='logger')
class AbstractAgent {

	private Map dict = [:]

	AgentClass agentClass
	String name

	/*
	 * Used to set the AgentClass and the Name of the agent for introspection
	 * @param clazz - the AgentClass
	 * @param agentName - a descriptive name for the class
	 */
	void setInfo(AgentClass clazz, String agentName) {
		agentClass = clazz
		name = agentName
	}

	/*
	 * Used to define the map of the method parameters for the Agent class
	 * @param dict - the Map of the DictionaryItem
	 */
	protected void setDictionary(Map dict) {
		this.dict = dict
	}

	/**
	 * Used to get a catalog of the methods that can be invoked along with the parameters and
	 * results map that the methods have
	 * @return List that describes the callable methods for an Agent class
	 */
	Map dictionary() {
		return dict
	}

	/**
	 * Used to construct the Map of parameters that will be used to call the remote method
	 * @param action - the ApiAction that contains the methodParams
	 * @param context - the domain context that the data for the parameters will come from
	 * @return the map of the parameters with the appropriate values
	 */
	protected Map buildMethodParamsWithContext(ApiAction action, AssetComment task) {
		Map methodParams = [:]
		for(param in action.listMethodParams) {
			switch (ContextType[param.context]) {
				case ContextType.TASK:
					methodParams << [ (param.param) : task[param.property] ]
					break
				case ContextType.ASSET:
				case ContextType.APPLICATION:
				case ContextType.DATABASE:
				case ContextType.DEVICE:
				case ContextType.STORAGE:
					if (task.assetEntity) {
						methodParams << [(param.param): task.assetEntity[param.property]]
					}
					break
				case ContextType.USER_DEF:
					methodParams << [ (param.param) : param.value ]
					break
				default:
					throw new InvalidRequestException("Param context ${param.context} not supported")
			}
		}

		def (epReplaced, paramsUsed) = AbstractAgent.replaceParameters(action.endpointPath, methodParams, true)
		action.endpointPath = epReplaced
		methodParams = paramsUsed

		methodParams
	}

	/**
	 * replace in a String mustache like parameters with the dictionary provided
	 *
	 * @param parametrizedStr  String with Mustache like parameters to be replaced
	 * @param params Parameters Dictionary
	 * @param discardMatchParams Indicate if the returned Dictionary should remove those parameters already replaced
	 * @return a Tuple of the String with the parameters replaced with the values and a copy of the Params Map (with
	 * with the used parameters removed from it if the discardMatchParams is set)
	 */
	static replaceParameters(String parametrizedStr, Map params, boolean discardMatchParams = false){
		Map cpyParams = params.clone()

		StringBuffer sb = new StringBuffer()
		Matcher m = parametrizedStr =~ /\{\{([^\}]*)\}\}/

		while(m.find()) {
			String param = m.group(1)
			String repString = params[param]
			if(repString) {
				m.appendReplacement(sb, repString)
				if (discardMatchParams) {
					cpyParams.remove(param)
				}
			}
		}

		m.appendTail(sb)

		return [sb.toString(), cpyParams]
	}

	// A commonly used set of Maps used for varius parameters and results
	private Map taskIdParam() { [type:Integer, description: 'The task Id to reference the task'] }
	private Map assetGuidParam() { [type:String, descrition: 'The globally unique reference id for an asset'] }

	private Map statusResult() { [type:String, description: 'Status of process (success|error|failed|running)'] }
	private Map causeResult() { [type:String, description: 'The cause of an error or failure'] }

	protected Map invokeResults() { [status: statusResult(), cause: causeResult() ] }
	private Map notImplementedResults() { [status:'error', cause:'Method not implemented'] }

	// Define some of the standard parameters
	private Map queueNameParam() { [type:String, description: 'The name of the queue/topic to send message to'] }
	private Map callbackMethodParam() { [type:String, description: 'The name of the callback method that the async response should trigger'] }
	private Map messageParam() { [type:Object, description: 'The data to pass to the message'] }

	// Build of some of the standard interfaces for agent methods
	protected LinkedHashMap queueParams() {
		[ 	queueName: queueNameParam(),
			callbackMethod: callbackMethodParam(),
			message: messageParam()
		]
	}

}