package com.tdssrc.grails

import groovy.util.logging.Slf4j
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject

import java.util.regex.Pattern

/**
 * Api Catalog transformation util class
 *
 * Having a parameter definition like:
 *
 * "paramDef": {
 *     "HOSTNAME_PARAM": {
 *         "paramName": "HOSTNAME",
 *         "description": "The ServiceNow Hostname of the instance to interact with",
 *         "type": "String",
 *         "context": "USER_DEF",
 *         "fieldName": null,
 *         "value": "Enter your FQDN to ServiceNow",
 *         "required": 1,
 *         "readonly": 0,
 *         "encoded": 0
 *       },
 *       "TABLE_PARAM": {
 *         "paramName": "TABLE",
 *         "description": "The table name from the ServiceNow Table API",
 *         "type": "String",
 *         "context": "USER_DEF",
 *         "fieldName": "$fieldName$",
 *         "value": "Enter your FQDN to ServiceNow",
 *         "required": 1,
 *         "readonly": "$readonly$",
 *         "encoded": 0
 * }
 *
 * This utility class is able to transform JSON templates like
 *
 * "paramGroup": {
 * 		"FOO_GRP": [
 *         "$paramDef.HOSTNAME_PARAM$",
 *         "$fn(paramDef.TABLE_PARAM, cmdb_ci_appl, 1)$"
 *       ]
 *  }
 *
 * Into
 *
 * "paramGroup": {
 *      "FOO_GRP": [
 *			{
 *                 "paramName": "HOSTNAME",
 *                 "description": "The ServiceNow Hostname of the instance to interact with",
 *                 "type": "String",
 *                 "context": "USER_DEF",
 *                 "fieldName": null,
 *                 "value": "Enter your FQDN to ServiceNow",
 *                 "required": 1,
 *                 "readonly": 0,
 *                 "encoded 0
 *             },
 *             {
 *                 "paramName": "TABLE",
 *                 "description": "The table name from the ServiceNow Table API",
 *                 "type": "String",
 *                 "context": "USER_DEF",
 *                 "fieldName": " cmdb_ci_appl",
 *                 "value": " cmdb_ci_appl",
 *                 "required": 1,
 *                 "readonly": " ,
 *            "encoded": 0
 *             }
 *         ]
 * }
 *
 */
@Slf4j
class ApiCatalogUtil {

	private static final String DICTIONARY_ROOT_ELEMENT = 'dictionary'
	private static final String[] DICTIONARY_PRIMARY_KEYS = ['info', 'variable', 'credential', 'paramDef', 'paramGroup', 'method']
	private static final String DOT = '.'
	private static final Pattern BETWEEN_PARENTHESIS = ~/\((.*?)\)/

	/**
	 * Parse a dictionary object
	 *
	 * @param jsonDictionary - the json dictionary
	 * @return a parsed and transformed api catalog dictionary
	 */
	private static def transformJsonDictionary(JSONObject jsonDictionary) {
		if (!jsonDictionary.dictionary instanceof Map) {
			 throw new Exception('Dictionary must be a Map structure.')
		}

		// transform json dictionary in using the desired order of primary keys
		DICTIONARY_PRIMARY_KEYS.each {key ->
			def element = jsonDictionary.dictionary.get(key)
			if (element instanceof Map) {
				element = transformMap(element, jsonDictionary)
			} else if (element instanceof List) {
				element = transformArray(element, jsonDictionary)
			} else {
				// do element placeholder replacement if needed
				if (element ==~ /^\$.*\$$/) {
					element = getParam(element, jsonDictionary)
				}
			}
			jsonDictionary.dictionary.put(key, element)
		}

		return jsonDictionary
	}

	/**
	 * Iterate over a map elements finding values matching a regular expression
	 * like <code>$context.tag$</code> to be transformed into the real value
	 *
	 * @param map - a map
	 * @param jsonDictionary - the json dictionary
	 * @return a parsed and transformed map elements
	 */
	private static def transformMap(Object map, JSONObject jsonDictionary) {
		map.each {
			if (it.value instanceof Map) {
				it.value = transformMap(it.value, jsonDictionary)
			} else if (it.value instanceof List) {
				it.value = transformArray(it.value, jsonDictionary)
			} else {
				// do element placeholder replacement if needed
				if (it.value ==~ /^\$.*\$$/) {
					it.value = getParam(it.value, jsonDictionary)
				}
			}
		}
		return map
	}

	/**
	 * Iterate over an array elements finding values matching a regular expression
	 * like <code>$context.tag$</code> to be transformed into the real value
	 *
	 * @param array - an array
	 * @param jsonDictionary - the json dictionary
	 * @return a parsed and transformed array elements
	 */
	private static def transformArray(Object array, JSONObject jsonDictionary) {
		array.eachWithIndex { e, i ->
			if (e instanceof Map) {
				return transformMap(e, jsonDictionary)
			} else if (e instanceof List) {
				return transformArray(e, jsonDictionary)
			} else {
				// do element placeholder replacement if needed
				if (e ==~ /^\$.*\$$/) {
					array[i] = getParam(e, jsonDictionary)
				}
			}
		}
		return array.flatten()
	}

