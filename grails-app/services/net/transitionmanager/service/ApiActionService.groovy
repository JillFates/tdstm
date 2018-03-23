package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.CamelHostnameIdentifier
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.ThreadLocalVariable
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.agent.AbstractAgent
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.agent.ContextType
import net.transitionmanager.agent.DictionaryItem
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionException
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ApiActionScriptBinding
import net.transitionmanager.integration.ApiActionScriptBindingBuilder
import net.transitionmanager.integration.ApiActionScriptCommand
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import org.apache.camel.Exchange
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.grails.web.json.JSONObject

@Slf4j
@Transactional
class ApiActionService implements ServiceMethods {
	public static final ThreadLocalVariable[] THREAD_LOCAL_VARIABLES = [
			ThreadLocalVariable.ACTION_REQUEST,
			ThreadLocalVariable.TASK_FACADE,
			ThreadLocalVariable.REACTION_SCRIPTS
	]
	CamelHostnameIdentifier camelHostnameIdentifier
	CredentialService credentialService
	DataScriptService dataScriptService
	SecurityService securityService

	// This is a map of the AgentClass enums to the Agent classes (see agentClassForAction)
	private static Map agentClassMap = [
		//(AgentClass.AWS.name())      	: net.transitionmanager.agent.AwsAgent,
		(AgentClass.SERVICE_NOW.name()) : net.transitionmanager.agent.ServiceNowAgent,
		(AgentClass.HTTP.name())		: net.transitionmanager.agent.HttpAgent,
		(AgentClass.VCENTER.name())		: net.transitionmanager.agent.VMwarevCenterAgent
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
	 * Generates a detailed map of an agent and associated methods with all properties of the methods
	 * @param id - the Agent code to look up (e.g. AWS)
	 * @return the method dictionary for the specified agent code
	 * @throws InvalidParamException when
	 */
	Map agentDictionary (String id) throws InvalidParamException {
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
		} else {
			// Iterate over the dictionary replacing the enum in params.context with the enum's name
			for (String key in dictionary.keySet()) {
				DictionaryItem agentInfo = dictionary[key]
				if (agentInfo.params) {
					for (Map paramsMap in agentInfo.params) {
						if (paramsMap.containsKey('context') && paramsMap.context && paramsMap.context instanceof ContextType) {
							paramsMap.context = paramsMap.context.name()
						}
					}
				}
			}
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

		List<ApiAction> apiActions = ApiAction.where {
			project == project
			if (producesDataFilter != null) {
				producesData == producesDataFilter
			}
		}.order("name", "asc").list()
		List<Map> results = []
		apiActions.each { apiAction ->
			results << apiActionToMap(apiAction, minimalInfo)
		}
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
			// sl : this is already verified in TaskService
			//if (! context.isActionInvocable() ) {
			//	throw new InvalidRequestException('Task state does not permit action to be invoked')
			//}

			// Get the method definition of the Agent method
			DictionaryItem methodDef = methodDefinition(action)

			// We only need to implement Task for the moment
			remoteMethodParams = buildMethodParamsWithContext(action, context)

			//
			// This is disabled for the time being until we reimplement messaging
			//
			// // add Camel hostname message identifier
			// remoteMethodParams << [messageOwner: camelHostnameIdentifier.hostnameIdentifierDigest]

			// boolean methodRequiresCallbackMethod = methodDef.params.containsKey('callbackMethod')

			// if (methodRequiresCallbackMethod) {
			// 	if (! action.callbackMethod) {
			// 		log.warn 'Action is missing required callback: {}', action.toString()
			// 		throw new InvalidConfigurationException("Action $action missing required callbackMethod name")
			// 	}
			// 	remoteMethodParams << [callbackMethod: action.callbackMethod]
			// }

			if (CallbackMode.MESSAGE == action.callbackMode) {
				// We're going to perform an Async Message Invocation
				if (!action.asyncQueue) {
					throw new InvalidConfigurationException("Action $action missing required message queue name")
				}
				def agent = agentInstanceForAction(action)

				// Lets try to invoke the method
				log.debug 'About to invoke the following command: {}.{}, queue: {}, params: {}', agent.name, action.agentMethod, action.asyncQueue, remoteMethodParams
				agent."${action.agentMethod}"(action.asyncQueue, remoteMethodParams)
			} else if (!action.callbackMode || CallbackMode.DIRECT == action.callbackMode) {
				// add additional data to the api action execution to have it available when needed
				remoteMethodParams << [
						actionId: action.id,
						taskId: context.id,
						producesData: action.producesData,
						credentials: action.credential?.toMap(),
						apiAction: apiActionToMap(action)
				]

				// get api action agent instance
				def agent = agentInstanceForAction(action)

				ActionRequest actionRequest = new ActionRequest(remoteMethodParams)

				// POC if credential authentication method is COOKIE (vcenter)
				// TODO : SL 2/2018 : use case statement to handle COOKIE, HTTP_SESSION, JWT
				// TODO  : SL 2/2018 : use a method in Credential domain to determine if the call requires pre-authentication
				// if (action?.credential && action.credential.authenticationMethod == AuthenticationMethod.COOKIE) {
				// 	Map authentication = credentialService.authenticate(action.credential)
				// 	if (authentication) {
				// 		// TODO  : SL 2/2018 : this needs to come from CredentialService according to the authenticationMethod being used
				// 		actionRequest.config.setProperty('AUTH_COOKIE_ID', 'vmware-api-session-id')
				// 		actionRequest.config.setProperty('AUTH_COOKIE_VALUE', authentication.value)
				// 	}
				// }

				// check pre script
				JSONObject reactionScripts = JsonUtil.parseJson(action.reactionScripts)
				String preScript = reactionScripts[ReactionScriptCode.PRE.name()]

				ThreadLocalUtil.setThreadVariable(ThreadLocalVariable.ACTION_REQUEST, actionRequest)
				ThreadLocalUtil.setThreadVariable(ThreadLocalVariable.REACTION_SCRIPTS, reactionScripts)

				TaskFacade taskFacade = grailsApplication.mainContext.getBean(TaskFacade.class, context)
				ThreadLocalUtil.setThreadVariable(ThreadLocalVariable.TASK_FACADE, taskFacade)

				// execute PRE script if present
				if (preScript) {
					try {
						invokeReactionScript(ReactionScriptCode.PRE, preScript, actionRequest, new ApiActionResponse(), taskFacade, new AssetFacade(null, null, true), new ApiActionJob())
					} catch (ApiActionException preScriptException) {
						addTaskScriptInvocationError(taskFacade, ReactionScriptCode.PRE, preScriptException)
						String errorScript = reactionScripts[ReactionScriptCode.ERROR.name()]
						String defaultScript = reactionScripts[ReactionScriptCode.DEFAULT.name()]
						String finalizeScript = reactionScripts[ReactionScriptCode.FINAL.name()]

						// execute ERROR or DEFAULT scripts if present
						if (errorScript) {
							try {
								invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, new ApiActionResponse(), taskFacade, new AssetFacade(null, null, true), new ApiActionJob())
							} catch (ApiActionException errorScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
							}
						} else if (defaultScript) {
							try {
								invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, new ApiActionResponse(), taskFacade, new AssetFacade(null, null, true), new ApiActionJob())
							} catch (ApiActionException defaultScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
							}
						}

						// finalize PRE branch when it failed
						if (finalizeScript) {
							try {
								invokeReactionScript(ReactionScriptCode.FINAL, finalizeScript, actionRequest, new ApiActionResponse(), taskFacade, new AssetFacade(null, null, true), new ApiActionJob())
							} catch (ApiActionException finalizeScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.FINAL, finalizeScriptException)
							}
						}

						ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
						return
					}
				}

