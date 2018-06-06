import {Component, Input, OnInit} from '@angular/core';

import {TaskCommentService} from '../../service/task-comment.service';
import {SingleCommentComponent} from '../single-comment/single-comment.component';
import {SingleCommentModel} from '../single-comment/single-comment.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ModalType} from '../../../../shared/model/constants';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskCommentColumnsModel} from './model/task-comment-columns.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TaskService} from '../../../taskManager/service/task.service';

@Component({
	selector: `task-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/task-comment/task-comment.component.html',
	styles: []
})
export class TaskCommentComponent implements OnInit {
	@Input('asset-id') id: number;
	@Input('pref-value') prefValue ? = false;
	@Input('view-unpublished-value') viewUnpublishedValue ? = false;
	@Input('has-publish-permission') hasPublishPermission ? = false;
	@Input('can-edit-comments') canEdit ? = false;
	@Input('can-edit-tasks') canEditTasks ? = false;

	private dataGridTaskCommentOnHelper: DataGridOperationsHelper;
	private taskCommentColumnModel = new TaskCommentColumnsModel();
	private modalType = ModalType;

	private showAll: boolean;
	private comments: any[] = [];

	constructor(private taskService: TaskCommentService, private dialogService: UIDialogService, public promptService: UIPromptService, public taskManagerService: TaskService) {
	}

	ngOnInit(): void {
		this.showAll = this.prefValue;
		this.getAllComments();
	}

	/**
	 * Get all comments
	 * @returns {any}
	 */
	private getAllComments(): any {
		this.taskService.searchComments(this.id, '')
			.subscribe((res) => {
				this.comments = res;
				this.dataGridTaskCommentOnHelper = new DataGridOperationsHelper(this.getCommentsWithFilter(), null, null);
			}, (err) => console.log(err));
	}

	/**
	 * Change the list based on the Filters being applied
	 * @returns {any}
	 */
	public getCommentsWithFilter(): any {
		return this.comments
			.filter(comment => this.viewUnpublishedValue || comment.commentInstance.isPublished)
			.filter(comment => this.showAll
				|| (comment.commentInstance.commentType === 'issue' && comment.commentInstance.status !== 'Completed')
				|| (comment.commentInstance.commentType === 'comment' && !comment.commentInstance.isResolved));
	}

	public getAssignedTo(comment): any {
		return comment.assignedTo + (comment.commentInstance.commentType === 'comment' ? '' : `/${comment.role}`);
	}

	/**
	 * Create a new comment
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
				id: '',
				text: ''
			},
			asset: {
				id: '',
				text: ''
			}
		};

		this.dialogService.extra(SingleCommentComponent, [
			{provide: SingleCommentModel, useValue: singleCommentModel}
		], true, false).then(result => {
			console.log('Success');
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Open the Comment Detail or Task Detail
	 * @param comment
	 */
	public openComment(comment: any, modalType: ModalType): void {
		let singleCommentModel: SingleCommentModel = {
			id: comment.commentInstance.id,
			modal: {
				title: 'Comment Detail',
				type: modalType
			},
			archive: comment.commentInstance.isResolved !== 0,
			comment: comment.commentInstance.comment,
			category: comment.commentInstance.category,
			assetClass: {
				text: comment.assetType,
			},
			asset: {
				id: '0',
				text: comment.assetName
			},
			lastUpdated: comment.commentInstance.lastUpdated,
			dateCreated: comment.commentInstance.dateCreated
		};

		this.dialogService.extra(SingleCommentComponent, [
			{provide: SingleCommentModel, useValue: singleCommentModel}
		], true, false).then(result => {
			this.getAllComments();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	public reloadGrid(): void {
		this.dataGridTaskCommentOnHelper.reloadData(this.getCommentsWithFilter());
	}

	/**
	 * Delete the Asset Comment
	 */
	protected onDelete(dataItem: any): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.taskManagerService.deleteTaskComment(dataItem.commentInstance.id).subscribe((res) => {
						// delete the item
						this.dataGridTaskCommentOnHelper.removeDataItem(dataItem);
						this.dataGridTaskCommentOnHelper.reloadData(this.dataGridTaskCommentOnHelper.gridData.data);
					});
				}
			})
			.catch((error) => console.log(error));
	}
}