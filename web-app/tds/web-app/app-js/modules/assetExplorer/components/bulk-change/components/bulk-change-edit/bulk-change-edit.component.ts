import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';

import {BulkChangeModel} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../../../shared/directives/ui-prompt.directive';
import {BulkActions, BulkOperationResult} from '../../model/bulk-change.model';
import {AssetExplorerService} from '../../../../service/asset-explorer.service';
import {Permission} from '../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../shared/services/permission.service';
import {BulkChangeEditColumnsModel} from '../../model/bulk-change-edit-columns.model';

@Component({
	selector: 'tds-bulk-change-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-edit/bulk-change-edit.component.html'
})
export class BulkChangeEditComponent extends UIExtraDialog implements OnInit {
	selectedItems: string[] = [];
	selectedAction: BulkActions;
	ACTION = BulkActions; // Make enum visible to the view
	gridColumns: BulkChangeEditColumnsModel;
	constructor(private bulkChangeModel: BulkChangeModel, private promptService: UIPromptService, private assetExplorerService: AssetExplorerService, private permissionService: PermissionService) {
		super('#bulk-change-edit-component');

		/*
		this.selectedItems = this.bulkChangeModel.selectedItems || [];
		this.selectedAction = this.ACTION.Edit;
		*/
		console.log('Selected items');
	}

	ngOnInit() {
		this.gridColumns = new BulkChangeEditColumnsModel();
	}

	/**
	 * Close the Dialog
	 */
	cancelCloseDialog(bulkOperationResult: BulkOperationResult): void {
		this.dismiss(bulkOperationResult || {action: null, success: false});
	}

	closeDialog(bulkOperationResult: BulkOperationResult): void {
		this.close(bulkOperationResult);
	}

	onNext() {
		alert('Editing');
	}

	hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

}