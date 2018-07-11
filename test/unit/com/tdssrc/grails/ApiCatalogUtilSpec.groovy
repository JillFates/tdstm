package com.tdssrc.grails

import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.See
import spock.lang.Specification

class ApiCatalogUtilSpec extends Specification {

	private static final String DICTIONARY = '''{
  "dictionary": {
    "info": {
      "provider": "VMware",
      "name": "vCenter-CSI v1",
      "description": "blah blah blah",
      "providerVersion": {
        "label": "3.2",
        "min": "5.2",
        "max": "6.6",
        "publishDate": "2018-12-13"
      },
      "tmVersion": {
        "label": "112.12b",
        "min": "5.2",
        "max": "6.6",
        "publishDate": "2018-12-13"
      },
      "docUrl": "http://about.com",
      "agent": "HttpAgent|AwsAgent|PowerShell|BashShell",
      "credentialGroup": "x"
    },
    "variable": {
      "HOSTNAME_VAR": {
        "name": "HOSTNAME",
        "type": "String",
        "placeHolder": "Enter hostname to vCenter API",
        "scope": "dictionary|environment",
        "value": "http://about.com"
      }
    },
    "credential": {
      "authMethod": "BasicAuth",
      "hostname": "$variable.HOSTNAME_VAR.value$",
      "httpMethod": "POST",
      "requestMode": "FORM_VAR",
      "validationExpression": "header Location missing '/login/form'",
      "headerName": "vmware-api-session-id@cookie:vmware-api-session-id"
    },
    "paramDef": {
      "HOSTNAME_PARAM": {
        "paramName": "HOSTNAME",
        "description": "The ServiceNow Hostname of the instance to interact with",
        "type": "String",
        "context": "USER_DEF",
        "fieldName": null,
        "value": "Enter your FQDN to ServiceNow",
        "required": 1,
        "readonly": 0,
        "encoded": 0
      },
      "TABLE_PARAM": {
        "paramName": "TABLE",
        "description": "The table name from the Now Table API",
        "type": "String",
        "context": "USER_DEF",
        "fieldName": null,
        "value": "Enter your FQDN to ServiceNow",
        "required": 1,
        "readonly": 0,
        "encoded": 0
      },
      "SYSPARM_DISPLAY_VALUE": {
        "paramName": "sysparm_display_value",
        "description": "Set to true will return all fields, false returns only the fields in sysparm_fields (option true|false, default false)",
        "type": "String",
        "context": "USER_DEF",
        "fieldName": "$fieldName",
        "value": "Enter table name",
        "required": 1,
        "readonly": "$readOnly",
        "encoded": 1
      },
      "CSV_PARAM": {
		"paramName": "CSV",
		"description": "Indicate the list format as CSV",
		"type": "String",
		"context": "USER_DEF",
		"fieldName": null,
		"value": "true",
		"required": 1,
		"readonly": 1,
		"encoded": 1
	  }
    },
    "paramGroup": {
      "FOO_GRP": [
        "$paramDef.HOSTNAME_PARAM$",
        "$paramDef.TABLE_PARAM$"
      ]
    },
    "method": [
      {
        "apiMethod": "ApplicationList",
        "name": "Application List (cmdb_ci_appl)",
        "description": "List of all Applications",
        "endpointUrl": "https://{HOSTNAME}.service-now.com/{TABLE}.do",
        "docUrl": "http://about.com/docs#appList",
        "method": "fetchAssetList",
        "producesData": 1,
        "params": [
          "$paramGroup.FOO_GRP$",
          "$paramDef.SYSPARM_DISPLAY_VALUE$",
          {
            "key": "value"
          }
        ],
        "params_no_replacement": [
          "a",
          "b",
          "z",
          "aa"
        ]
      },
      {
        "apiMethod": "DatabaseList",
        "name": "Database List (cmdb_ci_database)",
        "description": "List of databases",
        "endpointUrl": "https://{HOSTNAME}.service-now.com/{TABLE}.do",
        "docUrl": "http://about.com/docs#appList",
        "method": "fetchAssetList",
        "producesData": 1,
        "params": [
          "$paramGroup.FOO_GRP$",
          "$paramDef.CSV_PARAM$"
        ]
      }
    ]
  }
}'''

	@See('TM-10608')
	def 'test can transform json dictionary'() {
		given: 'an ApiCatalog command'
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: DICTIONARY)
			def jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(command)
		expect: 'transformation of the dictionary can be done successfully'
			JSONObject dictionaryTransformed = JsonUtil.parseJson(jsonDictionaryTransformed)
		and: 'transformed dictionary contains expected elements'
			dictionaryTransformed
			dictionaryTransformed.dictionary
			dictionaryTransformed.dictionary.method
			dictionaryTransformed.dictionary.method.size() == 2
			dictionaryTransformed.dictionary.method[0].params
			dictionaryTransformed.dictionary.method[0].params.size() == 4
			dictionaryTransformed.dictionary.method[1].params
			dictionaryTransformed.dictionary.method[1].params.size() == 3
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
			Map methods = ApiCatalogUtil.getCatalogMethods(DICTIONARY)
		then: 'transformed dictionary contains expected methods and parameters placeholders transformed'
			methods
			methods.size() == 2
			methods.containsKey('ApplicationList')
			methods.containsKey('DatabaseList')

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
	}
}
