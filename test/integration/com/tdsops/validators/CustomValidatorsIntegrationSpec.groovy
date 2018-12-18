package com.tdsops.validators

import com.tds.asset.Application
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.Errors
import spock.lang.Shared

class CustomValidatorsIntegrationSpec extends IntegrationSpec {
	CustomDomainService customDomainService
	Logger log = LoggerFactory.getLogger(CustomValidatorsIntegrationSpec.class)

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	void setup() {
		customDomainService.metaClass.customFieldsList = { project, assetClass ->
			[
					  [
							    field: 'custom1',
							    control: 'String'
					  ]/*,
					  [
							    field: 'custom2',
							    control: 'String'
					  ]*/
			]
		}

	}


	void 'Test validateCustomFields'() {
		given:
			Closure validator = CustomValidators.validateCustomFields()
			def errorCalls = 0
			Errors errors = Stub(Errors) {
				rejectValue(_) >> { args ->
					log.info("CAlled rejectValue: $args")
					errorCalls++
				}
			}
			Application app = new Application()
			app.project = project
			String data = "any data"

		when: 'Apply a validation'
			validator.call(data, app, errors)
		/*
			errors.rejectValue(
					  "field",
					  "message",
					  ["a", "b"].toArray(),
					  "dafault message"
			)
		*/
		then: 'No Errors'
			errorCalls == 0
	}
}
