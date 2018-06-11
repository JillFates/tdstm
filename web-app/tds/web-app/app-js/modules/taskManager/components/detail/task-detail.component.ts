import {Component, HostListener} from '@angular/core';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `task-detail`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/detail/task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormatTime: string;

	constructor(public taskDetailModel: TaskDetailModel, public taskManagerService: TaskService, public promptService: UIPromptService) {
		super('#task-detail-component');
		this.loadTaskDetail();
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getCommentCategories().subscribe((res) => {
			//
		});
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
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}