/**
 * Component used to provide angular form binding and validation capabilities to custom controls
 * - Group properties common across all controls, like required, tabIndex
 * - Relying on the field rules factory, gets the corresponding validation rules based on the component type
 */
import {Input} from '@angular/core';
import {
	ControlValueAccessor,
	FormControl
} from '@angular/forms'

import {ObjectUtils} from '../../../utils/object.utils';
import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';

export abstract class TDSCustomControl implements ControlValueAccessor {
	// common attributes
	@Input('value') _value: any;
	@Input() tabindex = null;
	@Input() required = false;
	onTouched = () => { /* Default on touched */} ;

	private propagateChange: any = () => { /* Notify changes to host */ };
	private validateFunction: any = () => { /* Validator function */};

	constructor(protected validationRulesFactory: ValidationRulesFactoryService) {

	}

	get value() {
		return this._value;
	}

	set value(value: any) {
		this._value = value;
		this.propagateChange(this._value);
	}

	/**
	 * Based on the component type, setup the function to validate the changes on the component value
	 * Propagates the change to the ControlValueAccessor
	 * @param factoryType - Type of custom control
	 * @param constraints - Object containing the constraints to apply
	 */
	setupValidatorFunction(factoryType: CUSTOM_FIELD_TYPES, constraints: any) {
		const rulesFactory = this.validationRulesFactory.createFieldRulesFactory(factoryType);
		this.validateFunction =  this.applyRules(rulesFactory(constraints))
		this.propagateChange(this.value);
	}

	writeValue(value: any) {
		if (value !== undefined) {
			this.value = value;
		}
	}

	registerOnChange(fn: any) {
		this.propagateChange = fn;
	}

	registerOnTouched(fn: any) {
		/* Notify this controls was touched */
		this.onTouched = fn;
	}

	validate(formControl: FormControl) {
		return this.validateFunction(formControl);
	}

	/**
	 * Apply a set of validation rules
	 * Returns the curried function used by the angular forms validation function
	 * @param rulesToApply - Array of validations rules to apply
	 * @return The resulting error object, it there is no errors returns null
	 */
	private applyRules(rulesToApply: Function[]) {
		return (formControl: FormControl) => {
			const error = rulesToApply
				.reduce((accumulator: any, ruleValidator: Function) =>
						({...accumulator, ...ruleValidator(formControl)()}), {});

			return ObjectUtils.isEmpty(error) ? null : error;
		}
	}
}