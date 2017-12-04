import { Component, Input, OnInit } from '@angular/core';

import { TaskCommentService } from '../../service/task-comment.service';

@Component({
	selector: `task-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/task-comment/task-comment.component.html',
	styles: [`
	.headerwidth3 {
		width: 30px;
	}
	.headerwidth4 {
		width: 40px;
	}
	.headerwidth6 {
		width: 60px;
	}
	.headerwidth10 {
		width: 100px;
	}
	.headerwidth12 {
		width: 120px;
	}
	.headerwidth13 {
		width: 130px;
	}
	.headerwidth20 {
		width: 200px;
	}
	`]
})
export class TaskCommentComponent implements OnInit {
	@Input('asset-id') id: number;
	@Input('pref-value') prefValue?= false;
	@Input('view-unpublished-value') viewUnpublishedValue?= false;
	@Input('has-publish-permission') hasPublishPermission?= false;
	@Input('can-edit-comments') canEdit?= false;
	@Input('can-edit-tasks') canEditTasks?= false;

	showAll: boolean;
	comments: any[] = [];

	constructor(private taskService: TaskCommentService) { }

	ngOnInit(): void {
		this.showAll = this.prefValue;
		this.taskService.searchComments(this.id, '')
			.subscribe((res) => {
				this.comments = res;
			}, (err) => console.log(err));
	}

	getCommentsWithFilter() {
		return this.comments
			.filter(comment => this.viewUnpublishedValue || comment.commentInstance.isPublished)
			.filter(comment => this.showAll
				|| (comment.commentInstance.commentType === 'issue' && comment.commentInstance.status !== 'Completed')
				|| (comment.commentInstance.commentType === 'comment' && !comment.commentInstance.isResolved));
	}

	getAssignedTo(comment) {
		return comment.assignedTo + (comment.commentInstance.commentType === 'comment' ? '' : `/${comment.role}`);
	}

}