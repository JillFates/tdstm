import {Component, ElementRef, HostListener, Inject, OnInit, Renderer2, ViewChild} from '@angular/core';
import {EventsService} from '../../service/events.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {EventModel} from '../../model/event.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {Store} from '@ngxs/store';
import {SetEvent} from '../../action/event.actions';

@Component({
	selector: `event-view-edit-component`,
	templateUrl: 'event-view-edit.component.html',
})
export class EventViewEditComponent implements OnInit {
	public eventModel: EventModel = null;
	public savedModel: EventModel = null;
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
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		private translatePipe: TranslatePipe,
		private store: Store,
		@Inject('id') private id: any) {
		this.canEditEvent = this.permissionService.hasPermission('EventEdit');
		this.eventId = this.id;
	}

	ngOnInit() {
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

	public confirmDeleteEvent() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Are you sure you want to delete this event?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteEvent();
				}
			})
			.catch((error) => console.log(error));
	}

	private deleteEvent() {
		this.eventsService.deleteEvent(this.eventId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.store.dispatch(new SetEvent(null));
					this.activeDialog.close();
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

	public saveForm() {
		if (DateUtils.validateDateRange(this.eventModel.estStartTime, this.eventModel.estCompletionTime) && this.validateRequiredFields(this.eventModel)) {
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
	public formatForDateTimePicker (time) {
		let localDateFormatted = DateUtils.convertFromGMT(time, this.userTimeZone);
		return time ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.savedModel)) {
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

	public cancelEdit(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.savedModel)) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						this.editing = false;
						this.eventModel = JSON.parse(JSON.stringify(this.savedModel));
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.editing = false;
		}
	}
}
