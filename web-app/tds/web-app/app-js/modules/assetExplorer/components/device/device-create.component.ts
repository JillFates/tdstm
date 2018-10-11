/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */

import * as R from 'ramda';
import {Component, Inject} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';
import {DeviceCommonComponent} from './model-device/device-common.component';

declare var jQuery: any;

export function DeviceCreateComponent(template, model: any, metadata: any) {

	@Component({
		selector: `tds-device-create`,
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	}) class DeviceCreateComponent extends DeviceCommonComponent {

		nameValidation: boolean;
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
			this.toggleAssetTypeFields();

		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.asset = {}; // R.clone(editModel.asset);
			if (!this.model.asset.scale || this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: { value: '', text: ''}
				};
			} else {
				this.model.asset.scale.name = { value: this.model.asset.scale.name, text: ''}
			}
			this.model.asset.assetTypeSelectValue = {id: null};
			this.model.asset.manufacturerSelectValue = {id: null};
			this.model.asset.modelSelectValue = {id: null};
			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.model.asset.planStatus = this.model.planStatusOptions[0];
			this.model.asset.environment = this.model.environmentOptions[0];
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		private onCreate(): void {
			let modelRequest = R.clone(this.model);

			if (!this.validateAssetRequiredValues(modelRequest)) {
				this.nameValidation = true;
				return;
			}
			this.prepareModelRequestToSave(modelRequest);

			// MoveBundle
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;

			// AssetClass
			modelRequest.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.DEVICE
			};
			this.model.asset.assetClass = modelRequest.asset.assetClass;

			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result.id && !isNaN(result.id) && result.id > 0) {
					this.createTags(result.id);
				}
			});
		}
	}
	return DeviceCreateComponent;
}