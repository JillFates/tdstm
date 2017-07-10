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

	// static readonly MIN_LIMIT = 0;
	// static readonly MAX_LIMIT = 255;

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
}