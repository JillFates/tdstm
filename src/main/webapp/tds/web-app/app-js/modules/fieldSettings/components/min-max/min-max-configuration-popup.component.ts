import { Component, Input, ViewChild, OnInit, Inject } from '@angular/core';
import {
	FieldSettingsModel,
	ConstraintModel,
} from '../../model/field-settings.model';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { ConfigurationCommonComponent } from '../configuration-common/configuration-common.component';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { NgForm } from '@angular/forms';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: 'min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig',
})
export class MinMaxConfigurationPopupComponent
	extends ConfigurationCommonComponent
	implements OnInit {
	@ViewChild('templateForm', { static: false })
	protected templateForm: NgForm;
	public show = false; // first time should open automatically.
	public model: ConstraintModel;
	public minIsValid = true;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		public prompt: UIPromptService,
		public translate: TranslatePipe,
		public activeDialog: UIActiveDialogService
	) {
		super(field, activeDialog, prompt, translate);
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
		this.displayWarningMessage().then((confirm: boolean) => {
			if (confirm) {
				this.field.constraints = { ...this.model };
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

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
