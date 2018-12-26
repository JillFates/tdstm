// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
// Model
import {LicenseModel, RequestLicenseModel} from '../../model/license.model';

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
	private dataSignature: string;

	constructor(
		public licenseModel: LicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
	}

	/**
	 * Create Edit a Provider
	 */
	protected onCreateRequestLicense(): void {
		this.licenseAdminService.createRequestLicense(this.requestLicense).subscribe( (newLicense: any) => {
			this.activeDialog.close(this.requestLicense);
		});
	}

	ngOnInit(): void {
		this.licenseAdminService.getLicense(this.licenseModel.id).subscribe((licenseModel: LicenseModel) => {
			console.log(licenseModel)
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
