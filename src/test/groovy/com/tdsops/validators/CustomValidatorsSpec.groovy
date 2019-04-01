package com.tdsops.validators

import net.transitionmanager.asset.Application
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.ControlType
import net.transitionmanager.project.Project
import net.transitionmanager.service.CustomDomainService
import org.springframework.context.ApplicationContext
import org.springframework.validation.Errors
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

@Title('Tests for the Validators of Assets')
class CustomValidatorsSpec extends Specification {
	static final String LIST_ITEM = 'Item_1'

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
					  [
							    field: 'custom4',
							    control: ControlType.DATE.value()
					  ],
					  [
							    field: 'custom5',
							    control: ControlType.DATETIME.value()
					  ],
					  [
							    field: 'custom6',
							    control: ControlType.DATETIME.value()
					  ],
					  [
							    field: 'custom7',
							    control: ControlType.LIST.value(),
							    constraints: [
									      required: true,
									      values: [LIST_ITEM]
							    ]
					  ]

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
			app.custom4 = "2018-12-18"
			app.custom5 = "2018-12-18T23:38:43Z"
			app.custom6 = "2018-12-18T23:38Z"
			app.custom7 = LIST_ITEM



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

		then: 'Has an error validating the Number'
			1 * errors.rejectValue('custom1', 'typeMismatch.java.lang.Long', [null], '')

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

		then: 'Has an error validating the Yes/No field'
			1 * errors.rejectValue('custom3', 'field.invalid.notInListOrBlank', ['Wrong YES NO', null, 'Yes, No'], '')

	}

	void 'Test fail validating a wrong Date in Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom4 = "Wrong DATE data"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'Has an error validating the Date field'
			1 * errors.rejectValue('custom4', 'field.incorrect.dateFormat', ['Wrong DATE data', null], '')

	}

	void 'Test fail validating a wrong DateTime in Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom5 = "Wrong DATETIME data"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'Has an error validating the DateTime field'
			1 * errors.rejectValue('custom5', 'field.incorrect.dateFormat', ['Wrong DATETIME data', null], '')

	}

	void 'Test fail validating a wrong LIST Item in Custom Fields'() {
		setup:

			Project project = new Project()
			Errors errors = Mock(Errors)
			String dataDummy = "any data" //just used to trigger all validators

			Application app = new Application()
			app.project = project
			app.custom7 = "Wrong ITEM in List data"


			Closure validator = CustomValidators.validateCustomFields()

		when: 'Apply a validation'
			validator.call(dataDummy, app, errors)

		then: 'Has an error validating the LIST field'
			1 * errors.rejectValue('custom7', 'field.invalid.notInList', ['Wrong ITEM in List data', null, 'Item_1'], '')

	}


}
