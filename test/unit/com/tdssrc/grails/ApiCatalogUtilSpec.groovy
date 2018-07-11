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
        "results": "invokeResults()",
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
			dictionaryTransformed.method
			dictionaryTransformed.method.size() == 1
			dictionaryTransformed.method[0].params
			dictionaryTransformed.method[0].params.size() == 4
	}

	@See('TM-10608')
	def 'test cannot transform invalid json dictionary'() {
		when: 'try to parse an invalid json dictionary'
			String dictionary =  '{"key": "value","key"}'
			def jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
		then:
			thrown InvalidParamException
	}

	@See('TM-10608')
	def 'test transform json dictionary'() {
		when: 'valid json dictionary with no placeholders'
			String dictionary = '{"dictionary": {"key": "value"}}'
			def jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(dictionary)
		then:
			'{\n    "key": "value"\n}' == jsonDictionaryTransformed
	}

}
