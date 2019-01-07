package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.StringUtil
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ApiActionMethodParam
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.service.InvalidParamException
import org.grails.web.json.JSONObject

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */
@Slf4j
@ToString(includes='name, connectorMethod, provider', includeNames=true, includePackage=false)
class ApiAction {

	String name
	String description

	// Indicates the catalog to use when invoking the action
	ApiCatalog apiCatalog

	// The method on the connector to invoke
	String connectorMethod

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
	// ApiCredential connectorCredential

	// The method that the async response should call to return response
	String callbackMethod = ''

	// Optional credentials required by the connector.
	Credential credential

	// Determines how async API calls notify the completion of an action invocation
	CallbackMode callbackMode = CallbackMode.NA

	// Determines which HTTP method to use when visiting the action endpoint
	ApiActionHttpMethod httpMethod = ApiActionHttpMethod.GET

	// The fully qualified URL to the endpoint
	String endpointUrl

	// The URL of the documentation to the endpoint if available (this can be formatted title|url or just url)
	String docUrl=''

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

	Date dateCreated

	Date lastUpdated

	static belongsTo = [
		project: Project,
		provider: Provider,
		apiCatalog: ApiCatalog
	]

	static constraints = {
		connectorMethod size: 1..64
		asyncQueue nullable: true, size: 0..64
		callbackMethod nullable: true
		callbackMode nullable: true
		credential nullable: true, validator: crossProviderValidator()
		defaultDataScript nullable: true, validator: crossProviderValidator()
		description nullable: true
		endpointUrl nullable: true, blank: true, validator: ApiAction.&endpointUrlValidator
		docUrl nullable: true, blank: true, validator: ApiAction.&docUrlValidator
		isPolling range: 0..1
		lastUpdated nullable: true
		methodParams nullable: true, validator: ApiAction.&methodParamsValidator
		name size: 1..64, unique: 'project'
		pollingLapsedAfter min: 0
		pollingStalledAfter min: 0
		producesData range:0..1
		provider validator: providerValidator()
		reactionScripts size: 1..65535, blank: false, validator: reactionJsonValidator()
		reactionScriptsValid range: 0..1
		timeout min:0
		useWithAsset range: 0..1
		useWithTask range: 0..1
	}

	static mapping = {
		columns {
			connectorMethod 	sqlType: 'varchar(64)'
			callbackMode 	sqlType: 'varchar(64)'
			callbackMethod	sqlType: 'varchar(64)'
			asyncQueue 		sqlType: 'varchar(64)'
			name 			sqlType: 'varchar(64)'
			methodParams	sqlType: 'text'
			endpointUrl		sqlType: 'varchar(255)'
			docUrl			sqlType: 'varchar(255)'
			httpMethod		sqlType: 'varchar(10)'
		}
	}

	static transients = ['methodParamsList', 'connector']

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
// TODO : JPM 3/2018 : DO NOT trap exceptions like this and pretend that it didn't happen. Users have no clue what went wrong
			log.warn 'getMethodParamsList() methodParams impropertly formed JSON (value={}) : {}', methodParams, e.getMessage()
		}
		return list
	}

//
// TODO : JPM 3/2018 : The getListMethodParams method name is really confusing with getMethodParamsList. Naming needs to be sorted out to be more intuitive.
//		getMethodParamsAsListOfMap and getMethodParamsAsListOfObject ??

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
	 * Used to transform the Endpoint URL on the domain where the {{placeholders}} are replaced
	 * with the values that in the provided map. The values in the map are expected to be unescaped
	 * and may contain non ASCII characters. The values will be encoded as part of the process.
	 *
	 * @param params Map containing the placeholders to replace
	 * @return
	 * @Deprecated - Use new ActionHttpRequestElements class to get URL
	 */
	@Deprecated
	String endpointUrlWithPlaceholdersSubstituted(Map params) {
		return StringUtil.replacePlaceholders(endpointUrl, params)
	}

	/**
	 * Custom validator for the reactionJson that evaluates that:
	 * - The string is a valid JSON.
	 * - EVALUATE and SUCCESS are present.
	 * - DEFAULT or ERROR are present.
	 */
	static Closure reactionJsonValidator() {
		return { String reactionJsonString, ApiAction apiAction ->
			JSONObject reactionJson = null

			try {
				reactionJson = JsonUtil.parseJson(reactionJsonString)
			} catch (InvalidParamException e) {
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
	}

	/**
	 * Used to validate that the Provider is of the same Project as that of the ApiAction
	 * @param value - the value to be set on a property
	 * @param domainObject - the ApiAction domain object being created/updated
	 */
	static Closure providerValidator() {
		return { Provider providerObject, ApiAction domainObject ->
			Long providerProjectId = 0

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

			if (providerProjectId != domainObject.project.id) {
				return Message.InvalidFieldForDomain
			}
		}
	}

	/**
	 * Used to validate the field value is of the same Provider as the ApiAction
	 * @param value - the value to be set on a property
	 * @param domainObject - the ApiAction domain object being created/updated
	 */
	static Closure crossProviderValidator() {
		return { value, ApiAction domainObject ->
			if (!value) {
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

			if (valueProviderId != domainObject.provider.id) {
				return Message.InvalidFieldForDomain
			}
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
	 * Used to validate the docUrl if it has a value that it is either a URL or a Markup URL (title|URL)
	 */
	static docUrlValidator(value, ApiAction apiAction) {
		if (StringUtil.isNotBlank(value)) {
			if (! (HtmlUtil.isURL(value) || HtmlUtil.isMarkupURL(value) ) ) {
				return Message.InvalidURLFormat
			}
		}
		return true
	}

	/**
	 * Validate that all placeholders in the endpoint path exist in the methodParams list
	 * @param value
	 * @param apiAction
	 */
	static endpointUrlValidator (String value, ApiAction apiAction) {

		Set<String> placeholders = StringUtil.extractPlaceholders(value)
		Set<String> methodParamNames = apiAction.getMethodParamsListOrException().collect { it.paramName }
		Set<String> missingPlaceholders = placeholders - methodParamNames

		if (missingPlaceholders) {
			return [Message.ParamReferenceInURLNotFound, missingPlaceholders.join(', ')]
		}

		return true
	}
}