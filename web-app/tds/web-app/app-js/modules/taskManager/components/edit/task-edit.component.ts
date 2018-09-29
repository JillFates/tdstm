import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {NgForm} from '@angular/forms';
import {clone} from 'ramda';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils, DatePartUnit} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from './../model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from './../model/task-notes-columns.model';
import {RowClassArgs} from '@progress/kendo-angular-grid';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DateRangeSelectorComponent} from '../../../../shared/components/date-range-selector/date-range-selector.component';
import {DateRangeSelectorModel} from '../../../../shared/components/date-range-selector/model/date-range-selector.model';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {YesNoList, TaskEditCreateModelHelper} from './task-edit-create-model.helper';

declare var jQuery: any;

@Component({
	selector: `task-edit`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/edit/task-edit.component.html',
	styles: []
})
export class TaskEditComponent extends UIExtraDialog  implements OnInit {
	@ViewChild('taskEditForm') public taskEditForm: NgForm;

	public modalType = ModalType;
	public dateFormat: string;
	public dateFormatTime: string;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	public taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	public taskNotesColumnsModel = new TaskNotesColumnsModel();
	public collapsedTaskDetail = false;
	public hasCookbookPermission = false;
	public modalOptions: DecoratorOptions;
	public model: any = null;
	public getAssetList: Function;
	public yesNoList =  [...YesNoList];
	public predecessorSuccessorColumns: any[];
	public userTimeZone: string;
	public hasModelChanges = false;
	public hasDeleteTaskPermission = false;
	public hasEditTaskPermission = false;
	public modelHelper: TaskEditCreateModelHelper;

	constructor(
		public taskDetailModel: TaskDetailModel,
		public taskManagerService: TaskService,
		private dialogService: UIDialogService,
		public promptService: UIPromptService,
		public userPreferenceService: PreferenceService,
		private permissionService: PermissionService) {

		super('#task-edit-component');
		this.modalOptions = { isResizable: true, isCentered: true };
		this.getTasksForComboBox = this.getTasksForComboBox.bind(this);
	}

