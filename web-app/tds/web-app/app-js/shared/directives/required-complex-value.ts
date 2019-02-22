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