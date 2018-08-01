import {Component, Input, Output, EventEmitter} from '@angular/core';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';
import {BulkChangeActionsComponent} from '../bulk-change-actions/bulk-change-actions.component';
import {UIDialogService} from '../../../../../../shared/services/ui-dialog.service';
import {BulkChangeModel, BulkActionResult} from '../../model/bulk-change.model';

@Component({
	selector: 'tds-bulk-change-button',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-button/bulk-change-button.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeButtonComponent {
	@Input() selectedItems: number[];
	@Input() affected: number;
	@Output() operationResult = new EventEmitter<BulkActionResult>();

	constructor(private dialogService: UIDialogService) {
		this.selectedItems = [];
	}

	onClick() {
		this.showBulkActions();
	}

	showBulkActions() {
		const bulkChangeModel: BulkChangeModel = { selectedItems: this.selectedItems, affected: this.affected };

		this.dialogService.extra(BulkChangeActionsComponent, [
			{provide: BulkChangeModel, useValue: bulkChangeModel}
		], true, false).then(bulkOperationResult => {
			this.operationResult.emit(bulkOperationResult);
		}).catch(err => {
			console.log(err);
			this.operationResult.emit(err);
		});
	}
}