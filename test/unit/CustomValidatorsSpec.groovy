import com.tdsops.validators.CustomValidators
import spock.lang.Specification
import test.AbstractUnitSpec

/**
 * Created by octavio on 7/17/17.
 */
class CustomValidatorsSpec extends Specification{
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
}
