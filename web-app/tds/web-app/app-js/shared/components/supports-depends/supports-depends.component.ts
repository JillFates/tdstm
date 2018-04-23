/**
 * Structure does not allows to introduce other base Modules
 * So this is not in the Asset Explorer Module and belongs here instead.
 */
import {Component, Input, OnInit} from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {DependencySupportModel, SupportOnColumnsModel} from './model/support-on-columns.model';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {AssetExplorerService} from '../../../modules/assetExplorer/service/asset-explorer.service';
import {ComboBoxSearchModel} from '../combo-box/model/combobox-search-param.model';
import {DEPENDENCY_TYPE} from './model/support-depends.model';
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
	private dataGridDependsOnHelper: DataGridOperationsHelper;
	private supportOnColumnModel: SupportOnColumnsModel;
	private selectableSettings: SelectableSettings = {mode: 'single', checkboxOnly: false};
	private initialSort: any = [{
		dir: 'desc',
		field: 'dateCreated'
	}];
	private dataFlowFreqList = [];
	private dependencyClassList = [];
	private typeList = [];
	private statusList = [];
	private moveBundleList = [];
	public dependencyType = DEPENDENCY_TYPE;

	constructor(private assetExplorerService: AssetExplorerService) {
		this.getAssetListForComboBox = this.getAssetListForComboBox.bind(this);
	}

	/**
	 * Prepare the list from the Model
	 */
	ngOnInit(): void {
		// Lists
		this.dataFlowFreqList = R.clone(this.model.dataFlowFreq);
		this.typeList = R.clone(this.model.dependencyMap.dependencyType);
		this.statusList = R.clone(this.model.dependencyMap.dependencyStatus);
		this.model.moveBundleList.forEach((moveBundle) => {
			this.moveBundleList.push({id: moveBundle.id, text: moveBundle.name});
		});
		for (let prop in this.model.dependencyMap.assetClassOptions) {
			if (this.model.dependencyMap.assetClassOptions[prop]) {
				this.dependencyClassList.push({id: prop, text: this.model.dependencyMap.assetClassOptions[prop]});
			}
		}

		this.getDependencyList('supportAssets', DEPENDENCY_TYPE.SUPPORT).subscribe((dataGridDependsOnHelper) => {
			this.dataGridSupportsOnHelper = dataGridDependsOnHelper;
		});

		this.getDependencyList('dependentAssets', DEPENDENCY_TYPE.DEPENDENT).subscribe((dataGridDependsOnHelper) => {
			this.dataGridDependsOnHelper = dataGridDependsOnHelper;
		});
	}

	/**
	 * Get the List of Dependencies
	 */
	private getDependencyList(dependencyMap: string, dependencyType): Observable<DataGridOperationsHelper> {
		return new Observable(observer => {
			this.supportOnColumnModel = new SupportOnColumnsModel();
			let dependencies = [];
			if (this.model.dependencyMap && this.model.dependencyMap[dependencyMap]) {
				let assets = R.clone(this.model.dependencyMap[dependencyMap]);
				assets.forEach((dependency) => {
					let assetClass = this.dependencyClassList.find((dc) => dc.id === dependency.asset.assetType);
					let dependencySupportModel: DependencySupportModel = {
						id: dependency.id,
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
						type: dependency.type,
						status: dependency.status,
						dependencyType: dependencyType
					};
					dependencies.push(dependencySupportModel);
				});
			}
			observer.next(new DataGridOperationsHelper(dependencies, null, null));
		});
	}

	/**
	 * Add a new Dependency
	 */
	public onAdd(dependencyType: string, dataGrid: DataGridOperationsHelper): void {
		let dependencySupportModel: DependencySupportModel = {
			id: 0,
			dataFlowFreq: this.dataFlowFreqList[0],
			assetClass: this.dependencyClassList[0],
			assetDepend: {
				id: '',
				text: '',
				moveBundle: {
					id: 0,
					name: ''
				}
			},
			type: this.typeList[0],
			status: this.statusList[0],
			dependencyType: dependencyType
		};
		dataGrid.addDataItem(dependencySupportModel);
	}

	/**
	 * On Change the Selected Dependency Class
	 * @param {DependencySupportModel} dataItem
	 */
	public onDependencyClassChange(dataItem: DependencySupportModel): void {
		dataItem.assetDepend = {
			id: '',
			text: '',
			moveBundle: dataItem.assetDepend.moveBundle
		};
	}

	/**
	 * Detects whe
	 * @param {DependencySupportModel} dataItem
	 */
	public onDependencyChange(dependency: any, dataItem: DependencySupportModel): void {
		let changeParams = {
			assetId: dependency.id,
			dependentId: dataItem.id,
			type: dataItem.dependencyType
		};
		this.assetExplorerService.retrieveChangedBundle(changeParams).subscribe((res: any) => {
			if (res.id && res.id !== dataItem.assetDepend.moveBundle.id) {
				let mb = this.moveBundleList.find((mbi) => mbi.id === res.id);
				if (mb) {
					dataItem.assetDepend.moveBundle = mb;
				}
			}
		});
	}

	/**
	 * Calculate the Color for the Move Bundle
	 * @returns {string}
	 */
	public getMoveBundleColor(dataItem: any): string {
		if (dataItem.assetDepend.moveBundle && dataItem.assetDepend.moveBundle.id !== 0) {
			if (this.model.asset.moveBundle.id !== dataItem.assetDepend.moveBundle.id && dataItem.status === 'Validated') {
				return 'bundle-dep-no-valid';
			} else {
				if (dataItem.status !== 'Questioned' && dataItem.status !== 'Validated') {
					return 'bundle-dep-unknown';
				}
				return 'bundle-dep-' + dataItem.status.toLocaleLowerCase();
			}
		}
		return 'nothing';
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
	public onOpenMoveBundle(dropDownFooter: any, dataItem: any): void {
		if (dropDownFooter && dropDownFooter.wrapper && dropDownFooter.wrapper.nativeElement) {
			setTimeout(() => {
				jQuery('.k-list-container').addClass(this.getMoveBundleColor(dataItem));
				jQuery('.k-list-container ul.k-list li').addClass('move-bundle-item');
			});
		}
	}
}