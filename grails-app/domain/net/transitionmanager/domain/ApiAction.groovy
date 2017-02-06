package net.transitionmanager.domain

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

	String description

	// Indicates the class to use when invoking the action
	AgentClass agentClass

	// The method on the agentClass to invoke
	String agentMethod

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

	static belongsTo = [project: Project]

	static constraints = {
		agentClass  nullable: false
		agentMethod nullable: false, size: 1..64
		callbackMode nullable: false
		asyncQueue nullable: false, size: 0..64
		name nullable: false, size: 1..64
		methodParams nullable: true
		lastModified nullable: true
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

	static transients = ['methodParamsJson']

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
				// println "JsonSlurper failed : ${e.getMessage()}"
				log.error "getMethodParamsList() methodParams was not propertly formed JSON (value=$methodParams) : ${e.getMessage()}"
			}
		}
		return list
	}

	JSONElement getMethodParamsJson(){
		JSON.parse(methodParams)
	}
}