import com.tdsops.validators.CustomValidators
import spock.lang.Specification
import test.AbstractUnitSpec
import com.tdsops.validators.CustomValidators.ErrorHolder

/**
 * Created by octavio on 7/17/17.
 */
class CustomValidatorsSpec extends Specification{

	List<ErrorHolder> errors

	void 'test controlDefaultValidator validator'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [

				constraints: [
					minSize : 2,
					maxSize : 5
				]

			]

		when: "testing that a value 'hello' is Valid"
			def validator = CustomValidators.controlDefaultValidator('hello', fieldSpec)
			validator.apply()

		then: 'no error should be reported'
			! validator.hasErrors()
	}

	void '02. Test controlDateTimeValidator validator with format MM/dd/yyyy hh:mm a'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
					constraints: [
							format : "MM/dd/yyyy hh:mm a"
					]

			]
		when: 'the value "10/26/2018 02:31 PM" is passed'
			def validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31 PM", fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31:15 PM", fieldSpec)
			errors = validator.apply()
		then: 'an invalid date format should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.dateFormat' == errors[0].i18nMessageId
		when: 'the string is an invalid value'
			validator = CustomValidators.controlDateTimeValidator("invalid", fieldSpec)
			errors = validator.apply()
		then: 'an invalid date format should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.dateFormat' == errors[0].i18nMessageId
	}

	void '03. Test controlDateTimeValidator validator with format MM/dd/yyyy hh:mm:ss a'() {
		setup: 'setting the Field Specification Map'
		Map<String, Object> fieldSpec = [
				constraints: [
						format : "MM/dd/yyyy hh:mm:ss a"
				]

		]
		when: 'the value "10/26/2018 02:31:15 PM" is passed'
			def validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31:15 PM", fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31 PM", fieldSpec)
			errors = validator.apply()
		then: 'a valueOutOfRange error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.dateFormat' == errors[0].i18nMessageId
	}
}
