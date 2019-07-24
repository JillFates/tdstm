// Angular
import {Component} from '@angular/core';
// Service
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
// Model
import {LicenseModel} from '../../model/license.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {AlertType} from '../../../../shared/model/alert.model';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-license-apply-key',
	templateUrl: 'apply-key.component.html'
})
export class ApplyKeyComponent extends UIExtraDialog {

	public modalOptions: DecoratorOptions;
	public licenseKey = '';
	private dataSignature = {};

	constructor(
		private licenseModel: LicenseModel,
		private notifierService: NotifierService,
		private promptService: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
		super('#licenseApplyKey');
		this.modalOptions = {isFullScreen: false, isResizable: false};
		this.dataSignature = JSON.stringify(this.licenseKey);
	}

	public cancelCloseDialog($event): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Abandon Changes?',
				'You have unsaved changes. Click Confirm to abandon your changes.',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.close();
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
			this.close();
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
