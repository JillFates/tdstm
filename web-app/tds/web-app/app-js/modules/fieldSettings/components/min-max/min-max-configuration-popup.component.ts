import { Component, Input, ViewChild, OnInit } from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';

@Component({
	moduleId: module.id,
	selector: 'min-max-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/min-max/min-max-configuration-popup.component.html',
	styles: [``]
})

export class MinMaxConfigurationPopupComponent implements OnInit {

	@Input() domain: string;
	@Input() field: FieldSettingsModel;

	public show = true; // first time should open automatically.
	public model: ConstraintModel;

	public onSave(): void {
		this.field.constraints = { ...this.model };
		this.onToggle();
	}

	ngOnInit(): void {
		this.model = { ...this.field.constraints };
	}

	public onToggle(): void {
		this.show = !this.show;
	}
}