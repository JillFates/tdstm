package net.transitionmanager.service

import groovy.util.logging.Slf4j
import grails.converters.JSON
import grails.transaction.Transactional

import net.transitionmanager.agent.*
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import com.tds.asset.AssetComment
import net.transitionmanager.service.InvalidRequestException

@Slf4j(value='logger')
@Transactional
class ApiActionService {

	// This is a map of the AgentClass enums to the Agent classes (see agentClassForAction)
	private static Map agentClassMap = [
		(AgentClass.AWS): AwsAgent,
		(AgentClass.RIVER_MEADOW): RiverMeadowAgent ].asImmutable()

    // This is a map of the AgentClass enums to the Agent classes (see agentClassForAction)
	private static Map agentClassMap = [
		(AgentClass.AWS): AwsAgent,
		(AgentClass.RIVER_MEADOW): RiverMeadowAgent ].asImmutable()ApiAction find(Long id){
        return ApiAction.get(id)
    }

    ApiAction findOrCreateApiAction(Long id, Project project) {
        ApiAction apiAction = find(id)



        return apiAction
    }

	List<Map> list(Project project) {
		List actions = ApiAction.createCriteria().list() {
			eq('project', project)
			order('name', 'asc')
		}
		List<Map> list = actions.collect { [ id: it.id, name: it.name ] }

		return list
	}

	/**
	 * Used to invoke an agent method with a given context
	 * @param action - the ApiAction to be invoked
	 * @param context - the context from which the method parameter values will be derivied
	 */
	void invoke(ApiAction action, Object context) {
		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = [:]
		if (context.hasErrors()) {
			println "TASK ERRORS: ${com.tdssrc.grails.GormUtil.allErrorsString(context)}"
		}
		if (! context) {
			throw new InvalidRequestException('invoke() required context was null')
		}

		if (context instanceof AssetComment) {
			// Validate that the method is still invocable
			if (! context.isActionInvocable() ) {
				throw new InvalidRequestException('Task state does not permit action to be invoked')
			}

			// Get the method definition of the Agent method
			DictionaryItem methodDef = methodDefinition(action)

			// We only need to implement Task for the moment
			remoteMethodParams = buildMethodParamsWithContext(action, context)

			boolean methodRequiresCallbackMethod = methodDef.params.containsKey('callbackMethod')

			if (methodRequiresCallbackMethod) {
				if (! action.callbackMethod) {
					println action.toString()
					throw new InvalidConfigurationException("Action $action missing required callbackMethod name")
				}
				remoteMethodParams << [callbackMethod: action.callbackMethod]
			}

			if (CallbackMode.NA != action.callbackMode) {
				// We're going to perform an Async Message Invocation
				if (! action.asyncQueue) {
					throw new InvalidConfigurationException("Action $action missing required message queue name")
				}
				def agent = agentInstanceForAction(action)

				// Lets try to invoke the method
				println "About to invoke the following command: ${agent.name}.${action.agentMethod}('${action.asyncQueue}', ($remoteMethodParams))"
				agent."${action.agentMethod}"(action.asyncQueue, remoteMethodParams)

			} else {
				throw new InvalidRequestException('Synchronous invocation not supported')
			}
		} else {
			throw new InvalidRequestException(
				'invoke() not implemented for class ' + context.getClass().getName() )
		}
	}

	/**
	 * Used to construct the method paramater values Map needed to invoke the function
	 * @param action - the ApiAction to generate the method params for
	 * @param context - the context to get the property values from
	 * @return A map with the defined ApiAction property names and values from the context
	 */
	private Map buildMethodParamsWithContext(ApiAction action, Object context) {
		AbstractAgent agent = agentInstanceForAction(action)

		// This just does a call back on the Agent class to get the built up parameters
		agent.buildMethodParamsWithContext(action, context)
	}

	/**
	 * Used to retrieve the method definition (aka Dictionary Item) for a give ApiAction
	 * @param action - the ApiAction object to lookup the method definition
	 * @return the method definition
	 * @throws InvalidRequestException if the method name is invalid or the agent is not implemented
	 */
	private DictionaryItem methodDefinition(ApiAction action) {
		Object agent = agentInstanceForAction(action)
		Map dict = agent.dictionary()
		DictionaryItem methodDef = dict[action.agentMethod]
		if (! methodDef) {
			throw new InvalidRequestException(
				"Action class ${action.agentClass} method ${action.agentMethod} not implemented" )
		}
		methodDef
	}

	/**
	 * Used to retrieve the Agent class that will handle the method
	 * @param action - the ApiAction to be used to invoke the method
	 * @return the Agent class to invoke the method on
	 * @throws InvalidRequestException if the class is not implemented or invalid method specified
	 */
	private Class agentClassForAction(ApiAction action) {
		Class clazz = agentClassMap[action.agentClass]
		if (! clazz) {
			throw new InvalidRequestException("Action class ${action.agentClass} not implemented")
		}
		clazz
	}

	/**
	 * Used to retrieve an instance of Agent class that will handle the method
	 * @param action - the ApiAction to be used to invoke the method
	 * @return the Agent class instance to invoke the method on
	 * @throws InvalidRequestException if the class is not implemented or invalid method specified
	 */
	private AbstractAgent agentInstanceForAction(ApiAction action) {
		Class clazz = agentClassForAction(action)
		clazz.instance
	}

}
