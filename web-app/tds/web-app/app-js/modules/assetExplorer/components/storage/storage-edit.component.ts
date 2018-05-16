/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, Inject, OnInit } from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';
import {AssetShowComponent} from '../asset/asset-show.component';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';

declare var jQuery: any;

export function StorageEditComponent(template: string, editModel: any): any {
	@Component({
		selector: 'storage-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class StorageShowComponent implements  OnInit {
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService,
			private assetExplorerService: AssetExplorerService,
			private dialogService: UIDialogService,
			private notifierService: NotifierService) {
		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onUpdate(): void {
			let modelRequest = R.clone(this.model);
			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;
			// Date Formats
			// modelRequest.asset.maintExpDate = DateUtils.translateTimeZoneFormat(modelRequest.asset.maintExpDate);
			// modelRequest.asset.retireDate
			this.assetExplorerService.saveAsset(modelRequest).subscribe((res) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
			});
		}

		private showAssetDetailView(assetClass: string, id: number) {
			this.dialogService.replace(AssetShowComponent, [
					{ provide: 'ID', useValue: id },
					{ provide: 'ASSET', useValue: assetClass }],
				'lg');
		}
	}

	return StorageShowComponent;
}