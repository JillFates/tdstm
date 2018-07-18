import {Component, Input, Output, EventEmitter} from '@angular/core';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';
import {BulkChangeActionsComponent} from '../bulk-change-actions/bulk-change-actions.component';
import {UIDialogService} from '../../../../../../shared/services/ui-dialog.service';
import {BulkChangeModel, BulkOperationResult} from '../../model/bulk-change.model';

@Component({
	selector: 'tds-bulk-change-button',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-button/bulk-change-button.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeButtonComponent {
	@Input() selectedItems: string[];
	@Output() operationResult = new EventEmitter<BulkOperationResult>();

	constructor(private dialogService: UIDialogService) {
		this.selectedItems = [];
	}

	onClick() {
		this.showBulkActions();
	}

	showBulkActions() {
		const bulkChangeModel: BulkChangeModel = { selectedItems: this.selectedItems };

		this.dialogService.extra(BulkChangeActionsComponent, [
			{provide: BulkChangeModel, useValue: bulkChangeModel}
		], true, false).then(bulkOperationResult => {
			this.operationResult.emit(bulkOperationResult);
		}).catch(err => {
			console.log(err);
			console.log('Dismissed Dialog');
			this.operationResult.emit(err);
		});
	}
}