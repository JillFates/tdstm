import {Component, Input, ViewChild, OnInit, Inject} from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NumberConfigurationConstraintsModel} from './number-configuration-constraints.model';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/number/number-configuration-popup.component.html',
})

export class NumberConfigurationPopupComponent {

	protected model: NumberConfigurationConstraintsModel;
	protected outputFormat: string;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		private activeDialog: UIActiveDialogService) {
			this.outputFormat = '';
			this.model = { ...this.field.constraints } as NumberConfigurationConstraintsModel;
			this.initModel();
			this.onFormatChange();
			console.log(this.model);
	}

	/**
	 * Init the model values if is a new number field configuration.
	 */
	private initModel(): void {
		this.model.minRange = this.model.minRange ? this.model.minRange : 0;
		this.model.maxRange = this.model.maxRange ? this.model.maxRange : 1000;
		this.model.decimalPlaces = this.model.decimalPlaces ? this.model.decimalPlaces : 0;
		this.model.useThousandSeparator = this.model.useThousandSeparator ? this.model.useThousandSeparator : false;
		this.model.allowNegatives = this.model.allowNegatives ? this.model.allowNegatives : false;
	}

	/**
	 * On thousandsSeparator, decimalPlaces change recalculate the number format.
	 */
	protected onFormatChange(): void {
		let format = '';
		if (this.model.decimalPlaces > 0 || this.model.useThousandSeparator) {
			format = `n${(this.model.decimalPlaces > 0 ? this.model.decimalPlaces : '')}`;
		}
		this.outputFormat = format;
	}

	/**
	 * On button save click
	 */
	protected onSave(): void {
		this.field.constraints = { ...this.model } as any;
		this.activeDialog.dismiss();
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
