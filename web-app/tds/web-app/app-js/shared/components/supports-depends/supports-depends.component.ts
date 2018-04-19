/**
 * Structure does not allows to introduce other base Modules
 * So this is not in the Asset Explorer Module and belongs here instead.
 */
import {Component, Inject, Input, OnInit} from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {DependencySupportModel, SupportOnColumnsModel} from '../../../modules/assetExplorer/model/support-on-columns.model';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {AssetExplorerService} from '../../../modules/assetExplorer/service/asset-explorer.service';
import {ComboBoxSearchModel} from '../combo-box/model/combobox-search-param.model';
import * as R from 'ramda';
import {Observable} from 'rxjs/Rx';

@Component({
	selector: 'tds-supports-depends',
	templateUrl: '../tds/web-app/app-js/shared/components/supports-depends/supports-depends.component.html',
	styles: []
})

export class SupportsDependsComponent implements OnInit {
	@Input('model') model: any;

	private dataGridSupportsOnHelper: DataGridOperationsHelper;
	private supportOnColumnModel: SupportOnColumnsModel;
	private selectableSettings: SelectableSettings = {mode: 'single', checkboxOnly: false};
	private initialSort: any = [{
		dir: 'desc',
		field: 'dateCreated'
	}];
	private dataFlowFreqList = [];
	private dependencyTypeList = [];
	private dependencyStatusList = [];
	private dependencyClassList = [];

	constructor(private assetExplorerService: AssetExplorerService) {
		this.getAssetListForComboBox = this.getAssetListForComboBox.bind(this);
	}

	/**
	 * Prepare the list from the Model
	 */
	ngOnInit(): void {
		// Lists
		this.dataFlowFreqList = R.clone(this.model.dataFlowFreq);
		this.dependencyTypeList = R.clone(this.model.dependencyMap.dependencyType);
		this.dependencyStatusList = R.clone(this.model.dependencyMap.dependencyStatus);
		for (let prop in this.model.dependencyMap.assetClassOptions) {
			if (this.model.dependencyMap.assetClassOptions[prop]) {
				this.dependencyClassList.push({id: prop, text: this.model.dependencyMap.assetClassOptions[prop]});
			}
		}

		this.getSupportOnList();
	}

	/**
	 * Get the List of Supports On
	 */
	private getSupportOnList(): void {
		this.supportOnColumnModel = new SupportOnColumnsModel();
		let supportsOn = [];
		if (this.model.dependencyMap && this.model.dependencyMap.supportAssets) {
			let supportAssets = R.clone(this.model.dependencyMap.supportAssets);
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

	public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.assetExplorerService.getAssetListForComboBox(searchParam);
	}
}