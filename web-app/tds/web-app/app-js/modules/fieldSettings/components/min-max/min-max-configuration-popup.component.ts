import {Component, Input, ViewChild, OnInit, Output, EventEmitter} from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/min-max/min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig'
})

export class MinMaxConfigurationPopupComponent implements OnInit {

	@Input() domain: string;
	@Input() field: FieldSettingsModel;
	@Output('onShowPopup') onShowPopupEmitter = new EventEmitter<any>();

	public show = false; // first time should open automatically.
	public model: ConstraintModel;
	public minIsValid = true;

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
		if (this.show) {
			setTimeout( () => {
				this.onShowPopupEmitter.emit();
			}, 200);
		}
	}

	public validateModel(): void {
		this.minIsValid = true;
		if (this.model.minSize > this.model.maxSize || this.model.minSize < 0) {
			this.minIsValid = false;
		}
	}
}