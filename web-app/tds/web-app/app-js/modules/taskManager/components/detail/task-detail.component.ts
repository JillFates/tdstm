import {Component, HostListener, OnInit} from '@angular/core';
import {DIALOG_SIZE, KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from './../model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from './../model/task-notes-columns.model';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {TaskEditComponent} from '../edit/task-edit.component';
import {clone} from 'ramda';
import {TaskEditCreateModelHelper} from '../edit/task-edit-create-model.helper';

@Component({
	selector: `task-detail`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/detail/task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog  implements OnInit {

	public modalType = ModalType;
	public dateFormat: string;
	public dateFormatTime: string;
	public userTimeZone: string;
	public modelHelper: TaskEditCreateModelHelper;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	public taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	public taskNotesColumnsModel = new TaskNotesColumnsModel();
	public collapsedTaskDetail = false;
	public hasCookbookPermission = false;
	public hasEditTaskPermission = false;
	public hasDeleteTaskPermission = false;
	public modalOptions: DecoratorOptions;
	public model: any = {};

	constructor(
		public taskDetailModel: TaskDetailModel,
		public taskManagerService: TaskService,
		private dialogService: UIDialogService,
		public promptService: UIPromptService,
		public userPreferenceService: PreferenceService,
		private permissionService: PermissionService) {

		super('#task-detail-component');
		this.modalOptions = { isResizable: true, isCentered: true };
	}

	ngOnInit() {
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();
		this.loadTaskDetail();
		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);
		this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
		this.hasDeleteTaskPermission = this.permissionService.hasPermission(Permission.TaskDelete);
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getTaskDetails(this.taskDetailModel.id).subscribe((res) => {
			this.dateFormat = this.userPreferenceService.getUserDateFormat();
			this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
			this.taskDetailModel.detail = res;

			this.modelHelper = new TaskEditCreateModelHelper(this.userTimeZone, this.userPreferenceService.getUserCurrentDateFormatOrDefault());
			this.model = this.modelHelper.cleanAndSetModel(this.taskDetailModel);

			this.model.instructionLink = this.model.instructionsLinkLabel + '|' + this.model.instructionsLinkURL;

			this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null);
			this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null);
			// Notes are coming into an Array of Arrays...
			this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.modelHelper.generateNotes(this.model.notesList), null, null);
			// Convert the Duration into a Human Readable form
			this.model.durationText = DateUtils.formatDuration(this.model.duration, this.model.durationScale);

			// Get Assigned Team
			/*
			if (this.model.assignedTo) {
				this.getAssignedTeam(this.model.id, this.model.assignedTo.id);
			}
			*/

		});
	}

	/**
	 * Open the Task Detail
	 * @param task
	 */
	public openTaskDetail(task: any, modalType: ModalType): void {
		this.close({commentInstance: {id: task.taskId}});
	}

	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
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
	/**
	 * Open view to edit task details
	 */
	public editTaskDetail(): void {
		this.dialogService.extra(TaskEditComponent,
			[
				{provide: UIDialogService, useValue: this.dialogService},
				{provide: TaskService, useValue:  this.taskManagerService},
				{provide: UIPromptService, useValue: this.promptService},
				{provide: PreferenceService, useValue: this.userPreferenceService} ,
				{provide: PermissionService, useValue: this.permissionService},
			{provide: TaskDetailModel, useValue: clone(this.model)}
		], false, false)
		.then(result => {
			if (result) {
				this.loadTaskDetail();
			}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}
}