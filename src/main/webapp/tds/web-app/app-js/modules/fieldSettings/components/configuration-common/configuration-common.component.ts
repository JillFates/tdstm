import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {NgForm} from '@angular/forms';
import {ViewChild} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {FieldSettingsModel} from '../../model/field-settings.model';

export abstract class ConfigurationCommonComponent {
	@ViewChild('templateForm', {static: false}) protected templateForm: NgForm;

	constructor(
		public field: FieldSettingsModel,
		public activeDialog: UIActiveDialogService,
		public prompt: UIPromptService,
		public translate: TranslatePipe) {
		/* constructor */
	}

	/**
	 * Display warning message about loosing values if user moves forward
	 * just display the warning when the field is not new
	 * @returns {Promise<boolean>}
	 */
	protected displayWarningMessage(): Promise<boolean> {
		if (this.field.isNew) {
			return Promise.resolve(true);
		}

		return this.prompt.open(
			this.translate.transform('GLOBAL.CONFIRM'),
			this.translate.transform('FIELD_SETTINGS.WARNING_VALIDATION_CHANGE'),
			this.translate.transform('GLOBAL.CONFIRM'),
			this.translate.transform('GLOBAL.CANCEL'));
	}

	/**
	 * Get the dirty state
	 */
	abstract isDirty(): boolean;

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
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
