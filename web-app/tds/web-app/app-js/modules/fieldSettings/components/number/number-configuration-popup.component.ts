import {Component, Input, ViewChild, OnInit, Inject} from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NumberConfigurationConstraintsModel} from './number-configuration-constraints.model';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';

@Component({
	selector: 'number-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/number/number-configuration-popup.component.html',
})

export class NumberConfigurationPopupComponent {

	protected model: NumberConfigurationConstraintsModel;
	protected outputFormat: string;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		private activeDialog: UIActiveDialogService) {
			this.model = { ...this.field.constraints } as NumberConfigurationConstraintsModel;
			NumberControlHelper.initConfiguration(this.model);
			this.outputFormat = NumberControlHelper.buildFormat(this.model);
	}

	/**
	 * On thousandsSeparator, decimalPlaces change recalculate the number format.
	 */
	protected onFormatChange(): void {
		this.outputFormat = NumberControlHelper.buildFormat(this.model);
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
