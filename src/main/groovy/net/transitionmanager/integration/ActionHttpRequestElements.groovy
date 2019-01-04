package net.transitionmanager.integration

import org.springframework.http.HttpMethod
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.UrlUtil
import net.transitionmanager.service.InvalidParamException


/**
 * The intent of this class is to transform an endpoint URL and an associated map of name/value parameters that
 * represent {{placeholder}} values, query string parameters and/or body JSON parameters.
 *
 * The URI will be separated into three (3) elements:
 *    1. baseUrl (e.g. https://example.com )
 *    2. urlPath (e.g. /api/module/action/ID )
 *    3. queryString (e.g. ?arg1=a&arg2=b )
 *
 * The constructor will perform the following steps to transform the endpoint URI and parameters appropriately so that individual
 * elements can be accessed correctly with the URIBuilder and other logic:
 *
 *		1) Perform a {{placeholder}} replacement on the the endpoint URL
 *			Note that the placeholder values will be URL Encoded
 * 		2) Split the the URI into the baseUrl, urlPath and the queryString
 * 		3) Any query string parameters will be converted to a name value map
 *			Note that the values will be URL decoded
 * 		4)
 * baseUrl + urlPath ? queryParams and an
 */
class ActionHttpRequestElements {

	// These parameters are injected into the Params but should be moved to something else
	// TODO :JPM 3/2018 : TM-9963 Move params else where
	static final List<String>ParamsToIgnored = ['actionId', 'producesData', 'credentials']

	// Represents the first section of the URI up to the path
	protected String baseUrl = ''

	// Represents the 2nd section of the URI that is the directory path up to the querystring (?)
	protected String urlPath = ''

	// Represents the final section of the URI that contains the unaltered query string of the URI
	protected String queryString = ''

	// queryParams will contain the name/value pairs of any query string parameter explicitly
	// defined in the URI
	protected Map queryParams = [:]

	// Will contain any of the param values passed in the constructor that were not consumed
	// by the URI {{placeholders}}. These name/values will either appear as additional query
	// string parameters or as the JSON body payload.
	protected Map extraParams = [:]

	/**
	 * Constructs an instance that parses the uri into the individual elements and addresses
	 * encoding appropriately.
	 *
	 * @param uri - the complete URI of an endpoint
	 * @param extraParams - a map containing placeholder, querystring and JSON body parameter name/values
	 */
	ActionHttpRequestElements(String uri, ActionRequest actionRequest) {
		parse(uri, actionRequest)
	}

	ActionHttpRequestElements(ActionRequest actionRequest) {
		parse(actionRequest.options.apiAction.endpointUrl, actionRequest)
	}

	/**
	 * Used to retrieve the parameters as a URI query string based on the HTTP Method. For the GET method
	 * all parameters (excluding those that were used as placeholders in the baseUrl & urlPath) will be
	 * concatenated as: name=value&name2=value2 where the name and values are URL encoded. For all other
	 * modes the querystring will ONLY contain those parameters that were explicitely defined in the
	 * original URI passed to the constructor.
	 *
	 * @param method - the indentended Http Method that this querystring will be used with
	 * @return the appropriate parameters and values URL encoded
	 */
	// TODO : JPM 3/2018 : queryStringMap is returning String instead of Map - Need to solve
	String queryStringMap(HttpMethod method) {
		return UrlUtil.queryStringToMap(queryString, method)
	}

	/**
	 * Used to retrieve all of the query string parameters as a map. The values should for all intended
	 * purposes not be encoded.
	 * @return the map of all query string params
	 */
	// TODO : JPM 3/2018 : queryStringMap is returning String instead of Map - Need to solve
	String queryStringMap() {
		return queryStringMap(HttpMethod.GET)
	}

	/**
	 * Used to retrieve all of the query string parameters as a map. The values should for all intended
	 * @param method - the HTTP method
	 * @return a string containing all query string parameters
	 */
	String queryString(HttpMethod method) {
		if (HttpMethod.GET == method) {
			String queryStringParams = buildQueryStringParams()
			if (queryStringParams) {
				return [queryString, queryStringParams].join('&')
			} else {
				return queryString
			}
		} else {
			return queryString
		}
	}

	/**
	 * Used to build the extra query string parameters into a url query string where the
	 * values are encoded.
	 */
	protected String buildQueryStringParams() {
		String qs = ''
		// Integer x=0
		qs = extraParams.collect { k, v ->
			// These parameters are injected into the Params but should be moved to something else
			// TODO :JPM 3/2018 : TM-9963
			if (! paramsToIgnored.contains(k)) {
				k + '=' + v.toString()
			}
		}.join('&')
		return qs
	}

