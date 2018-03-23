package net.transitionmanager.agent

import net.transitionmanager.domain.ApiAction
import net.transitionmanager.service.InvalidRequestException
import com.tds.asset.AssetComment
import com.tdssrc.grails.UrlUtil

import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * AbstractAgent Class
 *
 * The AbstractAgent provides the base class that all API Action Agents will be derived from. The
 * agent will represent a collection of API Action endpoints with the necessary attributes and parameters
 * definitions to allow the corresponding service the ability to contact the endpoints appropriately.
 *
 */
@Slf4j()
@CompileStatic
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
	 * Used to construct the Map of parameters that will be used to call the remote method. Note
	 * that the values are not URL encoded.
	 *
	 * @param action - the ApiAction that contains the methodParams
	 * @param context - the domain context that the data for the parameters will come from
	 * @return the map of the parameters with the appropriate values
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	protected Map buildMethodParamsWithContext(ApiAction action, AssetComment task) {
		Map methodParams = [:]

		String value
		for (param in action.methodParamsList) {

			switch (ContextType.lookup(param.context)) {
				case ContextType.TASK:
					value = task[param.fieldName]
					break
				case ContextType.ASSET:
				case ContextType.APPLICATION:
				case ContextType.DATABASE:
				case ContextType.DEVICE:
				case ContextType.STORAGE:
					if (task.assetEntity) {
						// This line prevents the @CompileStatic
						value = task.assetEntity[param.fieldName]
					}
					break
				case ContextType.USER_DEF:
					value = param.value
					break
				default:
					// Shouldn't actually ever get here but just in case - put a bullet in this execution
					throw new InvalidRequestException("Parameter context ${param.context} is not supported")
			}
			if (param.encoded == 1) {
				value = UrlUtil.decode(value)
			}
			methodParams.put(param.paramName,value)
		}
		return methodParams
	}

	// A commonly used set of Maps used for varius parameters and results
	private LinkedHashMap taskIdParam() { [
		paramName: 'taskId',
		description: 'The task Id to reference the task',
		type:Integer,
		context: ContextType.TASK,
		fieldName: 'id',
		value: null,
		required: 0,
		readonly: 0,
		encoded: 0
	] }

	// private LinkedHashMap assetGuidParam() { [type:String, descrition: 'The globally unique reference id for an asset'] }

	private LinkedHashMap statusResult() { [type:String, description: 'Status of process (success|error|failed|running)'] }

	private LinkedHashMap causeResult() { [type:String, description: 'The cause of an error or failure'] }

	protected LinkedHashMap invokeResults() { [status: statusResult(), cause: causeResult() ] }

	private LinkedHashMap notImplementedResults() { [status:'error', cause:'Method not implemented'] }

	// Define some of the standard parameters
	private LinkedHashMap queueNameParam() { [
		paramName: 'queueName',
		desc: 'The name of the queue/topic to send message to',
		type: 'String',
		context: ContextType.USER_DEF,
		fieldName: null,
		value: '',
		required: 1,
		readonly: 0,
		encoded: 0
	] }

	private LinkedHashMap callbackMethodParam() { [
		property: 'callbackMethod',
		desc: 'The name of the callback method that the async response should trigger',
		type: 'String',
		context: ContextType.USER_DEF,
		param: null,
		value: '',
		required: 1,
		readonly: 0,
		encoded: 0
	] }
	private LinkedHashMap messageParam() { [
		property: 'message',
		desc: 'The data that represents the message',
		type: 'Object',
		context: null,
		param: null,
		value: null,
		required: 1,
		readonly: 0,
		encoded: 0
	] }

	// Build of some of the standard interfaces for agent methods
	protected List<LinkedHashMap> queueParams() {
		[
			queueNameParam(),
			callbackMethodParam(),
			messageParam()
		]
	}

}