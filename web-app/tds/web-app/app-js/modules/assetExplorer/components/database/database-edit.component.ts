/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';

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
			private assetExplorerService: AssetExplorerService) {

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

		public onUpdate(): void {
			this.assetExplorerService.saveAsset(this.model).subscribe((res) => {
				console.log(res);
			});
		}
	}
	return DatabaseShowComponent;
}