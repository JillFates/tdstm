package test.helper

import com.tdssrc.grails.ApiCatalogUtil
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang.RandomStringUtils as RSU

class ApiCatalogTestHelper {

	public static final String DICTIONARY = '''{
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
      },
      {
        "apiMethod": "callEndpoint",
        "name": "Method used by application tests",
        "description": "Call endpoint",
        "endpointUrl": "https://{HOSTNAME}.about.com/{TABLE}.do",
        "docUrl": "http://about.com/docs#appList",
        "method": "fetchAssetList",
        "producesData": 1,
        "params": [
          "$paramDef.HOSTNAME_PARAM$"
        ]
      }
    ]
  }
}'''

	ApiCatalog createApiCatalog(Project project, Provider provider) {
		ApiCatalog apiCatalog = new ApiCatalog(
				project: project,
				provider: provider,
				name: RSU.randomAlphabetic(10),
				dictionary: DICTIONARY,
				dictionaryTransformed: ApiCatalogUtil.transformDictionary(DICTIONARY),
		).save(flush: true)
		return apiCatalog
	}
}
