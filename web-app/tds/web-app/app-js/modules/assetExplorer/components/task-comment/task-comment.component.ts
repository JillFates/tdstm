import {Component, Input, OnInit} from '@angular/core';

import {TaskCommentService} from '../../service/task-comment.service';
import {SingleCommentComponent} from '../single-comment/single-comment.component';
import {SingleCommentModel} from '../single-comment/single-comment.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ModalType} from '../../../../shared/model/constants';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskCommentColumnsModel} from './model/task-comment-columns.model';

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

	constructor(private taskService: TaskCommentService, private dialogService: UIDialogService) {
	}

	ngOnInit(): void {
		this.showAll = this.prefValue;
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
			console.log('Success');
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Delete the selected element
	 */
	public onDeleteTaskComment(dataItem: any): void {
		this.dataGridTaskCommentOnHelper.removeDataItem(dataItem);
	}

	public reloadGrid(): void {
		this.dataGridTaskCommentOnHelper.reloadData(this.getCommentsWithFilter());
	}
}