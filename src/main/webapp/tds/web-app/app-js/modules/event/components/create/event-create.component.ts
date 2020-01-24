import {Component, HostListener, OnInit} from '@angular/core';
import {EventsService} from '../../service/events.service';
import {EventModel} from '../../model/event.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {KEYSTROKE} from '../../../../shared/model/constants';

@Component({
	selector: `event-create`,
	templateUrl: 'event-create.component.html',
})
export class EventCreateComponent implements OnInit {
	public bundles: any[] = [];
	public runbookStatuses: string[] = [];
	public assetTags: any[] = [];
	public showSwitch = false;
	public showClearButton = false;
	public eventModel: EventModel = null;
	private defaultModel = null;
	private requiredFields = ['name'];

	constructor(
		private eventsService: EventsService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private activeDialog: UIActiveDialogService) {
	}

	ngOnInit() {
		this.getModel();
		this.eventModel = new EventModel();
		this.defaultModel = {
			name: '',
			description: '',
			tagIds: [],
			moveBundle: [],
			runbookStatus: '',
			runbookBridge1: '',
			runbookBridge2: '',
			videolink: '',
			estStartTime: '',
			estCompletionTime: '',
			apiActionBypass: false
		};
		this.eventModel = Object.assign({}, this.defaultModel, this.eventModel);
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
		this.eventsService.getModelForEventCreate().subscribe((result: any) => {
			this.bundles = result.bundles;
			this.runbookStatuses = result.runbookStatuses;
			this.assetTags = result.tags;
		});
	}

	public onAssetTagChange(event) {
		this.eventModel.tagIds = event.tags;
	}

	public clearButtonBundleChange(event) {
		this.showClearButton =  event && event.length > 1;
	}

	public saveForm() {
		if (DateUtils.validateDateRange(this.eventModel.estStartTime, this.eventModel.estCompletionTime) && this.validateRequiredFields(this.eventModel)) {
			this.eventsService.saveEvent(this.eventModel).subscribe((result: any) => {
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
	public validateRequiredFields(model: EventModel): boolean {
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
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.defaultModel)) {
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
