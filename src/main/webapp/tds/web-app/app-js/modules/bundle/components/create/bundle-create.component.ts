import {Component, ElementRef, HostListener, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {EventModel} from '../../../event/model/event.model';

@Component({
	selector: `bundle-create`,
	templateUrl: 'bundle-create.component.html',
})
export class BundleCreateComponent implements OnInit {
	public managers;
	public rooms;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public bundleModel: BundleModel = null;
	private defaultModel = null;
	private requiredFields = ['name', 'workflowCode'];

	constructor(
		private bundleService: BundleService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private activeDialog: UIActiveDialogService) {
	}

	ngOnInit() {
		this.getModel();
		this.bundleModel = new BundleModel();
		this.defaultModel = {
			name: '',
			description: '',
			fromId: 0,
			toId: 0,
			startTime: '',
			completionTime: '',
			projectManagerId: 0,
			moveManagerId: 0,
			operationalOrder: 1,
			useForPlanning: false,
		};
		this.bundleModel = Object.assign({}, this.defaultModel, this.bundleModel);
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

	private getModel() {
		this.bundleService.getModelForBundleCreate().subscribe((result: any) => {
			let data = result.data;
			this.bundleModel.operationalOrder = 1;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.rooms = data.rooms;
		});
	}

	public saveForm() {
		if (DateUtils.validateDateRange(this.bundleModel.startTime, this.bundleModel.completionTime)) {
			this.bundleService.saveBundle(this.bundleModel).subscribe((result: any) => {
				if (result.status === 'success') {
					this.activeDialog.close();
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
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						this.activeDialog.close();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.close();
		}
	}
}
