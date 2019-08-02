// Angular
import {Component} from '@angular/core';
// Service
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseManagerService} from '../../service/license-manager.service';
// Model
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {AlertType} from '../../../../shared/model/alert.model';
// Other
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-request-import-license',
	templateUrl: 'request-import.component.html'
})
export class RequestImportComponent {

	protected modalOptions: DecoratorOptions;
	private dataSignature = {};
	public licenseKey = '';

	constructor(
		private notifierService: NotifierService,
		private promptService: UIPromptService,
		private licenseManagerService: LicenseManagerService,
		private translatePipe: TranslatePipe,
		public activeDialog: UIActiveDialogService) {
		this.modalOptions = {isFullScreen: false, isResizable: false};
		this.dataSignature = JSON.stringify(this.licenseKey);
	}

	public cancelCloseDialog($event): void {
		if (this.isDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Import the key and close the dialog, also send an info msg
	 */
	public requestImportLicense(): void {
		this.licenseManagerService.requestImportLicense(this.licenseKey).subscribe((res: any) => {
			let message = '';
			let alertType: AlertType = null;

			if (res) {
				alertType = AlertType.INFO;
				message = 'License was successfully Imported';
			} else {
				message = 'License was not applied. Review the provided License Key is correct.';
				alertType = AlertType.WARNING;
			}

			this.notifierService.broadcast({
				name: alertType,
				message: message
			});
			this.activeDialog.close(res);
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	private isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.licenseKey);
	}
}
