// Angular
import {NgForm} from '@angular/forms';
import {Input, ViewChild} from '@angular/core';
// Model
import {FieldSettingsModel} from '../../model/field-settings.model';
import {Dialog, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Observable} from 'rxjs';

export abstract class ConfigurationCommonComponent extends Dialog {
	@Input() data: any;

	@ViewChild('templateForm', {static: false}) protected templateForm: NgForm;

	public field: FieldSettingsModel;

	protected constructor(
		public dialogService: DialogService,
		public translate: TranslatePipe) {
		super();
	}

	/**
	 * Set the Field Settings Model from the Extended Class
	 * @param field
	 */
	protected setField(field: FieldSettingsModel): void {
		this.field = field;
	}

	/**
	 * Display warning message about loosing values if user moves forward
	 * just display the warning when the field is not new
	 * @returns {Observable<any>}
	 */
	protected displayWarningMessage(validationMessage: string): Observable<any> {
		if (this.field.isNew) {
			return Observable.of({confirm: DialogConfirmAction.CONFIRM});
		}

		return this.dialogService.confirm(
			this.translate.transform('GLOBAL.CONFIRM'),
			validationMessage,
		);
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
			this.dialogService.confirm(
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
