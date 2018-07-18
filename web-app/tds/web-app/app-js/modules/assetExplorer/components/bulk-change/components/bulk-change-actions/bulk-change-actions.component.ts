import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';
import { TranslatePipe } from '../../../../../../shared/pipes/translate.pipe';

import {BulkChangeModel} from '../../model/bulk-change.model';

@Component({
	selector: 'tds-bulk-change-actions',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-actions/bulk-change-actions.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeActionsComponent extends UIExtraDialog {
	selectedItems: string[] = [];

	constructor(private bulkChangeModel: BulkChangeModel) {
		super('#bulk-change-action-component');
		this.selectedItems = this.bulkChangeModel.selectedItems || [];
		console.log('Selected items');
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

}