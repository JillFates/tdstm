import {Component, Inject} from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NumberConfigurationConstraintsModel} from './number-configuration-constraints.model';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';

@Component({
	selector: 'number-configuration-popup',
	templateUrl: 'number-configuration-popup.component.html',
})

export class NumberConfigurationPopupComponent {

	private readonly MIN_EXAMPLE_VALUE = -10000;
	private readonly MAX_EXAMPLE_VALUE = 10000;
	public model: NumberConfigurationConstraintsModel;
	public localMinRange: number;
	public exampleValue: number;
	private hasChanged: boolean;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		private activeDialog: UIActiveDialogService) {
			this.model = { ...this.field.constraints } as NumberConfigurationConstraintsModel;
			this.localMinRange = this.model.isDefaultConfig ? null : this.model.minRange;
			this.buildExampleValue();
	}

	/**
	 * On Min range value changes.
	 */
	public onMinRangeChange($event): void {
		this.model.minRange = ($event === null) ? 0 : $event;
		this.onFormatChange();
	}

	/**
	 * On Allow Negatives changes.
	 * @param $event
	 */
	public onAllowNegativesChange($event): void {
		this.onFormatChange();
	}

	/**
	 * When any configuration changes, recalculate the number format.
	 */
	protected onFormatChange(): void {
		this.hasChanged = true;
		if (this.localMinRange !== null || this.model.maxRange !== null) {
			this.model.allowNegative = false;
		}
		if (this.model.allowNegative === true) {
			this.model.minRange = null;
		} else {
			if ((!this.localMinRange || this.localMinRange === null) && this.model.minRange === null) {
				this.model.minRange = 0;
			}
		}
		this.model.format = NumberControlHelper.buildFormat(this.model);
		this.buildExampleValue();
	}

	private buildExampleValue(): void {
		if (this.model.allowNegative) {
			this.exampleValue = this.MIN_EXAMPLE_VALUE;
		} else if (this.model.maxRange !== null) {
			this.exampleValue = this.model.maxRange;
		} else {
			this.exampleValue = this.MAX_EXAMPLE_VALUE;
		}
	}

	/**
	 * On button save click
	 */
	public onSave(): void {
		delete this.model.isDefaultConfig;
		this.field.constraints = { ...this.model } as any;
		this.activeDialog.close(this.hasChanged);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
