package com.tdsops.validators

import com.tds.asset.AssetOptions
import com.tds.asset.AssetOptions.AssetOptionsType
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.ControlType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.service.AssetOptionsService
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
	static Closure optionsClosure(AssetOptionsType type) {
		return {
			AssetOptionsService assetOptionsService = ApplicationContextHolder.getBean('assetOptionsService', AssetOptionsService)
			assetOptionsService.findAllValuesByType(type)
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
			validatorHandlers[ControlType.DATE.toString()] = CustomValidators.&controlDateValidator
			validatorHandlers[ControlType.DATETIME.toString()] = CustomValidators.&controlDateTimeValidator

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

				Collection<ErrorHolder> errorsHolders = validator(value, fieldSpec, object).apply()

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
	static controlNotEmptyValidator ( String value, Map fieldSpec, Object domain) {
		new Validator ( fieldSpec ) {
			void validate() {
				if ( isRequired() && ! value ) {
					addError ('default.blank.message', [getLabel(), GormUtil.domainShortName(domain)])
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
	static controlYesNoControlValidator ( String value, Map fieldSpec, Object domain) {
		final List<String> yesNoList = ['Yes', 'No']

		new Validator ( fieldSpec ) {
			void validate() {
				// value = StringUtils.defaultString(value)
				addErrors( controlNotEmptyValidator ( value, fieldSpec, domain ).apply() )

				if ( ! hasErrors() && StringUtils.isNotBlank(value) && !yesNoList.contains(value) ) {
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
	static controlListValidator ( String value, Map fieldSpec, Object domain) {
		new Validator ( fieldSpec ) {
			void validate() {
				addErrors( controlNotEmptyValidator ( value, fieldSpec, domain ).apply() )

				def optValues = fieldSpec.constraints?.values ?: []

				if( ! hasErrors() && StringUtils.isNotBlank(value) && ! optValues.contains(value) ) {
					addError ( 'field.invalid.notInList', [value, getLabel(), optValues.join(', ')] )
				}
			}
		}
	}

	/**
	 * Date Time Validator that Checks that the given <code>String</code> value
	 * represents a date, and its format is the same date/time format that
	 * the one in the constraints contained in the fieldSpec Map.
	 * See TM-11723
	 *
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	@CompileStatic
	static  controlDateTimeValidator ( String value, Map fieldSpec, Object domain) {
		new Validator ( fieldSpec ) {
			void validate() {
				// if the field is empty, validate the 'required' constraint
				if (!value) {
					def required = fieldSpec.constraints?.required ?: null
					if (required) {
						addError ('default.blank.message', [getLabel(), GormUtil.domainShortName(domain)])
					}
				} else {
					if (!TimeUtil.canParseDateTime(value)) {
						addError('field.incorrect.dateFormat', [value, getLabel()])
					}
				}
			}
		}
	}

	/**
	 * Date Validator that Checks that the given <code>String</code> value
	 * represents a date, and its format is the ISO 8601 format.
	 * See TM-11723
	 *
	 * @param value
	 * @param fieldSpec
	 * @return
	 */
	@CompileStatic
	static  controlDateValidator ( String value, Map fieldSpec, Object domain) {
		new Validator ( fieldSpec ) {
			void validate() {
				// if the field is empty, validate the 'required' constraint
				if (!value) {
					def required = fieldSpec.constraints?.required ?: null
					if (required) {
						addError ('default.blank.message', [getLabel(), GormUtil.domainShortName(domain)])
					}
				} else {
					if (!TimeUtil.canParseDate(value)) {
						addError('field.incorrect.dateFormat', [value, getLabel()])
					}
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
	static  controlNumberValidator ( String value, Map fieldSpec, Object domain) {
		new Validator ( fieldSpec ) {
			void validate() {
				// if the field is empty, validate the 'required' constraint
				if (!value) {
					def required = fieldSpec.constraints?.required ?: null
					if (required) {
						addError ('default.blank.message', [getLabel(), GormUtil.domainShortName(domain)])
					}
					return // with or without error, as the value is empty, just return
				}
                // try to convert to numeric
				Long number
				try {
					NumberFormat nf = NumberFormat.getInstance()
					// For some reason a value of the form 'n-' is parsed to 'n' by NumberFormat.parse(). Anyway that is not a valid
					// value format (it should be -n) so if that is the case, consider the value as a wrong format value.
					if(value.charAt(value.size()-1) == '-') {
						throw new ParseException('The number format is incorrect', 0)
					}
					number = nf.parse(value)
				} catch (ParseException e) {
					// If it's not a number there is not much to do, so return
					addError ('typeMismatch.java.lang.Long', [getLabel()])
					return
				}
			    // finally, validate the constraints in the fieldSpec
				def minRange = fieldSpec.constraints?.minRange ?: null
				def maxRange = fieldSpec.constraints?.maxRange ?: null
				def precision = fieldSpec.constraints?.precision ?: null
				def allowNegative = fieldSpec.constraints?.allowNegative

				// if 'minRange' or 'maxRange' fields are present, validate the value range
				if ((minRange && number < minRange) || (maxRange && number > maxRange)) {
					addError ('default.invalid.range.message', [getLabel(), null, value, minRange, maxRange])
				}
				// 'allowNegative' only makes sense if 'minRange' is NOT present,
				// if a 'minRange' is present then that value is used for the lower limit and 'allowNegative' is ignored, otherwise:
				if (!minRange && (number < 0 && !allowNegative)) {
					// if 'allowNegative' field is not present and the number is negative, it should error
					addError ('field.invalid.negativeNotAllowed', [value, getLabel()] )
				}
				// if 'precision' field is present and the value has a fractional part, we should check
				// that the count of fractional digits does not exceed the 'precision' value
				if (precision && value.contains('.')){
					def fractionalPart = value.substring(value.indexOf('.') + 1, value.size())
					if (!fractionalPart.size() > precision) {
						addError ('field.invalid.precisionExceeded', [value, getLabel(), precision])
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
	static controlDefaultValidator( String value, Map fieldSpec, Object domain) {
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
