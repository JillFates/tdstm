package net.transitionmanager.domain

import com.tdssrc.grails.JsonUtil
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.command.ApiActionMethodParam
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject

import java.util.regex.Matcher

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */
@Slf4j
@ToString(includes='name, agentClass, agentMethod, provider', includeNames=true, includePackage=false)
class ApiAction {

	String name
	String description

	// Indicates the class to use when invoking the action
	AgentClass agentClass

	// The method on the agentClass to invoke
	String agentMethod

	Provider provider

	Project project

	// The default DataScript that is intended to be used to transform the data that the action generates
	DataScript defaultDataScript

	// Flag that indicates if the action generates data that can be ingested
	Integer producesData=0

	/*
	 * A JSON object that contains the mapping of method parameters and where the values will be sourced from
	 * [ {
	 * 		"param":"assetId",
	 *		"desc": "The unique id to reference the asset",
	 *		"type":"string",
	 *		"context": ContextType.ASSET.toString(),	// The context that the param value will be pulled from
	 * 		"property": "field", 	// The property on the context that the value will be pulled from
	 *		"value": "user def value"	// The value to use context is ContextType.USER_DEF
	 *   }
	 * ]
	 */
	String methodParams

	// The name of the queue/topic that the message should be sent on on if Async
	String asyncQueue = ''

	// Determines how long before an async action raises an alarm that the response hasn't been made
	// If set to zero (0) then no alarm will be raised
	Integer timeout=0

	// The interval (sec) that polling will check with an async service for results
	Integer pollingInterval = 0

	// The credentials that should be used with the method
	// ApiCredential agentCredential

	// The method that the async response should call to return response
	String callbackMethod = ''

	// Optional credentials required by the agent.
	Credential credential

	// Determines how async API calls notify the completion of an action invocation
	CallbackMode callbackMode = CallbackMode.NA

	Date dateCreated

	Date lastUpdated

	// The URL to the endpoint
	String endpointUrl

	// The Path to the endpoint
	String endpointPath

	// A flag that indicates if the action will poll for a result
	Integer isPolling = 0

	// The time period after after which a polling action is determined to have lapsed (seconds)
	Integer pollingLapsedAfter = 0

	// The time period after no increment in status of a polling action results in the LAPSED event get invoked (seconds)
	Integer pollingStalledAfter = 0

	// A flag that indicates that all of the syntax of the reactionScripts has been validated
	Integer reactionScriptsValid = 0

	// The JSON hash that will contain the scripts to be invoked appropriately.
	String reactionScripts

	// Flag indicating that the action interacts with a Task.
	Integer useWithAsset = 0

	// Flag indicating that the action interacts with an Asset.
	Integer useWithTask = 0


	static belongsTo = [
		project: Project,
		provider: Provider
	]

	static constraints = {
		agentMethod size: 1..64
		asyncQueue nullable: true, size: 0..64
		callbackMethod nullable: true
		callbackMode nullable: true
		credential nullable: true, validator: crossProviderValidator
		defaultDataScript nullable: true, validator: crossProviderValidator
		description nullable: true
		endpointPath nullable: true, blank: true, validator: ApiAction.&endpointPathValidator
		endpointUrl nullable: true, blank: true
		isPolling range: 0..1
		lastUpdated nullable: true
		methodParams nullable: true, validator: ApiAction.&methodParamsValidator
		name size: 1..64, unique: 'project'
		pollingLapsedAfter min: 0
		pollingStalledAfter min: 0
		producesData range:0..1
		provider validator: providerValidator
		reactionScripts size: 1..65535, blank: false, validator: reactionJsonValidator
		reactionScriptsValid range: 0..1
		timeout min:0
		useWithAsset range: 0..1
		useWithTask range: 0..1
	}

	static mapping = {
		columns {
			agentClass 		sqlType: 'varchar(64)'
			agentMethod 	sqlType: 'varchar(64)'
			callbackMode 	sqlType: 'varchar(64)'
			callbackMethod	sqlType: 'varchar(64)'
			asyncQueue 		sqlType: 'varchar(64)'
			name 			sqlType: 'varchar(64)'
			methodParams	sqlType: 'text'
		}
	}

	static transients = ['methodParamsList', 'agent']

	/*
	 * Used to determine if the action is performed asyncronously
	 * @return true if action is async otherwise false
	 */
	boolean isAsync() {
		callbackMode != CallbackMode.NA
	}

	/*
	 * Used to determine if the action is performed syncronously
	 * @return true if action is syncronous otherwise false
	 */
	boolean isSync() {
		! isAsync()
	}

	/**
	 * transforms the methodParams JSON Text to a list of objects of an Exception if the JSON is malformed
	 * @return The methodParams JSON as Groovy List<Map>
	 */
	List<Map> getMethodParamsListOrException() throws RuntimeException {
		List<Map> list = []
		if (methodParams) {
			list = JsonUtil.parseJsonList(methodParams)
		}
		return list
	}

