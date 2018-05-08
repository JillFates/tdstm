/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */

import * as R from 'ramda';
import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/Observable';
import {ComboBoxSearchResultModel} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {is} from '@uirouter/core';

declare var jQuery: any;

export function DeviceEditComponent(template, editModel) {

	@Component({
		selector: `device-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DeviceEditComponent implements OnInit {

		private dateFormat: string;
		private showRackFields = true;
		private showRackSourceInput: 'none'|'new'|'select' = 'none';
		private showRackTargetInput: 'none'|'new'|'select' = 'none';
		private rackSourceOptions: Array<any> = [];
		private rackTargetOptions: Array<any> = [];
		private showBladeFields = true;
		private showBladeSourceInput: 'none'|'new'|'select' = 'none';
		private showBladeTargetInput: 'none'|'new'|'select' = 'none';
		private bladeSourceOptions: Array<any> = [];
		private bladeTargetOptions: Array<any> = [];

		constructor(
					@Inject('model') private model: any,
					private activeDialog: UIActiveDialogService,
					private preference: PreferenceService,
					private assetExplorerService: AssetExplorerService) {

			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
			this.initModel();
			this.toggleAssetTypeFields();
		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		private initModel(): void {
			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);
			this.model.asset.locationSource = null;
			this.model.asset.newRoomSource = null;
			this.model.asset.locationTarget = null;
			this.model.asset.newRoomTarget = null;
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}
			this.model.asset.assetTypeSelectValue = {id: null};
			if (this.model.asset.assetType) {
				this.model.asset.assetTypeSelectValue.id = this.model.asset.assetType;
				this.model.asset.assetTypeSelectValue.text = this.model.asset.assetType;
			}
			this.model.asset.manufacturerSelectValue = {id: null};
			if (this.model.asset.manufacturer) {
				this.model.asset.manufacturerSelectValue.id = this.model.asset.manufacturer.id;
			}
			this.model.asset.modelSelectValue = {id: null};
			if (this.model.asset.model) {
				this.model.asset.modelSelectValue.id = this.model.asset.model.id;
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

		private showHideRackFields(show: boolean): void {
			this.showRackFields = show; // pub.showRackFields();
			if (!show) {
				this.showRackSourceInput = 'none';
				this.showRackTargetInput = 'none';
			} else {
				this.populateRackSelect();
			}
		}

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

		private showHideBladeFields(show: boolean): void {
			this.showBladeFields = show; // pub.showRackFields();
			if (!show) {
				this.showBladeSourceInput = 'none';
				this.showBladeTargetInput = 'none';
			} else {
				this.populateBladeSelect();
			}
		}

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

		protected searchAssetTypes = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel>  => {
			searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}`;
			return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
		}

		protected searchManufacturers = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = `assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
			return this.assetExplorerService.getManufacturersForComboBox(searchModel);
		}

		protected searchModels = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}
			&assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
			return this.assetExplorerService.getModelsForComboBox(searchModel);
		}

		protected onAssetTypeValueChange(value: any): void {
			if (!value) {
				value = {id: null};
			}
			this.model.asset.assetTypeSelectValue = value;
			console.log(this.model.asset.assetTypeSelectValue);
			// this.model.asset.manufacturerSelectValue = {id: null};
			this.model.asset.modelSelectValue = {id: null};
			this.toggleAssetTypeFields();
		}

		protected onManufacturerValueChange(value: any): void {
			if (!value) {
				value = {id: null};
			}
			this.model.asset.manufacturerSelectValue = value;
			console.log(this.model.asset.manufacturerSelectValue);
			// this.model.asset.assetTypeSelectValue = {id: null};
			this.model.asset.modelSelectValue = {id: null};
		}

		protected onModelValueChange(value: any): void {
			this.model.asset.assetTypeSelectValue.id = value.assetType;
			this.model.asset.manufacturerSelectValue.id = value.manufacturerId;
			this.model.asset.modelSelectValue.id = value.id;
			this.model.asset.modelSelectValue.text = value.text;
			console.log(this.model.asset.modelSelectValue);
			this.toggleAssetTypeFields();
		}

		protected onRoomSourceValueChange(event: any): void {
			this.toggleAssetTypeFields();
		}

		protected onRoomTargetValueChange(event: any): void {
			this.toggleAssetTypeFields();
		}

		/***
		 * Close the Active Dialog
		 */
		private cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return DeviceEditComponent;
}