package test.helper

import com.tdssrc.grails.ApiCatalogUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang3.RandomStringUtils as RSU

@Transactional
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
      "connector": "HttpConnector|AwsConnector|PowerShell|BashShell",
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
        "httpMethod": "GET",
        "producesData": 1,
        "params": [
          "$paramGroup.FOO_GRP$",
          "$paramDef.SYSPARM_DISPLAY_VALUE$",
          {
            "key": "value"
          }
        ]
      },
      {
        "apiMethod": "DatabaseList",
        "name": "Database List (cmdb_ci_database)",
        "description": "List of databases",
        "endpointUrl": "https://{HOSTNAME}.service-now.com/{TABLE}.do",
        "docUrl": "http://about.com/docs#appList",
        "method": "fetchAssetList",
        "httpMethod": "GET",
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
        "httpMethod": "GET",
        "producesData": 1,
        "params": [
          "$paramDef.HOSTNAME_PARAM$"
        ]
      }
    ]
  }
}'''

	public static final String DICTIONARY_WITH_SCRIPTS = '''{
    "dictionary": {
        "info": {
            "name": "HTTP API v1",
            "connector": "HttpConnector",
            "docUrl": "http://about.com",
            "provider": "Http Provider",
            "tmVersion": {
                "max": "4.5",
                "min": "4.5",
                "label": "Version 4.5.0 (Development)",
                "publishDate": "2018-07-12"
            },
            "description": "Http Connector",
            "credentialGroup": "N/A",
            "providerVersion": {
                "max": "1",
                "min": "1",
                "label": "Version 4.5.0 (Development)",
                "publishDate": "2018-07-12"
            }
        },
        "scriptDef": {
          "ERROR_SCRIPT": "// Put the task on hold and add a comment with the cause of the error
          task.error( response.error )", 
          "200_SUCCESS_SCRIPT": "// Update the task status that the task completed
          task.done()", 
          "204_SUCCESS_SCRIPT": "// Success script for 204 status code - nocontent
          task.hold( 'Moving the task to hold since no content was received' )"
        },
        "script": {
          "STATUS": "// Check the HTTP response code for a 200 OK
          if (response.status == SC.OK) { 
            return SUCCESS 
        } else { 
            return ERROR 
        }",  
          "ERROR": "$scriptDef.ERROR_SCRIPT$",
          "SUCCESS": "$scriptDef.200_SUCCESS_SCRIPT$"
        },
        "method": [
            {
                "apiMethod": "callEndpoint",
                "name": "Call Endpoint",
                "description": "Performs a call to an HTTP endpoint",
                "endpointUrl": "https://SOME-DOMAIN/SOME/PATH",
                "docUrl": "http://about.com/docs#appList",
                "method": "invokeHttpRequest",
                "httpMethod": "GET",
                "producesData": 0,
                "params": [
                	"$paramDef.HOSTNAME_PARAM$"
                ],
                "script": {
                    "SUCCESS": "$scriptDef.204_SUCCESS_SCRIPT$",
                    "FAILED": "// a script that isn't in the dictionary.script declaration
                    // Failed -logic to perform when API call receives 400 or 500 series HTTP error code.
                     task.error( response.error )"
                },
            }
        ],
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
		  	}
        },
        "variable": {},
        "credential": {},
        "paramGroup": {}
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
