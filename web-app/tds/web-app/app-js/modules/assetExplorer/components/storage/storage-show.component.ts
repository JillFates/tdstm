import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

export function StorageShowComponent(template) {
	@Component({
		selector: `storage-show`,
		template: template
	}) class StorageShowComponent implements OnInit {
		constructor(private activeDialog: UIActiveDialogService) {

		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		cancelCloseDialog(): void {
			this.activeDialog.dismiss();
		}

		showAssetDetailView(assetClass: string, id: number) {
			this.activeDialog.close({
				assetClass: assetClass,
				id: id
			});
		}

	}
	return StorageShowComponent;
}