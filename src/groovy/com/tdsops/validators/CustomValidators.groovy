package com.tdsops.validators

import com.tds.asset.AssetOptions
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.ControlType
import com.tdssrc.grails.GormUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.service.CustomDomainService
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.validation.Errors

import java.text.NumberFormat
import java.text.ParseException

@Slf4j
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
			CustomDomainService customDomainService = ApplicationContextHolder.getBean('customDomainService', CustomDomainService)
			List<Map> customFieldSpecs = customDomainService.customFieldsList(object.project, object.assetClass.toString())

			// Initializing Validators list
			Map<String, Closure> validatorHandlers = [:]
			validatorHandlers[ControlType.YES_NO.toString()] = CustomValidators.&controlYesNoControlValidator
			validatorHandlers[ControlType.LIST.toString()] = CustomValidators.&controlListValidator
			validatorHandlers[ControlType.NUMBER.toString()] = CustomValidators.&controlNumberValidator
			validatorHandlers[ControlType.STRING.toString()] = CustomValidators.&controlDefaultValidator

			// check all the custom fields against the validators
			for ( Map fieldSpec : customFieldSpecs ) {
				String field = fieldSpec.field
				String value = StringUtils.defaultString(object[field])

				String control = fieldSpec.control

				// don't use a default validator, throw an exception(Runtime) notifying that the validator is missing
				Closure validator = validatorHandlers[control]

				if(!validator) {
					log.error("No validator defined for '{}' Custom Control", control)
					throw new RuntimeException("No validator defined for '${control}' Custom Control")
				}

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
				if ( isRequired() && ! value ) {
					addError ( 'field.invalid.notEmpty', [value, getLabel()] )
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
		final List<String> yesNoList = ['Yes', 'No']

		new Validator ( fieldSpec ) {
			void validate() {
				// value = StringUtils.defaultString(value)
				addErrors( controlNotEmptyValidator ( value, fieldSpec ).apply() )

				if ( ! hasErrors() && ! yesNoList.contains(value) ) {
					addError ( 'field.invalid.notInListOrBlank', [value, getLabel(), yesNoList.join(', ')] )
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

				if( ! hasErrors() && StringUtils.isNotBlank(value) && ! optValues.contains(value) ) {
					addError ( 'field.invalid.notInList', [value, getLabel(), optValues.join(', ')] )
				}
			}
		}
	}

	/**
	 * Number Validator that Checks the numeric type of the value and the
	 * given constraints in the fieldSpec Map.
	 * See TM-8447
	 *
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	static  controlNumberValidator ( String value, Map fieldSpec ) {
		new Validator ( fieldSpec ) {
			void validate() {
			    // check that the value is not empty
				addErrors( controlNotEmptyValidator(value, fieldSpec).apply() )
                // try to convert to numeric
				Long number
				try {
					NumberFormat nf = NumberFormat.getInstance()
					number = nf.parse(value)
				} catch (ParseException e) {
					// If it's not a number there is not much to do, so return
					addError ( 'field.invalid.NaN', [value, getLabel(), e.getMessage()])
					return
				}
			    // finally, validate the constraints in the fieldSpec
				def minRange = fieldSpec.constraints?.minRange ?: null
				def maxRange = fieldSpec.constraints?.maxRange ?: null
				def decimalPlaces = fieldSpec.constraints?.decimalPlaces ?: null
				def useThousandSeparator = fieldSpec.constraints?.useThousandSeparator ?: false
				def allowNegatives = fieldSpec.constraints?.allowNegatives

				if ((minRange && number < minRange) || (maxRange && number > maxRange)) {
					addError ('field.invalid.valueOutOfRange', [value, getLabel(), minRange, maxRange])
				}
				if (decimalPlaces && value.contains('.')){
					def fractionalPart = value.substring(value.indexOf('.') + 1, value.size())
					if (!fractionalPart.size() > decimalPlaces) {
						addError ('field.invalid.decimalPlacesExceeded', [value, getLabel(), decimalPlaces])
					}
				}
				if (number < 0 && !allowNegatives) {
					addError ('field.invalid.negativesNotAllowed', [value, getLabel()] )
				}
				if (useThousandSeparator) {
					// remove fractional part, if exists
					int integerPart = (int) number
					// if bigger or equal than a thousand and it doesn't have the separator
					if (integerPart.toString().size() > 3 && !value.contains(',')) {
						addError ('field.invalid.thousandSeparatorRequired', [value, getLabel()] )
					}
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
				if (size < minSize || size > maxSize) {
					addError( 'field.invalid.sizeOutOfBounds', [value, getLabel(), minSize, maxSize] )
				}
			}
		}
	}

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
		private String label
		private boolean required
		private Map fieldSpec


		Validator(Map fieldSpec){
			this.fieldSpec = fieldSpec
			field = fieldSpec?.field
			label = fieldSpec?.label
			required = BooleanUtils.toBoolean(fieldSpec?.constraints?.required ?: false)
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
