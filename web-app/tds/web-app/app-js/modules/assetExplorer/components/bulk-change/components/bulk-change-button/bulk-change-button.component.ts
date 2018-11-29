import {Component, Input, Output, EventEmitter} from '@angular/core';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';
import {BulkChangeActionsComponent} from '../bulk-change-actions/bulk-change-actions.component';
import {UIDialogService} from '../../../../../../shared/services/ui-dialog.service';
import {BulkChangeModel, BulkActionResult} from '../../model/bulk-change.model';

@Component({
	selector: 'tds-bulk-change-button',
	template: `
        <button type="button" (click)="onClick()" class="btn btn-default btnBulkChange pull-left" id="btnBulkChange" [disabled]="!enabled">
            <span class="glyphicon glyphicon-option-vertical" aria-hidden="true"></span>  {{'ASSET_EXPLORER.BULK_CHANGE.TITLE' | translate}}
        </button>
	`,
	providers: [TranslatePipe]
})
export class BulkChangeButtonComponent {
	@Input() enabled: boolean ;
	@Output() operationResult = new EventEmitter<BulkActionResult>();
	@Output() clickBulk = new EventEmitter<void>();

	private selectedItems: number[];
	private selectedAssets: Array<any>;

	constructor(private dialogService: UIDialogService) {
		this.enabled = false;
		this.selectedItems = [];
		this.selectedAssets = [];
	}

	onClick() {
		this.clickBulk.emit();
	}

	/**
	 * Opens the dialog windows to Bulk Data
	 * @param data
	 */
	public bulkData(data: any): void {
		this.selectedItems = data && data.bulkItems ? data.bulkItems : null;
		this.selectedAssets = data && data.assetsSelectedForBulk ? data.assetsSelectedForBulk : null;
		if (this.selectedItems && this.selectedItems.length) {
			this.showBulkActions();
		}
	}

	showBulkActions() {
		const bulkChangeModel: BulkChangeModel = { selectedItems: this.selectedItems, selectedAssets: this.selectedAssets, affected: this.selectedItems.length };
		this.dialogService.extra(BulkChangeActionsComponent, [
			{provide: BulkChangeModel, useValue: bulkChangeModel}
		], true, false).then(bulkOperationResult => {
			this.operationResult.emit(bulkOperationResult);
		}).catch(err => {
			this.operationResult.emit(err);
		});
	}
}