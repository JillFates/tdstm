package net.transitionmanager.service

import com.tdsops.common.security.spring.CamelHostnameIdentifier
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.agent.*
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import com.tds.asset.AssetComment
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Slf4j
@Transactional
class ApiActionService implements ServiceMethods {

	CamelHostnameIdentifier camelHostnameIdentifier
	CredentialService credentialService
	DataScriptService dataScriptService
	ProviderService providerService

	// This is a map of the AgentClass enums to the Agent classes (see agentClassForAction)
	private static Map agentClassMap = [
		(AgentClass.AWS)         : AwsAgent,
		(AgentClass.RIVER_MEADOW): RiverMeadowAgent,
		(AgentClass.SERVICE_NOW) : ServiceNowAgent
	].asImmutable()

	/**
	 * Get a list of agent names
	 * @return
	 */
	List<String> agentNamesList () {
		List<Map> agents = new ArrayList<>()

		agentClassMap.each { entry ->
			Class clazz = entry.value
			AbstractAgent agent = clazz.newInstance()
			Map info = [
				id  : agent.agentClass.toString(),
				name: agent.name
			]
			agents << info
		}

		return agents
	}

	/**
	 * Get an agent details by agent name
	 * @param agentCode
	 * @return the method dictionary for a specified agent
	 */
	Map agentDictionary (String id) {
		Map dictionary = [:]
		List<String> agentIds = []
		agentClassMap.each { entry ->
			Class clazz = entry.value
			AbstractAgent agent = clazz.newInstance()
			String agentId = agent.agentClass.toString()
			agentIds << agentId
			if (agentId == id) {
				dictionary = agent.dictionary()
			}
		}

		if (!dictionary) {
			throw new InvalidParamException("Invalid agent ID $id, options are $agentIds")
		}

		return dictionary
	}

	/**
	 * Find an ApiAction by id
	 * @param id
	 * @return
	 */
	ApiAction find (Long id) {
		return ApiAction.get(id)
	}

	/**
	 * Find and ApiAction by id and project it belongs to
	 * @param id
	 * @param project
	 * @parm throwException
	 * @return
	 */
	ApiAction find(Long id, Project project, boolean throwException = false) {
		return GormUtil.findInProject(project, ApiAction, id, throwException)
	}

	/**
	 * List all the API Action for the given project.
	 * @param project
	 * @param minimalInfo - if true, only the id and name for each API Action will be returned.
	 * @param filterParams - filters for narrowing down the search.
	 * @return
	 */
	List<Map> list (Project project, Boolean minimalInfo = true, Map filterParams = [:]) {

		Integer producesDataFilter = NumberUtil.toZeroOrOne(filterParams["producesData"])

		List apiActions = ApiAction.where {
			project == project
			if (producesDataFilter != null) {
				producesData == producesDataFilter
			}
		}.order("name", "asc").list()
		List<Map> results = []
		apiActions.each { apiAction ->
			AbstractAgent agent = agentInstanceForAction(apiAction)
			results << apiAction.toMap(agent, minimalInfo)
		}
		return results
	}

