package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil
import groovy.json.JsonSlurper
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import groovy.util.logging.Slf4j
import groovy.transform.ToString

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */
@Slf4j(value='logger')
@ToString(excludes='methodParams', includeNames=true, includeFields=true)
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

	static transients = ['methodParamsList']

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
	 * Create a map with the data for this ApiAction
	 * @param minimalInfo - flag that signals if only the m
	 * @return
	 */
	Map toMap(boolean minimalInfo = true) {
		Map basicFields = [id: id, name: name]
		Map extendedFields = [:]
		if (!minimalInfo) {
			extendedFields = [
					agentClass: agentClass.name(),
					agentMethod: agentMethod,
					asyncQueue: asyncQueue,
					callbackMethod: callbackMethod,
					callbackMode: callbackMode.name(),
					dateCreated: dateCreated,
					defaultDataScriptName: defaultDataScript,
					description: description,
					lastModified: lastModified,
					methodParams: methodParams,
					pollingInterval: pollingInterval,
					producesData: producesData,
					provider: [
					        id: provider.id,
							name: provider.name
					],

					timeout: timeout,
			]
		}
		return basicFields + extendedFields
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}