	/*
	 * Used to access the methodParams as a List of Map objects instead of JSON text
	 * @return The methodParams JSON as Groovy List<Map>
	 */
	List<Map> getMethodParamsList(){
		List<Map> list = []
		try {
			list = getMethodParamsListOrException()
		} catch (e) {
			log.warn 'getMethodParamsList() methodParams impropertly formed JSON (value={}) : {}', methodParams, e.getMessage()
		}
		return list
	}

	/**
	 * return a list of ApiActionMethodParams from the methodParams JSON field
	 * @return
	 */
	List<ApiActionMethodParam> getListMethodParams() {
		List<ApiActionMethodParam> list = []
		if (methodParams) {
			List listJson = getMethodParamsListOrException()
			list = listJson.collect { new ApiActionMethodParam(it) }
		}
		return list
	}

	/**
	 * Custom validator for the reactionJson that evaluates that:
	 * - The string is a valid JSON.
	 * - EVALUATE and SUCCESS are present.
	 * - DEFAULT or ERROR are present.
	 */
	static reactionJsonValidator = { String reactionJsonString, ApiAction apiAction ->
		JSONObject reactionJson = null
		try {
			reactionJson = JsonUtil.parseJson(reactionJsonString)
		} catch(InvalidParamException e ) {
			return Message.InvalidFieldForDomain
		}

		// STATUS and SUCCESS are mandatory.
		if (reactionJson[ReactionScriptCode.STATUS.name()] && reactionJson[ReactionScriptCode.SUCCESS.name()]) {
			// Either DEFAULT or ERROR need to be specified.
			if (!reactionJson[ReactionScriptCode.DEFAULT.name()] && !reactionJson[ReactionScriptCode.ERROR.name()]) {
				apiAction.reactionScriptsValid = 0
				return Message.ApiActionMissingDefaultAndErrorInReactionJson
			}
		} else {
			return Message.ApiActionMissingStatusOrSuccessInReactionJson
		}

		boolean errors = false

		Set<String> invalidKeys = []
		// Iterate over all the keys warning and removing anything not defined in ReactionScriptCode. See TM-8697
		for (key in reactionJson.keySet()) {
			if (!ReactionScriptCode.lookup(key)) {
				log.warn("Unrecognized key $key in reaction JSON.")
				invalidKeys << key
				errors = true
			}
		}

		// If errors were detected update the reactionJson.
		if (errors) {
			reactionJson.keySet().removeAll(invalidKeys)
			apiAction.reactionScripts = JsonUtil.toJson(reactionJson)
		}

		apiAction.reactionScriptsValid = errors ? 0 : 1

		// Set to true, otherwise the validation fails.
		return true
	}

	/**
	 * Used to validate that the Provider is of the same Project as that of the ApiAction
	 * @param value - the value to be set on a property
	 * @param domainObject - the ApiAction domain object being created/updated
	 */
	static providerValidator = { Provider providerObject, ApiAction domainObject ->
		Long providerProjectId=0
		if (providerObject.project) {
			providerProjectId = providerObject.project.id
		} else {
			// Need to use a new session to fetch the Provider so as not to mess up the current
			// objects in the session.
			Provider.withNewSession {
				Provider p = Provider.read(providerObject.id)
				if (p) {
					providerProjectId = p.project.id
				}
			}
		}

		if ( providerProjectId != domainObject.project.id) {
			return Message.InvalidFieldForDomain
		}
	}

	/**
	 * Used to validate the field value is of the same Provider as the ApiAction
	 * @param value - the value to be set on a property
	 * @param domainObject - the ApiAction domain object being created/updated
	 */
	static crossProviderValidator = { value, ApiAction domainObject ->
		if (! value) {
			return true
		}

		Long valueProviderId = 0

		if (value.provider) {
			valueProviderId = value.provider.id
		} else {
			// Need to use a new session to fetch the Domain so as not to mess up the current
			// objects in the session.
			value.class.withNewSession {
				def obj = value.class.read(value.id)
				if (obj) {
					valueProviderId = obj.provider.id
				}
			}
		}

		if ( valueProviderId !=  domainObject.provider.id) {
			return Message.InvalidFieldForDomain
		}
	}

	/**
	 * Used to validate that the field methodParams is a well-formed JSON
	 * @param value
	 * @param object
	 * @return
	 */
	static methodParamsValidator (value, ApiAction apiAction) {
		try {
			// delegate the validation to the List builder method
			apiAction.methodParamsListOrException
			return true
		} catch (e) {
			return Message.InvalidJsonFormat
		}
	}

	/**
	 * Validates that id the endpoint path has parameters they exist in the parameter list
	 * @param value
	 * @param apiAction
	 */
	static endpointPathValidator (value, ApiAction apiAction) {

		if (! value) {
			return true
		}

		Matcher m = value =~ /\{\{([^\}]*)\}\}/

		List<String> params = []
		while(m.find()) {
			params << m.group(1)
		}

		HashSet<String> methodParamList = apiAction.listMethodParams.collect {
			it.param
		}

		List paramsNotFound = params.findAll {
			! methodParamList.contains(it)
		}

		if (paramsNotFound) {
			return [Message.ParamReferenceInURINotFound, paramsNotFound.join(', ')]
		}

		return true
	}
}