				// Lets try to invoke the method if nothing came up with the PRE script execution
				log.debug 'About to invoke the following command: {}.{}, request: {}', agent.name, action.agentMethod, actionRequest
				try {
					agent.invoke(action.agentMethod, actionRequest)
				} finally {
					ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
				}
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
	Map invoke (ApiAction action) {
		assert action != null: 'No action provided'

		// get the agent instance
		def agent = agentInstanceForAction(action)

		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = agent.buildMethodParamsWithContext(action, null)

		// TODO : JPM 3/2018 : TM-9936 Move these vars to new property in ActionRequest
		remoteMethodParams << [
			actionId: action.id,
			producesData: action.producesData,
			credentials: action.credential?.toMap()
		]

		ActionRequest actionRequest = new ActionRequest(remoteMethodParams)

		log.debug 'About to invoke the following command: {}.{} with params {}', agent.name, action.agentMethod, remoteMethodParams

		// execute action and return any result coming
		return agent.invoke(action.agentMethod, actionRequest)
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
		String clazzName = action.agentClass.name()
		Class clazz = agentClassMap[clazzName]
		if (clazz) {
			return clazz
		} else {
			throw new InvalidRequestException("Action class ${clazzName} not implemented")
		}
	}

	/**
	 * Used to retrieve an instance of Agent class that will handle the method
	 * @param action - the ApiAction to be used to invoke the method
	 * @return the Agent class instance to invoke the method on
	 * @throws InvalidRequestException if the class is not implemented or invalid method specified
	 */
	AbstractAgent agentInstanceForAction (ApiAction action) {
		Class clazz = agentClassForAction(action)
		clazz.instance
	}

