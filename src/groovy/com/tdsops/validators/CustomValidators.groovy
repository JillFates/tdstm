package com.tdsops.validators

import com.tds.asset.AssetOptions
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang3.BooleanUtils
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

			if ((value == null && nullable) ||
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
	 * @param type - the AssetOptions type to lookup values for
	 * @return the list closure
	 */
	static Closure optionsClosure(type) {
		return {
			AssetOptions.findAllByType(type)*.value
		}
	}


	/**
	 * Validate the custom Fields in the Class AssetEntity
	 * @param className
	 */
	static Closure validateCustomFields(){
		return { val, object, Errors errors ->
			String className = object.class.simpleName
			List<Map> customFieldSpecs = object.customDomainService.customFieldsList(object.project, className)

			for ( Map fieldSpec : customFieldSpecs ) {
				boolean required = BooleanUtils.toBoolean(fieldSpec.constraints?.required)
				String field = fieldSpec.field
				String label = fieldSpec.label
				String value = object[field]
				String control = fieldSpec.control

				switch (control) {
					case 'YesNo' :
						List<String> yesNoList = ['Yes', 'No']

						if( ! value && required) {
							errors.rejectValue(
								field, 'custom.notInList',
								[value, label, yesNoList.join(', ')].toArray(),
								""
							)

						} else if( ! value || ! yesNoList.contains(value)) {

							errors.rejectValue (
								field,
								'custom.notInList',
								[value, label, "${yesNoList.join(', ')} or BLANK"].toArray(),
								""
							)
						}
						break

					case 'Select List' :
						def optValues = fieldSpec.constraints?.values ?: []

						if(!value && required) {
							errors.rejectValue(
								field,
								'custom.notInList',
								[value, label, optValues.join()].toArray(),
								""
							)

						} else if(value && ! optValues.contains(value)) {
							errors.rejectValue(
								field,
								'custom.notInList',
								[value, label, optValues.join(', ')].toArray(),
								""
							)

						}
						break

					default :
						def minSize = fieldSpec.constraints?.minSize ?: 0
						def maxSize = fieldSpec.constraints?.maxSize ?: Integer.MAX_VALUE

						int size = value?.length() ?: 0
						if( size < minSize && size > maxSize ) {
							errors.rejectValue(
								field,
								'custom.sizeOutOfBounds',
								[value, label, minSize, maxSize].toArray(),
								""
							)
						}
						break
				}
			}
		}
	}
}
