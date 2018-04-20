/**
 * Structure does not allows to introduce other base Modules
 * So this is not in the Asset Explorer Module and belongs here instead.
 */
import {Component, ElementRef, Inject, Input, OnInit, ViewChild} from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {DependencySupportModel, SupportOnColumnsModel} from '../../../modules/assetExplorer/model/support-on-columns.model';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {AssetExplorerService} from '../../../modules/assetExplorer/service/asset-explorer.service';
import {ComboBoxSearchModel} from '../combo-box/model/combobox-search-param.model';
import * as R from 'ramda';
import {Observable} from 'rxjs/Rx';

declare var jQuery: any;

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
	private dependencyClassList = [];
	private dependencyTypeList = [];
	private dependencyStatusList = [];
	private dependencyMoveBundleList = [];

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
		this.model.moveBundleList.forEach((moveBundle) => {
			this.dependencyMoveBundleList.push({id: moveBundle.id, text: moveBundle.name});
		});
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
				let assetClass = this.dependencyClassList.find((dc) => dc.id === dependency.asset.assetType);
				let dependencySupportModel: DependencySupportModel = {
					dataFlowFreq: dependency.dataFlowFreq,
					assetClass: assetClass,
					assetDepend: {
						id: dependency.asset.id,
						text: dependency.asset.name,
						moveBundle: {
							id: dependency.asset.moveBundle.id,
							name: dependency.asset.moveBundle.name
						}
					},
					dependencyType: dependency.type,
					dependencyStatus: dependency.status,
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
		this.dataGridSupportsOnHelper.addDataItem({
			dataFlowFreq: this.dataFlowFreqList[0],
			assetClass: this.dependencyClassList[0],
			assetDepend: {
				id: '',
				text: '',
				metaParam: this.dependencyClassList[0].id
			},
			dependencyType: this.dependencyTypeList[0],
			dependencyStatus: this.dependencyStatusList[0],
		});
	}

	/**
	 * On Change the Selected Dependency Class
	 * @param {DependencySupportModel} dataItem
	 */
	public onDependencyClassChange(dataItem: DependencySupportModel): void {
		dataItem.assetDepend = {
			id: '',
			text: '',
		};
	}

	/**
	 * Calculate the Color for the Move Bundle
	 * @returns {string}
	 */
	public getMoveBundleColor(): string {
		return 'bundle-dep-no-valid';
	}

	/**
	 * Delete the selected element
	 */
	public onDeleteSupport(dataItem: any): void {
		this.dataGridSupportsOnHelper.removeDataItem(dataItem);
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.assetExplorerService.getAssetListForComboBox(searchParam);
	}

	/**
	 * Attach the color to each element
	 */
	public onOpenMoveBundle(dropDownFooter: any): void {
		if (dropDownFooter && dropDownFooter.wrapper && dropDownFooter.wrapper.nativeElement) {
			setTimeout(() => {
				jQuery('.k-list-container').addClass('bundle-dep-no-valid');
				jQuery('.k-list-container ul.k-list li').addClass('move-bundle-item');
			});
		}
	}
}