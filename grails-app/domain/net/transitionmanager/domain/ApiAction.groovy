package net.transitionmanager.domain

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import groovy.json.JsonSlurper
import net.transitionmanager.agent.AbstractAgent
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

	// The JSON hash that will contain the scripts to be invoked appropriately.
	String reactionJson

	// Flag indicating that the action interacts with a Task.
	Integer useWithAsset

	// Flag indicating that the action interacts with an Asset.
	Integer useWithTask

	// The frequency that a polling action is called (seconds)
	Integer pollingFrequency

	// The time period after after which a polling action is determined to have lapsed (seconds)
	Integer pollingLapsedAfter

	// The time period after no increment in status of a polling action results in the LAPSED event get invoked (seconds)
	Integer pollingStalledAfter

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
		credential nullable: true
		defaultDataScript nullable: true
		endpointPath nullable: true, blank: true
		endpointUrl nullable: true, blank: true
		name nullable: false, size: 1..64
		methodParams nullable: true
		lastModified nullable: true
		pollingFrequency nullable: false, range: 0..1
		pollingLapsedAfter nullable: false, range: 0..1
		pollingStalledAfter nullable: false, range: 0..1
		producesData nullable: false, range:0..1
		reactionJson size: 1..65535, blank: false, validator: reactionJsonValidator
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
	static reactionJsonValidator = { String reactionJsonString ->
		try {
			JSONObject reactionJson = JsonUtil.parseJson(reactionJsonString)
			// EVALUATE and SUCCESS are mandatory.
			if (reactionJson[ReactionScriptCode.EVALUATE.name()] && reactionJson[ReactionScriptCode.SUCCESS.name()]) {
				// Either DEFAULT or ERROR need to be specified.
				if (!reactionJson[ReactionScriptCode.DEFAULT.name()] && !reactionJson[ReactionScriptCode.ERROR.name()]) {
					return Message.ApiActionMissingDefaultAndErrorInReactionJson
				}
			} else {
				return Message.ApiActionMissingEvaluateOrSuccessInReactionJson
			}
		} catch (InvalidParamException e) {
			return Message.ApiActionInvalidReactionJson
		}
	}
}