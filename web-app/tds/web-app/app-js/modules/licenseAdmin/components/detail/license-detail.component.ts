// Angular
import {Component, OnInit} from '@angular/core';
// Component
import {ApplyKeyComponent} from '../apply-key/apply-key.component';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {LicenseModel, MethodOptions, LicenseStatus} from '../../model/license.model';
// Other
import {DateUtils} from '../../../../shared/utils/date.utils';

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
		private dialogService: UIDialogService) {
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
}
