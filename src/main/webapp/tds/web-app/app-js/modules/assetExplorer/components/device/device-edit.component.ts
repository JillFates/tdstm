// Angular
import {Component, ComponentFactoryResolver, Inject, OnInit} from '@angular/core';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
// Service
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {DialogService} from 'tds-component-library';
import {TagService} from '../../../assetTags/service/tag.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Component
import {DeviceCommonComponent} from './model-device/device-common.component';
// Other
import * as R from 'ramda';

export function DeviceEditComponent(template, editModel, metadata: any, parentDialog: any) {

	@Component({
		selector: `tds-device-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DeviceEditComponent extends DeviceCommonComponent implements OnInit {
		constructor(
			@Inject('model') model: any,
			componentFactoryResolver: ComponentFactoryResolver,
			userContextService: UserContextService,
			permissionService: PermissionService,
			assetExplorerService: AssetExplorerService,
			dialogService: DialogService,
			notifierService: NotifierService,
			tagService: TagService,
			translatePipe: TranslatePipe
		) {

			super(componentFactoryResolver, model, userContextService, permissionService, assetExplorerService, dialogService, notifierService, tagService, metadata, translatePipe, parentDialog);
		}

		ngOnInit() {
			this.initModel();
			this.toggleAssetTypeFields();
			this.focusControlByName('assetName');
			this.onFocusOutOfCancel();
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
