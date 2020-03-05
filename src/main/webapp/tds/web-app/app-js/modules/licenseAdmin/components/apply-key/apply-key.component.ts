// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Model
import {LicenseModel} from '../../model/license.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {AlertType} from '../../../../shared/model/alert.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {NotifierService} from '../../../../shared/services/notifier.service';
import {LicenseAdminService} from '../../service/license-admin.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import 'rxjs/add/operator/finally';
import {NgForm} from '@angular/forms';

@Component({
	selector: 'tds-license-apply-key',
	templateUrl: 'apply-key.component.html'
})
export class ApplyKeyComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('applyLicenseForm', {read: NgForm, static: true}) applyLicenseForm: NgForm;

	public licenseKey = '';
	private dataSignature = {};
	private licenseModel: LicenseModel;

	constructor(
		private notifierService: NotifierService,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private licenseAdminService: LicenseAdminService) {
		super();
		this.dataSignature = JSON.stringify(this.licenseKey);
	}

	ngOnInit(): void {
		this.licenseModel = Object.assign({}, this.data.licenseModel);

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'applyKey',
			icon: 'key',
			text: 'Apply',
			show: () => true,
			disabled: () => !this.applyLicenseForm.form.valid,
			type: DialogButtonType.CONTEXT,
			action: this.applyKey.bind(this)
		});
	}

	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * Apply the key and close the dialog, also send an info msg
	 */
	public applyKey(): void {
		this.licenseAdminService.applyKey(this.licenseModel.id, this.licenseKey).subscribe((res: any) => {
			let message = '';
			let alertType: AlertType = null;

			if (res) {
				alertType = AlertType.INFO;
				message = 'License was successfully applied';
			} else {
				message = 'License was not applied';
				alertType = AlertType.WARNING;
			}

			this.notifierService.broadcast({
				name: alertType,
				message: message
			});
			this.onAcceptSuccess();
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	private isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.licenseKey);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
