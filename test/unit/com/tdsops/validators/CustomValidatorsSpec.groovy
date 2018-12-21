package com.tdsops.validators

import com.tds.asset.Application
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.ControlType
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import org.springframework.context.ApplicationContext
import org.springframework.validation.Errors
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

@Title('Tests for the Validators of Assets')
class CustomValidatorsSpec extends Specification {
	@Shared
	CustomDomainService customDomainService

	@Shared
	ApplicationContext originalApplicationContext

	void setup() {
		customDomainService = Stub(CustomDomainService) {
			customFieldsList(_, _) >> [
					  [
								 field: 'custom1',
								 control: ControlType.NUMBER.value()
					  ],
					  [
								 field: 'custom2',
								 control: ControlType.STRING.value()
					  ],
					  [
								 field: 'custom3',
								 control: ControlType.YES_NO.value()
					  ],
			]
		}

		ApplicationContext applicationContext = Stub(ApplicationContext) {
			getBean(_, _) >> customDomainService
		}

		originalApplicationContext = ApplicationContextHolder.getInstance().applicationContext
		ApplicationContextHolder.getInstance().applicationContext = applicationContext

	}

	void cleanup() {
		ApplicationContextHolder.getInstance().applicationContext = originalApplicationContext
	}

	void 'Test correct validation of Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom1 = "123"
			app.custom2 = "Hello World"
			app.custom3 = "Yes"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'No Errors'
			0 * errors.rejectValue(*_)

	}

	void 'Test fail validating a wrong number in Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom1 = "ABC"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'No Errors'
			1 * errors.rejectValue(*_)

	}

	void 'Test fail validating a wrong YES/NO in Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom3 = "Wrong YES NO"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'No Errors'
			1 * errors.rejectValue(*_)

	}

}
