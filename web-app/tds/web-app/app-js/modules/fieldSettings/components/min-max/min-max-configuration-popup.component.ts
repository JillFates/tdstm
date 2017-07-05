import { Component, Input, ViewChild, OnInit } from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';

@Component({
	moduleId: module.id,
	selector: 'min-max-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/min-max/min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig'
})

export class MinMaxConfigurationPopupComponent implements OnInit {

	@Input() domain: string;
	@Input() field: FieldSettingsModel;

	public show = false; // first time should open automatically.
	public model: ConstraintModel;

	static readonly MIN = 0;
	static readonly MAX = 255;

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
	 * - Validates {model.minSize} should not be greater than {model.maxSize}
	 * - Validates {model.minSize} should not be less than 0 (MIN)
	 * - Validates {model.minSize} should not be greater than 255 (MAX)
	 */
	public validateMinSize(): void {
		if (this.model.minSize > this.model.maxSize) {
			this.model.minSize = MinMaxConfigurationPopupComponent.MIN;
		} else if (this.model.minSize < MinMaxConfigurationPopupComponent.MIN) {
			this.model.minSize = MinMaxConfigurationPopupComponent.MIN;
		} else if (this.model.minSize > MinMaxConfigurationPopupComponent.MAX) {
			this.model.minSize = MinMaxConfigurationPopupComponent.MAX;
		}
	}

	/**
	 * - Validates {model.maxSize} should not be less than {model.minSize}
	 * - Validates {model.maxSize} should not be greater than 255 (MAX)
	 * - Validates {model.maxSize} should not be less than 0 (MIN)
	 */
	public validateMaxSize(): void {
		if (this.model.maxSize < this.model.minSize) {
			this.model.maxSize = MinMaxConfigurationPopupComponent.MAX;
		} else if (this.model.maxSize > MinMaxConfigurationPopupComponent.MAX) {
			this.model.maxSize = MinMaxConfigurationPopupComponent.MAX;
		} else if (this.model.maxSize < MinMaxConfigurationPopupComponent.MIN) {
			this.model.maxSize = MinMaxConfigurationPopupComponent.MIN;
		}
	}

	/**
	 * Component validation:
	 * - Validates minSize & maxSize
	 */
	public validate(): void {
		this.validateMinSize();
		this.validateMaxSize();
	}
}