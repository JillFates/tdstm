package net.transitionmanager.service

import com.tdsops.common.security.spring.CamelHostnameIdentifier
import com.tdssrc.grails.NumberUtil
import groovy.util.logging.Slf4j
import grails.transaction.Transactional

import net.transitionmanager.agent.*
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import com.tds.asset.AssetComment
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.criterion.CriteriaSpecification

@Slf4j
@Transactional
class ApiActionService {
	CamelHostnameIdentifier camelHostnameIdentifier

	// This is a map of the AgentClass enums to the Agent classes (see agentClassForAction)
	private static Map agentClassMap = [
			(AgentClass.AWS): AwsAgent,
			(AgentClass.RIVER_MEADOW): RiverMeadowAgent,
			(AgentClass.SERVICE_NOW): ServiceNowAgent
	].asImmutable()

	ApiAction find(Long id){
		return ApiAction.get(id)
	}

	ApiAction find(Long id, Project project) {
		return ApiAction.where {
			id == id
			project == project
		}.get()
	}

	/**
	 * List all the API Action for the given project.
	 * @param project
	 * @param minimalInfo - if true, only the id and name for each API Action will be returned.
	 * @param filterParams - filters for narrowing down the search.
	 * @return
	 */
	List<Map> list(Project project, Boolean minimalInfo = true, Map filterParams = [:]) {

		Integer producesDataFilter = NumberUtil.toZeroOrOne(filterParams["producesData"])

		List apiActions = ApiAction.where {
			project == project
			if (producesDataFilter != null) {
				producesData == producesDataFilter
			}
		}.order("name", "asc").list()
		List<Map> results = []
		apiActions.each {results << it.toMap(minimalInfo)}
		return results
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
			log.warn 'Invoke() encountered data errors in context: {}', com.tdssrc.grails.GormUtil.allErrorsString(context)
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

			// add Camel hostname message identifier
			remoteMethodParams << [messageOwner: camelHostnameIdentifier.hostnameIdentifierDigest]

			boolean methodRequiresCallbackMethod = methodDef.params.containsKey('callbackMethod')

			if (methodRequiresCallbackMethod) {
				if (! action.callbackMethod) {
					log.warn 'Action is missing required callback: {}', action.toString()
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
				log.debug 'About to invoke the following command: {}.{}, queue: {}, params: {}', agent.name, action.agentMethod, action.asyncQueue, remoteMethodParams
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
	 * Used to invoke an agent method within action parameters solely.
	 * @param action - the ApiAction to be invoked
	 * @return
	 */
	Map invoke(ApiAction action) {
		assert action != null : 'No action provided.'

		// get the agent instance
		def agent = agentInstanceForAction(action)

		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = agent.buildMethodParamsWithContext(action, null)

		log.debug 'About to invoke the following command: {}.{} with params {}', agent.name, action.agentMethod, remoteMethodParams

		// execute action and return any result coming
		return agent."${action.agentMethod}"(remoteMethodParams)
	}

	/**
	 * Get ApiAction parameters values from context object.
	 * @param apiAction
	 * @param assetComment
	 * @return
	 */
	Map<String, ?> getApiActionParametersAndValuesFromContext(ApiAction apiAction, AssetComment assetComment) {
		return buildMethodParamsWithContext(apiAction, assetComment)
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

	/**
	 * Delete the given ApiAction.
	 * @param id
	 * @param project
	 * @param flush
	 */
	void delete(Long id, Project project, boolean flush = false) {
		ApiAction apiAction = findOrException(id, project)
		apiAction.delete(flush: flush)
	}

	/**
	 * Find a given API Action or throw an exception.
	 * @param apiActionId
	 * @param project
	 * @return
	 */
	ApiAction findOrException(Long apiActionId, Project project) {
		ApiAction apiAction = find(apiActionId, project)
		if (!apiAction) {
			throw new EmptyResultException("Cannot find an API Action with the given id.")
		}
		return apiAction
	}

	/**
	 * Validate that no other API Action with the same name exists for this project.
	 * @param project
	 * @param name
	 * @param apiActionId - if null, it's a create operation, otherwise an update.
	 * @return
	 */
	boolean validateApiActionName(Project project, String name, Long apiActionId = null){
		boolean isValid = false

		// Both name and project are required for validating.
		if (name && project) {
			// Find an API Action for this project with the given name.
			ApiAction apiAction = ApiAction.where {
				project == project
				name == name
			}.find()

			// If no API Action was found or the IDs match, the name it's okay.
			if (!apiAction || apiAction.id == apiActionId) {
				isValid = true
			}
		}
		return isValid

	}

}
