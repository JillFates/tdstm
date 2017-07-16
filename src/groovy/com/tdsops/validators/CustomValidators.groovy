package com.tdsops.validators

import com.tds.asset.AssetOptions
import com.tdsops.tm.enums.ControlType
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
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

			// Initializing Validators list
			Map<String, Closure> validatorHandlers = [:]
			validatorHandlers[ControlType.YES_NO.toString()] = CustomValidators.&controlYesNoControlValidator
			validatorHandlers[ControlType.LIST.toString()] = CustomValidators.&controlListValidator

			// check all the custom fields against the validators
			for ( Map fieldSpec : customFieldSpecs ) {
				String field = fieldSpec.field
				String value = object[field]
				String control = fieldSpec.control

				Closure validator = validatorHandlers[control] ?: CustomValidators.&controlDefaultValidator

				Collection<ErrorHolder> errorsHolders = validator(value, fieldSpec).apply()

				for(ErrorHolder e : errorsHolders){
					errors.rejectValue(
						e.field,
						e.i18nMessageId,
						e.messageParamsArray(),
						e.defaultMessage
					)
				}
			}
		}
	}

	/* SOME HELPER FUNCTIONS *****************************************************************************/
	/**
	 * NotEmpty Validator used when the value is required
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	static controlNotEmptyValidator ( String value, Map fieldSpec ) {
		new Validator ( fieldSpec ) {
			void validate() {
				if ( ! value && isRequired() ) {
					addError ( 'custom.notEmptySelect', [value, getLabel()] )
				}
			}
		}
	}

	/**
	 * YesNo Validator that checks against a List of Yes, No Values
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	static controlYesNoControlValidator ( String value, Map fieldSpec ) {
		new Validator ( fieldSpec ) {
			void validate() {
				// value = StringUtils.defaultString(value)
				addErrors( controlNotEmptyValidator ( value, fieldSpec ).apply() )

				List<String> yesNoList = ['Yes', 'No']

				if ( ! hasErrors() && ( ! value || ! yesNoList.contains(value)) ) {
					addError ( 'custom.notInList', [value, getLabel(), "${yesNoList.join(', ')} or BLANK"] )
				}

			}
		}
	}

	/**
	 * List Validator that Checks against a List of values
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	static controlListValidator ( String value, Map fieldSpec ) {
		new Validator ( fieldSpec ) {
			void validate() {
				addErrors( controlNotEmptyValidator ( value, fieldSpec ).apply() )

				def optValues = fieldSpec.constraints?.values ?: []

				if( ! hasErrors() && (value && ! optValues.contains(value)) ) {
					addError ( 'custom.notInList', [value, getLabel(), optValues.join(', ')] )
				}
			}
		}
	}

	/**
	 * default validator that checks for min, max size
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	static controlDefaultValidator( String value, Map fieldSpec ) {
		new Validator( fieldSpec ) {
			void validate() {
				def minSize = fieldSpec?.constraints?.minSize ?: 0
				def maxSize = fieldSpec?.constraints?.maxSize ?: Integer.MAX_VALUE

				int size = value?.length() ?: 0
				if (size < minSize && size > maxSize) {
					addError( 'custom.sizeOutOfBounds', [value, getLabel(), minSize, maxSize] )
				}
			}
		}
	}

	/*
	static controlNotEmptyValidator = { String value, Map fieldSpec ->
		List<ErrorHolder> errors = []

		boolean required = BooleanUtils.toBoolean(fieldSpec.constraints?.required)
		String field = fieldSpec.field
		String label = fieldSpec.label

		if( ! value && required) {
			errors << new ErrorHolder (
				field,
				'custom.notInList',
				[value, label, yesNoList.join(', ')]
			)

		}

		return errors
	}

	static controlYesNoControlValidator = { String value, Map fieldSpec ->
		List<ErrorHolder> errors = controlNotEmptyValidator ( value, fieldSpec )

		List<String> yesNoList = ['Yes', 'No']

		if ( ! errors && ( ! value || ! yesNoList.contains(value)) ) {

			String field = fieldSpec.field
			String label = fieldSpec.label

			errors << new ErrorHolder (
					field,
					'custom.notInList',
					[value, label, "${yesNoList.join(', ')} or BLANK"]
			)
		}

		return errors

	}

	static controlDefaultValidator = { String value, Map fieldSpec ->
		List<ErrorHolder> errors = []

		def minSize = fieldSpec.constraints?.minSize ?: 0
		def maxSize = fieldSpec.constraints?.maxSize ?: Integer.MAX_VALUE

		int size = value?.length() ?: 0
		if( size < minSize && size > maxSize ) {
			String field = fieldSpec.field

			errors << new ErrorHolder (
					field,
					'custom.sizeOutOfBounds',
					[value, label, minSize, maxSize]
			)
		}

		return errors
	}
	*/

	/**
	 * Error holder to return from the validator Function Handlers
	 */
	static private class ErrorHolder {
		String field
		String i18nMessageId
		Collection messageParams
		// the following message is not required while the 18nId is present
		String defaultMessage = StringUtils.EMPTY

		ErrorHolder(String field, String i18nMessageId, Collection messageParams){
			this.field = field
			this.i18nMessageId = i18nMessageId
			this.messageParams = messageParams
		}

		Object[] messageParamsArray(){
			messageParams.toArray()
		}
	}

	/**
	 * Abstract Validator that validates a fieldSpec implemented in validate()
	 */
	static private abstract class Validator {
		private List<ErrorHolder> errors = []
		private String field
		public String label
		private boolean required
		private Map fieldSpec


		Validator(Map fieldSpec){
			this.fieldSpec = fieldSpec
			field = fieldSpec?.field
			label = fieldSpec?.label
			required = BooleanUtils.toBoolean(fieldSpec?.constraints?.required)
		}

		/**
		 * Actual Validation function
		 */
		abstract void validate()

		/**
		 * Add Errors to the ErrorHolder list
		 * @param i18Message
		 * @param params
		 */
		void addError(String i18Message, Collection params) {
			errors << new ErrorHolder(
				field,
				i18Message,
				params
			)
		}

		/**
		 * copy all errors from other validations
		 * @param errors
		 */
		void addErrors(Collection<ErrorHolder> errors) {
			errors.addAll(errors ?: [])
		}

		/*
		 *	The accessors are required due that for some reason the anonymous classes do't allow the access to the public properties
		 */
		boolean hasErrors() {
			errors
		}

		String getLabel(){
			label
		}

		boolean isRequired(){
			required
		}

		/**
		 * Apply the current validator and return the errors
		 * @return
		 */
		List<ErrorHolder> apply() {
			validate()
			return errors
		}
	}
}
