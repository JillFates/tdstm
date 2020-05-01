// Angular
import {Component, OnInit, ElementRef, ViewChild} from '@angular/core';
// Model
import {EventModel} from '../../model/event.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {EventsService} from '../../service/events.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `event-create`,
	templateUrl: 'event-create.component.html',
})
export class EventCreateComponent extends Dialog implements OnInit {
	@ViewChild('eventCreateName', {static: false}) eventCreateName: ElementRef;
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
		private dialogService: DialogService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit() {
		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.validateRequiredFields(this.eventModel),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

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
		setTimeout(() => {
			this.onSetUpFocus(this.eventCreateName);
		});
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
		const validateDate = DateUtils.validateDateRange(this.eventModel.estStartTime, this.eventModel.estCompletionTime) && this.validateRequiredFields(this.eventModel);
		if (!validateDate) {
			this.dialogService.notify(
				'Validation Required',
				'The completion time must be later than the start time.'
			).subscribe();
		} else {
			this.eventsService.saveEvent(this.eventModel).subscribe((result: any) => {
				if (result.status === 'success') {
					super.onAcceptSuccess();
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
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
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
