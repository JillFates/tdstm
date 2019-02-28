import { Component, Input, Output, EventEmitter } from '@angular/core';
import {UIExtraDialog} from '../../../../services/ui-dialog.service';
import {TranslatePipe} from '../../../../pipes/translate.pipe';

import {BulkChangeModel, BulkChangeType} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../directives/ui-prompt.directive';
import {UIDialogService} from '../../../../services/ui-dialog.service';
import {BulkActions, BulkActionResult} from '../../model/bulk-change.model';
import {BulkChangeService} from '../../../../services/bulk-change.service';
import {Permission} from '../../../../model/permission.model';
import {PermissionService} from '../../../../services/permission.service';
import {BulkChangeEditComponent} from '../bulk-change-edit/bulk-change-edit.component';

@Component({
	selector: 'tds-bulk-change-actions',
	templateUrl: 'bulk-change-actions.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeActionsComponent extends UIExtraDialog {
	protected bulkChangeType: BulkChangeType;
	protected selectedItems: number[] = [];
	protected selectedAction: BulkActions;
	protected affected: number;
	protected ACTION = BulkActions; // Make enum visible to the view
	protected ACTION_TYPE = BulkChangeType;
	protected itemType: string;
	public showEdit: boolean;
	public showDelete: boolean;

	constructor(
		private bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private bulkChangeService: BulkChangeService,
		private permissionService: PermissionService,
		private dialogService: UIDialogService,
		private translatePipe: TranslatePipe) {
			super('#bulk-change-action-component');
			this.selectedItems = this.bulkChangeModel.selectedItems || [];
			this.affected = this.bulkChangeModel.affected;
			this.selectedAction = this.bulkChangeModel.showEdit ? this.ACTION.Edit : this.ACTION.Delete;
			this.showDelete = this.bulkChangeModel.showDelete;
			this.showEdit = this.bulkChangeModel.showEdit;
			this.bulkChangeType = this.bulkChangeModel.bulkChangeType;
			this.itemType =  this.bulkChangeType === BulkChangeType.Assets ?
				this.getSinglePluralAssetName() : this.getSinglePluralDependenceName()
	}

	public cancelCloseDialog(bulkOperationResult: BulkActionResult): void {
		this.dismiss(bulkOperationResult || {action: null, success: false});
	}

	public closeDialog(bulkOperationResult: BulkActionResult): void {
		this.close(bulkOperationResult);
	}

	onNext(): void {
		(this.selectedAction === this.ACTION.Delete) ? this.deleteAction() : this.editAction();
	}

	private editAction(): void {
		const bulkChangeModel: BulkChangeModel = {
			selectedItems: this.selectedItems,
			selectedAssets: this.bulkChangeModel.selectedAssets,
			affected: this.bulkChangeModel.affected
		};

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
		const translationKey = this.bulkChangeType === BulkChangeType.Assets
			? 'ASSET_EXPLORER.BULK_CHANGE.DELETE.CONFIRM_DELETE_ASSETS'
			: 'ASSET_EXPLORER.BULK_CHANGE.DELETE.CONFIRM_DELETE_DEPENDENCIES';

		const singleOrPluralName = this.bulkChangeType === BulkChangeType.Assets ?
			this.getSinglePluralAssetName() : this.getSinglePluralDependenceName();

		const message = this.translatePipe.transform(translationKey,
			[this.affected, singleOrPluralName, singleOrPluralName]);
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
		return new Promise((resolve, reject) =>  {
			if (this.hasAssetDeletePermission()) {
				this.bulkChangeService
					.bulkDelete(this.bulkChangeType, items)
					.subscribe(
						result => resolve({action: BulkActions.Delete, success: true, message: result.message || result.resp}),
						err => reject({action: BulkActions.Delete, success: false, message: err.message || err}));
			} else {
				reject({action: BulkActions.Delete, success: false, message: 'Forbidden operation' });
			}

		});

	}

	private hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	/*
	* Based on effected elements, get the single/plural entity name
	 */
	protected getSinglePluralDependenceName() {
		const targetKey = this.affected === 1 ?
			'DEPENDENCIES.SINGLE_NAME' : 'DEPENDENCIES.PLURAL_NAME';

		return this.translatePipe.transform(targetKey);
	}

	/*
	* Based on effected elements, get the single/plural asset name
	 */
	protected getSinglePluralAssetName() {
		const targetKey = this.affected === 1 ?
			'ASSETS.SINGLE_NAME' : 'ASSETS.PLURAL_NAME';

		return this.translatePipe.transform(targetKey);
	}

}