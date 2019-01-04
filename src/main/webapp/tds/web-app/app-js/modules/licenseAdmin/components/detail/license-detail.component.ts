// Angular
import {Component, OnInit} from '@angular/core';
// Component
import {ApplyKeyComponent} from '../apply-key/apply-key.component';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
// Model
import {LicenseModel, MethodOptions, LicenseStatus} from '../../model/license.model';
// Other
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AlertType} from '../../../../shared/model/alert.model';
import {ManualRequestComponent} from '../manual-request/manual-request.component';

@Component({
	selector: 'tds-license-detail',
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/detail/license-detail.component.html'
})
export class LicenseDetailComponent implements OnInit {

	protected environmentList: any = [];
	protected projectList: any = [];
	protected dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	protected methodOptions = MethodOptions;
	protected licenseStatus = LicenseStatus;

	constructor(
		public licenseModel: LicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService) {
	}

	ngOnInit(): void {

		this.preferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
			this.dateFormat = dateFormat;
		});

		this.licenseAdminService.getLicense(this.licenseModel.id).subscribe((licenseModel: LicenseModel) => {
			this.licenseModel = licenseModel;
		})
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * Open Apply License Key Dialog
	 */
	protected applyLicenseKey(): void {
		this.dialogService.extra(ApplyKeyComponent,
			[{provide: LicenseModel, useValue: this.licenseModel}],
			false, false)
			.then((result) => {
				//
			})
			.catch(error => console.log('Cancel Apply Key'));
	}

	/**
	 * Open a dialog for a Manual Request
	 */
	protected manuallyRequest(): void {
		this.dialogService.extra(ManualRequestComponent,
			[{provide: LicenseModel, useValue: this.licenseModel}],
			false, false)
			.then((result) => {
				//
			})
			.catch(error => console.log('Cancel Manual Request'));
	}

	/**
	 * Submit again the License in case there was an error on the original creation
	 */
	protected resubmitLicenseRequest(): void {
		this.licenseAdminService.resubmitLicenseRequest(this.licenseModel.id).subscribe(
			(result) => {
				let message = '';
				let alertType: AlertType = null;
				if (result) {
					alertType = AlertType.INFO;
					message = 'Request License was successfully';
				} else {
					message = 'There was an error on the request';
					alertType = AlertType.WARNING;
				}
				this.notifierService.broadcast({
					name: alertType,
					message: message
				});
			},
			(err) => console.log(err));
	}

	/**
	 * Delete the current License
	 */
	protected onDelete(): void {
		this.prompt.open('Confirmation Required', 'You are about to delete the license. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.licenseAdminService.deleteLicense(this.licenseModel.id).subscribe(
						(result) => {
							this.activeDialog.dismiss();
						},
						(err) => console.log(err));
				}
			});
	}
}
