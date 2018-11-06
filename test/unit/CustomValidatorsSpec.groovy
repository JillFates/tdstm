import com.tdsops.validators.CustomValidators
import spock.lang.Specification
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
		when: "the value 'hello' is passed"
			def validator = CustomValidators.controlDefaultValidator('hello', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: "the minSize is not satisfied"
			validator = CustomValidators.controlDefaultValidator('h', fieldSpec)
			errors = validator.apply()
		then: 'a sizeOutOfBounds error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.sizeOutOfBounds' == errors[0].i18nMessageId
		when: "the maxSize is exceeded"
			validator = CustomValidators.controlDefaultValidator('hello my friends', fieldSpec)
			errors = validator.apply()
		then: 'a sizeOutOfBounds error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.sizeOutOfBounds' == errors[0].i18nMessageId
	}

	void '02. Test controlNumberValidator validator ranges'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
					constraints: [
							maxRange : 100,
							minRange : 1,
							precision:2,
							allowNegative: true,
							required: 0
					]

			]
		when: 'the value 10 is passed'
			def validator = CustomValidators.controlNumberValidator('10', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the max allowed value is passed'
			validator = CustomValidators.controlNumberValidator('100', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the min allowed value is passed'
			validator = CustomValidators.controlNumberValidator('1', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the max allowed value is exceeded'
			validator = CustomValidators.controlNumberValidator('101', fieldSpec)
			errors = validator.apply()
		then: 'a "default.invalid.range.message" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'default.invalid.range.message' == errors[0].i18nMessageId
		when: 'the min allowed value is not reached'
			validator = CustomValidators.controlNumberValidator('0', fieldSpec)
			errors = validator.apply()
		then: 'a "default.invalid.range.message" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'default.invalid.range.message' == errors[0].i18nMessageId
        when: 'a non-numeric value is passed'
            validator = CustomValidators.controlNumberValidator('NaN', fieldSpec)
            errors = validator.apply()
        then: 'a "typeMismatch.java.lang.Long" error should be reported'
            validator.hasErrors()
			1 == errors.size()
			'typeMismatch.java.lang.Long' == errors[0].i18nMessageId
	}

	void '03. Test controlNumberValidator validator for negatives and precision'() {
		setup: 'setting the Field Specification Map'
		Map<String, Object> fieldSpec = [
				constraints: [
						maxRange : 1500,
						minRange : -20,
						precision: 2,
						allowNegative: true,
						required: 0
				]

		]
		when: 'the number is negative'
			def validator = CustomValidators.controlNumberValidator('-10', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the minus (-) sign is passed as a suffix and not a prefix'
			validator = CustomValidators.controlNumberValidator('10-', fieldSpec)
			errors = validator.apply()
		then: 'that is not even a valid number format, so a typeMismatch.java.lang.Long error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'typeMismatch.java.lang.Long' == errors[0].i18nMessageId
		when: 'we restrict only to positive numbers and call again with a negative value'
			fieldSpec.constraints.allowNegative = false
			validator = CustomValidators.controlNumberValidator('-10', fieldSpec)
			errors = validator.apply()
		then: 'a negativeNotAllowed error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.negativeNotAllowed' == errors[0].i18nMessageId
	}

	void '04. Test controlNumberValidator validator with a blank and null value and a required:0 (field not required) constraint'() {
		setup: 'setting the Field Specification Map with a required:0 constraint'
		Map<String, Object> fieldSpec = [
				constraints: [
						maxRange : 100,
						minRange : 1,
						precision:2,
						allowNegative: true,
						required: 0
				]

		]
		when: 'a blank is passed'
		def validator = CustomValidators.controlNumberValidator('', fieldSpec)
		validator.apply()
		then: 'no error should be reported'
		!validator.hasErrors()
		when: 'a null is passed'
		validator = CustomValidators.controlNumberValidator(null, fieldSpec)
		validator.apply()
		then: 'no error should be reported'
		!validator.hasErrors()
	}

	void '05. Test controlNumberValidator validator with a blank and null value and a required:1 (field required) constraint'() {
		setup: 'setting the Field Specification Map with a required:1 constraint'
		Map<String, Object> fieldSpec = [
				constraints: [
						maxRange : 100,
						minRange : 1,
						precision:2,
						allowNegative: true,
						required: 1
				]

		]
		when: 'a blank is passed'
		def validator = CustomValidators.controlNumberValidator('', fieldSpec)
		errors = validator.apply()
		then: 'a notEmpty error should be reported'
		validator.hasErrors()
		1 == errors.size()
		'field.invalid.notEmpty' == errors[0].i18nMessageId
		when: 'a null is passed'
		validator = CustomValidators.controlNumberValidator(null, fieldSpec)
		errors = validator.apply()
		then: 'a notEmpty error should be reported'
		validator.hasErrors()
		1 == errors.size()
		'field.invalid.notEmpty' == errors[0].i18nMessageId
	}
}
