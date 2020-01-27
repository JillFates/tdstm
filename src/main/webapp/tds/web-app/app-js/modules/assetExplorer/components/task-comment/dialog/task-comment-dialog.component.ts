import {Component, Inject} from '@angular/core';
import {UIActiveDialogService} from '../../../../../shared/services/ui-dialog.service';
import {AssetModalModel} from '../../../model/asset-modal.model';

@Component({
	selector: `tds-task-comment-dialog`,
	template: `
        <div tds-autofocus
             tds-handle-escape (escPressed)="cancelCloseDialog()"
             class="tds-modal-content has-side-nav tds-angular-component-content task-comment-dialog-component"
             id="task-comment-dialog-component">
            <div class="modal-header">
                <tds-button-close aria-label="Close" class="close" icon="close" [flat]="true"
                                  (click)="cancelCloseDialog()">
                </tds-button-close>
                <h4 *ngIf="assetModalModel.modalType == 'COMMENT'" class="modal-title">{{ 'ASSET_EXPLORER.SHOW_COMMENTS' | translate}}</h4>
                <h4 *ngIf="assetModalModel.modalType == 'TASK'" class="modal-title">{{ 'ASSET_EXPLORER.SHOW_TASKS' | translate}}</h4>
            </div>
            <div class="modal-body">
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
            <div class="modal-sidenav form-group-center">
                <nav class="modal-sidenav btn-link">
                    <tds-button-close tooltip="Close" (click)="cancelCloseDialog()"></tds-button-close>
                </nav>
            </div>
        </div>

	`
})
export class TaskCommentDialogComponent {
	constructor(
		public assetModalModel: AssetModalModel,
		public activeDialog: UIActiveDialogService,
		@Inject('currentUserId') private currentUserId: number) {}
	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
