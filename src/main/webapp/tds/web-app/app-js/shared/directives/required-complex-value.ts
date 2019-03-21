/**
 * Provides angular form validations (required) for models which contains the value as a property object
 * Example: { id: 10, description: 'the value to validate is present' }
 * It requires the property name to validate has value (line above it could be "description"
 */

import {Directive, Input} from '@angular/core';
import {NG_VALIDATORS, Validator, AbstractControl} from '@angular/forms';

@Directive({
	selector: '[requiredComplexValue]',
	providers: [
		{	provide: NG_VALIDATORS,
			useExisting: UIRequiredComplexValueDirective,
			multi: true
		}
	]
})
export class UIRequiredComplexValueDirective implements Validator {
	@Input()
	propertyName: string;

	validate(c: AbstractControl) {
		let value = c.value;
		if ((value === null || value === undefined || value === '') || (value && !value[this.propertyName]) ) {
			return {
				propertyName: {
					condition: this.propertyName
				}
			};
		}
		return null;
	}

}