// Angular
import {Component, ViewChild, OnInit, Inject} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {
	FieldSettingsModel,
	ConstraintModel,
} from '../../model/field-settings.model';
import {DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Component
import {ConfigurationCommonComponent} from '../configuration-common/configuration-common.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: 'min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig',
})
export class MinMaxConfigurationPopupComponent extends ConfigurationCommonComponent implements OnInit {
	@ViewChild('templateForm', {static: false})
	protected templateForm: NgForm;
	public show = false; // first time should open automatically.
	public model: ConstraintModel;
	public minIsValid = true;

	public field: FieldSettingsModel;
	public domain: string;

	constructor(
		public dialogService: DialogService,
		public translate: TranslatePipe
	) {
		super(dialogService, translate);
	}

	ngOnInit(): void {
		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			disabled: () => !this.isDirty() || (!this.templateForm.valid && !this.minIsValid),
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.field = this.data.fieldSettingsModel;
		this.domain = this.data.domain;

		this.setField(this.field);

		setTimeout(() => {
			this.setTitle(`String Configuration - ${this.field.field}`);
		});

		this.model = {...this.field.constraints};
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
		this.displayWarningMessage(this.translate.transform('FIELD_SETTINGS.WARNING_VALIDATION_CHANGE_RANGE')).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				this.field.constraints = {...this.model};
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
