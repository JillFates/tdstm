import { Component, Input, ViewChild, OnInit } from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';
import {AllowOnlyNumbersDirective} from '../../../../shared/directives/allow-only-numbers.directive';

@Component({
	moduleId: module.id,
	selector: 'min-max-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/min-max/min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig',
	providers: [AllowOnlyNumbersDirective ]
})

export class MinMaxConfigurationPopupComponent implements OnInit {

	@Input() domain: string;
	@Input() field: FieldSettingsModel;

	public show = false; // first time should open automatically.
	public model: ConstraintModel;

	//static readonly MIN_LIMIT = 0;
	//static readonly MAX_LIMIT = 255;

	public onSave(): void {
		this.field.constraints = { ...this.model };
		this.onToggle();
	}

	ngOnInit(): void {
		this.model = { ...this.field.constraints };
		this.model.maxSize = this.model.maxSize || 255;
		if (this.model.required) {
			this.model.minSize = this.model.minSize || 1;
		}
	}

	public onToggle(): void {
		this.show = !this.show;
	}

	/**
	 * TODO: remove this code
	 * @param event
	 * @returns {boolean}
	 */
	/*
	private validateIsANumberValue(event): boolean {
		if ( isNaN(event.target.valueAsNumber) ) {
			return false;
		}
		return true;
	}
	*/

	/**
	 * TODO: remove this code
	 * - Validates {model.minSize} should not be greater than {model.maxSize}
	 * - Validates {model.minSize} should not be less than 0 (MIN)
	 * - Validates {model.minSize} should not be greater than 255 (MAX)
	 */
	/*
	public validateMinSize(event): void {
		if (this.validateIsANumberValue(event) ) {
			let min = event.target.valueAsNumber;
			if (min > this.model.maxSize
			|| min < MinMaxConfigurationPopupComponent.MIN_LIMIT
			|| min > MinMaxConfigurationPopupComponent.MAX_LIMIT) {
				// do nothing since we want to skip changing model from input
			} else {
				this.model.minSize = min;
			}
		}
		event.target.value = this.model.minSize;
	}
	*/

	/**
	 * TODO: remove this code
	 * - Validates {model.maxSize} should not be less than {model.minSize}
	 * - Validates {model.maxSize} should not be greater than 255 (MAX)
	 * - Validates {model.maxSize} should not be less than 0 (MIN)
	 */
	/*
	public validateMaxSize(event): void {
		if (this.validateIsANumberValue(event) ) {
			let max = event.target.valueAsNumber;
			if (max < this.model.minSize
			|| max > MinMaxConfigurationPopupComponent.MAX_LIMIT
			|| max < MinMaxConfigurationPopupComponent.MIN_LIMIT) {
				// do nothing since we want to skip changing model from input
			} else {
				this.model.maxSize = max;
			}
		}
		event.target.value = this.model.maxSize;
	}
	*/
}