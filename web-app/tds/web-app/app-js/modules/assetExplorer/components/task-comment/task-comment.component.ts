import {Component, Input, OnInit} from '@angular/core';

import {TaskCommentService} from '../../service/task-comment.service';
import {SingleCommentComponent} from '../single-comment/single-comment.component';
import {SingleCommentModel} from '../single-comment/model/single-comment.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ModalType} from '../../../../shared/model/constants';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskColumnsModel, CommentColumnsModel} from './model/task-comment-columns.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TaskService} from '../../../taskManager/service/task.service';
import {TaskDetailComponent} from '../../../taskManager/components/detail/task-detail.component';
import {TaskCreateComponent} from '../../../taskManager/components/create/task-create.component';
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';

@Component({
	selector: `task-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/task-comment/task-comment.component.html',
	styles: []
})
export class TaskCommentComponent implements OnInit {
	@Input('asset-id') id: number;
	@Input('has-publish-permission') hasPublishPermission ? = false;
	@Input('can-edit-comments') canEdit ? = false;
	@Input('can-edit-tasks') canEditTasks ? = false;
	@Input('asset-name') assetName: string;
	@Input('asset-type') assetType: string;
	@Input('show-task') showTask: boolean;
	@Input('show-comment') showComment: boolean;
	@Input('asset-class') assetClass: string;
	@Input('user-id') currentUserId: string;

	// Grid Configuration for Task and Comment
	private dataGridTaskHelper: DataGridOperationsHelper;
	private dataGridCommentHelper: DataGridOperationsHelper;

	protected taskColumnModel = new TaskColumnsModel();
	protected commentColumnModel = new CommentColumnsModel();
	private modalType = ModalType;
	private viewUnpublished = false;

	private showAllComments: boolean;
	private showAllTasks: boolean;
	private taskCommentsList: any[] = [];

	constructor(private taskService: TaskCommentService, private dialogService: UIDialogService, public promptService: UIPromptService, public taskManagerService: TaskService, private preferenceService: PreferenceService) {
		this.getPreferences();
	}

	ngOnInit(): void {
		this.showAllComments = false;
		this.showAllTasks = false;
		this.createDataGrids();
	}

	/**
	 * Create the List for the Data Grids for Task and Comments
	 * @returns {any}
	 */
	private createDataGrids(): any {
		this.taskService.searchComments(this.id, '')
			.subscribe((res) => {
				this.taskCommentsList = res;
				this.dataGridTaskHelper = new DataGridOperationsHelper(this.getTaskWithFilter(), null, null);
				this.dataGridCommentHelper = new DataGridOperationsHelper(this.getCommentsWithFilter(), null, null);
			}, (err) => console.log(err));
	}

	/**
	 * Get the Tasks and change the list based on the Filters being applied
	 * @returns {any}
	 */
	public getTaskWithFilter(): any {
		return this.taskCommentsList
			.filter(comment => this.viewUnpublished || comment.commentInstance.isPublished)
			.filter(comment => comment.commentInstance.commentType === 'issue' || comment.commentInstance.taskNumber)
			.filter(comment => this.showAllTasks || comment.commentInstance.status !== 'Completed');
	}

	/**
	 * Get the Comments and change the list based on the Filters being applied
	 * @returns {any}
	 */
	public getCommentsWithFilter(): any {
		let filteredList = this.taskCommentsList.filter(comment => comment.commentInstance.commentType === 'comment' && comment.commentInstance.dateResolved === null);
		if (this.showAllComments) {
			filteredList = this.taskCommentsList.filter(comment => comment.commentInstance.commentType === 'comment');
		}
		return filteredList;
	}

	public getAssignedTo(comment): any {
		let assignedToRoleLabel = (comment.role ? `/${comment.role}` : '');
		return comment.assignedTo + (comment.commentInstance.commentType === 'comment' ? '' : assignedToRoleLabel);
	}

	/**
	 * Create a task
	 * @param comment
	 */
	public createComment(comment: any): void {
		let singleCommentModel: SingleCommentModel = {
			modal: {
				title: 'Create Comment',
				type: ModalType.CREATE
			},
			archive: false,
			comment: '',
			category: '',
			assetClass: {
				text: this.assetType
			},
			asset: {
				id: this.id,
				text: this.assetName
			}
		};

		this.dialogService.extra(SingleCommentComponent, [
			{provide: SingleCommentModel, useValue: singleCommentModel}
		], false, false).then(result => {
			this.createDataGrids();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Open the Comment Detail or Task Detail
	 * @param comment
	 */
	public openTaskComment(comment: any, modalType: ModalType): void {
		if (comment.commentInstance.taskNumber && comment.commentInstance.taskNumber !== 'null') {
			this.openTaskDetail(comment, modalType);
		} else {
			this.openCommentDetail(comment, modalType);
		}
	}

	/**
	 * Open the Comment Detail
	 * @param comment
	 */
	public openCommentDetail(comment: any, modalType: ModalType): void {
		let singleCommentModel: SingleCommentModel = {
			id: comment.commentInstance.id,
			modal: {
				title: (modalType === ModalType.EDIT) ?  'Edit Comment' :  'Comment Detail',
				type: modalType
			},
			archive: comment.commentInstance.dateResolved !== null,
			comment: comment.commentInstance.comment,
			category: comment.commentInstance.category,
			assetClass: {
				text: comment.assetType,
			},
			asset: {
				id: this.id,
				text: comment.assetName
			},
			lastUpdated: comment.commentInstance.lastUpdated,
			dateCreated: comment.commentInstance.dateCreated
		};

		this.dialogService.extra(SingleCommentComponent, [
			{provide: SingleCommentModel, useValue: singleCommentModel}
		], false, false).then(result => {
			this.createDataGrids();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Open the Task Create
	 * @param dataItem
	 */
	public openTaskCreate(): void {

		let taskCreateModel: TaskDetailModel = {
			id: this.id.toString(),
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE
			},
			detail: {
				assetClass: this.assetClass,
				assetEntity: this.id,
				assetName: this.assetName,
				currentUserId: this.currentUserId
			}
		};

		this.dialogService.extra(TaskCreateComponent, [
			{provide: TaskDetailModel, useValue: taskCreateModel}
		], false, false)
			.then(result => {
				if (result) {
					this.createDataGrids();
				}

			}).catch(result => {
				console.log('Cancel:', result);
			});

	}

	/**
	 * Open the Task Detail
	 * @param dataItem
	 */
	public openTaskDetail(dataItem: any, modalType: ModalType): void {
		let taskDetailModel: TaskDetailModel = {
			id: dataItem.commentInstance.id,
			modal: {
				title: 'Task Detail',
				type: modalType
			},
			detail: {
				currentUserId: this.currentUserId
			}
		};

		this.dialogService.extra(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		], false, false)
			.then(result => {
				if (result) {
					if (result.isDeleted) {
						this.deleteTaskComment(dataItem)
							.then(() => this.createDataGrids())
					} else if (result.commentInstance) {
						this.openTaskDetail(result, ModalType.VIEW);
					}
				}

			}).catch(result => {
				if (result) {
					this.createDataGrids();
				}
			});
	}

	public reloadTasksGrid(): void {
		this.dataGridTaskHelper.reloadData(this.getTaskWithFilter());
	}

	public reloadCommentsGrid(): void {
		this.dataGridCommentHelper.reloadData(this.getCommentsWithFilter());
	}

	/**
	 * Safe the preference as soon a user change the value
	 */
	public onViewUnpublishedChange(): void {
		this.preferenceService.setPreference(PREFERENCES_LIST.VIEW_UNPUBLISHED, this.viewUnpublished.toString()).subscribe();
	}

	/**
	 * Get Preference for the View Unpublished
	 * @returns {Observable<any>}
	 */
	private getPreferences(): void {
		this.preferenceService.getPreferences(PREFERENCES_LIST.VIEW_UNPUBLISHED).subscribe((preferences: any) => {
			this.viewUnpublished =  preferences[PREFERENCES_LIST.VIEW_UNPUBLISHED].toString() ===  'true';
		});
	}

	/**
	 * Prompt for delete the Asset Comment
	 */
	protected onDelete(dataItem: any): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteTaskComment(dataItem);
				}
			})
			.catch((error) => console.log(error));
	}

	/**
	 * Delete the Asset Task/Comment
	 */
	private deleteTaskComment(dataItem: any): Promise<boolean> {
		return new Promise((resolve, reject) => {
			const commentId = dataItem.commentInstance.id;

			this.taskManagerService.deleteTaskComment(commentId).subscribe((res) => {
				// delete the item
				this.dataGridTaskHelper.removeDataItem(dataItem);
				this.dataGridCommentHelper.removeDataItem(dataItem);
				// Reload Grids
				this.dataGridTaskHelper.reloadData(this.dataGridTaskHelper.gridData.data);
				this.dataGridCommentHelper.reloadData(this.dataGridCommentHelper.gridData.data);
				// update task and comment collections
				this.taskCommentsList = this.taskCommentsList.filter((comment) => comment.commentInstance.id !== commentId);
				return resolve(true);
			}, err => reject(false));
		});
	}
}