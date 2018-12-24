// Angular
import {ElementRef, Component, OnInit, ViewChild, HostListener } from '@angular/core';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {RequestLicenseModel} from '../../model/license.model';
import {KEYSTROKE} from '../../../../shared/model/constants';

@Component({
	selector: 'tds-license-create',
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/request/request-license.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class RequestLicenseComponent implements OnInit {

	@ViewChild('providerNameElement', {read: ElementRef}) providerNameElement: ElementRef;
	@ViewChild('licenseViewEditContainer') licenseViewEditContainer: ElementRef;
	protected requestLicense = new RequestLicenseModel();
	protected environmentList: any = [];
	protected projectList: any = [];
	private dataSignature: string;

	constructor(
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
		this.dataSignature = JSON.stringify(this.requestLicense);
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveProvider(): void {
		// this.licenseAdminService.saveProvider(this.requestLicense).subscribe(
		// 	(result: any) => {
		// 		this.activeDialog.close(result);
		// 	},
		// 	(err) => console.log(err));
	}

	ngOnInit(): void {
		this.licenseAdminService.getEnvironments().subscribe((environmentList: any) => {
			this.environmentList = environmentList;
			this.requestLicense.environment = this.environmentList[0];
		});
		this.licenseAdminService.getProjects().subscribe((projectList: any) => {
			this.projectList = projectList;
			this.requestLicense.project = this.projectList[0];
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
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					} else {
						this.focusForm();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditProvider(): void {
		this.focusForm();
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteProvider(): void {
		this.prompt.open('Confirmation Required', 'There are associated Datasources. Deleting this will not delete historical imports. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					// this.licenseAdminService.deleteProvider(this.requestLicense.id).subscribe(
					// 	(result) => {
					// 		this.activeDialog.close(result);
					// 	},
					// 	(err) => console.log(err));
				}
			});
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Verify if the Name is Empty
	 * @returns {boolean}
	 */
	protected isEmptyValue(): boolean {
		let term = '';
		if (this.requestLicense.name) {
			term = this.requestLicense.name.trim();
		}
		return term === '';
	}

	private focusForm() {
		this.licenseViewEditContainer.nativeElement.focus();
	}

}
