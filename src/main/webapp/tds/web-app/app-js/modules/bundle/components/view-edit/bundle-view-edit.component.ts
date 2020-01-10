import {Component, ElementRef, HostListener, Inject, OnInit, Renderer2, ViewChild} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {BundleModel} from '../../model/bundle.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {TaskService} from '../../../taskManager/service/task.service';

@Component({
	selector: `bundle-view-edit-component`,
	templateUrl: 'bundle-view-edit.component.html',
})
export class BundleViewEditComponent implements OnInit {
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
	protected userTimeZone: string;
	private requiredFields = ['name'];
	@ViewChild('startTimePicker', {static: false}) startTimePicker;
	@ViewChild('completionTimePicker', {static: false}) completionTimePicker;
	constructor(
		private bundleService: BundleService,
		private taskService: TaskService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		private translatePipe: TranslatePipe,
		@Inject('id') private id) {
		this.canEditBundle = this.permissionService.hasPermission('BundleEdit');
		this.bundleId = this.id;
	}

	ngOnInit() {
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

	// Close dialog if back button is pressed
	@HostListener('window:popstate', ['$event'])
	onPopState(event) {
		this.activeDialog.close()
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

	public confirmDeleteBundle() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams and any related step data',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteBundle();
				}
			})
			.catch((error) => console.log(error));
	}

	public confirmDeleteBundleAndAssets() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams, any related step data, AND ASSIGNED ASSETS (NO UNDO)',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteBundleAndAssets();
				}
			})
			.catch((error) => console.log(error));
	}

	private deleteBundle() {
		this.bundleService.deleteBundle(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
				}
			});
	}

	private deleteBundleAndAssets() {
		this.bundleService.deleteBundleAndAssets(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
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
				this.bundleModel.moveEvent = data && data.moveBundleInstance.moveEvent ? data.moveBundleInstance.moveEvent : {id: null, name: ''};

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
		if (DateUtils.validateDateRange(this.bundleModel.startTime, this.bundleModel.completionTime)) {
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
	public formatForDateTimePicker (time) {
		let localDateFormatted = DateUtils.convertFromGMT(time, this.userTimeZone);
		return time ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.savedModel)) {
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
		if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.savedModel)) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						this.editing = false;
						this.bundleModel = JSON.parse(JSON.stringify(this.savedModel));
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.editing = false;
		}
	}
}
