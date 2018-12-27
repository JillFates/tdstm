// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {LicenseModel, RequestLicenseModel, MethodOptions, LicenseStatus} from '../../model/license.model';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-license-detail',
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/detail/license-detail.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class LicenseDetailComponent implements OnInit {

	protected requestLicense = new RequestLicenseModel();
	protected environmentList: any = [];
	protected projectList: any = [];
	protected dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	protected methodOptions = MethodOptions;
	protected licenseStatus = LicenseStatus;
	private dataSignature: string;

	constructor(
		public licenseModel: LicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService) {
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
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.requestLicense);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
