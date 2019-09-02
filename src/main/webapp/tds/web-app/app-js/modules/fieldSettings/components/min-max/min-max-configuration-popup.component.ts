import {Component, Input, ViewChild, OnInit, Inject} from '@angular/core';
import { FieldSettingsModel, ConstraintModel } from '../../model/field-settings.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ConfigurationCommonComponent} from '../configuration-common/configuration-common.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'min-max-configuration-popup',
	templateUrl: 'min-max-configuration-popup.component.html',
	exportAs: 'minmaxConfig'
})

export class MinMaxConfigurationPopupComponent extends ConfigurationCommonComponent implements OnInit {
	public show = false; // first time should open automatically.
	public model: ConstraintModel;
	public minIsValid = true;
	private hasChanges: boolean;

	constructor(
		public field: FieldSettingsModel,
		@Inject('domain') public domain: string,
		public prompt: UIPromptService,
		public translate: TranslatePipe,
		private activeDialog: UIActiveDialogService) {
		super(prompt, translate);
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
		this.hasChanges = true;
		if (this.model.minSize > this.model.maxSize || this.model.minSize < 0) {
			this.minIsValid = false;
		}
	}

	/**
	 * On button save click
	 */
	public onSave(): void {
		this.displayWarningMessage()
			.then((confirm: boolean) => {
				if (confirm) {
					this.field.constraints = { ...this.model };
					this.activeDialog.close(this.hasChanges);
				}
			});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.hasChanges) {
			this.prompt.open(
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translate.transform('GLOBAL.CONFIRM'),
				this.translate.transform('GLOBAL.CANCEL'),
			).then((confirm: boolean) => {
				if (confirm) {
					this.activeDialog.dismiss();
				}
			});
		} else {
			this.activeDialog.dismiss();
		}
	}
}
