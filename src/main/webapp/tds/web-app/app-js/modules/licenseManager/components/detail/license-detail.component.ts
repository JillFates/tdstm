// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseManagerService} from '../../service/license-manager.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
// Model
import {LicenseModel, MethodOptions, LicenseStatus, LicenseActivityColumnModel} from '../../model/license.model';
// Other
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AlertType} from '../../../../shared/model/alert.model';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {SortUtils} from '../../../../shared/utils/sort.utils';

@Component({
	selector: 'tds-license-manager-detail',
	templateUrl: '../tds/web-app/app-js/modules/licenseManager/components/detail/license-detail.component.html'
})
export class LicenseDetailComponent implements OnInit {

	private dataSignature: string;
	protected environmentList: any = [];
	protected projectList: any = [];
	protected dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	protected methodOptions = MethodOptions;
	protected licenseStatus = LicenseStatus;
	protected editMode = false;
	protected pendingLicense = false;
	protected expiredOrTerminated = false;
	protected activeShowMode = false;
	protected range = { start: null, end: null };
	protected licenseKey = 'Licenses has not been issued';
	protected reloadRequired = false;
	protected activityLog = [];
	protected licenseActivityColumnModel = new LicenseActivityColumnModel();
	protected COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	protected userTimeZone: string;

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
		this.getEnvironmentData();

		this.preferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
			this.dateFormat = dateFormat;
			this.userTimeZone = this.preferenceService.getUserTimeZone();
		});

		this.licenseManagerService.getLicense(this.licenseModel.id).subscribe((licenseModel: any) => {
			this.licenseModel = licenseModel;
			this.licenseModel.activationDate = DateUtils.toDateUsingFormat(licenseModel.activationDate, DateUtils.SERVER_FORMAT_DATE);
			this.licenseModel.expirationDate = DateUtils.toDateUsingFormat(licenseModel.expirationDate, DateUtils.SERVER_FORMAT_DATE);
			this.dataSignature = JSON.stringify(this.licenseModel);
			this.prepareControlActionButtons();
			this.prepareLicenseKey();
			this.getActivityLog();
		});
	}

	/**
	 *  Get the List on Environments
	 */
	private getEnvironmentData(): void {
		this.licenseManagerService.getEnvironments().subscribe((res: any) => {
			this.environmentList = res;
		});
	}

	private prepareLicenseKey(): void {
		if (this.licenseModel.status === 'ACTIVE') {
			this.licenseManagerService.getKeyCode(this.licenseModel.id).subscribe((licenseKey: any) => {
				this.licenseKey = licenseKey;
			});
		}
	}

	private getActivityLog(): void {
		this.licenseManagerService.getActivityLog(this.licenseModel.id).subscribe((activityLog: any) => {
			this.activityLog = activityLog.sort((a, b) => SortUtils.compareByProperty(a, b, 'dateCreated'));
		});
	}

	/**
	 * Save the current status of the License
	 */
	protected saveLicense(): void {
		this.licenseManagerService.saveLicense(this.licenseModel).subscribe((license: any) => {
			this.editMode = false;
			this.prepareControlActionButtons();
		});
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
	 * Does the activation of the current license if this is not active
	 */
	protected activateLicense(): void {
		this.licenseManagerService.activateLicense(this.licenseModel.id).subscribe((res: any) => {
			let message = '';
			let alertType: AlertType = null;

			if (res === 'Ok') {
				alertType = AlertType.INFO;
				message = 'The license was activated and the license was emailed.';

				this.licenseModel.status = 'ACTIVE';
				this.prepareControlActionButtons();
				this.prepareLicenseKey();
			} else {
				message = 'License was not activated. ' + res;
				alertType = AlertType.WARNING;
			}

			this.notifierService.broadcast({
				name: alertType,
				message: message
			});
		});
	}

	/**
	 * Manually Invoke the request
	 */
	protected manuallyRequest(): void {
		this.licenseManagerService.activateLicense(this.licenseModel.id).subscribe((res: any) => {
			let message = '';
			let alertType: AlertType = null;

			if (res === 'Ok') {
				alertType = AlertType.INFO;
				message = 'Email License was successfully';
			} else {
				message = res;
				alertType = AlertType.WARNING;
			}

			this.notifierService.broadcast({
				name: alertType,
				message: message
			});
		})
	}

	/**
	 * Revoke the License
	 */
	protected revokeLicense(): void {
		this.prompt.open('Confirmation Required', 'Are you sure you want to revoke it? This action cannot be undone.', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.licenseManagerService.revokeLicense(this.licenseModel.id).subscribe((res: any) => {
						this.activeDialog.close();
					});
				}
			});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.editMode) {
			this.editMode = false;
			this.licenseModel = JSON.parse(this.dataSignature);
			this.prepareControlActionButtons();
		} else if (this.reloadRequired) {
			this.activeDialog.close();
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Delete the current License
	 */
	protected onDelete(): void {
		this.prompt.open('Confirmation Required', 'You are about to delete the selected license. Are you sure? Click Confirm to delete otherwise press Cancel.', 'Yes', 'No')
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
