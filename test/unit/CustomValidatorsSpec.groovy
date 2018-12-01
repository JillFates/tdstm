import com.tds.asset.AssetEntity
import com.tdsops.validators.CustomValidators
import spock.lang.Specification
import com.tdsops.validators.CustomValidators.ErrorHolder

/**
 * Created by octavio on 7/17/17.
 */
class CustomValidatorsSpec extends Specification{

	List<ErrorHolder> errors
	AssetEntity mockDomain = new AssetEntity()

	void '01. Test controlDefaultValidator validator'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
				constraints: [
					minSize : 2,
					maxSize : 5
				]
			]
		when: "the value 'hello' is passed"
			def validator = CustomValidators.controlDefaultValidator('hello', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: "the minSize is not satisfied"
			validator = CustomValidators.controlDefaultValidator('h', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a sizeOutOfBounds error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.sizeOutOfBounds' == errors[0].i18nMessageId
		when: "the maxSize is exceeded"
			validator = CustomValidators.controlDefaultValidator('hello my friends', fieldSpec, mockDomain)
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
							required: 0
					]

			]
		when: 'the value 10 is passed'
			def validator = CustomValidators.controlNumberValidator('10', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the max allowed value is passed'
			validator = CustomValidators.controlNumberValidator('100', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the min allowed value is passed'
			validator = CustomValidators.controlNumberValidator('1', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the max allowed value is exceeded'
			validator = CustomValidators.controlNumberValidator('101', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a "default.invalid.range.message" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'default.invalid.range.message' == errors[0].i18nMessageId
		when: 'the min allowed value is not reached'
			validator = CustomValidators.controlNumberValidator('0', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a "default.invalid.range.message" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'default.invalid.range.message' == errors[0].i18nMessageId
        when: 'a non-numeric value is passed'
            validator = CustomValidators.controlNumberValidator('NaN', fieldSpec, mockDomain)
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
		when: 'the number is negative, inside of "minRange" lower limit'
			def validator = CustomValidators.controlNumberValidator('-10', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'we restrict only to positive numbers and call again with a negative value'
			fieldSpec.constraints.allowNegative = false
			validator = CustomValidators.controlNumberValidator('-10', fieldSpec, mockDomain)
			validator.apply()
		then: 'as there is a "minRange" present (and the number fits in the range), the "allowNegative" constraint is ignored and no error is reported'
			!validator.hasErrors()
		when: 'we remove the "minRange" constraint so the "allowNegative" constraint is not ignored anymore'
			fieldSpec.constraints.minRange = ''
			validator = CustomValidators.controlNumberValidator('-10', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'now as there is no "minRange" lower limit and "allowNegative = false", a negativeNotAllowed error is reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.negativeNotAllowed' == errors[0].i18nMessageId
		when: 'we use a number with a fractional part, and the fractional part is NO bigger than the precision constraint'
			validator = CustomValidators.controlNumberValidator('2.55', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'we use a number with a fractional part, and the fractional part is bigger than the precision constraint'
			validator = CustomValidators.controlNumberValidator('2.555', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'the precision constraint is exceeded, and an error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.invalid.precisionExceeded' == errors[0].i18nMessageId
		when: 'the minus (-) sign is passed as a suffix and not a prefix'
			validator = CustomValidators.controlNumberValidator('10-', fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'that is not even a valid number format, so a typeMismatch.java.lang.Long error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'typeMismatch.java.lang.Long' == errors[0].i18nMessageId
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
			def validator = CustomValidators.controlNumberValidator('', fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'a null is passed'
			validator = CustomValidators.controlNumberValidator(null, fieldSpec, mockDomain)
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
		def validator = CustomValidators.controlNumberValidator('', fieldSpec, mockDomain)
		errors = validator.apply()
		then: 'a default.blank.message error should be reported'
		validator.hasErrors()
		1 == errors.size()
		'default.blank.message' == errors[0].i18nMessageId
		when: 'a null is passed'
		validator = CustomValidators.controlNumberValidator(null, fieldSpec, mockDomain)
		errors = validator.apply()
		then: 'a default.blank.message error should be reported'
		validator.hasErrors()
		1 == errors.size()
		'default.blank.message' == errors[0].i18nMessageId
	}

	void '07. Test controlDateValidator validator'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
					constraints: [
							required : 0
					]

			]
		when: 'the value "2018-10-26" is passed'
			def validator = CustomValidators.controlDateValidator("2018-10-26", fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateValidator("10/26/2018 02:31:15 PM", fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
		when: 'the string is an invalid value'
			validator = CustomValidators.controlDateValidator("invalid", fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
	}

	void '08. Test controlDateTimeValidator validator'() {
		setup: 'setting the Field Specification Map'
		Map<String, Object> fieldSpec = [
				constraints: [
						required : 0
				]
		]
		when: "the value 2018-10-26T22:00:15Z is passed (ISO8601 compliant)"
			def validator = CustomValidators.controlDateTimeValidator("2018-10-26T22:00:15Z", fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: "the value 2018-10-26T22:00Z is passed (ISO8601 compliant)"
			validator = CustomValidators.controlDateTimeValidator("2018-10-26T22:00Z", fieldSpec, mockDomain)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the date string does not match the format'
			validator = CustomValidators.controlDateTimeValidator("10/26/2018 02:31 PM", fieldSpec, mockDomain)
			errors = validator.apply()
		then: 'a "field.incorrect.dateFormat" error should be reported'
			validator.hasErrors()
			1 == errors.size()
			'field.incorrect.dateFormat' == errors[0].i18nMessageId
	}
}
