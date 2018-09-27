/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

declare var jQuery: any;

export function StorageCreateComponent(template: string, metadata: any): any {
	@Component({
		selector: 'tds-storage-create',
		template: template,
		providers: [
			{ provide: 'model', useValue: { asset: {} } }
		]
	})
	class StorageCreateComponent extends AssetCommonEdit {

		constructor(
			@Inject('model') model: any,
			activeDialog: UIActiveDialogService,
			preference: PreferenceService,
			assetExplorerService: AssetExplorerService,
			dialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService) {

			super(model, activeDialog, preference, assetExplorerService, dialogService, notifierService, tagService, metadata, promptService);
			this.initModel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			} else {
				this.model.asset.scale.name = { value: this.model.asset.scale.name, text: ''}
			}
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onCreate(): void {
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
			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}

	}

	return StorageCreateComponent;
}
