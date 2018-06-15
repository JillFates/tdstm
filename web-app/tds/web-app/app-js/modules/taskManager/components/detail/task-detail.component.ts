import {Component, HostListener} from '@angular/core';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from './model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from './model/task-notes-columns.model';
import {RowClassArgs} from '@progress/kendo-angular-grid';

@Component({
	selector: `task-detail`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/detail/task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormat: string;
	public dateFormatTime: string;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	public taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	public taskNotesColumnsModel = new TaskNotesColumnsModel();

	constructor(public taskDetailModel: TaskDetailModel, public taskManagerService: TaskService, public promptService: UIPromptService, public userPreferenceService: PreferenceService) {
		super('#task-detail-component');
		this.loadTaskDetail();
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getTaskDetails(this.taskDetailModel.id).subscribe((res) => {
			this.dateFormat = this.userPreferenceService.getUserTimeZone();
			this.dateFormatTime = this.userPreferenceService.getUserTimeZone() + ' ' + DateUtils.DEFAULT_FORMAT_TIME;
			this.taskDetailModel.detail = res;
			this.taskDetailModel.detail.instructionLink = this.taskDetailModel.detail.instructionsLinkLabel + '|' + this.taskDetailModel.detail.instructionsLinkURL;

			this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.taskDetailModel.detail.predecessorList, null, null);
			this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.taskDetailModel.detail.successorList, null, null);
			// Notes are coming into an Array of Arrays...
			this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.generateNotes(this.taskDetailModel.detail.notes), null, null);
			// Convert the Duration into a Human Readable form
			this.taskDetailModel.detail.durationText = DateUtils.formatDuration(this.taskDetailModel.detail.assetComment.duration, this.taskDetailModel.detail.assetComment.durationScale.name);

			// Get Assigned Team
			this.getAssignedTeam(this.taskDetailModel.detail.assetComment.id, this.taskDetailModel.detail.assetComment.assignedTo.id);
		});
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
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
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
}