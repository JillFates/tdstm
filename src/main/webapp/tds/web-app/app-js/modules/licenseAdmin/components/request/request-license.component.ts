// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
// Model
import {RequestLicenseModel} from '../../model/license.model';
// Other
import {Observable} from 'rxjs';

@Component({
	selector: 'tds-license-create',
	templateUrl: 'request-license.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class RequestLicenseComponent implements OnInit {
	public requestLicense = new RequestLicenseModel();
	public environmentList: any = [];
	public projectList: any = [];
	private dataSignature: string;

	constructor(
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
	}

	/**
	 * Create Edit a Provider
	 */
	public onCreateRequestLicense(): void {
		this.licenseAdminService.createRequestLicense(this.requestLicense).subscribe( (newLicense: any) => {
			this.activeDialog.close(this.requestLicense);
		});
	}

	ngOnInit(): void {
		Observable.forkJoin(this.licenseAdminService.getEnvironments(), this.licenseAdminService.getProjects()).subscribe( (res: any) => {
			this.environmentList = res[0];
			this.requestLicense.environment = this.environmentList[0];
			this.projectList = res[1];
			this.requestLicense.project = this.projectList[0];
			this.dataSignature = JSON.stringify(this.requestLicense);
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.requestLicense);
	}

	/**
	 * Change the client based on the selected project
	 * @param selectedProject
	 */
	public onProjectSelect(selectedProject: any): void {
		this.requestLicense.clientName = selectedProject.client.name;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
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
}
