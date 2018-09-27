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
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/Observable';
import {ComboBoxSearchResultModel} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TagService} from '../../../assetTags/service/tag.service';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

declare var jQuery: any;

export function DeviceCreateComponent(template, metadata: any) {

	@Component({
		selector: `tds-device-create`,
		template: template,
		providers: [
			{ provide: 'model', useValue: { asset: {} } }
		]
	}) class DeviceCreateComponent extends AssetCommonEdit {

		private showRackFields = true;
		private showRackSourceInput: 'none'|'new'|'select' = 'none';
		private showRackTargetInput: 'none'|'new'|'select' = 'none';
		private rackSourceOptions: Array<any> = [];
		private rackTargetOptions: Array<any> =   [];
		private showBladeFields = true;
		private showBladeSourceInput: 'none'|'new'|'select' = 'none';
		private showBladeTargetInput: 'none'|'new'|'select' = 'none';
		private bladeSourceOptions: Array<any> = [];
		private bladeTargetOptions: Array<any> = [];

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
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			} else {
				this.model.asset.scale.name = { value: this.model.asset.scale.name, text: ''}
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
				this.rackSourceOptions = this.model.sourceRackSelect;
			}
			if (this.model.targetRackSelect) {
				this.rackTargetOptions = this.model.targetRackSelect;
			}
			if (this.model.sourceChassisSelect) {
				this.bladeSourceOptions = this.model.sourceChassisSelect;
			}
			if (this.model.targetChassisSelect) {
				this.bladeTargetOptions = this.model.targetChassisSelect;
			}
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		private onCreate(): void {
			let modelRequest = R.clone(this.model);

			// currentAssetType, manufacturerId, modelId
			modelRequest.asset.manufacturerId = null;
			if ( this.model.asset.manufacturerSelectValue.id > 0 ) {
				modelRequest.asset.manufacturerId = this.model.asset.manufacturerSelectValue.id.toString();
			}
			modelRequest.asset.currentAssetType = null;
			if (this.model.asset.assetTypeSelectValue.id) {
				modelRequest.asset.currentAssetType = this.model.asset.assetTypeSelectValue.id.toString();
			}
			modelRequest.asset.modelId = null;
			if (this.model.asset.modelSelectValue.id > 0) {
				modelRequest.asset.modelId = this.model.asset.modelSelectValue.id.toString();
			}

			// roomSourceId, roomSource(new room)
			modelRequest.asset.roomSourceId = '0';
			if (this.model.asset.roomSource && this.model.asset.roomSource.id > 0) {
				modelRequest.asset.roomSourceId = this.model.asset.roomSource.id.toString();
			}
			modelRequest.asset.roomSource = this.model.asset.newRoomSource;
			delete modelRequest.asset.newRoomSource;

			// roomTargetId, roomTarget(new room)
			modelRequest.asset.roomTargetId = '0';
			if (this.model.asset.roomTarget && this.model.asset.roomTarget.id > 0) {
				modelRequest.asset.roomTargetId = this.model.asset.roomTarget.id.toString();
			}
			modelRequest.asset.roomTarget = this.model.asset.newRoomTarget;
			delete modelRequest.asset.newRoomTarget;

			// rackSourceId, rackSource (new rack)
			modelRequest.asset.rackSourceId = '-1';
			if (this.model.asset.rackSource && this.model.asset.rackSource.id > 0) {
				modelRequest.asset.rackSourceId = this.model.asset.rackSource.id.toString();
			}
			modelRequest.asset.rackSource = this.model.asset.newRackSource;
			delete modelRequest.asset.newRackSource;

			// rackTargetId, rackTarget(new rack)
			modelRequest.asset.rackTargetId = '-1';
			if (this.model.asset.rackTarget && this.model.asset.rackTarget.id > 0) {
				modelRequest.asset.rackTargetId = this.model.asset.rackTarget.id.toString();
			}
			modelRequest.asset.rackTarget = this.model.asset.newRackTarget;
			delete modelRequest.asset.newRackTarget;

			// sourceChassis
			if (this.model.asset.sourceChassis && this.model.asset.sourceChassis.id > 0) {
				modelRequest.asset.sourceChassis = this.model.asset.sourceChassis.id.toString();
			} else {
				modelRequest.asset.sourceChassis = '0';
			}

			// targetChassis
			if (this.model.asset.targetChassis && this.model.asset.targetChassis.id > 0) {
				modelRequest.asset.targetChassis = this.model.asset.targetChassis.id.toString();
			} else {
				modelRequest.asset.targetChassis = '0';
			}

			// MoveBundle
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;

			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;

			// Custom Fields
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});

			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}

		/**
		 * Taken from entity.crud.js
		 */
		private toggleAssetTypeFields(): void {
			const assetType = this.model.asset.assetTypeSelectValue.id;
			switch (assetType) {
				case 'Blade': /* TODO: should 'Blade Chassis' also be considered here ??*/
					this.showHideRackFields(false); // pub.hideRackFields();
					this.showHideBladeFields(true); // pub.showChassisFields();
					break;
				case 'VM':
					this.showHideRackFields(false); // pub.hideRackFields();
					this.showHideBladeFields(false); // pub.hideChassisFields();
					break;
				default:
					// Rack-able device
					this.showHideRackFields(true); // pub.showRackFields();
					this.showHideBladeFields(false); // pub.hideChassisFields();
			}
		}

		/**
		 * Show/Hide Rack fields logic.
		 * @param {boolean} show
		 */
		private showHideRackFields(show: boolean): void {
			this.showRackFields = show; // pub.showRackFields();
			if (!show) {
				this.showRackSourceInput = 'none';
				this.showRackTargetInput = 'none';
			} else {
				this.populateRackSelect();
			}
		}

		/**
		 * Calls endpoints to populate Rack selects.
		 */
		private populateRackSelect(): void {
			// source room field
			const roomId = this.model.asset.roomSource ? this.model.asset.roomSource.id : null;
			if (roomId && roomId > 0) {
				this.showRackSourceInput = 'select';
				this.assetExplorerService.getRacksForRoom(roomId, 'S').subscribe(response => {
					this.rackSourceOptions = response.data;
				});
			} else if (roomId && roomId === -1) { /* -1 (New Room)*/
				this.showRackSourceInput = 'new';
			} else {
				this.showRackSourceInput = 'none';
			}

			// target room fields
			const targetRoomId = this.model.asset.roomTarget ? this.model.asset.roomTarget.id : null;
			if (targetRoomId && targetRoomId > 0) {
				this.showRackTargetInput = 'select';
				this.assetExplorerService.getRacksForRoom(targetRoomId, 'T').subscribe(response => {
					this.rackTargetOptions = response.data;
				});
			} else if (targetRoomId && targetRoomId === -1) { /* -1 (New Room)*/
				this.showRackTargetInput = 'new';
			} else {
				this.showRackTargetInput = 'none';
			}
		}

		/**
		 * Show/Hide Blade(Chassis) fields logic.
		 * @param {boolean} show
		 */
		private showHideBladeFields(show: boolean): void {
			this.showBladeFields = show; // pub.showRackFields();
			if (!show) {
				this.showBladeSourceInput = 'none';
				this.showBladeTargetInput = 'none';
			} else {
				this.populateBladeSelect();
			}
		}

		/**
		 * Calls endpoints to populate Blade(Chassis) selects
		 */
		private populateBladeSelect(): void {
			// source room field
			const roomId = this.model.asset.roomSource ? this.model.asset.roomSource.id : null;
			if (roomId && roomId > 0) {
				this.showBladeSourceInput = 'select';
				this.assetExplorerService.getChassisForRoom(roomId).subscribe(response => {
					this.bladeSourceOptions = response.data;
				});
			} else {
				this.showBladeSourceInput = 'none';
			}

			// target room field
			const targetRoomId = this.model.asset.roomTarget ? this.model.asset.roomTarget.id : null;
			if (targetRoomId && targetRoomId > 0) {
				this.showBladeTargetInput = 'select';
				this.assetExplorerService.getChassisForRoom(targetRoomId).subscribe(response => {
					this.bladeTargetOptions = response.data;
				});
			} else {
				this.showBladeTargetInput = 'none';
			}
		}

		/**
		 * Function that handles the request of the Asset Types tds-combobox
		 * @param {ComboBoxSearchModel} searchModel
		 * @returns {Observable<ComboBoxSearchResultModel>}
		 */
		protected searchAssetTypes = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel>  => {
			searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}`;
			return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
		}

		/**
		 * Function that handles the request of the Manufacturers tds-combobox
		 * @param {ComboBoxSearchModel} searchModel
		 * @returns {Observable<ComboBoxSearchResultModel>}
		 */
		protected searchManufacturers = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = `assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
			return this.assetExplorerService.getManufacturersForComboBox(searchModel);
		}

		/**
		 * Function that handles the request of the Models tds-combobox
		 * @param {ComboBoxSearchModel} searchModel
		 * @returns {Observable<ComboBoxSearchResultModel>}
		 */
		protected searchModels = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}
			&assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
			return this.assetExplorerService.getModelsForComboBox(searchModel);
		}

		/**
		 * On Asset Types combobox change.
		 * @param value
		 */
		protected onAssetTypeValueChange(value: any): void {
			if (!value) {
				value = {id: null};
			}
			this.model.asset.assetTypeSelectValue = value;
			this.model.asset.modelSelectValue = {id: null};
			this.toggleAssetTypeFields();
		}

		/**
		 * On Manufacturers combobox change.
		 * @param value
		 */
		protected onManufacturerValueChange(value: any): void {
			if (!value) {
				value = {id: null};
			}
			this.model.asset.manufacturerSelectValue = value;
			this.model.asset.modelSelectValue = {id: null};
		}

		/**
		 * On Models combobox change.
		 * @param value
		 */
		protected onModelValueChange(value: any): void {
			this.model.asset.assetTypeSelectValue.id = value.assetType;
			this.model.asset.manufacturerSelectValue.id = value.manufacturerId;
			this.model.asset.modelSelectValue.id = value.id;
			this.model.asset.modelSelectValue.text = value.text;
			this.toggleAssetTypeFields();
		}

		/**
		 * On Source Room dropdown select change.
		 * @param event
		 */
		protected onRoomSourceValueChange(event: any): void {
			this.toggleAssetTypeFields();
		}

		/**
		 * On Target Room dropdown select change.
		 * @param event
		 */
		protected onRoomTargetValueChange(event: any): void {
			this.toggleAssetTypeFields();
		}

	}
	return DeviceCreateComponent;
}