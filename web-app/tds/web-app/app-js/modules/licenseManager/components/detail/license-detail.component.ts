// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseManagerService} from '../../service/license-manager.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
// Model
import {LicenseModel, MethodOptions, LicenseStatus} from '../../model/license.model';
// Other
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AlertType} from '../../../../shared/model/alert.model';

@Component({
	selector: 'tds-license-manager-detail',
	templateUrl: '../tds/web-app/app-js/modules/licenseManager/components/detail/license-detail.component.html'
})
export class LicenseDetailComponent implements OnInit {

	protected environmentList: any = [];
	protected projectList: any = [];
	protected dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	protected methodOptions = MethodOptions;
	protected licenseStatus = LicenseStatus;
	protected editMode = false;
	protected pendingLicense = false;
	protected expiredOrTerminated = false;
	protected activeShowMode = false;

	constructor(
		public licenseModel: LicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseManagerService: LicenseManagerService,
		private preferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService) {
	}

	ngOnInit(): void {

		this.preferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
			this.dateFormat = dateFormat;
		});

		this.licenseManagerService.getLicense(this.licenseModel.id).subscribe((licenseModel: LicenseModel) => {
			this.licenseModel = licenseModel;
			this.prepareControlActionButtons();
		})
	}

	/**
	 * Controls buttons to show
	 */
	protected prepareControlActionButtons(): void {
		this.pendingLicense = this.licenseModel.status === 'PENDING' && !this.editMode;
		this.expiredOrTerminated = (this.licenseModel.status === 'EXPIRED' || this.licenseModel.status === 'TERMINATED');
		this.activeShowMode = this.licenseModel.status === 'ACTIVE' && !this.expiredOrTerminated && !this.editMode;
	}

	/**
	 * Change the status to Edit
	 */
	protected enterEditMode(): void {
		this.editMode = true;
		this.prepareControlActionButtons();
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * Validate interger only
	 * @param event
	 * @param model
	 */
	protected validateIntegerOnly(event: any, model: any): void {
		try {
			let newVal = parseInt(model, 2);
			if (!isNaN(newVal)) {
				model = newVal;
			} else {
				model = 0;
			}
			if (event && event.currentTarget) {
				event.currentTarget.value = model;
			}
		} catch (e) {
			console.warn('Invalid Number Exception', model);
		}
	}

	/**
	 * Submit again the License in case there was an error on the original creation
	 */
	protected resubmitLicenseRequest(): void {
		this.licenseManagerService.resubmitLicenseRequest(this.licenseModel.id).subscribe(
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
					this.licenseManagerService.deleteLicense(this.licenseModel.id).subscribe(
						(result) => {
							this.activeDialog.dismiss();
						},
						(err) => console.log(err));
				}
			});
	}
}
