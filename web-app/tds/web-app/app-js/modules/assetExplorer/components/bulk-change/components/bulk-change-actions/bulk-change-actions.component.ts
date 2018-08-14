import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';
import { TranslatePipe } from '../../../../../../shared/pipes/translate.pipe';

import {BulkChangeModel} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../../../shared/directives/ui-prompt.directive';
import {UIDialogService} from '../../../../../../shared/services/ui-dialog.service';
import {BulkActions, BulkActionResult} from '../../model/bulk-change.model';
import {AssetExplorerService} from '../../../../service/asset-explorer.service';
import {Permission} from '../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../shared/services/permission.service';
import {BulkChangeEditComponent} from '../bulk-change-edit/bulk-change-edit.component';

@Component({
	selector: 'tds-bulk-change-actions',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-actions/bulk-change-actions.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeActionsComponent extends UIExtraDialog {
	selectedItems: number[] = [];
	selectedAction: BulkActions;
	affected: number;
	ACTION = BulkActions; // Make enum visible to the view

	constructor(
		private bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService,
		private dialogService: UIDialogService,
		private translatePipe: TranslatePipe
	) {
		super('#bulk-change-action-component');
		this.selectedItems = this.bulkChangeModel.selectedItems || [];
		this.affected = this.bulkChangeModel.affected;
		this.selectedAction = this.ACTION.Edit;
	}

	cancelCloseDialog(bulkOperationResult: BulkActionResult): void {
		this.dismiss(bulkOperationResult || {action: null, success: false});
	}

	closeDialog(bulkOperationResult: BulkActionResult): void {
		this.close(bulkOperationResult);
	}

	onNext(): void {
		(this.selectedAction === this.ACTION.Delete) ? this.deleteAction() : this.editAction();
	}

	private editAction(): void {
		const bulkChangeModel: BulkChangeModel = { selectedItems: this.selectedItems, affected: this.bulkChangeModel.affected };

		this.dialogService.extra(BulkChangeEditComponent, [
			UIDialogService,
			{provide: BulkChangeModel, useValue: bulkChangeModel}
		], true, false).then(bulkOperationResult => {
			this.closeDialog(bulkOperationResult);
		}).catch(err => {
			this.cancelCloseDialog(err);
		});
	}

	private deleteAction(): void  {
		this.confirmDelete()
			.then(this.deleteBulk.bind(this))
			.then(this.closeDialog.bind(this))
			.catch((result) => this.cancelCloseDialog(result))
	}

	private confirmDelete(): Promise<boolean> {
		const message = this.translatePipe.transform('ASSET_EXPLORER.BULK_CHANGE.DELETE.CONFIRM_DELETE', [this.affected]);
		return new Promise((resolve, reject) =>  {
			this.promptService.open(this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				message,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then((result) => result ? resolve() : reject({action: BulkActions.Delete, success: false, message: 'canceled'}))
		})
	}

	private deleteBulk(): Promise<BulkActionResult> {
		const items = this.selectedItems.map((value: number) => value.toString());
		// return Promise.resolve({action: BulkActions.Delete, success: true});
		return new Promise((resolve, reject) =>  {
			if (this.hasAssetDeletePermission()) {
				this.assetExplorerService.deleteAssets(items)
					.subscribe((result) =>  {
						resolve({action: BulkActions.Delete, success: true, message: result.message});
					}, err => reject({action: BulkActions.Delete, success: false, message: err.message || err}))
			} else {
				reject({action: BulkActions.Delete, success: false, message: 'Forbidden operation' });
			}

		});

	}

	private hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

}