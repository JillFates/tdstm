import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService, UIExtraDialog} from '../../../../../shared/services/ui-dialog.service';
import {AssetModalModel} from '../../../model/asset-modal.model';
import {DecoratorOptions} from '../../../../../shared/model/ui-modal-decorator.model';

@Component({
	selector: `tds-task-comment-dialog`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/task-comment/dialog/task-comment-dialog.component.html'
})
export class TaskCommentDialogComponent extends UIExtraDialog {

	public modalOptions: DecoratorOptions;
	constructor(protected assetModalModel: AssetModalModel) {
		super('#task-comment-dialog-component');
		this.modalOptions = { isResizable: true, isCentered: true };
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.dismiss(false);
	}
}