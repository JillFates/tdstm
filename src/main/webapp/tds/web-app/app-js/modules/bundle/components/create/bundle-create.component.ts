import {Component, ElementRef, HostListener, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';
import {Router} from '@angular/router';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {KEYSTROKE} from '../../../../shared/model/constants';

@Component({
	selector: `bundle-create`,
	templateUrl: 'bundle-create.component.html',
})
export class BundleCreateComponent implements OnInit {
	public managers;
	public workflowCodes;
	public rooms;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public bundleModel: BundleModel = null;
	private defaultModel = null;

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
			workflowCode: 'STD_PROCESS',
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
			this.workflowCodes = data.workflowCodes;
			this.rooms = data.rooms;
		});
	}

	public saveForm() {
		if (this.validateTimes(this.bundleModel.startTime, this.bundleModel.completionTime)) {
			this.bundleService.saveBundle(this.bundleModel).subscribe((result: any) => {
				if (result.status === 'success') {
					this.activeDialog.close();
				}
			});
		}
	}

	private validateTimes(startTime: Date, completionTime: Date): boolean {
		if (!startTime || !completionTime) {
			return true;
		} else if (startTime > completionTime) {
			alert('The completion time must be later than the start time.');
			return false;
		} else {
			return true;
		}
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