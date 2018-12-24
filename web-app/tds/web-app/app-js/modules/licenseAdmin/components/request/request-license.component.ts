import {ElementRef, Component, OnInit, ViewChild, HostListener } from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {LicenseModel} from '../../model/license.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {LicenseAdminService} from '../../service/license-admin.service';

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
	public providerModel: LicenseModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private isUnique = true;
	private providerName = new Subject<String>();

	constructor(
		public originalModel: LicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private licenseAdminService: LicenseAdminService) {

		this.providerModel = Object.assign({}, this.originalModel);
		this.dataSignature = JSON.stringify(this.providerModel);
		this.providerName.next(this.providerModel.name);
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveProvider(): void {
		// this.licenseAdminService.saveProvider(this.providerModel).subscribe(
		// 	(result: any) => {
		// 		this.activeDialog.close(result);
		// 	},
		// 	(err) => console.log(err));
	}

	ngOnInit(): void {
		this.providerName
			.debounceTime(800)        // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged()   // ignore if next search term is same as previous
			.subscribe(term => {
				if (term) {
					term = term.trim();
				}
				if (term && term !== '') {
					this.providerModel.name = this.providerModel.name.trim();
					// this.licenseAdminService.validateUniquenessProviderByName(this.providerModel).subscribe(
					// 	(result: any) => {
					// 		this.isUnique = result.isUnique;
					// 	},
					// 	(err) => console.log(err));
				}
			});
		setTimeout(() => { // Delay issues on Auto Focus
			if (this.providerNameElement) {
				this.providerNameElement.nativeElement.focus();
			}
		}, 500);
	}

	protected onValidateUniqueness(): void {
		this.providerName.next(this.providerModel.name);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.providerModel);
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
					// this.licenseAdminService.deleteProvider(this.providerModel.id).subscribe(
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
		if (this.providerModel.name) {
			term = this.providerModel.name.trim();
		}
		return term === '';
	}

	private focusForm() {
		this.licenseViewEditContainer.nativeElement.focus();
	}

}
