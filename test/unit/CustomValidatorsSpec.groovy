import com.tdsops.validators.CustomValidators
import spock.lang.Specification
import test.AbstractUnitSpec

/**
 * Created by octavio on 7/17/17.
 */
class CustomValidatorsSpec extends Specification{

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
			! validator.hasErrors()
		when: "the minSize is not satisfied"
			validator = CustomValidators.controlDefaultValidator('h', fieldSpec)
			validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
		when: "the maxSize is exceeded"
			validator = CustomValidators.controlDefaultValidator('hello my friends', fieldSpec)
			validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
	}

	void '02. Test controlNumberValidator validator ranges'() {
		setup: 'setting the Field Specification Map'
			Map<String, Object> fieldSpec = [
					constraints: [
							maxRange : 100,
							minRange : 1,
							decimalPlaces:2,
							useThousandSeparator: true,
							allowNegatives: true
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
			validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
		when: 'the min allowed value is not reached'
			validator = CustomValidators.controlNumberValidator('0', fieldSpec)
			validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
        when: 'a non-numeric value is passed'
            validator = CustomValidators.controlNumberValidator('NaN', fieldSpec)
            validator.apply()
        then: 'an error should be reported'
            validator.hasErrors()
	}

	void '03. Test controlNumberValidator validator negatives, decimal places and thousand separator'() {
		setup: 'setting the Field Specification Map'
		Map<String, Object> fieldSpec = [
				constraints: [
						maxRange : 1500,
						minRange : -20,
						decimalPlaces:2,
						useThousandSeparator: true,
						allowNegatives: true
				]

		]
		when: 'the value 1,000 is passed, using thousand separator'
			def validator = CustomValidators.controlNumberValidator('1,000', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'the value 1,000 is passed, without thousand separator (mandatory accoring tho the given field spec)'
			validator = CustomValidators.controlNumberValidator('1000', fieldSpec)
			validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
		when: 'the number is negative'
			validator = CustomValidators.controlNumberValidator('-10', fieldSpec)
			validator.apply()
		then: 'no error should be reported'
			!validator.hasErrors()
		when: 'we restrict only to positive numbers and call that again'
		fieldSpec.constraints.allowNegatives = false
		validator = CustomValidators.controlNumberValidator('-10', fieldSpec)
		validator.apply()
		then: 'an error should be reported'
			validator.hasErrors()
	}
}
