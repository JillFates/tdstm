// Angular
import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
// Store
import {Store} from '@ngxs/store';
// Model
import {EventModel} from '../../model/event.model';
import {SetEvent} from '../../action/event.actions';
// Service
import {EventsService} from '../../service/events.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import * as R from 'ramda';
import {ActionType} from '../../../dataScript/model/data-script.model';

@Component({
	selector: `event-view-edit-component`,
	templateUrl: 'event-view-edit.component.html',
})
export class EventViewEditComponent extends Dialog implements OnInit {
	@ViewChild('eventName', {static: false}) eventName: ElementRef;
	@Input() data: any;

	public eventModel: EventModel = null;
	public savedModel: EventModel = null;
	public showSwitch = false;
	public showClearButton = false;
	public availableBundles: any[] = [];
	public runbookStatuses: string[] = [];
	public availableTags: any[] = [];
	public canEditEvent;
	public eventId;
	public editing = false;
	protected userTimeZone: string;
	protected userDateTimeFormat: string;
	private requiredFields = ['name'];
	@ViewChild('startTimePicker', {static: false}) startTimePicker;
	@ViewChild('completionTimePicker', {static: false}) completionTimePicker;

	constructor(
		private eventsService: EventsService,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private translatePipe: TranslatePipe,
		private store: Store) {
		super();
		this.canEditEvent = this.permissionService.hasPermission('EventEdit');
	}

	ngOnInit() {
		this.eventId = R.clone(this.data.eventId);
		this.editing = this.data.actionType === ActionType.EDIT;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			tooltipText: 'Edit',
			show: () => true,
			active: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.switchToEdit.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => this.editing,
			disabled: () => !this.validateRequiredFields(this.eventModel) || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			tooltipText: 'Delete',
			show: () => this.canEditEvent,
			type: DialogButtonType.ACTION,
			action: this.confirmDeleteEvent.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => !this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelEdit.bind(this)
		});

		this.eventModel = new EventModel();
		const defaultEvent = {
			name: '',
			description: '',
			tagIds: [],
			bundles: [],
			runbookStatus: '',
			runbookBridge1: '',
			runbookBridge2: '',
			videolink: '',
			estStartTime: '',
			estCompletionTime: '',
			apiActionBypass: false
		};
		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.userDateTimeFormat = this.preferenceService.getUserDateTimeFormat();
		this.eventModel = Object.assign({}, defaultEvent, this.eventModel);
		this.getModel(this.eventId);

		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
	}

	public confirmDeleteEvent() {
		this.dialogService.confirm(
			'Confirmation Required',
			'WARNING: Are you sure you want to delete this event?')
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.deleteEvent();
				}
			});
	}

	private deleteEvent() {
		this.eventsService.deleteEvent(this.eventId)
			.subscribe((result) => {
				if (result.status === 'success') {
					setTimeout(() => {
						this.store.dispatch(new SetEvent(null));
						this.onCancelClose();
					});
				}
			});
	}

	public switchToEdit() {
		this.editing = true;
		if (this.eventModel.estStartTime) {
			this.startTimePicker.dateValue = this.formatForDateTimePicker(this.eventModel.estStartTime);
		}
		if (this.eventModel.estCompletionTime) {
			this.completionTimePicker.dateValue = this.formatForDateTimePicker(this.eventModel.estCompletionTime);
		}
		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
	}

	private getModel(id) {
		this.eventsService.getModelForEventViewEdit(id)
			.subscribe((result) => {
				let data = result;
				let eventModel = this.eventModel;
				// Fill the model based on the current person.
				Object.keys(data.moveEventInstance).forEach((key) => {
					if (key in eventModel && data.moveEventInstance[key]) {
						eventModel[key] = data.moveEventInstance[key];
					}
				});
				this.eventModel = eventModel;
				this.availableBundles = data.availableBundles;
				this.availableTags = data.tags;
				this.runbookStatuses = data.runbookStatuses;
				if (data.selectedTags) {
					data.selectedTags.forEach((item) => {
						this.availableTags.forEach((availableTag) => {
							if (item.id === availableTag.id) {
								this.eventModel.tagIds.push(availableTag);
							}
						});
					});
				}
				this.eventModel.moveBundle = data.selectedBundles;

				this.updateSavedFields();
			});

	}

	private updateSavedFields() {
		this.savedModel = JSON.parse(JSON.stringify(this.eventModel));
	}

	public onAssetTagChange(event) {
		this.eventModel.tagIds = event.tags;
	}

	public clearButtonBundleChange(event) {
		this.showClearButton = event && event.length > 1;
	}

	public saveForm() {
		const validateDate = DateUtils.validateDateRange(this.eventModel.estStartTime, this.eventModel.estCompletionTime) && this.validateRequiredFields(this.eventModel);
		if (!validateDate) {
			this.dialogService.notify(
				'Validation Required',
				'The completion time must be later than the start time.'
			).subscribe();
		} else {
			this.eventsService.saveEvent(this.eventModel, this.eventId).subscribe((result: any) => {
				if (result.status === 'success') {
					this.updateSavedFields();
					this.editing = false;
				}
			});
		}
	}

	public isDirty() {
		return JSON.stringify(this.savedModel) !== JSON.stringify(this.eventModel);
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
	 *  Put date in format to be accepted in a dateTimePicker
	 */
	public formatForDateTimePicker(time) {
		let localDateFormatted = DateUtils.convertFromGMT(time, this.userTimeZone);
		return time ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.savedModel)) {
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
			this.onCancelClose();
		}
	}

	public cancelEdit(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.savedModel)) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
						this.editing = false;
						this.eventModel = JSON.parse(JSON.stringify(this.savedModel));
						this.setTitle(this.getModalTitle());
					} else if (data.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
						this.onCancelClose();
					}
				});
		} else {
			this.editing = false;
			if (!this.data.openFromList) {
				this.setTitle(this.getModalTitle());
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @returns {string}
	 */
	private getModalTitle(): string {
		setTimeout(() => {
			this.onSetUpFocus(this.eventName);
		});

		if (this.editing) {
			return 'Event Edit';
		}
		return 'Event Detail';
	}
}
