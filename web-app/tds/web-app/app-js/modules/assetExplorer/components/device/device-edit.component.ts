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
		}

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
		}

		protected searchAssetTypes = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel>  => {
			searchModel.query = 'term=&manufacturerId=';
			return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
		}

		protected searchManufacturers = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = 'assetEntity/manufacturer?term=&assetType=';
			return this.assetExplorerService.getManufacturersForComboBox(searchModel);
		}

		protected searchModels = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
			searchModel.query = 'modelsOf?id=&manufacturerId=&assetType=&term=';
			return this.assetExplorerService.getModelsForComboBox(searchModel);
		}

	}
	return DeviceEditComponent;
}