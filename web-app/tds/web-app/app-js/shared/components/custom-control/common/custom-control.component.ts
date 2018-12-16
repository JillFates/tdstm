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

import {fieldRulesFactory} from './field-rules-factory.helper';
import {ObjectUtils} from '../../../utils/object.utils';

export abstract class TDSCustomControl implements ControlValueAccessor {
	// common attributes
	@Input('value') _value: any;
	@Input() tabindex = null;
	@Input() required = false;
	onTouched = () => { /* Default on touched */} ;

	private propagateChange: any = () => { /* Notify changes to host */ };
	private validateFn: any = () => { /* Validator function */};

	get value() {
		return this._value;
	}

	set value(value: any) {
		this._value = value;
		this.propagateChange(this._value);
	}

	private createRulesFactory(factoryType: string) {
		return fieldRulesFactory(factoryType).create();
	}

	setupValidatorFunction(factoryType: string, constraints: any) {
		const rulesFactory = this.createRulesFactory(factoryType);
		this.validateFn =  this.applyRules(rulesFactory(constraints))
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
		return this.validateFn(formControl);
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