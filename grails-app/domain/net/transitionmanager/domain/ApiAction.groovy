package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil
import groovy.json.JsonSlurper
import net.transitionmanager.agent.AbstractAgent
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import net.transitionmanager.service.ApiActionService

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */
@Slf4j(value='logger')
@ToString(includes='name, agentClass, agentMethod, provider', includeNames=true, includePackage=false)
class ApiAction {
	// Transient reference to retrieve the agent for this action.
	ApiActionService apiActionService

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

	static belongsTo = [
		project: Project,
		provider: Provider
	]

	static constraints = {
		agentClass  nullable: false
		agentMethod nullable: false, size: 1..64
		asyncQueue nullable: false, size: 0..64
		callbackMode nullable: false
		credential nullable: true
		defaultDataScript nullable: true
		name nullable: false, size: 1..64
		methodParams nullable: true
		lastModified nullable: true
		producesData nullable: false, range:0..1
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

	static transients = ['methodParamsList', 'apiActionService', 'agent']

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

	/**
	 * Return the AbstractAgent instance for this API Action.
	 * @return
	 */
	AbstractAgent getAgent() {
		return apiActionService.agentInstanceForAction(this)
	}

	/**
	 * Create a map with the data for this ApiAction
	 * @param minimalInfo - flag that signals if only the m
	 * @return
	 */
	Map toMap(boolean minimalInfo = true) {
		Map fields = [id: id, name: name]
		AbstractAgent agent = getAgent()
		if (!minimalInfo) {
			Map credentialMap = null
			if (credential) {
				credentialMap = [id: credential.id, name: credential.name]
			}

			fields.agentClass  = [
			        id: agent.agentClass.name(),
					name: agent.name
			]
			fields.agentMethod = agentMethod
			fields.asyncQueue = asyncQueue
			fields.callbackMethod = callbackMethod
			fields.callbackMode = callbackMode.name()
			fields.credential = credentialMap
			fields.dateCreated = dateCreated
			fields.defaultDataScriptName = defaultDataScript
			fields.description = description
			fields.lastModified = lastModified
			fields.methodParams = methodParams
			fields.pollingInterval = pollingInterval
			fields.producesData = producesData
			fields.provider = [
					id  : provider.id,
					name: provider.name
			]
			fields.timeout = timeout

		}
		return fields
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}