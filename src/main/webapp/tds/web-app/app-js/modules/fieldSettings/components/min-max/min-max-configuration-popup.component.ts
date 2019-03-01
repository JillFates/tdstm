import {Component, Input, ViewChild, OnInit, Inject} from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: 'min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig'
})

export class MinMaxConfigurationPopupComponent implements OnInit {

	public show = false; // first time should open automatically.
	public model: ConstraintModel;
	public minIsValid = true;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		private activeDialog: UIActiveDialogService) {
	}

	ngOnInit(): void {
		this.model = { ...this.field.constraints };
		this.model.maxSize = this.model.maxSize || 255;
		if (this.model.required) {
			this.model.minSize = this.model.minSize || 1;
		}
	}

	/**
	 * Validates the form
	 */
	public validateModel(): void {
		this.minIsValid = true;
		if (this.model.minSize > this.model.maxSize || this.model.minSize < 0) {
			this.minIsValid = false;
		}
	}

	/**
	 * On button save click
	 */
	public onSave(): void {
		this.field.constraints = { ...this.model };
		this.activeDialog.dismiss();
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
