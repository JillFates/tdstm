// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {FieldSettingsModel} from '../../model/field-settings.model';
import {NumberConfigurationConstraintsModel} from './number-configuration-constraints.model';
import {DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Component
import {ConfigurationCommonComponent} from '../configuration-common/configuration-common.component';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'number-configuration-popup',
	templateUrl: 'number-configuration-popup.component.html',
})
export class NumberConfigurationPopupComponent extends ConfigurationCommonComponent implements OnInit {
	@ViewChild('templateForm', {static: false}) protected templateForm: NgForm;
	private readonly MIN_EXAMPLE_VALUE = -10000;
	private readonly MAX_EXAMPLE_VALUE = 10000;
	public model: NumberConfigurationConstraintsModel;
	public localMinRange: number;
	public exampleValue: number;
	public minRange: number;

	public field: FieldSettingsModel;
	public domain: string;

	constructor(
		public activeDialog: UIActiveDialogService,
		public dialogService: DialogService,
		public translate: TranslatePipe) {
		super(dialogService, translate);
	}

	ngOnInit(): void {
		this.buttons.push({
			name: 'save',
			icon: 'check',
			text: 'Ok',
			disabled: () => !this.templateForm.valid || !this.isDirty(),
			type: DialogButtonType.CONTEXT,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			text: 'Cancel',
			type: DialogButtonType.CONTEXT,
			action: this.cancelCloseDialog.bind(this)
		});

		this.field = this.data.fieldSettingsModel;
		this.domain = this.data.domain;

		this.setField(this.field);

		setTimeout(() => {
			this.setTitle(`Number Configuration - ${this.field.field}`);
		});

		this.model = {...this.field.constraints} as NumberConfigurationConstraintsModel;
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
		this.displayWarningMessage(this.translate.transform('FIELD_SETTINGS.WARNING_VALIDATION_CHANGE_RANGE')).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				delete this.model.isDefaultConfig;
				this.field.constraints = {...this.model} as any;
				this.onAcceptSuccess({isDirty: this.isDirty()});
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
