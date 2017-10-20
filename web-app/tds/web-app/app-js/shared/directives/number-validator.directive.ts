/**
 * Created by David Ontiveros on 7/7/2017.
 */

import {Attribute, Directive, forwardRef, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, Validator} from '@angular/forms';
import {ValidationUtils} from '../utils/validation.utils';

/**
 * Custom Validator Directive which extends the validations of an html input number type element.
 * 1. Validates if input value (if given) is a valid number
 * 2. If min attribute is present, then validates if valid value number is greater than min.
 * 3. If max attribute is present, then validates if valid value number is less than max.
 *
 * ## Usage
 *  <input type="number" validateNumber #orderInput="ngModel">
 *
 * Different validations error thrown:
 * - notANumber (Validation 1.)
 * - invalidMin (Validation 2.)
 * - invalidMax (Validation 3.)
 *
 * ## Error Usage
 * 	 <small [hidden]="!orderInput.hasError('notANumber')" class="text-danger">
 */
@Directive({
	selector: '[validateNumber]',
	providers: [
		{ provide: NG_VALIDATORS, useExisting: NumberValidator, multi: true }
	]
})
export class NumberValidator implements Validator {

	@Input('min') min: number;
	@Input('max') max: number;

	validate (control: AbstractControl): { [key: string]: any } {
		let value = control.value;

		// we don't validate against undefined or null fields, for this use 'required' validator on your attribute
		if (!value || value === null) {
			return null;
		}

		if ( !ValidationUtils.isValidNumber(value) ) {
			return { notANumber: true };
		}

		if (this.min && this.min !== null && value < this.min) {
			return { invalidMin: true };
		}

		if (this.max && this.max !== null && value > this.max) {
			return { invalidMax: true };
		}
		return null;
	}
}