	/**
	 * Used to retrieve the complete URI based on the intended HTTP Method that the URI will be used for.
	 * The URI will consist of the baseUrl + urlPath and the query string appropriately.
	 *
	 * @See queryString() for details on how the query string is formed
	 * @param method - the indentended Http Method that this querystring will be used with
	 * @return the appropriate parameters and values URL encoded
	 */
	String uri(HttpMethod method) {
		return baseUrl + getUrlPathWithQueryString(method)
	}

	/**
	 * Used to retrieve the complete URI intended for a GET request therefore all of the query params will
	 * be included in the URI encoded appropriately.
	 * @return the complete URL with the query string parameters
	 */
	String uri(){
		uri(HttpMethod.GET)
	}

	// Accessor methods to the properties that should be readonly visable
	String getBaseUrl() { return baseUrl }
	String getUrlPath() { return urlPath }
	String getQueryString() { return queryString }
	Map getExtraParams() { return extraParams }
	Map getQueryParams() { return queryParams }

	/**
	 * Used to get the path and potentially part of the query string if the ApiAction endpoint uri had
	 * any explicit parameters specified.
	 * @param method - if defined with GET then all parameters will be included otherwise only query string params explicitely
	 * @return the path and possibly the query string
	 */
	String getUrlPathWithQueryString(HttpMethod method) {
		String qs = queryString(method)
		return urlPath + (qs ? '?' + qs : '')
	}

	/**
	 * Used to retrieve the query string as a set of name value parameters as a Map and optionally
	 * decoding the values.
	 * @param decodeValue - a flag if the value should be decoded in the results (default true)
	 * @return the map of the name/value pairs
	 */
	Map<String, String> queryStringAsMap(Boolean decodeValues=true) {
		return UrlUtil.queryStringToMap(queryString, decodeValues)
	}

	/**
	 * This is called by the constructor to rip apart the URI and split the parameters into the
	 * appropriate map containers.
	 *
	 * @param uri - the complete URI of an endpoint
	 * @param paramValues - a map containing placeholder, querystring and JSON body parameter name/values
	 */
	private void parse(final String uri, final ActionRequest actionRequest) {
		if (! uri) {
			throw new InvalidParamException('Action endpoint URL appears to be undefined')
		}

		baseUrl = uri

		// Need to first determine baseUrl and urlPath placeholders from query string placeholders
		List urlParts = baseUrl.split(/\?/)
		if (urlParts.size() > 2) {
			throw new InvalidParamException('Action endpoint URL has extraneous \'?\' character')
		}
		// Get the unique list of the placeholders
		// TODO : SL 03/2018 : Move methods from StringUtil to UrlUtil since this is URL stuff related
		Set<String> placeholderNames = StringUtil.extractPlaceholders(urlParts[0])
		Set<String> queryPlaceholderNames = (urlParts.size() > 1 ? StringUtil.extractPlaceholders(urlParts[1]) : [])
		placeholderNames += queryPlaceholderNames

		// Replace all placeholders within the URI with the encoded named values
		if (placeholderNames) {
			Map uriParamValues = [:]
			// Grab the values for the placeholders and encode them
			for (String name in placeholderNames) {
				if (actionRequest.params.hasProperty(name)) {
					uriParamValues.put(name, actionRequest.params.getProperty(name) )
				}
			}

			// Now do a replacement of the placeholders
			baseUrl = StringUtil.replacePlaceholders(baseUrl, uriParamValues)
		}

		// Stuff any query string parameters into queryParams map decoded
		if (queryPlaceholderNames) {
			urlParts = baseUrl.split(/\?/)
			Map qsMap = UrlUtil.queryStringToMap(urlParts[1])
			for (pv in qsMap) {
				queryParams.put(pv.key,  pv.value )
			}
		}

		// Split the URI into 3 parts and update the baseUrl and urlPath
		URL url = new URL(baseUrl)
		queryString = url.getQuery()
		urlPath = url.getPath()
		Integer numCharsNotBase = urlPath.size() + (queryString ? 1 + queryString.size() : 0)
		baseUrl = baseUrl.substring(0, baseUrl.size() - numCharsNotBase )

		// Now load extraParams with the parameters that were not used as placeholders
		if (placeholderNames) {
			this.extraParams = actionRequest.params.getAllProperties().findAll {k, v ->
				! placeholderNames.contains(k) }
		} else {
			this.extraParams = actionRequest.params.getAllProperties().clone()
		}
	}
}
