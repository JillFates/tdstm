/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {DependencySupportModel, SupportOnColumnsModel} from '../../model/support-on-columns.model';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import * as R from 'ramda';
import {Observable} from 'rxjs/Rx';

declare var jQuery: any;

export function DatabaseEditComponent(template, editModel) {

	@Component({
		selector: `database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DatabaseShowComponent implements OnInit {

		private dateFormat: string;
		private dataGridSupportsOnHelper: DataGridOperationsHelper;
		private dataGridDependsOnHelper: DataGridOperationsHelper;
		private supportOnColumnModel: SupportOnColumnsModel;
		private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
		private initialSort: any = [{
			dir: 'desc',
			field: 'dateCreated'
		}];
		private dataFlowFreqList = [];
		private dependencyTypeList = [];
		private dependencyStatusList = [];
		private dependencyClassList = [];

		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService,
			private assetExplorerService: AssetExplorerService) {

			this.getAssetListForComboBox = this.getAssetListForComboBox.bind(this);

			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');

			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);

			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}

			// Lists
			this.dataFlowFreqList = R.clone(editModel.dataFlowFreq);
			this.dependencyTypeList = R.clone(editModel.dependencyMap.dependencyType);
			this.dependencyStatusList = R.clone(editModel.dependencyMap.dependencyStatus);
			for (let prop in editModel.dependencyMap.assetClassOptions) {
				if (editModel.dependencyMap.assetClassOptions[prop]) {
					this.dependencyClassList.push({id: prop, text: editModel.dependencyMap.assetClassOptions[prop]});
				}
			}
			// Supports On
			this.getSupportOnList();
			// Depends On
		}

		/**
		 * Get the List of Supports On
		 */
		private getSupportOnList(): void {
			this.supportOnColumnModel = new SupportOnColumnsModel();
			let supportsOn = [];
			if (editModel.dependencyMap && editModel.dependencyMap.supportAssets) {
				let supportAssets = R.clone(editModel.dependencyMap.supportAssets);
				supportAssets.forEach((dependency) => {
					let dependencySupportModel: DependencySupportModel = {
						dataFlowFreq: dependency.dataFlowFreq,
						assetDepend: {
							id: dependency.asset.id,
							text: dependency.asset.name,
							metaParam: dependency.asset.assetType
						},
						dependencyType: dependency.type,
						dependencyStatus: dependency.status,
						assetClass: dependency.asset.assetType
					};
					supportsOn.push(dependencySupportModel);
				});
			}
			this.dataGridSupportsOnHelper = new DataGridOperationsHelper(supportsOn, this.initialSort, this.selectableSettings);
		}

		/**
		 * Add a new Support On Dependency
		 */
		public onAddSupportsOn(): void {
			this.dataGridSupportsOnHelper.addDataItem({});
		}

		/**
		 * Delete the selected element
		 */
		public onDeleteSupport(dataItem: any): void {
			this.dataGridSupportsOnHelper.removeDataItem(dataItem);
		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
		}

		public onUpdate(): void {
			console.log(JSON.stringify(this.model.asset));
		}

		public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
			return this.assetExplorerService.getAssetListForComboBox(searchParam);
		}

	}
	return DatabaseShowComponent;
}