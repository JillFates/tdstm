package net.transitionmanager.domain

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import groovy.json.JsonSlurper
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */
@Slf4j(value='logger')
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
	 * [ {	'param':'assetId',
	 *			'desc': 'The unique id to reference the asset',
	 *			'type':'string',
	 *			'context': ContextType.ASSET.toString(),	// The context that the param value will be pulled from
	 * 			'property': 'id', 	// The property on the context that the value will be pulled from
	 *			'value': 'user def value'	// The value to use context is ContextType.USER_DEF
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

	Date lastModified

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
		agentClass  nullable: false
		agentMethod nullable: false, size: 1..64
		asyncQueue nullable: true, size: 0..64
		callbackMethod nullable: true
		callbackMode nullable: true
		credential nullable: true, validator: crossProviderValidator
		defaultDataScript nullable: true, validator: crossProviderValidator
		endpointPath nullable: true, blank: true
		endpointUrl nullable: true, blank: true
		isPolling nullable: false, range: 0..1
		lastModified nullable: true
		methodParams nullable: true
		name nullable: false, size: 1..64, unique: 'project'
		pollingLapsedAfter nullable: false
		pollingStalledAfter nullable: false
		producesData nullable: false, range:0..1
		provider nullable: false, validator: providerValidator
		reactionScripts size: 1..65535, blank: false, validator: reactionJsonValidator
		reactionScriptsValid nullable: false, range: 0..1
		timeout nullable: true
		useWithAsset nullable: false, range: 0..1
		useWithTask nullable: false, range: 0..1
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
		callbackMode == CallbackMode.NA
	}

	/*
	 * Used to access the methodParams as a List of Map objects instead of JSON text
	 * @return The methodParams JSON as Groovy List<Map>
	 */
	List<Map> getMethodParamsList(){
		JsonSlurper slurper = new groovy.json.JsonSlurper()
		List<Map> list = []
		if (methodParams) {
			try {
				list = slurper.parseText(methodParams)
			} catch (e) {
				log.warn 'getMethodParamsList() methodParams impropertly formed JSON (value={}) : {}', methodParams, e.getMessage()
			}
		}
		return list
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

	/**
	 * Custom validator for the reactionJson that evaluates that:
	 * - The string is a valid JSON.
	 * - EVALUATE and SUCCESS are present.
	 * - DEFAULT or ERROR are present.
	 */
	static reactionJsonValidator = { String reactionJsonString, ApiAction apiAction ->
		try {
			JSONObject reactionJson = JsonUtil.parseJson(reactionJsonString)
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
			// Iterate over all the keys warning and removing anything not defined in ReactionScriptCode.
			for (key in reactionJson.keySet()) {
				try {
					ReactionScriptCode.valueOf(key)
				} catch (IllegalArgumentException iae) {
					logger.warn("Unrecognized key $key in reaction JSON.")
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

		} catch (InvalidParamException ipe) {
			return Message.InvalidFieldForDomain
		}
	}

	/**
	 * A validator that takes an API Action and a field and checks
	 * that both reference the same project.
	 */
	static providerValidator = { provider, apiAction ->
		if (provider.project.id != apiAction.project.id) {
			return Message.InvalidFieldForDomain
		}
	}

	/**
	 * Validator that accepts a field of an ApiAction and the corresponding
	 * ApiAction and checks that the providers are the same.
	 */
	static crossProviderValidator = { aField, apiAction ->
		if (aField) {
			if (aField.provider.id != apiAction.provider.id) {
				return Message.InvalidFieldForDomain
			}
		}
	}

}