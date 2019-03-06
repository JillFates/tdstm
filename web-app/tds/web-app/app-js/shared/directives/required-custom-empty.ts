/**
 * Provides angular form validations for checking custom empty fields
 * It requires the constant text that represents the empty value, for example 'Select...'
 */

import {Directive, Input} from '@angular/core';
import {NG_VALIDATORS, Validator, AbstractControl} from '@angular/forms';

@Directive({
	selector: '[requiredCustomEmpty]',
	providers: [
		{	provide: NG_VALIDATORS,
			useExisting: UIRequiredCustomEmptyDirective,
			multi: true
		}
	]
})
export class UIRequiredCustomEmptyDirective implements Validator {
	@Input()
	empty: string;

	validate(c: AbstractControl) {
		let value = c.value;
		if ((value === null || value === undefined || value === '' || value === this.empty)) {
			return {
				propertyName: {
					condition: this.empty
				}
			};
		}
		return null;
	}

}