import {Component, Inject} from '@angular/core';
import {UIActiveDialogService} from '../../../../../shared/services/ui-dialog.service';
import {AssetModalModel} from '../../../model/asset-modal.model';

@Component({
	selector: `tds-task-comment-dialog`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/task-comment/dialog/task-comment-dialog.component.html'
})
export class TaskCommentDialogComponent {

	constructor(protected assetModalModel: AssetModalModel,
	            public activeDialog: UIActiveDialogService,
	            @Inject('currentUserId') private currentUserId: number) {}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}