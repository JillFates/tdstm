package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ActionType
import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.ThreadLocalVariable
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.connector.AbstractConnector
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.connector.DictionaryItem
import net.transitionmanager.connector.GenericHttpConnector
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionRequestParameter
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionException
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ApiActionScriptBinding
import net.transitionmanager.integration.ApiActionScriptBindingBuilder
import net.transitionmanager.integration.ApiActionScriptCommand
import net.transitionmanager.integration.ApiActionScriptEvaluator
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import org.grails.web.json.JSONObject

@Transactional
class ApiActionService implements ServiceMethods {
	public static final ThreadLocalVariable[] THREAD_LOCAL_VARIABLES = [
			ActionThreadLocalVariable.ACTION_REQUEST,
			ActionThreadLocalVariable.TASK_FACADE,
			ActionThreadLocalVariable.ASSET_FACADE,
			ActionThreadLocalVariable.REACTION_SCRIPTS
	]
	CredentialService credentialService
	DataScriptService dataScriptService
	CustomDomainService customDomainService
	ProviderService providerService
	ApiCatalogService apiCatalogService

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
	 * Used to invoke an connector method with a given context
	 * @param action - the ApiAction to be invoked
	 * @param context - the context from which the method parameter values will be derivied
	 */
	void invoke(ApiAction action, Object context) {
		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = [:]

		if (context.hasErrors()) {
			log.warn 'Invoke() encountered data errors in context: {}', GormUtil.allErrorsString(context)
		}

		if (! context) {
			throw new InvalidRequestException('invoke() required context was null')
		}

		if (context instanceof AssetComment) {
			// Get the method definition of the Action configured Api Catalog
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
				def connector = connectorInstanceForAction(action)

				// Lets try to invoke the method
				log.debug 'About to invoke the following command: {}.{}, queue: {}, params: {}', connector.name, action.connectorMethod, action.asyncQueue, remoteMethodParams
				connector."${action.connectorMethod}"(action.asyncQueue, remoteMethodParams)
			} else if (!action.callbackMode || CallbackMode.DIRECT == action.callbackMode) {
				// get api action connector instance
				def connector = connectorInstanceForAction(action)

				ActionRequest actionRequest
				TaskFacade taskFacade = grailsApplication.mainContext.getBean(TaskFacade.class, context)

				// try to construct action request object and execute preScript if there is any
					try {
					actionRequest = createActionRequest(action)
					} catch (ApiActionException preScriptException) {
						addTaskScriptInvocationError(taskFacade, ReactionScriptCode.PRE, preScriptException)
						String errorScript = reactionScripts[ReactionScriptCode.ERROR.name()]
						String defaultScript = reactionScripts[ReactionScriptCode.DEFAULT.name()]
						String finalizeScript = reactionScripts[ReactionScriptCode.FINAL.name()]

						// execute ERROR or DEFAULT scripts if present
						if (errorScript) {
							assetFacade.setReadonly(false)
							try {
								invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, new ApiActionResponse(), taskFacade, assetFacade, new ApiActionJob())
							} catch (ApiActionException errorScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
							}
							assetFacade.setReadonly(true)
						} else if (defaultScript) {
							assetFacade.setReadonly(false)
							try {
								invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, new ApiActionResponse(), taskFacade, assetFacade, new ApiActionJob())
							} catch (ApiActionException defaultScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
							}
							assetFacade.setReadonly(true)
						}

						// finalize PRE branch when it failed
						if (finalizeScript) {
							try {
								invokeReactionScript(ReactionScriptCode.FINAL, finalizeScript, actionRequest, new ApiActionResponse(), taskFacade, assetFacade, new ApiActionJob())
							} catch (ApiActionException finalizeScriptException) {
								addTaskScriptInvocationError(taskFacade, ReactionScriptCode.FINAL, finalizeScriptException)
							}
						}

						ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
						return
					}

				ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.ACTION_REQUEST, actionRequest)
				ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.TASK_FACADE, taskFacade)

				// setup asset facade if task has an asset associated
				AssetFacade assetFacade
				if (context.assetEntity) {
					AssetEntity assetEntity = context.assetEntity
					Map<String, ?> fieldSettings = customDomainService.allFieldSpecs(assetEntity.project, assetEntity.assetClass.toString(), false)
					assetFacade = new AssetFacade(context.assetEntity, fieldSettings, true)
				} else {
					assetFacade = new AssetFacade(null, null, true)
				}
				ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.ASSET_FACADE, assetFacade)

				// Lets try to invoke the method if nothing came up with the PRE script execution
				log.debug 'About to invoke the following command: {}.{}, request: {}', action.apiCatalog.name, action.connectorMethod, actionRequest
				try {
					if (context?.moveEvent?.apiActionBypass) {
						log.info('By passing API Action invocation with following command: {}.{}, request: {}', action.apiCatalog.name, action.connectorMethod, actionRequest)
						taskFacade.byPassed()
					} else {
						connector.invoke(methodDef, actionRequest)
					}
				} catch (Exception e) {
					log.warn(e.message)
					taskFacade.error(e.message)
				} finally {
					// When the API call has finished the ThreadLocal variables need to be cleared out to prevent a memory leak
					ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
				}
			} else {
				throw new InvalidRequestException('Synchronous invocation not supported')
			}
		} else {
			throw new InvalidRequestException('invoke() not implemented for class ' + context.getClass().getName() )
		}
	}

	/**
	 * Create action request object containing all necessary data for the api connector to invoke an api action.
	 * It executes the action pre-scripts if there is any.
	 * @param action
	 * @return
	 */
	@Transactional(noRollbackFor=[Throwable])
	ActionRequest createActionRequest(ApiAction action) {
		if (!action) {
			throw new InvalidRequestException('No action was provided to the invoke command')
		}

		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = buildMethodParamsWithContext(action, null)

		ActionRequest actionRequest = new ActionRequest(remoteMethodParams)
		Map optionalRequestParams = [
				actionId: action.id,
				projectId: action.project.id,
				producesData: action.producesData,
				credentials: action.credential?.toMap(),
				apiAction: apiActionToMap(action)
		]
		actionRequest.setOptions(new ActionRequestParameter(optionalRequestParams))

		// check pre script : set required request configurations
		JSONObject reactionScripts = JsonUtil.parseJson(action.reactionScripts)
		ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS, reactionScripts)
		String preScript = reactionScripts[ReactionScriptCode.PRE.name()]

		// execute PRE script if present
		if (preScript) {
			try {
				invokeReactionScript(ReactionScriptCode.PRE, preScript, actionRequest,
						new ApiActionResponse(),
						new TaskFacade(),
						new AssetFacade(null, null, true),
						new ApiActionJob()
				)
			} catch (ApiActionException preScriptException) {
				log.error('Error invoking PRE script from DataScript: {}', ExceptionUtil.stackTraceToString(preScriptException))
				throw preScriptException
			} finally {
				// When the API call has finished the ThreadLocal variables need to be cleared out to prevent a memory leak
				ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
			}
		}

		return actionRequest
	}

	/**
	 * Used to invoke a connector method within action parameters solely.
	 * @param action - the ApiAction to be invoked
	 * @return
	 */
	ApiActionResponse invoke(ApiAction action) {
		if (!action) {
			throw InvalidRequestException('No action was provided to the invoke command')
		}

		// Get the method definition of the Action configured Api Catalog
		DictionaryItem methodDef = methodDefinition(action)

		// get the connector instance
		def connector = connectorInstanceForAction(action)

		// methodParams will hold the parameters to pass to the remote method
		Map remoteMethodParams = buildMethodParamsWithContext(action, null)

		ActionRequest actionRequest = new ActionRequest(remoteMethodParams)
		Map optionalRequestParams = [
			actionId: action.id,
			projectId: action.project.id,
			producesData: action.producesData,
			credentials: action.credential?.toMap(),
			apiAction: apiActionToMap(action)
		]
		actionRequest.setOptions(new ActionRequestParameter(optionalRequestParams))

		// check pre script : set required request configurations
		JSONObject reactionScripts = JsonUtil.parseJson(action.reactionScripts)
		String preScript = reactionScripts[ReactionScriptCode.PRE.name()]
		// execute PRE script if present
		if (preScript) {
			try {
				invokeReactionScript(ReactionScriptCode.PRE, preScript, actionRequest,
						new ApiActionResponse(),
						new TaskFacade(),
						new AssetFacade(null, null, true),
						new ApiActionJob()
				)
			} catch (ApiActionException preScriptException) {
				log.error('Error invoking PRE script from DataScript: {}', ExceptionUtil.stackTraceToString(preScriptException))
				throw preScriptException
			}
		}

		log.debug 'About to invoke the following command: {}.{} with params {}', action.apiCatalog.name, action.connectorMethod, remoteMethodParams

		// execute action and return any result that were returned
		ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.ACTION_REQUEST, actionRequest)
		try {
			return connector.invoke(methodDef, actionRequest)
		} finally {
			ThreadLocalUtil.destroy(THREAD_LOCAL_VARIABLES)
		}
	}

	/**
	 * Used to construct the method paramater values Map needed to invoke the function
	 * @param action - the ApiAction to generate the method params for
	 * @param context - the context to get the property values from
	 * @return A map with the defined ApiAction property names and values from the context
	 */
	private Map buildMethodParamsWithContext (ApiAction action, Object context) {
		AbstractConnector connector = connectorInstanceForAction(action)

		// This just does a call back on the Connector class to get the built up parameters
		connector.buildMethodParamsWithContext(action, context)
	}

	/**
	 * Used to retrieve the method definition (aka Dictionary Item) for a give ApiAction
	 * @param action - the ApiAction object to lookup the method definition
	 * @return the method definition
	 * @throws InvalidRequestException if the method name is invalid or the connector is not implemented
	 */
	DictionaryItem methodDefinition (ApiAction action) {
		Map<String, ?> dict = ApiCatalogUtil.getCatalogMethods(action.apiCatalog.dictionaryTransformed)
		Map<String, ?> method = dict[action?.connectorMethod]

		if (!method) {
			throw new InvalidRequestException(
					"Action class ${action?.apiCatalog?.name} method ${action?.connectorMethod} not implemented")
		}

		DictionaryItem methodDef = new DictionaryItem(method)
		methodDef
	}

	/**
	 * Used to retrieve an instance of Connector class that will handle the method
	 * @param action - the ApiAction to be used to invoke the method
	 * @return the Connector class instance to invoke the method on
	 * @throws InvalidRequestException if the class is not implemented or invalid method specified
	 */
	AbstractConnector connectorInstanceForAction (ApiAction action) {
		// for now returning a generic connector instance to be able to re-use all abstract connector methods
		GenericHttpConnector.newInstance()
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

			// Make sure nobody changed it while the user was editing the data
			GormUtil.optimisticLockCheck(apiAction, version, 'API Action')
		} else {
			apiAction = new ApiAction(project: project)
		}

		// Populate the apiAction with the properties from the command object
		apiActionCommand.populateDomain(apiAction, false, ['constraintsMap'])

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

		// TM-10541 - Check if the ApiAction is referenced by any Tasks and prevent deleting
		int count = AssetComment.where {
			apiAction == apiAction
		}.count()

		if (count > 0) {
			throw new DomainUpdateException("Unable to delete Api Action since it is being referenced by Tasks")
		}

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

		def result = new ApiActionScriptEvaluator(scriptBinding).evaluate(script)

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

		List errors = new ApiActionScriptEvaluator(scriptBinding).checkSyntax(script)

		return [
				code: code.name(),
				validSyntax: errors.isEmpty(),
				errors     : errors
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

			return compileReactionScript(
						scriptBindingCommand.code,
						scriptBindingCommand.script,
						new ActionRequest(),
						new ApiActionResponse(),
						new TaskFacade(),
						new AssetFacade(new AssetEntity(), [:], true),
						new ApiActionJob())
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
		Map<String, Object> apiActionMap

	Map<String, Object> apiActionToMap(ApiAction apiAction, boolean minimalInfo = true) {
		Map<String, Object> apiActionMap = null
		// Load just the minimal or all by setting properties to null
		List<String> properties
		if (minimalInfo) {
			properties = ['id', 'name', 'actionType', 'isRemote']
			if (ActionType.WEB_API == apiAction.actionType) {
				properties << ['connectorMethod', 'timeout', 'httpMethod', 'endpointUrl', 'isPolling', 'pollingInterval', 'pollingLapsedAfter', 'pollingStalledAfter']
			} else {
				properties << ['commandLine', 'script', 'remoteCredentialMethod']
			}
		}

		// obtain a map representation of ApiAction with desired properties or all
		apiActionMap = GormUtil.domainObjectToMap(apiAction, properties)

		// If all the properties are required, the entry for the ConnectorClass has to be overwritten with the following map.
		if (!minimalInfo) {
			apiActionMap.connectorClass = [id: apiAction.apiCatalog?.id, name: apiAction.apiCatalog?.name]
			apiActionMap.version = apiAction.version
			apiActionMap.actionType = [id: apiAction.actionType.name(), name: apiAction.actionType.getType()]
			apiActionMap.remoteCredentialMethod = null
			if (apiAction.remoteCredentialMethod) {
				apiActionMap.remoteCredentialMethod = [id: apiAction.remoteCredentialMethod.name(), name: apiAction.remoteCredentialMethod.getCredentialMethod()]
			}
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

		// Blank out the apiCatalog if not selected for scripts
		if (cmdObj.actionType != ActionType.WEB_API.name()) {
			cmdObj.apiCatalog?.discard()
			cmdObj.apiCatalog = null
		}
	}

	/**
	 * Clone any existing api actions associated to sourceProject (if any),
	 * then associate those newly created tags to targetProject.
	 *
	 * @param sourceProject  The project from which the existing api actions will be cloned.
	 * @param targetProject  The project to which the new api actions will be associated.
	 */
	void cloneProjectApiActions(Project sourceProject, Project targetProject) {
		List<ApiAction> apiActions = ApiAction.where {
			project == sourceProject
		}.list()

		if (apiActions && !apiActions.isEmpty()) {

			apiActions.each { ApiAction sourceApiAction ->
				Provider targetProvider = providerService.getProvider(sourceApiAction.provider.name, targetProject, false)

				ApiCatalog targetApiCatalog = null
				if (sourceApiAction.apiCatalog) {
					Provider apiCatalogProvider = providerService.getProvider(sourceApiAction.apiCatalog.provider.name, targetProject, false)
					targetApiCatalog = apiCatalogService.getApiCatalog(sourceApiAction.apiCatalog.name, targetProject, apiCatalogProvider, false)
				}

				DataScript targetDataScript = null
				if (sourceApiAction.defaultDataScript) {
					Provider dataScriptProvider = providerService.getProvider(sourceApiAction.defaultDataScript.provider.name, targetProject, false)
					targetDataScript = dataScriptService.findByProjectAndProvider(sourceApiAction.defaultDataScript.name, targetProject, dataScriptProvider, false)
				}

				Credential targetCredential = null
				if (sourceApiAction.credential) {
					Provider credentialProvider = providerService.getProvider(sourceApiAction.credential.provider.name, targetProject, false)
					targetCredential = credentialService.findByProjectAndProvider(sourceApiAction.credential.name, targetProject, credentialProvider, false)
				}

				ApiAction newApiAction = (ApiAction)GormUtil.cloneDomainAndSave(sourceApiAction, [
						project: targetProject,
						provider: targetProvider,
						apiCatalog: targetApiCatalog,
						defaultDataScript: targetDataScript,
						credential: targetCredential
				], false, false)
				log.debug "Cloned api action ${newApiAction.name} for project ${targetProject.toString()}"
			}
		}
	}
}