	/**
	 * Create or Update an API Action based on a JSON Object.
	 * @param apiActionCommand - the command object containing the values loaded by controller
	 * @param apiActionId - the id of the ApiAction to update
	 * @param project - the project that the ApiAction should belong to (optional)
	 * @return the ApiAction instance that was created or updated
	 */
	ApiAction saveOrUpdateApiAction (ApiActionCommand apiActionCommand, Long apiActionId = null, Long version = null, Project project = null) {
		if (!project) {
			project = securityService.userCurrentProject
		}

		validateBeforeSave(project, apiActionId, apiActionCommand)

		ApiAction apiAction = null

		// If there's an apiActionId then it's an update operation.
		if (apiActionId) {
			// Retrieve the corresponding API Action instance
			apiAction = GormUtil.findInProject(project, ApiAction, apiActionId, true)

			// Make sure nobody changed it while the user was editting the data
			GormUtil.optimisticLockCheck(apiAction, version, 'API Action')
		} else {
			apiAction = new ApiAction(project: project)
		}

		// Populate the apiAction with the properties from the command object
		apiActionCommand.populateDomain(apiAction, false)

		apiAction.save(failOnError: true)

		return apiAction
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
		if (!project) {
			project = securityService.userCurrentProject
		}
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
	def parseEnum(Class enumClass, String field, String value, Boolean mandatory = false, Boolean throwException = false) {
		String baseErrorMsg = "Error trying to create or update an API Action."
		Object result = null
		if (value) {
			try {
				result = enumClass.valueOf(value)
			} catch (IllegalArgumentException e) {
				if (throwException) {
					throw new InvalidParamException("$baseErrorMsg $field with value '$value' is invalid.")
				}
			}
		} else {
			if (mandatory && throwException) {
				throw new InvalidParamException("$baseErrorMsg $field is mandatory.")
			}
		}
		return result

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
	Credential getCredential(Project project, Provider provider, Long credentialId, boolean throwException = false) {
		if (!project) {
			project = securityService.userCurrentProject
		}
		return credentialService.findByProjectAndProvider(credentialId, project, provider, throwException)
	}

	/**
	 * Find and return a DataScript with the given id for the corresponding provider and project.
	 * @param project
	 * @param provider
	 * @param dataScriptId
	 * @return
	 */
	DataScript getDataScript(Project project, Provider provider,  Long dataScriptId) {
		if (!project) {
			project = securityService.userCurrentProject
		}
		return dataScriptService.findByProjectAndProvider(dataScriptId, project, provider, false)
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
	@Transactional(noRollbackFor = [Exception])
	Map<String, ?> invokeReactionScript(
			ReactionScriptCode code,
			String script,
			ActionRequest request,
			ApiActionResponse response,
			TaskFacade task,
			AssetFacade asset,
			ApiActionJob job) {

		ApiActionScriptBinding scriptBinding = grailsApplication.mainContext.getBean(
				ApiActionScriptBindingBuilder.class)
			.with(request)
			.with(response)
			.with(asset)
			.with(task)
			.with(job)
			.build(code)

		def result = new GroovyShell(this.class.classLoader, scriptBinding)
				.evaluate(script, ApiActionScriptBinding.class.name)

		checkEvaluationScriptResult(code, result)

		return [
				result: result
		]
	}

	/**
	 * This method is used to parse the scripts syntax
	 * First prepares an instance of ApiActionScriptProcessor.
	 * Then it creates an instance of ApiActionScriptBinding based on code parameter.
	 * After that it parses the script using an instance of GroovyShell.
	 * @param code an instance of ReactionScriptCode to create a instance of ApiActionScriptBinding
	 * @param script the script code to be evaluated
	 * @param request ActionRequest instance to be bound in the ApiActionScriptBinding instance
	 * @param response ApiActionResponse instance to be bound in the ApiActionScriptBinding instance
	 * @param task ReactionTaskFacade instance to be bound in the ApiActionScriptBinding instance
	 * @param asset ReactionAssetFacade instance to be bound in the ApiActionScriptBinding instance
	 * @param job ApiActionJob instance to be bound in the ApiActionScriptBinding instance
	 * @return a Map that contains the result of the execution script using an instance of GroovyShell
	 */
	Map<String, ?> compileReactionScript(
			ReactionScriptCode code,
			String script,
			ActionRequest request,
			ApiActionResponse response,
			TaskFacade task,
			AssetFacade asset,
			ApiActionJob job) {

		ApiActionScriptBinding scriptBinding = grailsApplication.mainContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(response)
				.with(asset)
				.with(task)
				.with(job)
				.build(code)

		List<Map<String, ?>> errors = []

		try {

			new GroovyShell(this.class.classLoader, scriptBinding)
					.parse(script, ApiActionScriptBinding.class.name)

		} catch (MultipleCompilationErrorsException cfe) {
			ErrorCollector errorCollector = cfe.getErrorCollector()
			log.error("ETL script parse errors: " + errorCollector.getErrorCount(), cfe)
			errors = errorCollector.getErrors()
		}

		List errorsMap = errors.collect { error ->
			[
					startLine  : (error.cause?.startLine)?:"",
					endLine    : (error.cause?.endLine)?:"",
					startColumn: (error.cause?.startColumn)?:"",
					endColumn  : (error.cause?.endColumn)?:"",
					fatal      : (error.cause?.fatal)?:"",
					message    : (error.cause?.message)?:""
			]
		}

		return [
				code: code.name(),
				validSyntax: errors.isEmpty(),
				errors     : errorsMap
		]
	}

	/**
	 * It validates a request with a list of API action/reaction scripts.
	 * @see net.transitionmanager.service.ApiActionService#invokeReactionScript
	 * @param scripts a list of net.transitionmanager.integration.ApiActionScriptCommand
	 * @return a List with results of evaluating every instance of ApiActionScriptCommand
	 */
	List<Map<String, ?>> validateSyntax(List<ApiActionScriptCommand> scripts) {

		return scripts.collect { ApiActionScriptCommand scriptBindingCommand ->

			Map<String, ?> scriptResults = [:]
			try {

				scriptResults = compileReactionScript(
						scriptBindingCommand.code,
						scriptBindingCommand.script,
						new ActionRequest(),
						new ApiActionResponse(),
						new TaskFacade(),
						new AssetFacade(new AssetEntity(), [:], true),
						new ApiActionJob()
				)

			} catch (Exception ex) {
				log.error("Exception evaluating script ${scriptBindingCommand.script} with code: ${scriptBindingCommand.code}", ex)
				scriptResults.error = ex.getMessage()
			}

			return scriptResults
		}
	}

	/**
	 * It checks if the code is ReactionScriptCode.EVALUATE and the results is an instance of ReactionScriptCode.
	 * If results it is not an instance of ReactionScriptCode,  It throws an Exception.
	 * @param code
	 * @param result
	 */
	@Transactional(noRollbackFor = [Exception])
	private void checkEvaluationScriptResult(ReactionScriptCode code, result) {
		if (code == ReactionScriptCode.STATUS && !(result instanceof ReactionScriptCode)) {
			throw new ApiActionException(i18nMessage(Message.ApiActionMustReturnResults,
					'Script must return SUCCESS or ERROR'))
		}
	}

	/**
	 * Return a map representation for an ApiAction.
	 * @param apiAction
	 * @param minimalInfo - flag that indicates that only the id and the name are required.
	 * @return
	 */
	Map<String, Object> apiActionToMap(ApiAction apiAction, boolean minimalInfo = false) {
		Map<String, Object> apiActionMap = null
		// Load just the minimal or all by setting properties to null
		List<String> properties = minimalInfo ? ["id", "name"] : null
		apiActionMap = GormUtil.domainObjectToMap(apiAction, properties)

		// If all the properties are required, the entry for the AgentClass has to be overriden with the following map.
		if (!minimalInfo) {
			AbstractAgent agent = agentInstanceForAction(apiAction)
			apiActionMap.agentClass = [id: apiAction.agentClass.name(),name: agent.name]
			apiActionMap.version = apiAction.version
		}

		return apiActionMap
	}

	List apiActionMethodList(ApiAction apiAction, boolean minimalInfo = false) {
		Map<String, Object> apiActionMap = null
		// Load just the minimal or all by setting properties to null
		List<String> properties = minimalInfo ? ["id", "name"] : null
		apiActionMap = GormUtil.domainObjectToMap(apiAction, properties)

		// If all the properties are required, the entry for the AgentClass has to be overriden with the following map.
		if (!minimalInfo) {
			AbstractAgent agent = agentInstanceForAction(apiAction)
			apiActionMap.agentClass = [id: apiAction.agentClass.name(),name: agent.name]
			apiActionMap.version = apiAction.version
		}

		return apiActionMap
	}
	/**
	 * Add task error message through the task facade
	 * @param taskFacade - the task facade wrapper
	 * @param reactionScriptCode - the reaction script code generating the message
	 * @param apiActionException - the API Exception
	 */
	private void addTaskScriptInvocationError(TaskFacade taskFacade, ReactionScriptCode reactionScriptCode, ApiActionException apiActionException) {
		taskFacade.error(String.format('%s script failure: %s', reactionScriptCode, apiActionException.message))
	}

	/**
	 * Performs some additional checks before the save occurs that includes:
	 *    - validate that the name for the credential being created or updated doesn't already exist
	 * If the validations fail then the InvalidParamException exception is thrown with an appropriate message.
	 * @throws InvalidParamException
	 */
	private void validateBeforeSave(Project project, Long id, Object cmdObj) {
		// Make sure that name is unique
		int count = ApiAction.where {
			project == project
			name == cmdObj.name
			if (id) {
				id != id
			}
		}.count()

		if (count > 0) {
			throw new InvalidParamException('An ApiAction with the same name already exists')
		}
	}
}
