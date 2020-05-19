// Angular
import {Component, Input, OnInit, ViewChild, ElementRef} from '@angular/core';
// Model
import {BundleModel} from '../../model/bundle.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {BundleService} from '../../service/bundle.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `bundle-create`,
	templateUrl: 'bundle-create.component.html',
})
export class BundleCreateComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('bundleCreateNameInput', {static: false}) bundleCreateNameInput: ElementRef;
	public rooms;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public bundleModel: BundleModel = null;
	private defaultModel = null;
	private requiredFields = ['name'];

	constructor(
		private bundleService: BundleService,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit() {

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => true,
			disabled: () => !this.validateRequiredFields(this.bundleModel),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.getModel();
		this.bundleModel = new BundleModel();
		this.defaultModel = {
			name: '',
			description: '',
			fromId: 0,
			toId: 0,
			startTime: '',
			completionTime: '',
			operationalOrder: 1,
			useForPlanning: true,
		};
		this.bundleModel = Object.assign({}, this.defaultModel, this.bundleModel);
		setTimeout(() => {
			this.onSetUpFocus(this.bundleCreateNameInput);
		});
	}

	private getModel() {
		this.bundleService.getModelForBundleCreate().subscribe((result: any) => {
			let data = result.data;
			this.bundleModel.operationalOrder = 1;
			this.rooms = data.rooms;
		});
	}

	public saveForm() {
		const validateDate = DateUtils.validateDateRange(this.bundleModel.startTime, this.bundleModel.completionTime);
		if (!validateDate) {
			this.dialogService.notify(
				'Validation Required',
				'The completion time must be later than the start time.'
			).subscribe();
		} else {
			this.bundleService.saveBundle(this.bundleModel).subscribe((result: any) => {
				if (result.status === 'success') {
					this.onAcceptSuccess();
				}
			});
		}
	}

	/**
	 * Validate required fields before saving model
	 * @param model - The model to be saved
	 */
	public validateRequiredFields(model: BundleModel): boolean {
		let returnVal = true;
		this.requiredFields.forEach((field) => {
			if (!model[field]) {
				returnVal = false;
				return false;
			} else if (typeof model[field] === 'string' && !model[field].replace(/\s/g, '').length) {
				returnVal = false;
				return false;
			}
		});
		return returnVal;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.defaultModel)) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						this.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
