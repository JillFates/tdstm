/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */

import * as R from 'ramda';
import {Component, Inject, OnInit } from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
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
	}) class DeviceCreateComponent extends DeviceCommonComponent implements OnInit {
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

		}

		ngOnInit() {
			this.initModel();
			this.toggleAssetTypeFields();
			this.focusControlByName('assetName');
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.sourceRoomSelect = [this.defaultRoom, ...this.model.sourceRoomSelect];
			this.model.targetRoomSelect = [this.defaultRoom, ...this.model.targetRoomSelect];
			this.model.rackSourceOptions = [this.defaultRoom, ...this.model.rackSourceOptions];
			this.model.rackTargetOptions = [this.defaultRoom, ...this.model.rackTargetOptions];
			this.model.bladeSourceOptions = [this.defaultRoom, ...this.model.bladeSourceOptions];
			this.model.bladeTargetOptions = [this.defaultRoom, ...this.model.bladeTargetOptions];

			this.model.asset.assetTypeSelectValue = {id: null};
			this.model.asset.manufacturerSelectValue = {id: null};
			this.model.asset.modelSelectValue = {id: null};
			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.model.asset.planStatus = this.defaultPlanStatus;
			this.model.asset.validation = this.defaultValidation;
			this.model.asset.environment = '';
			this.model.asset.priority = '';
			this.model.asset.roomSource = this.defaultRoom;
			this.model.asset.roomTarget = this.defaultRoom;
			this.model.asset.rackSource = this.defaultRoom;
			this.model.asset.rackTarget = this.defaultRoom;
			this.model.sourceChassis = this.defaultRoom;
			this.model.targetChassis = this.defaultRoom;
			this.model.asset.scale = null;
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		private onCreate(): void {
			let modelRequest = R.clone(this.model);

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