	/**
	 * Used to invoke an agent method with a given context
	 * @param action - the ApiAction to be invoked
	 * @param context - the context from which the method parameter values will be derivied
	 */
	void invoke (ApiAction action, Object context) {
		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = [:]
		if (context.hasErrors()) {
			log.warn 'Invoke() encountered data errors in context: {}', com.tdssrc.grails.GormUtil.allErrorsString(context)
		}
		if (!context) {
			throw new InvalidRequestException('invoke() required context was null')
		}

		if (context instanceof AssetComment) {
			// Validate that the method is still invocable
			if (!context.isActionInvocable()) {
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
				if (!action.callbackMethod) {
					log.warn 'Action is missing required callback: {}', action.toString()
					throw new InvalidConfigurationException("Action $action missing required callbackMethod name")
				}
				remoteMethodParams << [callbackMethod: action.callbackMethod]
			}

			if (CallbackMode.NA != action.callbackMode) {
				// We're going to perform an Async Message Invocation
				if (!action.asyncQueue) {
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
					'invoke() not implemented for class ' + context.getClass().getName())
		}
	}

	/**
	 * Used to invoke an agent method within action parameters solely.
	 * @param action - the ApiAction to be invoked
	 * @return
	 */
	Map invoke (ApiAction action) {
		assert action != null: 'No action provided.'

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
	Map<String, ?> getApiActionParametersAndValuesFromContext (ApiAction apiAction, AssetComment assetComment) {
		return buildMethodParamsWithContext(apiAction, assetComment)
	}

	/**
	 * Used to construct the method paramater values Map needed to invoke the function
	 * @param action - the ApiAction to generate the method params for
	 * @param context - the context to get the property values from
	 * @return A map with the defined ApiAction property names and values from the context
	 */
	private Map buildMethodParamsWithContext (ApiAction action, Object context) {
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
	private DictionaryItem methodDefinition (ApiAction action) {
		Object agent = agentInstanceForAction(action)
		Map dict = agent.dictionary()
		DictionaryItem methodDef = dict[action.agentMethod]
		if (!methodDef) {
			throw new InvalidRequestException(
					"Action class ${action.agentClass} method ${action.agentMethod} not implemented")
		}
		methodDef
	}

	/**
	 * Used to retrieve the Agent class that will handle the method
	 * @param action - the ApiAction to be used to invoke the method
	 * @return the Agent class to invoke the method on
	 * @throws InvalidRequestException if the class is not implemented or invalid method specified
	 */
	private Class agentClassForAction (ApiAction action) {
		Class clazz = agentClassMap[action.agentClass]
		if (!clazz) {
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
	private AbstractAgent agentInstanceForAction (ApiAction action) {
		Class clazz = agentClassForAction(action)
		clazz.instance
	}

	/**
	 * Delete the given ApiAction.
	 * @param id
	 * @param project
	 * @param flush
	 */
	void delete (Long id, Project project, boolean flush = false) {
		ApiAction apiAction = GormUtil.findInProject(project, ApiAction, id, true)
		apiAction.delete(flush: flush)
	}

	/**
	 * Validate that no other API Action with the same name exists for this project.
	 * @param project
	 * @param name
	 * @param apiActionId - if null, it's a create operation, otherwise an update.
	 * @return
	 */
	boolean validateApiActionName (Project project, String name, Long apiActionId = null) {
		boolean isValid = false

		// Both name and project are required for validating.
		if (name && project) {
			// Find an API Action for this project with the given name.
			Long id = ApiAction.where {
				project == project
				name == name
			}.projections { property('id') }.find()

			// If no API Action was found or the IDs match, the name it's okay.
			if (!id || id == apiActionId) {
				isValid = true
			}
		}
		return isValid

	}

	/**
	 * Convert a string into the corresponding enum.
	 * @param enumClass
	 * @param field
	 * @param value
	 * @return
	 */
	def parseEnum(Class enumClass, String field, String value, Boolean mandatory = false) {
		String baseErrorMsg = "Error trying to create or update an API Action."
		if (!value && mandatory) {
			throw new InvalidParamException("$baseErrorMsg $field is mandatory.")
		}
		if (value) {
			try {
				return enumClass.valueOf(value)
			} catch (IllegalArgumentException e) {
				throw new InvalidParamException("$baseErrorMsg $field with value '$value' is invalid.")
			}
		} else {
			return null
		}

	}

	/**
	 *
	 * @param project
	 * @param provider
	 * @param credentialId
	 * @return
	 */
	Credential findCredential(Project project, Provider provider, Long credentialId) {
		return Credential.where {
			id == credentialId
			project == project
			provider == provider
		}

	}

	/**
	 * Create or Update an API Action based on a JSON Object.
	 * @param project
	 * @param apiActionJson
	 * @param apiActionId
	 * @return
	 */
	ApiAction saveOrUpdateApiAction (Project project, JSONObject apiActionJson, Long apiActionId = null) {

		ApiAction apiAction

		// If there's an apiActionId then it's an update operation.
		if (apiActionId) {
			// Retrieve the corresponding API Action instance
			apiAction = GormUtil.findInProject(project, ApiAction, apiActionId, true)
		} else {
			apiAction = new ApiAction(project: project)
		}

		String actionName = apiActionJson.name

		// Make sure the name is valid. Fail otherwise.
		if (!validateApiActionName(project, apiActionJson.name, apiActionId)) {
			throw new InvalidParamException("Invalid name for API Action.")
		}

		Provider prov = providerService.getProvider(NumberUtil.toLong(apiActionJson.providerId), project)

		Credential credentials = getCredential(project, prov, apiActionJson)

		DataScript dataScript = getDataScript(project, prov, apiActionJson)


		apiAction.with {
			agentClass = parseEnum(AgentClass, "Agent Class", apiActionJson.agentClass, true)
			agentMethod = apiActionJson.agentMethod
			asyncQueue = apiActionJson.asyncQueue
			callbackMethod = apiActionJson.callbackMethod
			callbackMode = parseEnum(CallbackMode, "Callback Mode", apiActionJson.callbackMode)
			credential = credentials
			description = apiActionJson.description
			defaultDataScript = dataScript
			description = apiActionJson.description
			methodParams = apiActionJson.methodParams
			name = actionName
			pollingInterval = NumberUtil.toInteger(apiActionJson.pollingInterval)
			producesData = NumberUtil.toZeroOrOne(apiActionJson.producesData)
			provider = prov
			timeout = NumberUtil.toInteger(apiActionJson.timeout)
		}

		apiAction.save(failOnError: true)
		return apiAction

	}

	/**
	 * Return the corresponding credential from this API Action JSON, validating that its provider matches
	 * the one for the API Action.
	 * The credential is not mandatory, so if none is provided, no exception will be thrown. However, if an
	 * invalid value is passed, the application will detect and throw an error.
	 * @param project
	 * @param provider
	 * @param credentialId
	 * @return
	 */
	private Credential getCredential(Project project, Provider provider, JSONObject apiActionJson) {
		Credential credential = null
		if (apiActionJson.credentialId) {
			Long credentialId = NumberUtil.toLong(apiActionJson.credentialId)
			credential = credentialService.findByProjectAndProvider(credentialId, project, provider, true)
		}
		return credential
	}

	/**
	 * Find and return a DataScript with the given id for the corresponding provider and project.
	 * @param project
	 * @param provider
	 * @param apiActionJson
	 * @return
	 */
	private DataScript getDataScript(Project project, Provider provider, JSONObject apiActionJson) {
		DataScript dataScript = null
		if (apiActionJson.defaultDataScriptId) {
			Long dataScriptId = NumberUtil.toLong(apiActionJson.defaultDataScriptId)
			dataScript = dataScriptService.findByProjectAndProvider(dataScriptId, project, provider, true)
		}
		return dataScript
	}

	/**
	 * This method is used to invoke/evaluate the scripts.
	 * First prepares an instance of ApiActionScriptProcessor.
	 * Then it creates an instance of ApiActionScriptBinding based on code parameter.
	 * After that it executes the script using an instance of GroovyShell.
	 * @param code an instance of ReactionScriptCode to create a instance of ApiActionScriptBinding
	 * @param script the script code to be evaluated
	 * @param request ActionRequest instance to be bound in the ApiActionScriptBinding instance
	 * @param response ApiActionResponse instance to be bound in the ApiActionScriptBinding instance
	 * @param task ReactionTaskFacade instance to be bound in the ApiActionScriptBinding instance
	 * @param asset ReactionAssetFacade instance to be bound in the ApiActionScriptBinding instance
	 * @param job ApiActionJob instance to be bound in the ApiActionScriptBinding instance
	 * @return a Map that contains the result of the execution script using an instance of GroovyShell
	 */
	Map<String, ?> evaluateReactionScript(ReactionScriptCode code, String script, ActionRequest request, ApiActionResponse response, ReactionTaskFacade task, ReactionAssetFacade asset, ApiActionJob job) {

		ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(response)
				.with(asset)
				.with(task)
				.with(job)
				.build(ReactionScriptCode.FINAL)

		def result = new GroovyShell(this.class.classLoader, scriptBinding)
				.evaluate(script, ApiActionScriptBinding.class.name)

		checkEvaluationScriptResult(code, result)

		return [
				result: result
		]
	}
	/**
	 * It checks if the code is ReactionScriptCode.EVALUATE and the results is an instance of ReactionScriptCode.
	 * If results it is not an instance of ReactionScriptCode,  It throws an Exception.
	 * @param code
	 * @param result
	 */
	private void checkEvaluationScriptResult(ReactionScriptCode code, result) {
		if (code == ReactionScriptCode.EVALUATE && !(result instanceof ReactionScriptCode)) {
			throw new ApiActionException(messageSource.getMessage(Message.ApiActionMustReturnResults,
					[] as String[],
					'Script must return SUCCESS or ERROR',
					LocaleContextHolder.locale))
		}
	}
}
