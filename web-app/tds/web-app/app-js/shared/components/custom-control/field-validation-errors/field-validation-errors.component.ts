/**
 * Component used to display the angular form errors reported by the custom field
 */
import {Component, Input} from '@angular/core';
import {pathOr} from 'ramda';
import {DateUtils} from '../../../utils/date.utils';

interface ErrorConstraints {
	required?: boolean;
	min?: number;
	max?: number;
	maxDate?: any;
	minDate?: any;
	notNegative?: boolean;
}
@Component({
	selector: 'tds-custom-validation-errors',
	template: `
		<div class="error" *ngIf="hasErrors">
			<div *ngIf="errors.isRequired">* {{label}} is required</div>
			<div *ngIf="errors.range; let range">
				<span>* Invalid range number</span>
				<div>Min: {{range.min}} Max: {{range.max}}</div>
			</div>
			<div *ngIf="errors.notNegative">* Cannot be negative</div>
			<div *ngIf="errors.maxDate">
			 * Date cannot be greather than: {{errors.maxDate.max | tdsDate:userDateFormat}}
			</div>
			<div *ngIf="errors.minDate">
			 * Date cannot be lower than: {{errors.minDate.min | tdsDate:userDateFormat}}
			</div>
		</div>
	`
})
export class TDSCustomValidationErrorsComponent {
	@Input() label = 'Field';
	@Input() submitted = false;
	@Input() touched = false;
	@Input() valid = false;
	@Input() dirty = false;
	@Input() userDateFormat = DateUtils.PREFERENCE_MIDDLE_ENDIAN;
	@Input('errors') errors: ErrorConstraints = {};

	// Determine if the field has errors
	get hasErrors(): boolean {
		return  (this.submitted &&  !this.valid) || (this.touched && this.dirty && !this.valid);
	}
}
