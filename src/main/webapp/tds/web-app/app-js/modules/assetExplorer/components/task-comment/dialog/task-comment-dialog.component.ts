// Angular
import {Component, Input, OnInit} from '@angular/core';
// Model
import {AssetModalModel} from '../../../model/asset-modal.model';
import {Dialog} from 'tds-component-library';
// Service
import {UIActiveDialogService} from '../../../../../shared/services/ui-dialog.service';
// Other
import * as R from 'ramda';

@Component({
	selector: `tds-task-comment-dialog`,
	template: `
		<div class="task-comment-dialog-component">
			<div class="clr-row">
				<div class="clr-col-12">
					<task-comment *ngIf="assetModalModel.modalType == 'COMMENT'"
								  [asset-id]="assetModalModel.assetId"
								  [has-publish-permission]="true"
								  [can-edit-comments]="true"
								  [can-edit-tasks]="true"
								  [asset-name]="assetModalModel.assetName"
								  [asset-type]="assetModalModel.assetType"
								  [show-comment]="true"
								  [user-id]="currentUserId">
					</task-comment>
				</div>
			</div>
			<div class="clr-row">
				<div class="clr-col-12">
					<task-comment *ngIf="assetModalModel.modalType == 'TASK'"
								  [asset-id]="assetModalModel.assetId"
								  [has-publish-permission]="true"
								  [can-edit-comments]="true"
								  [can-edit-tasks]="true"
								  [asset-name]="assetModalModel.assetName"
								  [asset-type]="assetModalModel.assetType"
								  [show-task]="true"
								  [user-id]="currentUserId">
					</task-comment>
				</div>
			</div>
		</div>
	`
})
export class TaskCommentDialogComponent extends Dialog implements OnInit {
	@Input() data: any;
	public assetModalModel: AssetModalModel;
	public currentUserId: number;
	constructor(
		public activeDialog: UIActiveDialogService) {
		super();
	}

	ngOnInit(): void {
		this.assetModalModel = R.clone(this.data.assetModalModel);
		this.currentUserId = R.clone(this.data.currentUserId);
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		super.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
