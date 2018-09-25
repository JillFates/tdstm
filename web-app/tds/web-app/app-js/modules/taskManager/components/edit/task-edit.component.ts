import {Component, HostListener, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
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
		this.getAssetList = this.taskManagerService.getAssetListForComboBox.bind(this.taskManagerService);
		this.predecessorSuccessorColumns = this.taskSuccessorPredecessorColumnsModel.columns.filter((column) => column.property === 'desc');

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
		// this.taskDetailModel.detail.predecessorList

		return  {
			id: assetComment.id,
			note: '',
			duration: assetComment.duration,
			taskSpec: assetComment.taskSpec,
			taskNumber: assetComment.taskNumber,
			hardAssigned: Boolean(assetComment.hardAssigned === 1) ? 'Yes' : 'No',
			sendNotification: Boolean(assetComment.sendNotification) ? 'Yes' : 'No',
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
				.map((item) => ({id: item.id, desc: item.desc, model: {id: item.id, text: item.desc }})),
			successorList: (this.taskDetailModel.detail.successorList || [])
				.map((item) => ({id: item.id, desc: item.desc, model: {id: item.id, text: item.desc }})),
			apiActionList: (detail.apiActionList || []).map((action) => ({id: action.id, text: action.name})),
			categoriesList: detail.categories || [],
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

	onAssetClassChange(asset): void {
	}

	onStatusChange(asset): void {
	}

	onAssetNameChange(asset): void {
	}

	onPersonChange(asset): void {
	}

	onTeamChange(asset): void {
	}

	updateTask(): void {
	}

	onDependencyTaskChange(event: any, collection: any[], gridHelper: DataGridOperationsHelper, rowIndex: number): void {
		let id = null;
		let text = null;

		if (event) {
			id = event.id;
			text = event.text;
			collection[rowIndex] = { id, desc:text, model: {id, text}};
			return;
		}

		collection.splice(rowIndex, 1);
		gridHelper.reloadData(collection);
	}

	onAddTaskDependency(collection: any, gridHelper: DataGridOperationsHelper): void {
		const predecessorTask = {
			id: 0,
			desc: '',
			model: {id: 0, text: ''}
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

	onLockChange(): void {
		this.model.locked = !this.model.locked;
	}

	getFormattedDate(date): any {
		return new Date(DateUtils.formatUserDateTime(this.userTimeZone, date));
	}

	getDateTimeFormat(value: any): string {
		return value ? this.dateFormat + ' h:mm a' : '';
	}

	openRangeDatesSelector(): void {
		const dateModel: DateRangeSelectorModel = {
			start: this.model.estimatedStart,
			end: this.model.estimatedFinish,
			locked: this.model.locked,
			format: this.getDateTimeFormat(true),
			duration: this.model.durationParts
		};

		this.dialogService.extra(DateRangeSelectorComponent,
			[
				{provide: UIPromptService, useValue: this.promptService},
				{provide: DateRangeSelectorModel, useValue: dateModel}
			], false, false).then((result: DateRangeSelectorModel) => {
				if (result) {
					const {start, end, duration, locked} = result;
					this.model.estimatedStart = start;
					this.model.estimatedFinish = end;
					this.model.durationParts =  duration;
					this.model.locked = locked;
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

	protected resizeWindow(resize: any): void {
		console.log(resize);
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
		this.dismiss();
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

	updateEstimatedFinish(unit: 'days' | 'hours' | 'minutes'): void {
		const originalParts = DateUtils.getDurationPartsAmongDates(this.model.estimatedStart, this.model.estimatedFinish);
		const diff = this.model.durationParts[unit] - originalParts[unit];

		if (this.model.estimatedFinish) {
			this.model.estimatedFinish =  DateUtils.increment(this.model.estimatedFinish, [{value: diff, unit}]);
		}
	}

	cleanEstimatedDates(): void {
		this.model.estimatedFinish = '';
		this.model.estimatedStart = '';
	}

	hasDuplicates(array: any[]): boolean {
		const ids = [];

		array.forEach((item) => {
			if (ids.indexOf(item.id) === -1) {
				ids.push(item.id);
			}
		});

		return ids.length < array.length;
	}

}