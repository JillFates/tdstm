/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */

import * as R from 'ramda';
import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs';
import {ComboBoxSearchResultModel} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TagService} from '../../../assetTags/service/tag.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DeviceCommonComponent} from './model-device/device-common.component';

declare var jQuery: any;

export function DeviceEditComponent(template, editModel, metadata: any) {

	@Component({
		selector: `tds-device-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DeviceEditComponent extends DeviceCommonComponent implements OnInit {
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
			this.model.asset = R.clone(editModel.asset);

			this.model.sourceRoomSelect = [this.defaultRoom, ...this.model.sourceRoomSelect];
			this.model.targetRoomSelect = [this.defaultRoom, ...this.model.targetRoomSelect];

			this.model.asset.priority = (this.model.asset.priority || '').toString();

			if (this.model.asset.scale && this.model.asset.scale.name) {
				this.model.asset.scale = { value: this.model.asset.scale.name, text: ''}
			}

			this.model.asset.assetTypeSelectValue = {id: null};
			if (this.model.asset.assetType) {
				this.model.asset.assetTypeSelectValue.id = this.model.asset.assetType;
				this.model.asset.assetTypeSelectValue.text = this.model.asset.assetType;
			}
			this.model.asset.manufacturerSelectValue = {id: null};
			if (this.model.asset.manufacturer) {
				this.model.asset.manufacturerSelectValue.id = this.model.asset.manufacturer.id;
				this.model.asset.manufacturerSelectValue.text = this.model.manufacturerName;
			}
			this.model.asset.modelSelectValue = {id: null};
			if (this.model.asset.model) {
				this.model.asset.modelSelectValue.id = this.model.asset.model.id;
				this.model.asset.modelSelectValue.text = this.model.modelName;
			}
			if (this.model.sourceRackSelect) {
				this.rackSourceOptions = [this.defaultRoom,  ...this.model.sourceRackSelect];
			}
			if (this.model.targetRackSelect) {
				this.rackTargetOptions = [this.defaultRoom,  ...this.model.targetRackSelect];
			}
			if (this.model.sourceChassisSelect) {
				this.bladeSourceOptions = [this.defaultRoom, ...this.model.sourceChassisSelect];
			}
			if (this.model.targetChassisSelect) {
				this.bladeTargetOptions = [this.defaultRoom, this.model.targetChassisSelect];
			}

			this.model.asset.roomSource = this.model.asset.roomSource || this.defaultRoom;
			this.model.asset.roomTarget = this.model.asset.roomTarget ||  this.defaultRoom;
			this.model.asset.rackSource = this.model.asset.rackSource || this.defaultRoom;
			this.model.asset.rackTarget = this.model.asset.rackTarget || this.defaultRoom;
			this.model.asset.sourceChassis = this.model.asset.sourceChassis || this.defaultRoom;
			this.model.asset.targetChassis = this.model.asset.targetChassis || this.defaultRoom;

			this.model.asset.scale = this.model.asset.scale || null;

		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		private onUpdate(): void {
			let modelRequest = R.clone(this.model);

			this.prepareModelRequestToSave(modelRequest);

			// MoveBundle
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;

			// Custom Fields
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});

			this.assetExplorerService.saveAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}

		protected onDeleteAsset() {
			this.deleteAsset(this.model.asset.id);
		}

	}
	return DeviceEditComponent;
}