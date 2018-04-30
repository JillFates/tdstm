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

		constructor(
					@Inject('model') private model: any,
					private activeDialog: UIActiveDialogService,
					private preference: PreferenceService,
					private assetExplorerService: AssetExplorerService) {
			this.initModel();
			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
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
				this.model.asset.manufacturerSelectValue.text = this.model.asset.manufacturer.text;
			}
			this.model.asset.modelSelectValue = {id: null};
			if (this.model.asset.model) {
				this.model.asset.modelSelectValue.id = this.model.asset.model.id;
				this.model.asset.modelSelectValue.text = this.model.asset.model.text;
			}
		}

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
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
			searchModel.query = `id=
			&manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}
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
		}

	}
	return DeviceEditComponent;
}