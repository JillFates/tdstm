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
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/apply-key/apply-key.component.html'
})
export class ApplyKeyComponent extends UIExtraDialog {

	protected modalOptions: DecoratorOptions;
	private dataSignature = {};

	constructor(
		private licenseModel: LicenseModel,
		private notifierService: NotifierService,
		private promptService: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
		super('#licenseApplyKey');
		this.modalOptions = {isFullScreen: false, isResizable: false};
		this.dataSignature = JSON.stringify(this.licenseModel);
	}

	protected cancelCloseDialog($event): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
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
	protected applyKey(): void {
		this.licenseAdminService.applyKey(this.licenseModel.id, this.licenseModel.key).subscribe((res: any) => {
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
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	private isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.licenseModel);
	}

}
