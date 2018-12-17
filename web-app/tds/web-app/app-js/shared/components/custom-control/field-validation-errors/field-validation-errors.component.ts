/**
 * Component used to display the angular form errors reported by the custom field
 */
import {Component, Input} from '@angular/core';
import {pathOr} from 'ramda';

interface ErrorConstraints {
	required?: boolean;
}
// TODO internationalize texts
@Component({
	selector: 'tds-field-validation-errors',
	template: `
		<div class="error" *ngIf="hasErrors">
			<div *ngIf="errors.isRequired">{{label}} is required</div>
			<div *ngIf="errors.range; let range">
				<span>Invalid range number</span>
				<div>Min: {{range.min}} Max: {{range.max}}</div>
			</div>
			<div *ngIf="errors.notNegative">Not could be negative</div>			
		</div>
	`
})
export class TDSFieldValidationErrorsComponent {
	@Input() label = '';
	@Input() submitted = false;
	@Input() valid = false;
	@Input() dirty = false;
	@Input('errors') errors: ErrorConstraints = {};

	// Determine if the field has errors
	get hasErrors(): boolean {
		return  (this.submitted &&  !this.valid) || (this.dirty && !this.valid);
	}
}
