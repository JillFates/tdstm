// Angular
import {Component, Input, OnInit, ViewChild, ElementRef} from '@angular/core';
// Model
import {RequestLicenseModel} from '../../model/license.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Service
import {LicenseAdminService} from '../../service/license-admin.service';
// Other
import {Observable} from 'rxjs';
import {NgForm} from '@angular/forms';

@Component({
	selector: 'tds-license-create',
	templateUrl: 'request-license.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class RequestLicenseComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('licenseRequestForm', {read: NgForm, static: true}) licenseRequestForm: NgForm;
	@ViewChild('contactEmailInput', {static: false}) contactEmailInput: ElementRef;
	public requestLicense = new RequestLicenseModel();
	public environmentList: any = [];
	public projectList: any = [];
	private dataSignature: string;

	constructor(
		public dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private licenseAdminService: LicenseAdminService
	) {
		super();
	}

	ngOnInit(): void {

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.licenseRequestForm.form.valid,
			type: DialogButtonType.ACTION,
			action: this.onCreateRequestLicense.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		Observable.forkJoin(this.licenseAdminService.getEnvironments(), this.licenseAdminService.getProjects()).subscribe((res: any) => {
			this.environmentList = res[0];
			this.requestLicense.environment = this.environmentList[0];
			this.projectList = res[1];
			this.requestLicense.project = this.projectList[0];
			this.dataSignature = JSON.stringify(this.requestLicense);
		});
		setTimeout(() => {
			this.onSetUpFocus(this.contactEmailInput);
		});
	}

	/**
	 * On Create Request Success
	 */
	public onCreateRequestLicense(): void {
		this.licenseAdminService.createRequestLicense(this.requestLicense).subscribe((newLicense: any) => {
			let data = {
				requestLicense: this.requestLicense
			};
			this.onAcceptSuccess(data);
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
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
