import grails.test.spock.IntegrationSpec
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Project
import net.transitionmanager.service.ApiCatalogService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ProviderService
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import spock.lang.See

class ApiCatalogServiceIntegrationSpec extends IntegrationSpec {
	ApiCatalogService apiCatalogService
	ProviderService providerService
	GrailsApplication grailsApplication
	SessionFactory sessionFactory

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

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

	def setup() {
		sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
		Project project = projectTestHelper.createProject()
		providerService.securityService = [getUserCurrentProject: { return project }] as SecurityService
		apiCatalogService.securityService = [getUserCurrentProject: { return project }] as SecurityService
	}

	@See('TM-10608')
	void '1. find api catalog by id returns api catalog'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			def foundApiCatalog = apiCatalogService.findById(apiCatalog.id)

		then:
			foundApiCatalog
			null != foundApiCatalog.id
	}

	@See('TM-10608')
	void '2. delete api catalog by id effectively deletes api catalog'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			def foundApiCatalogId = apiCatalog.id
			apiCatalogService.deleteById(foundApiCatalogId)

		then:
			null == apiCatalogService.findById(foundApiCatalogId)
	}

	@See('TM-10608')
	void '3. save api catalog giving invalid dictionary json throws invalid param exception'() {
		setup:
			String dictionary =  '{"key": "value","key"}'
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: dictionary)

		when:
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown InvalidParamException
	}

	@See('TM-10608')
	void '4. save api catalog with wrong version throws domain update exception'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			command = new ApiCatalogCommand(dictionary: DICTIONARY, id: apiCatalog.id, version: 2)
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown DomainUpdateException
	}

	@See('TM-10608')
	void '5. save api catalog with an existing/duplicate name throws domain update exception'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: DICTIONARY)
			apiCatalogService.saveOrUpdate(command)

		when:
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown DomainUpdateException
	}

}
