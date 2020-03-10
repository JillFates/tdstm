// Angular
import {Component, Input, OnInit} from '@angular/core';
// Model
import {AssetModalModel} from '../../../model/asset-modal.model';
import {Dialog, DialogButtonType} from 'tds-component-library';
// Other
import * as R from 'ramda';
import {ActionType} from '../../../../../shared/model/data-list-grid.model';
import {TranslatePipe} from '../../../../../shared/pipes/translate.pipe';

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
	constructor(private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit(): void {
		this.assetModalModel = R.clone(this.data.assetModalModel);
		this.currentUserId = R.clone(this.data.currentUserId);

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
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

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(): string {
		let title = '';
		if (this.assetModalModel.modalType === 'COMMENT') {
			title = this.translatePipe.transform('ASSET_EXPLORER.SHOW_COMMENTS');
		} else if (this.assetModalModel.modalType === 'TASK') {
			title = this.translatePipe.transform('ASSET_EXPLORER.SHOW_TASKS');
		}
		return title;
	}
}
