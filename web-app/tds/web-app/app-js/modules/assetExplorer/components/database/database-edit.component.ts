/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';
import {AssetShowComponent} from "../asset/asset-show.component";

declare var jQuery: any;

export function DatabaseEditComponent(template, editModel) {

	@Component({
		selector: `database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DatabaseShowComponent implements OnInit {

		private dateFormat: string;

		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService,
			private assetExplorerService: AssetExplorerService,
			private dialogService: UIDialogService) {

			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');

			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);

			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}
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
			this.assetExplorerService.saveAsset(modelRequest).subscribe((res) => {
				console.log(res);
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
	return DatabaseShowComponent;
}