package com.tdssrc.grails

import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.service.InvalidParamException
import org.grails.web.json.JSONObject
import spock.lang.See
import spock.lang.Specification
import test.helper.ApiCatalogTestHelper

class ApiCatalogUtilSpec extends Specification {

	@See('TM-10608')
	def 'test can transform json dictionary'() {
		given: 'an ApiCatalog command'
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY)
			def jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(command)
		expect: 'transformation of the dictionary can be done successfully'
			JSONObject dictionaryTransformed = JsonUtil.parseJson(jsonDictionaryTransformed)
		and: 'transformed dictionary contains expected elements'
			dictionaryTransformed
			dictionaryTransformed.dictionary
			dictionaryTransformed.dictionary.method
			dictionaryTransformed.dictionary.method.size() == 3
			dictionaryTransformed.dictionary.method[0].params
			dictionaryTransformed.dictionary.method[0].params.size() == 4
			dictionaryTransformed.dictionary.method[1].params
			dictionaryTransformed.dictionary.method[1].params.size() == 3
			dictionaryTransformed.dictionary.method[2].params
			dictionaryTransformed.dictionary.method[2].params.size() == 1
	}

	@See('TM-10608')
	def 'test cannot transform invalid json dictionary'() {
		setup:
			def dictionary
			def jsonDictionaryTransformed
		when: 'try to transform an invalid json dictionary'
			dictionary =  '{"key": "value","key"}'
			jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
		then:
			thrown InvalidParamException
		when: 'try to transform json missing primary keys'
			dictionary = '{"dictionary": {"key": "value"}}'
			jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
		then:
			thrown InvalidParamException
	}

	@See('TM-10608')
	def 'test get catalog methods return expected list of methods'() {
		when:
			String jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(ApiCatalogTestHelper.DICTIONARY)
			Map methods = ApiCatalogUtil.getCatalogMethods(jsonDictionaryTransformed)

		then: 'transformed dictionary contains expected methods and parameters placeholders transformed'
			methods
			methods.size() == 3
			methods.containsKey('ApplicationList')
			methods.containsKey('DatabaseList')
			methods.containsKey('callEndpoint')

			with(methods['ApplicationList']) {
				apiMethod == 'ApplicationList'
				params.size() == 4
				with(params[0]) {
					paramName == 'HOSTNAME'
				}
				with(params[1]) {
					paramName == 'TABLE'
				}
				with(params[2]) {
					paramName == 'sysparm_display_value'
				}
			}

		with(methods['DatabaseList']) {
			apiMethod == 'DatabaseList'
			params.size() == 3
			with(params[0]) {
				paramName == 'HOSTNAME'
			}
			with(params[1]) {
				paramName == 'TABLE'
			}
			with(params[2]) {
				paramName == 'CSV'
			}
		}

		with(methods['callEndpoint']) {
			apiMethod == 'callEndpoint'
			params.size() == 1
			with(params[0]) {
				paramName == 'HOSTNAME'
			}
		}
	}

	@See('TM-11427')
	def 'test invalid reaction script code entry'() {
		when: 'try to transform a dictionary with wrong reaction script code key'
			def dictionary =  '{"dictionary": {"info": {},  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"scriptDef": {}, ' +
					'"script": {"NON_EXISTING_REACTION_SCRIPT": "should error"},' +
					'"method": []}}'
			ApiCatalogUtil.transformDictionary(dictionary)
		then:
			def e = thrown InvalidParamException
			e.message == "Error transforming ApiCatalog dictionary. Invalid reaction script code entry: NON_EXISTING_REACTION_SCRIPT"

	}

	@See('TM-11427')
	def 'test method without reaction script entry gets added common reaction script'() {
		when: 'try to transform a dictionary with wrong reaction script code key'
			def dictionary =  '{"dictionary": {"info": {},  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"scriptDef": {}, ' +
					'"script": {"DEFAULT": "// default common reaction script"},' +
					'"method": [' +
					'	{' +
					'		"name": "Test Method",' +
					'		"apiMethod": "testMethod",' +
					'       "httpMethod": "OPTIONS"' +
					'	}' +
					']}}'
			String jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
			Map methods = ApiCatalogUtil.getCatalogMethods(jsonDictionaryTransformed)
		then:
			methods
			methods.size() == 1
			methods.containsKey('testMethod')
			with(methods['testMethod']) {
				apiMethod == 'testMethod'
				httpMethod == 'OPTIONS'
				script.size() == 1
				with(script) {
					DEFAULT == "// default common reaction script"
				}
			}
	}

	@See('TM-11427')
	def 'test transform dictionary with reaction scripts and methods will contain expected reactions scripts transformed and merged'() {
		when:
			String jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(ApiCatalogTestHelper.DICTIONARY_WITH_SCRIPTS)
			Map methods = ApiCatalogUtil.getCatalogMethods(jsonDictionaryTransformed)

		then: 'transformed dictionary contains expected methods with reaction scripts'
			methods
			methods.size() == 1
			methods.containsKey('callEndpoint')
			with(methods['callEndpoint']) {
				apiMethod == 'callEndpoint'
				script.size() == 4
				with(script) {
					SUCCESS == "// Success script for 204 status code - nocontent\n          task.hold( 'Moving the task to hold since no content was received' )"
					FAILED == "// a script that isn't in the dictionary.script declaration\n                    // Failed -logic to perform when API call receives 400 or 500 series HTTP error code.\n                     task.error( response.error )"
					STATUS == "// Check the HTTP response code for a 200 OK\n          if (response.status == SC.OK) { \n            return SUCCESS \n        } else { \n            return ERROR \n        }"
					ERROR == "// Put the task on hold and add a comment with the cause of the error\n          task.error( response.error )"
				}
			}

	}

	@See('TM-11589')
	def 'test invalid httpMethod entry'() {
		when: 'try to transform a dictionary with wrong httpMethod key'
			def dictionary =  '{"dictionary": {"info": {},  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"method": [' +
					'	{' +
					'		"name": "Test Method",' +
					'		"apiMethod": "testMethod",' +
					'       "httpMethod": "HTTP"' +
					'	}' +
					']}}'
				ApiCatalogUtil.transformDictionary(dictionary)
		then:
			def e = thrown InvalidParamException
			e.message == "Error transforming ApiCatalog dictionary. Api method testMethod has an invalid httpMethod value: HTTP"

	}

	@See('TM-11893')
	def 'test unexpected dictionary key'() {
		when: 'try to transform a dictionary with unexpected key'
			def dictionary =  '{"dictionary": {"info": { "agent": "tm-agent" },  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"method": [' +
					'	{' +
					'		"name": "Test Method",' +
					'		"apiMethod": "testMethod",' +
					'       "httpMethod": "GET"' +
					'	}' +
					']}}'
			ApiCatalogUtil.transformDictionary(dictionary)
		then:
			def e = thrown InvalidParamException
			e.message == 'Error transforming ApiCatalog dictionary. Attribute "dictionary.info.agent" has been renamed to "dictionary.info.connector"'

	}

	@See('TM-13027')
	def 'test can transform method definition'() {
		when: 'transforming a method definition into a DictionaryItem'
			def dictionary =  '{"dictionary": {"info": {},  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"method": [' +
					'	{' +
					'		"name": "Test Method",' +
					'		"apiMethod": "testMethod",' +
					'       "httpMethod": "GET",' +
					'       "description": "Hello test method",' +
					'       "endpointUrl": "http://endpoint.url",' +
					'       "docUrl": "http://documentation.url",' +
					'       "method": "invokeHttpRequest",' +
					'       "producesData": 0,' +
					'       "params": [{"param1":"value1"}],' +
					'       "script": {"SUCCESS": "task.done()"}' +
					'	}' +
					']}}'
			String jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
		then: 'method definition is transformed correctly'
			jsonDictionaryTransformed
		when: 'transforming an invalid method definition'
			def invalidDictionaryMethodDefinition =  '{"dictionary": {"info": {},  "paramDef": {}, "variable": {}, ' +
					'"credential": {}, "paramGroup": {}, ' +
					'"method": [' +
					'	{' +
					'		"name": "Test Method",' +
					'		"apiMethod": "testMethod",' +
					'       "httpMethod": "GET",' +
					'       "description": "Hello test method",' +
					'       "endpointUrl": "http://endpoint.url",' +
					'       "docUrl": "http://documentation.url",' +
					'       "method": "invokeHttpRequest",' +
					'       "producesData": 0,' +
					'       "params": [{"param1":"value1"}],' +
					'       "scripts": {"SUCCESS": "task.done()"}' +
					'	}' +
					']}}'
			ApiCatalogUtil.transformDictionary(invalidDictionaryMethodDefinition)
		then: 'InvalidParamException is thrown'
			def e = thrown InvalidParamException
			e.message == 'Error transforming ApiCatalog dictionary. Api method name: "Test Method" definition has an invalid property: scripts'
	}
}
