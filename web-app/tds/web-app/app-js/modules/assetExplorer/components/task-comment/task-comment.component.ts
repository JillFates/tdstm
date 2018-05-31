import {Component, Input, OnInit} from '@angular/core';

import {TaskCommentService} from '../../service/task-comment.service';
import {SingleCommentComponent} from '../single-comment/single-comment.component';
import {SingleCommentModel} from '../single-comment/single-comment.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ModalType} from '../../../../shared/model/constants';

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

	showAll: boolean;
	comments: any[] = [];

	constructor(private taskService: TaskCommentService, private dialogService: UIDialogService) {
	}

	ngOnInit(): void {
		this.showAll = this.prefValue;
		this.taskService.searchComments(this.id, '')
			.subscribe((res) => {
				this.comments = res;
			}, (err) => console.log(err));
	}

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

	public openCommentDetail(comment: any): void {
		let singleCommentModel: SingleCommentModel = {
			modal: {
				title: 'Comment Detail',
				type: ModalType.VIEW
			},
			archive: comment.commentInstance.isResolved !== 0,
			comment: comment.commentInstance.comment,
			category: comment.commentInstance.category,
			assetType: comment.assetType,
			asset: {
				name: comment.assetName,
				classType: 'DATABASE'
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
}