	/**
	 * Get a param value, it could be a <code>$fn(paramDef.TABLE_PARAM, cmdb_ci_appl, 1)$</code>
	 * or a <code>$context.tag$</code> to be transformed into the real value
	 *
	 * @param param - a param name like using <code>$...$</code> or <code>$fn(...)$</code> notation
	 * @param jsonDictionary - the json dictionary
	 * @return it can return a simple string or an object, depends on the dictionary definition and the param referenced value
	 */
	private static def getParam(String param, JSONObject jsonDictionary) {
		if (param ==~ /^\$?fn\(.*?\)\$$/) {
			return getFnParam(param, jsonDictionary)
		} else {
			def paramName = getParamName(param)
			if (paramName) {
				// prefix param name with root dictionary element "dictionary"
				paramName = DICTIONARY_ROOT_ELEMENT + DOT + paramName
				def paramValue = jsonDictionary
				paramName.split('\\.').each { p ->
					paramValue = paramValue[p]
				}
				return paramValue
			} else {
				return null
			}
		}
	}

	/**
	 * It returns an unescaped parameter
	 *
	 * e.g.
	 * $paramDef.DOCUMENTATION_URL$ -> paramDef.DOCUMENTATION_URL
	 *
	 * @param param - a param name like using <code>$...$</code> or <code>$fn(...)$</code> notation
	 * @return the string contained withing "$" signs
	 */
	private static String getParamName(String param) {
		return param.replaceAll('\\$', '')
	}

	/**
	 * It returns an unescaped function parameter
	 *
	 * e.g.
	 * $fn(paramDef.TABLE_PARAM, cmdb_ci_appl, 1)$
	 *
	 * Replaces TABLE_PARAM predefined placeholders ($fieldName$, $readonly$') into values passed in by $fn(param, fieldName, readOnly)
	 *
	 * @param param - a param name like <code>$fn(...)$</code> notation
	 * @param jsonDictionary - the json dictionary
	 * @return returns a map with transformed param definitions placeholders
	 */
	private static def getFnParam(String param, JSONObject jsonDictionary) {
		def paramMatcher = param =~ BETWEEN_PARENTHESIS
		def paramMatch = paramMatcher[0][1]
		String[] params = paramMatch.split(',')

		def fnParamRef = getParam(params[0], jsonDictionary)
		if (!fnParamRef instanceof Map) {
			throw new InvalidParamException('fn() param transformation is expecting a Map reference: ' + params[0])
		}

		def fnParam = fnParamRef.clone()
		fnParam['fieldName'] = params[1].trim()
		fnParam['value'] = params[1].trim()
		fnParam['readonly'] = NumberUtil.toInteger(params[2].trim(), 0)
		return fnParam
	}

	/**
	 * Verify if given JSONObject contains expected key
	 * @param jsonObject a JSONObject
	 * @param key a expected key
	 * @throws InvalidParamException
	 */
	private static void containsKey(JSONObject jsonObject, String key) {
		if (!jsonObject.containsKey(key)) {
			throw new InvalidParamException('Dictionary is missing key: ' + key)
		}
	}

	/**
	 * Transform a api catalog dictionary placeholders to the corresponding values as params definition indicates.
	 *
	 * @param dictionary - a json string containing the dictionary definition
	 * @return a json string with json dictionary transformed
	 */
	static String transformDictionary(String dictionary) {
		try {
			JSONObject jsonDictionaryParsed = JsonUtil.parseJson(dictionary)
			validateDictionaryHasPrimaryKeys(jsonDictionaryParsed)

			JSONObject jsonDictionaryTransformed = transformJsonDictionary(jsonDictionaryParsed)
			return JsonUtil.toPrettyJson(jsonDictionaryTransformed)
		} catch (Exception e) {
			String error = String.format('Error transforming ApiCatalog dictionary. %s', e.message)
			log.info(error)
			throw new InvalidParamException(error)
		}
	}

	/**
	 * Transform a api catalog dictionary placeholders to the corresponding values as params definition indicates.
	 *
	 * @param command - api catalog command with json string containing the dictionary definition
	 * @return a json string with json dictionary transformed
	 */
	static String transformDictionary(ApiCatalogCommand command) {
		return transformDictionary(command.dictionary)
	}

	/**
	 * Validate that json dictionary contains expected primary keys
	 * @param dictionary an api catalog json dictionary as String
	 * @throws InvalidParamException
	 */
	static void validateDictionaryHasPrimaryKeys(String dictionary) {
		JSONObject jsonDictionary = JsonUtil.parseJson(dictionary)

		validateDictionaryHasPrimaryKeys(jsonDictionary)
	}

	/**
	 * Validate that json dictionary contains expected primary keys
	 * @param jsonDictionary an api catalog json dictionary as JSONObject
	 * @throws InvalidParamException
	 */
	static void validateDictionaryHasPrimaryKeys(JSONObject jsonDictionary) {
		// validate root element
		containsKey(jsonDictionary, DICTIONARY_ROOT_ELEMENT)

		// validate primary keys
		jsonDictionary = jsonDictionary.get(DICTIONARY_ROOT_ELEMENT)
		DICTIONARY_PRIMARY_KEYS.each { key ->
			containsKey(jsonDictionary, key)
		}
	}

	/**
	 * Get a list of catalog dictionary methods expected by the ApiAction CRUD
	 * @param dictionary a catalog json dictionary
	 * @return a Map containing a dictionary methods where the key is the "apiMethod" and the value is the
	 * method definition details
	 * @throws InvalidParamException
	 */
	static Map<String, ?> getCatalogMethods(String dictionary) {
		try {
			JSONObject jsonDictionaryTransformed = JsonUtil.parseJson(dictionary)

			Map methods = [:]
			jsonDictionaryTransformed.dictionary.method.each { entry ->
				methods.put(entry.apiMethod, entry)
			}
			return methods
		} catch (Exception e) {
			String error = String.format('Error transforming ApiCatalog dictionary. %s', e.message)
			log.info(error)
			throw new InvalidParamException(error)
		}
	}
}