	ngOnInit() {
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();
		this.dateFormat = this.userPreferenceService.getDefaultDateFormatAsKendoFormat();
		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();

		this.modelHelper = new TaskEditCreateModelHelper(this.userTimeZone, this.userPreferenceService.getUserCurrentDateFormatOrDefault());
		this.model = this.modelHelper.setModel(this.taskDetailModel);

		this.getAssetList = this.taskManagerService.getAssetListForComboBox.bind(this.taskManagerService);

		this.predecessorSuccessorColumns = this.taskSuccessorPredecessorColumnsModel.columns;

		this.taskManagerService.getStatusList(this.model.id)
			.subscribe((data: string[]) => this.model.statusList = data);

		this.taskManagerService.getAssignedTeam(this.model.id)
			.subscribe((data: any[]) => {
				jQuery('[data-toggle="popover"]').popover();
				this.model.personList = data.map((item) => ({id: item.id, text: item.nameRole}))
			});

		this.taskManagerService.getStaffRoles()
			.subscribe((data: any[]) => {
				this.model.teamList = data.map((item) => ({id: item.id, text: item.description }));
			});

		this.userPreferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
			});

		this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null);
		this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null);
		this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.generateNotes(this.taskDetailModel.detail.notes), null, null);
		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);

		this.hasDeleteTaskPermission = this.permissionService.hasPermission(Permission.TaskDelete);
		this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.taskManagerService.getAssetListForComboBox(searchParam);
	}

	/**
	 * Everytime a task dependency changes, then update the corresponding collection that hold predeccessor/successor  tasks
	 * If the task is deleted set the model changes flag to true
	 * @param {object} dataItem
	 * @param {string} collectionType  could be predecessor/ successor string
	 * @param {object} gridHelper
	 * @returns {void}
	 */
	onDependencyTaskChange(dataItem: any, collectionType: string, gridHelper: DataGridOperationsHelper, rowIndex: number): void {
		let addTask, removeTask, getCollection;

		if (collectionType === 'predecessor') {
			addTask = this.modelHelper.addPredecessor;
			removeTask = this.modelHelper.deletePredecessor;
			getCollection = this.modelHelper.getPredecessor;
		} else {
			addTask = this.modelHelper.addSuccessor;
			removeTask = this.modelHelper.deleteSuccessor;
			getCollection = this.modelHelper.getSuccessor;
		}

		if (dataItem) {
			const {id, text} = dataItem;
			addTask.bind(this.modelHelper)(rowIndex, {id, text, ...dataItem.metaFields})
		} else {
			if (removeTask.bind(this.modelHelper)(rowIndex)) {
				this.hasModelChanges = true;
			}
		}

		gridHelper.reloadData(getCollection.bind(this.modelHelper)());
	}

	/**
	 * Adda a task predecessor/successor to the top of the corresponding collection sent as parameter
	 * @param {object[]} collection
	 * @param {object} gridHelper
	 * @returns {void}
	 */
	onAddTaskDependency(collection: any[], gridHelper: DataGridOperationsHelper): void {
		const defaultText = 'Please Select';
		const predecessorTask = {
			id: '',
			desc: defaultText,
			model: {id: 0, text: defaultText}
		};
		collection.unshift(predecessorTask);
		gridHelper.addDataItem(predecessorTask);
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getTasksForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.taskManagerService.getTasksForComboBox(searchParam);
	}

	/**
	 * Toggle lock property value
	 */
	toggleLocked(): void {
		this.modelHelper.toggleLocked();
		this.hasModelChanges = true;
	}

	/**
	 * Set the flag to determine if some property handled outside [(ngModel)] has changed
	 */
	onModelChange(value: any): void {
		if (value) {
			this.hasModelChanges = true;
		}
	}

	/**
	 * Set the reference to the asset entityt once combobox changed
	 */
	onAssetEntityChange(assetSelected: any): void {
		this.model.asset = assetSelected;
		this.onModelChange(assetSelected);
	}

	/**
	 * Return the date passed as argument formatted using the user timezone
	 * @param {any} date
	 * @returns {string}
	 */
	getFormattedDate(date): any {
		return new Date(DateUtils.formatUserDateTime(this.userTimeZone, date));
	}

	/**
	 * Passing a valid date returns the user preference date format plus the default time format
	 * Otherwise returns empty string
	 * @param {any} date
	 * @returns {string}
	 */
	getDateTimeFormat(date = null): string {
		return date ? `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}` :  this.dateFormat;
	}

	/**
	 * Open the view to allow select a range of estimated dates
	 */
	openRangeDatesSelector(): void {
		const dateModel: DateRangeSelectorModel = {
			start: this.model.estimatedStart,
			end: this.model.estimatedFinish,
			locked: this.model.locked,
			dateFormat: this.dateFormat,
			timeFormat: DateUtils.DEFAULT_FORMAT_TIME,
			duration: this.model.durationParts
		};

		this.dialogService.extra(DateRangeSelectorComponent,
			[
				{provide: UIPromptService, useValue: this.promptService},
				{provide: DateRangeSelectorModel, useValue: clone(dateModel)}
			], false, false).then((result: DateRangeSelectorModel) => {
				if (result) {
					const {start, end, duration, locked} = result;
					this.model.estimatedStart = start;
					this.model.estimatedFinish = end;
					this.model.durationParts =  duration;
					this.model.locked = locked;
					this.hasModelChanges = true;
				}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Open the Task Detail
	 * @param task
	 */
	public openTaskDetail(task: any, modalType: ModalType): void {
		this.close({commentInstance: {id: task.taskId}});
	}

	/**
	 * Create the structure of the notes from the simple array returned in the API
	 * @param notes
	 * @returns {Array<any>}
	 */
	private generateNotes(notes: any): Array<any> {
		let noteList = new Array<any>();
		notes.forEach((note) => {
			noteList.push({
				dateCreated: note[0],
				createdBy: note[1],
				note: note[2],
			});
		});

		return noteList;
	}

	protected onSave(): void {
		this.taskManagerService.updateTask(this.modelHelper.getPayloadForUpdate())
			.subscribe((result) => this.close(result));
	}

	/**
	 * Delete the Asset Comment
	 */
	protected onDelete(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					// this.taskManagerService.deleteTaskComment(this.singleCommentModel.id).subscribe((res) => {
					this.close();
					// });
				}
			})
			.catch((error) => console.log(error));
	}

	/**
	 * Get the assigned team name
	 * @param commentId
	 * @param assignedToId
	 * @returns {any}
	 */
	public getAssignedTeam(commentId: any, assignedToId: any): void {
		this.taskManagerService.getAssignedTeam(commentId).subscribe((res: any) => {
			let team = res.filter((team) => team.id === assignedToId);
			if (team) {
				// is this a real case in the legacy view?
				if (team.length > 1) {
					team = team[0];
				}
				this.taskDetailModel.detail.assignedTeam = team.nameRole.split(':')[0];
			}
		});
	}

	/**
	 * Toggle task details
	 */
	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
	}

	/**
	 * Change the background color based on the task status
	 * @param {RowClassArgs} context
	 * @returns {string}
	 */
	protected rowStatusColor(context: RowClassArgs) {
		const status = context.dataItem && context.dataItem.status || ' ';
		return 'task-' + status.toLowerCase();
	}

	/**
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		if (this.isFormDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
				if (result) {
					this.dismiss();
				}
			});
		} else {
			this.dismiss();
		}
	}

	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	deleteTask(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this task. There is no undo for this action',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.close({id: this.taskDetailModel, isDeleted: true})
			}
		});
	}

	/**
	 * Based upon the date unit, increment/decrement the value of estimated finish date
	 * @param {DatePartUnit} unit
	 * @returns {void}
	 */
	updateEstimatedFinish(unit: DatePartUnit): void {
		const originalParts = DateUtils.getDurationPartsAmongDates(this.model.estimatedStart, this.model.estimatedFinish);
		const diff = this.model.durationParts[unit] - originalParts[unit];

		if (this.model.estimatedFinish) {
			this.model.estimatedFinish =  DateUtils.increment(this.model.estimatedFinish, [{value: diff, unit}]);
		}
	}

	/**
	 * Clear the values of start/finish estimated dates
	 */
	cleanEstimatedDates(): void {
		if (this.model.estimatedStart || this.model.estimatedFinish) {
			this.model.estimatedFinish = '';
			this.model.estimatedStart = '';
			this.hasModelChanges = true;
		}
	}


	/**
	 * Determine if predecessor/successor collections contains invalid data, like duplicates or empty ids
	 * @returns {boolean}
	 */
	hasInvalidFields(): boolean {
		if (!this.modelHelper.hasDependencyTasksChanges()) {
			return false;
		}
		return this.modelHelper.hasInvalidTasksDependencies();
	}

	/**
	 * Determine whether the form should be disabled because of invalid fields
	 * @returns {boolean}
	 */
	isFormInvalid(): boolean {
		return !this.taskEditForm.form.valid ||
				this.hasInvalidFields() ||
				!(this.taskEditForm.form.dirty || this.hasModelChanges)
	}

	/**
	 * Determine if the form has changes based upon ngModel,external tds-checkbox or task grids changes
	 * @returns {boolean}
	 */
	isFormDirty(): boolean {
		return this.modelHelper.hasDependencyTasksChanges() || this.taskEditForm.dirty || this.hasModelChanges;
	}

	/**
	 *  Determine if a string has the format URL or label|URL
	 * @param labelURL {string}
	 * @returns {boolean}
	 */
	validateLabelURL(labelURL: string): void {
		let isEmpty = false;

		if (!labelURL) {
			isEmpty = true
		}

		const errors =  (!isEmpty && !ValidationUtils.isValidLabelURL(labelURL)) ? {'incorrect' : true}  : null;
		this.taskEditForm.form.controls['instructionLink'].setErrors(errors);
	}

	/**
	 * On change selection on asset class reset asset entity value
	 * @returns {void}
	 */
	onAssetClassChange(): void {
		this.model.asset = {id: '', text: ''};
	}

}