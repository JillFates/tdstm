// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Store
import {SetBundle} from '../../action/bundle.actions';
import {Store} from '@ngxs/store';
// Model
import {BundleModel} from '../../model/bundle.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {BundleService} from '../../service/bundle.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {TaskService} from '../../../taskManager/service/task.service';
import * as R from 'ramda';
import {ActionType} from '../../../dataScript/model/data-script.model';

@Component({
	selector: `bundle-view-edit-component`,
	templateUrl: 'bundle-view-edit.component.html',
})
export class BundleViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	public bundleModel: BundleModel = null;
	public savedModel: BundleModel = null;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public moveEvents;
	public rooms;
	public isDefaultBundle;
	public sourceRoom;
	public targetRoom;
	public projectId;
	public canEditBundle;
	public bundleId;
	public editing = false;
	public fromControlTT = '';
	public toControlTT = '';
	protected userTimeZone: string;
	protected userDateTimeFormat: string;
	private requiredFields = ['name'];
	@ViewChild('startTimePicker', {static: false}) startTimePicker;
	@ViewChild('completionTimePicker', {static: false}) completionTimePicker;

	constructor(
		private bundleService: BundleService,
		private dialogService: DialogService,
		private taskService: TaskService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private translatePipe: TranslatePipe,
		private store: Store) {
		super();
		this.canEditBundle = this.permissionService.hasPermission('BundleEdit');
	}

	ngOnInit() {
		this.bundleId = R.clone(this.data.bundleId);
		this.editing = this.data.actionType === ActionType.EDIT;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.canEditBundle,
			active: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.switchToEdit.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.editing,
			disabled: () => !this.validateRequiredFields(this.bundleModel) || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.canEditBundle,
			type: DialogButtonType.ACTION,
			action: this.confirmDeleteBundleAndAssets.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => !this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelEdit.bind(this)
		});

		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.userDateTimeFormat = this.preferenceService.getUserDateTimeFormat();

		this.loadModel();
	}

	/**
	 * Set up the initial default values for the model
	 * @returns {any}
	 */
	loadModel(): any {
		this.bundleModel = new BundleModel();
		const defaultBundle = {
			name: '',
			description: '',
			fromId: null,
			toId: null,
			startTime: '',
			completionTime: '',
			moveEvent: {},
			operationalOrder: 1,
			useForPlanning: false,
		};
		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.bundleModel = Object.assign({}, defaultBundle, this.bundleModel);
		this.getModel(this.bundleId);
	}

	public confirmDeleteBundle() {
		this.dialogService.confirm(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams and any related step data'
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.deleteBundle();
				}
			});
	}

	public confirmDeleteBundleAndAssets() {
		this.dialogService.confirm(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams, any related step data, AND ASSIGNED ASSETS (NO UNDO)',
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.deleteBundleAndAssets();
				}
			});
	}

	private deleteBundle() {
		this.bundleService.deleteBundle(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					setTimeout(() => {
						this.store.dispatch(new SetBundle(null));
						this.onCancelClose();
					});
				}
			});
	}

	private deleteBundleAndAssets() {
		this.bundleService.deleteBundleAndAssets(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					setTimeout(() => {
						this.store.dispatch(new SetBundle(null));
						this.onCancelClose();
					});
				}
			});
	}

	public switchToEdit() {
		this.editing = true;
		// Small delay when switch so the elements are visible
		setTimeout(() => {
			if (this.bundleModel.startTime) {
				this.startTimePicker.dateValue = this.formatForDateTimePicker(this.bundleModel.startTime);
			}
			if (this.bundleModel.completionTime) {
				this.completionTimePicker.dateValue = this.formatForDateTimePicker(this.bundleModel.completionTime);
			}
		});
	}

	/**
	 * Get ghe models for the specific bundle
	 * @param id  bundle id
	 */
	private getModel(id) {
		this.bundleService.getModelForBundleViewEdit(id)
			.subscribe((result) => {
				let data = result.data;
				let bundleModel = this.bundleModel;
				// Fill the model based on the current person.
				Object.keys(data.moveBundleInstance).forEach((key) => {
					if (key in bundleModel && data.moveBundleInstance[key]) {
						bundleModel[key] = data.moveBundleInstance[key];
					}
				});
				this.bundleModel = bundleModel;

				this.bundleModel.fromId = data.moveBundleInstance.sourceRoom ? data.moveBundleInstance.sourceRoom.id : null;
				this.bundleModel.toId = data.moveBundleInstance.targetRoom ? data.moveBundleInstance.targetRoom.id : null;
				this.bundleModel.moveEvent = data && data.moveBundleInstance.moveEvent ? data.moveBundleInstance.moveEvent : {
					id: null,
					name: ''
				};

				this.rooms = data.rooms;
				this.taskService.getEvents()
					.subscribe((results: any) => {
						this.moveEvents = results;
						if (this.bundleModel.moveEvent) {
							const currentEvent = results.find((result: any) => {
								return result.id === this.bundleModel.moveEvent.id;
							});
							if (currentEvent) {
								this.bundleModel.moveEvent.name = currentEvent.name;
							}
						}
						this.updateSavedFields();
					});
			});
	}

	private updateSavedFields() {
		this.savedModel = JSON.parse(JSON.stringify(this.bundleModel));
		this.rooms.forEach((room) => {
			if (room.id === this.savedModel.fromId) {
				this.sourceRoom = room.roomName;
			}
			if (room.id === this.savedModel.toId) {
				this.targetRoom = room.roomName;
			}
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
			this.bundleService.saveBundle(this.bundleModel, this.bundleId).subscribe((result: any) => {
				if (result.status === 'success') {
					this.bundleModel.startTime = this.bundleModel.startTime || '';
					this.bundleModel.completionTime = this.bundleModel.completionTime || '';

					this.updateSavedFields();
					this.editing = false;
				}
			});
		}
	}

	public isDirty() {
		return JSON.stringify(this.savedModel) !== JSON.stringify(this.bundleModel);
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
		if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.savedModel)) {
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
		if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.savedModel)) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
						this.editing = false;
						this.bundleModel = JSON.parse(JSON.stringify(this.savedModel));
						this.setTitle(this.getModalTitle());
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

	public onFromChange(): void {
		this.fromControlTT = this.rooms.find(r => r.id === this.bundleModel.fromId).roomName;
	}

	public onToChange(): void {
		this.toControlTT = this.rooms.find(r => r.id === this.bundleModel.toId).roomName;
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
		// Every time we change the title, it means we switched to View, Edit or Create
		setTimeout(() => {
			// This ensure the UI has loaded since Kendo can change the signature of an object
			// this.dataSignature = JSON.stringify(this.credentialModel);
		}, 800);

		if (this.editing) {
			return 'Bundle Edit';
		}
		return 'Bundle Detail';
	}
}
