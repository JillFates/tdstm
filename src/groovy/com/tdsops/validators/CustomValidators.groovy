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
		return { value, object, errors ->
			def allValues = aListClosure.call()
			if (value == null || allValues.contains(value.toString())) {
				return true;
			} else {
				errors.rejectValue(fieldName, "${fieldName}.notInList", "${value} of ${fieldName} not in list ${allValues}")
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
