import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';

declare var jQuery: any;

export function DeviceShowComponent(template) {
	@Component({
		selector: `device-show`,
		template: template
	}) class DeviceShowComponent implements OnInit {
		constructor(private activeDialog: UIActiveDialogService, private dialogService: UIDialogService) {

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
			this.dialogService.replace(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
				'lg');
		}

	}
	return DeviceShowComponent;
}