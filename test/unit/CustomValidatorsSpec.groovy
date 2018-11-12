import com.tdsops.validators.CustomValidators
import spock.lang.Specification
import test.AbstractUnitSpec
import com.tdsops.validators.CustomValidators.ErrorHolder

/**
 * Created by octavio on 7/17/17.
 */
class CustomValidatorsSpec extends Specification{

	List<ErrorHolder> errors

	void '01. Test controlDefaultValidator validator'() {
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

	void '02. Test controlDateValidator validator'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
					constraints: [
							required : 0
					]

			]
		when: 'the value "2018-10-26" is passed'
			def validator = CustomValidators.controlDateValidator("2018-10-26", fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateValidator("10/26/2018 02:31:15 PM", fieldSpec)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
		when: 'the string is an invalid value'
			validator = CustomValidators.controlDateValidator("invalid", fieldSpec)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
	}

	void '03. Test controlDateTimeValidator validator'() {
		setup: 'setting the Field Specification Map'
		Map<String, Object> fieldSpec = [
				constraints: [
						required : 0
				]
		]
		when: "the value 2018-10-26'T'22:00:15'Z' is passed"
			def validator = CustomValidators.controlDateTimeValidator("2018-10-26T22:00:15Z", fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31 PM", fieldSpec)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
	}
}
