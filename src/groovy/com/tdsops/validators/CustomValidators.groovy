package com.tdsops.validators

import com.tds.asset.AssetOptions
import com.tdssrc.grails.GormUtil
import org.springframework.validation.Errors

class CustomValidators {

	/**
	 * Creates a custom validator for a inList that is lazy computed
	 *
	 * @param aListClosure a closure that returns the list of valid values
	 * @param fieldName the field name
	 * @return the custom validator
	 */
	static Closure inList(Closure aListClosure, String fieldName) {
		// value = user input
		// object = the domain instance or comment object
		// errors = Spring error object
		return { value, object, Errors errors ->

			// Get the list of values from the list Closure
			List<String> validValues = aListClosure.call()

			// Determine if the field supports blank and nullable
			def blank = GormUtil.getConstraintValue(object.getClass(), fieldName, 'blank')
			def nullable = GormUtil.getConstraintValue(object.getClass(), fieldName, 'nullable')

			if ((value == null && nullable && blank) ||
			    (value == '' && blank) ||
			     value.toString() in validValues) {
				return
			}

			errors.rejectValue(fieldName, fieldName + '.notInList',
				"Value '$value' for property '$fieldName' is invalid, options are: ${validValues.join(', ')}")
		}
	}

	/**
	 * Creates a closure for the first parameter of the inList method that returns
	 * the list of {@link AssetOptions} with the specific type
	 *
	 * @param type the AssetOptions type
	 * @return the list closure
	 */
	static Closure optionsClosure(type) {
		return {
			return AssetOptions.findAllByType(type)*.value
		}
	}
}
