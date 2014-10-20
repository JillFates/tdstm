package com.tdsops.validators

import com.tds.asset.AssetOptions

class CustomValidators {

	/**
	 * Creates a custom validator for a inList that is lazy computed
	 * 
	 * @param aListClosure a closure that returns the list of valid values
	 * @param fieldName the field name
	 * @return the custom validator
	 */
	public static inList(aListClosure, fieldName) { 
		// value = user input
		// object = the domain object
		// errors = Spring error object
		return { value, object, errors ->

			// Get the list of values from the list Closure
			def validValues = aListClosure.call()

			// Determine if the field supports blank and nullable
			def blank = object.constraints[fieldName].blank
			def nullable = object.constraints[fieldName].nullable

			if ( (value == null && nullable && blank) || (value == '' && blank)  || validValues.contains(value.toString()) ) {
				return true;
			} else {
println "CustomValidators.inList failed for $fieldName with value ($value)"
				errors.rejectValue(fieldName, "${fieldName}.notInList", "${value} of ${fieldName} not in list ${validValues.join(', ')}")
				return false
			}
		}
	}
	
	/**
	 * Creates a closure for the first parameter of the inList method that returns 
	 * the list of {@link AssetOptions} with the specific type
	 * 
	 * @param type the AssetOptions type
	 * @return the list closure
	 */
	public static optionsClosure(type) {
		return { 
			return AssetOptions.findAllByType(type).collect { option -> option.value }
		}
	}
}
