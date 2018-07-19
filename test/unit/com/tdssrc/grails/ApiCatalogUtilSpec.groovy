package com.tdssrc.grails

import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject
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
		when: 'try to tramsform an invalid json dictionary'
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
}
