import {Component, HostListener, OnInit} from '@angular/core';
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

@Component({
	selector: `task-detail`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/detail/task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog  implements OnInit {

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
		this.loadTaskDetail();
		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getTaskDetails(this.taskDetailModel.id).subscribe((res) => {
			this.dateFormat = this.userPreferenceService.getUserDateFormat();
			this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
			this.taskDetailModel.detail = res;
			this.taskDetailModel.detail.instructionLink = this.taskDetailModel.detail.instructionsLinkLabel + '|' + this.taskDetailModel.detail.instructionsLinkURL;

			this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.taskDetailModel.detail.predecessorList, null, null);
			this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.taskDetailModel.detail.successorList, null, null);
			// Notes are coming into an Array of Arrays...
			this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.generateNotes(this.taskDetailModel.detail.notes), null, null);
			// Convert the Duration into a Human Readable form
			this.taskDetailModel.detail.durationText = DateUtils.formatDuration(this.taskDetailModel.detail.assetComment.duration, this.taskDetailModel.detail.assetComment.durationScale.name);

			// Get Assigned Team
			if (this.taskDetailModel.detail.assetComment.assignedTo) {
				this.getAssignedTeam(this.taskDetailModel.detail.assetComment.id, this.taskDetailModel.detail.assetComment.assignedTo.id);
			}
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
}