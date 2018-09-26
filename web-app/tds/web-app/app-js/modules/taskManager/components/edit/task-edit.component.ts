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
	public yesNoList = ['Yes', 'No'];
	public predecessorSuccessorColumns: any[];
	public userTimeZone: string;
	public hasModelChanges = false;
	private dataSignatureDependencyTasks: string;

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
		this.model = this.extractModel();
		this.dataSignatureDependencyTasks = JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});
		this.getAssetList = this.taskManagerService.getAssetListForComboBox.bind(this.taskManagerService);
		this.predecessorSuccessorColumns = this.taskSuccessorPredecessorColumnsModel.columns
			.filter((column) => column.property === 'desc');

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
	}

	/**
	 * Extract only the model fields used by the view
	 */
	extractModel(): any {
		const detail = this.taskDetailModel.detail;
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = this.yesNoList;

		// add empty default value
		detail.categories = (detail.categories || []);
		detail.categories.push('');

		return  {
			id: assetComment.id,
			note: '',
			duration: assetComment.duration,
			taskSpec: assetComment.taskSpec,
			taskNumber: assetComment.taskNumber,
			hardAssigned: Boolean(assetComment.hardAssigned === 1) ? yes : no,
			sendNotification: Boolean(assetComment.sendNotification) ? yes : no,
			durationScale,
			durationParts: DateUtils.getDurationParts(assetComment.duration, durationScale),
			durationLocked: assetComment.durationLocked,
			actualStart: detail.atStart ? detail.atStart : '',
			actualFinish: detail.dtResolved ? detail.dtResolved : '',
			dueDate: assetComment.dueDate ? this.getFormattedDate(assetComment.dueDate) : '',
			estimatedStartUTC: assetComment.estStart,
			estimatedFinishUTC: assetComment.estFinish,
			estimatedStart: assetComment.estStart ? this.getFormattedDate(assetComment.estStart) : '',
			estimatedFinish: assetComment.estFinish ? this.getFormattedDate(assetComment.estFinish)  : '',
			instructionLink: detail.instructionLink === '|' ? '' : detail.instructionLink,
			priority: assetComment.priority,
			assetName: detail.assetName,
			comment:  assetComment.comment || '',
			assetClass: {id: detail.assetClass, text: ''},
			assetClasses: Object.keys(detail.assetClasses || {}).map((key: string) => ({id: key, text: detail.assetClasses[key]}) ),
			status: assetComment.status,
			statusList: [],
			personList: [],
			teamList: [],
			predecessorList: (this.taskDetailModel.detail.predecessorList || [])
				.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
			successorList: (this.taskDetailModel.detail.successorList || [])
				.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
			apiActionList: (detail.apiActionList || []).map((action) => ({id: action.id, text: action.name})),
			categoriesList: detail.categories.sort(),
			eventList: (detail.eventList || []).map((event) => ({id: event.id, text: event.name})),
			priorityList: [1, 2, 3, 4, 5],
			asset: {id: detail.assetId, text: detail.assetName},
			assignedTo: {id : (assetComment.assignedTo && assetComment.assignedTo.id) || null, text: detail.assignedTo},
			assignedTeam: {id: assetComment.role, text: detail.roles},
			event: {id: (assetComment.moveEvent && assetComment.moveEvent.id) || null, text: detail.eventName},
			category: assetComment.category,
			apiAction: {id: detail.apiAction && detail.apiAction.id || '', text: detail.apiAction && detail.apiAction.name || ''}
		}
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
	 * @param {object[]} collection
	 * @param {object} gridHelper
	 * @returns {void}
	 */
	onDependencyTaskChange(dataItem: any, collection: any[], gridHelper: DataGridOperationsHelper, rowIndex: number): void {
		if (dataItem) {
			collection[rowIndex] = {
				id: dataItem.id,
				desc: dataItem.text,
				model: {id: dataItem.id, text: dataItem.text}
			};
			return;
		}

		if (collection[rowIndex].id) {
			this.hasModelChanges = true;
		}

		collection.splice(rowIndex, 1);
		gridHelper.reloadData(collection);
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
		this.model.locked = !this.model.locked;
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
		/* this.taskManagerService.saveComment(this.singleCommentModel).subscribe((res) => {
			this.close();
		}); */
		this.close();
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
		return 'task-' + context.dataItem.status.toLowerCase();
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
	 * Determine if the array of objects passed as argument has duplicated id properties
	 * @param {any[]} array
	 * @returns {boolean}
	 */
	hasDuplicates(array: any[]): boolean {
		const ids = [];

		array.forEach((item) => {
			if (ids.indexOf(item.id) === -1) {
				ids.push(item.id);
			}
		});

		return ids.length < array.length;
	}

	/**
	 * Determine if the array of objects passed as argument contains empty ids
	 * @param {any[]} array
	 * @returns {boolean}
	 */
	hasEmptyIds(array: any[]): boolean {
		return array.filter((item) => item.id === '').length > 0;
	}

	/**
	 * Determine if predecessor/successor collections contains invalid data, like duplicates or empty ids
	 * @returns {boolean}
	 */
	hasInvalidFields(): boolean {
		if (!this.hasDependencyTasksChanges()) {
			return false;
		}

		const {predecessorList, successorList} = this.model;
		return this.hasDuplicates(predecessorList) ||
			this.hasDuplicates(successorList) ||
			this.hasEmptyIds(predecessorList) ||
			this.hasEmptyIds(successorList);
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
		return this.hasDependencyTasksChanges() || this.taskEditForm.dirty || this.hasModelChanges;
	}

	/**
	 * Determine if predecessor/succesor collections contain changes
	 * @returns {boolean}
	 */
	hasDependencyTasksChanges(): boolean {
		return this.dataSignatureDependencyTasks !== JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});
	}

}