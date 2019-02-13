import {Component, Inject} from '@angular/core';
import {UIActiveDialogService} from '../../../../../shared/services/ui-dialog.service';
import {AssetModalModel} from '../../../model/asset-modal.model';

@Component({
	selector: `tds-task-comment-dialog`,
	template: `
        <div tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="task-comment-dialog-component modal-content " id="task-comment-dialog-component">
            <div class="modal-header">
                <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
                <h4 *ngIf="assetModalModel.modalType == 'COMMENT'" class="modal-title">{{ 'ASSET_EXPLORER.SHOW_COMMENTS' | translate}}</h4>
                <h4 *ngIf="assetModalModel.modalType == 'TASK'" class="modal-title">{{ 'ASSET_EXPLORER.SHOW_TASKS' | translate}}</h4>
            </div>
            <div class="modal-body tds-angular-component-content">
                <div class="box-body">
                    <!-- -->
                    <td colspan="2">
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
                    </td>
                </div>
            </div>
            <div class="modal-footer form-group-center">
                <button (click)="cancelCloseDialog()"
                        type="button" class="btn btn-default pull-right">
                    <span class="glyphicon glyphicon-ban-circle"></span> {{'GLOBAL.CANCEL' | translate }}
                </button>
            </div>
        </div>

	`
})
export class TaskCommentDialogComponent {
	constructor(
		protected assetModalModel: AssetModalModel,
		public activeDialog: UIActiveDialogService,
		@Inject('currentUserId') private currentUserId: number) {}
	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}