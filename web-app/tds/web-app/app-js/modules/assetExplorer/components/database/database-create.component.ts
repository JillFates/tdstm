/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';

declare var jQuery: any;

export function DatabaseCreateComponent(template, model: any, metadata: any) {

	@Component({
		selector: `tds-database-create`,
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	}) class DatabaseCreateComponent extends AssetCommonEdit {

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

			this.model.asset.retireDate = null;
			this.model.asset.maintExpDate = null;
			this.model.asset.scale = {
				name: ''
			};

			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.model.asset.planStatus = this.model.planStatusOptions[0];
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onCreate(): void {
			let modelRequest = R.clone(this.model);
			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;
			// this.model.customs.forEach((custom: any) => {
			// 	let customValue = modelRequest.asset[custom.field.toString()];
			// 	if (customValue && customValue.value) {
			// 		modelRequest.asset[custom.field.toString()] = customValue.value;
			// 	}
			// });
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;

			modelRequest.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.DATABASE
			};

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

	return DatabaseCreateComponent;
}