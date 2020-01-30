import {Component, Inject, ViewChild} from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NumberConfigurationConstraintsModel} from './number-configuration-constraints.model';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ConfigurationCommonComponent} from '../configuration-common/configuration-common.component';
import {NgForm} from '@angular/forms';

@Component({
	selector: 'number-configuration-popup',
	templateUrl: 'number-configuration-popup.component.html',
})
export class NumberConfigurationPopupComponent extends ConfigurationCommonComponent {
	@ViewChild('templateForm', {static: false}) protected templateForm: NgForm;
	private readonly MIN_EXAMPLE_VALUE = -10000;
	private readonly MAX_EXAMPLE_VALUE = 10000;
	public model: NumberConfigurationConstraintsModel;
	public localMinRange: number;
	public exampleValue: number;
	public minRange: number;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		public activeDialog: UIActiveDialogService,
		public prompt: UIPromptService,
		public translate: TranslatePipe) {
			super(field, activeDialog, prompt, translate);
			this.model = { ...this.field.constraints } as NumberConfigurationConstraintsModel;
			this.localMinRange = this.model.isDefaultConfig ? null : this.model.minRange;
			this.minRange = this.localMinRange;
			this.buildExampleValue();
	}
	/**
	 * Check Validity of the inputs. The sett
	 */
	public checkValidity(): void {
		setTimeout(() => {
			this.templateForm.form.controls.minRange.updateValueAndValidity();
			this.templateForm.form.controls.maxRange.updateValueAndValidity();
		})
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
		if (this.localMinRange !== null || this.model.maxRange !== null) {
			this.model.allowNegative = false;
		}
		if (this.model.allowNegative) {
			this.model.minRange = null;
		} else {
			if (!this.localMinRange && this.model.minRange === null) {
				this.model.minRange = 0;
			}
		}
		this.model.format = NumberControlHelper.buildFormat(this.model);
		this.checkValidity();
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
		this.displayWarningMessage()
			.then((confirm: boolean) => {
				if (confirm) {
					delete this.model.isDefaultConfig;
					this.field.constraints = { ...this.model } as any;
					this.activeDialog.close(this.isDirty());
				}
			});
		}

	/**
	 * Determine if the form has a dirty state
	 */
	isDirty(): boolean {
		return Boolean(this.templateForm && this.templateForm.dirty);
	}
}
