import { Component, Input, OnInit } from '@angular/core';

import { TaskCommentService } from '../../service/task-comment.service';
// import {ProviderViewEditComponent} from "../../../dataIngestion/components/provider-view-edit/provider-view-edit.component";
// import {ProviderModel} from "../../../dataIngestion/model/provider.model";
import {DialogService} from "@progress/kendo-angular-dialog";

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

	constructor(private taskService: TaskCommentService, private dialogService: DialogService) { }

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

	public openCommentDetail(): void {
		/* this.dialogService.open(ProviderViewEditComponent, [
			{ provide: ProviderModel, useValue: providerModel },
			{ provide: Number, useValue: actionType}
		]).then(result => {
			// update the list to reflect changes, it keeps the filter
			this.reloadData();
		}).catch(result => {
			console.log('Dismissed Dialog');
		}); */
	}
}