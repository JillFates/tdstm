import {DialogConfirmAction, DialogService} from 'tds-component-library';
/**
 * Structure does not allows to introduce other base Modules
 * So this is not in the Asset Explorer Module and belongs here instead.
 */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {DependencySupportModel, SupportOnColumnsModel} from './model/support-on-columns.model';
import {AssetExplorerService} from '../../../modules/assetManager/service/asset-explorer.service';
import {ComboBoxSearchModel} from '../combo-box/model/combobox-search-param.model';
import {DEPENDENCY_TYPE} from './model/support-depends.model';
import * as R from 'ramda';
import {Observable} from 'rxjs';
import {UIDialogService} from '../../services/ui-dialog.service';
import {AssetComment} from '../dependent-comment/model/asset-coment.model';
import {DependentCommentComponent} from '../dependent-comment/dependent-comment.component';

declare var jQuery: any;

@Component({
	selector: 'tds-supports-depends',
	templateUrl: 'support-depends.component.html'
})

export class SupportsDependsComponent implements OnInit {
	@Input('model') model: any;
	@Output('isValidForm') isValidForm: EventEmitter<any> = new EventEmitter();
	@Output('initDone') initDone: EventEmitter<any> = new EventEmitter();
	private supportOnColumnModel: SupportOnColumnsModel;
	private dataFlowFreqList = [];
	private dependencyClassList = [];
	private typeList = [];
	private statusList = [];
	private moveBundleList = [];
	public dependencyType = DEPENDENCY_TYPE;
	public dataGridDependsOnHelper: DataGridOperationsHelper;
	public dataGridSupportsOnHelper: DataGridOperationsHelper;
	private supportsToDelete = [];
	private dependentsToDelete = [];

	public showFilterDep = false;
	public showFilterSup = false;

	public baseSupportsGridTabIndex = 449;
	public baseDependentGridTabIndex = 650;

	constructor(private assetExplorerService: AssetExplorerService, private dialogService: UIDialogService, private tdsDialogService: DialogService) {
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
		if (!this.model.moveBundleList && this.model.dependencyMap && this.model.dependencyMap.moveBundleList) {
			this.model.moveBundleList = this.model.dependencyMap.moveBundleList;
		}
		this.model.moveBundleList.forEach((moveBundle) => {
			this.moveBundleList.push({ id: moveBundle.id, text: moveBundle.name });
		});
		for (let prop in this.model.dependencyMap.assetClassOptions) {
			if (this.model.dependencyMap.assetClassOptions[prop]) {
				this.dependencyClassList.push({ id: prop, text: this.model.dependencyMap.assetClassOptions[prop] });
			}
		}

		this.getDependencyList('supportAssets', DEPENDENCY_TYPE.SUPPORT)
			.subscribe((dataGridDependsOnHelper) => {
				this.dataGridSupportsOnHelper = dataGridDependsOnHelper;
				this.model.dependencyMap.supportAssets = this.dataGridSupportsOnHelper.gridData.data;
				if (this.dataGridDependsOnHelper) {
					this.initDone.emit(this.model);
				}
			});

		this.getDependencyList('dependentAssets', DEPENDENCY_TYPE.DEPENDENT).subscribe((dataGridDependsOnHelper) => {
			this.dataGridDependsOnHelper = dataGridDependsOnHelper;
			this.model.dependencyMap.dependentAssets = this.dataGridDependsOnHelper.gridData.data;
			if (this.dataGridSupportsOnHelper) {
				this.initDone.emit(this.model);
			}
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
							moveBundle: R.clone(dependency.asset.moveBundle)
						},
						type: dependency.type,
						status: dependency.status,
						dependencyType: dependencyType,
						comment: dependency.comment
					};
					dependencies.push(dependencySupportModel);
				});
			}
			observer.next(new DataGridOperationsHelper(dependencies, [{
				dir: 'asc',
				field: 'assetName'
			}], null, null, 2000));
		});
	}

	/**
	 * Add a new Dependency
	 */
	public onAdd(dependencyType: string, dataGrid: DataGridOperationsHelper): void {
		/*if (dependencyType === DEPENDENCY_TYPE.SUPPORT) {
			this.baseSupportsGridTabIndex++;
		}
		if (dependencyType === DEPENDENCY_TYPE.DEPENDENT) {
			this.baseDependentGridTabIndex++;
		}*/
		let unknownIndex = this.statusList.indexOf('Unknown');
		if (unknownIndex === -1) {
			unknownIndex = 0
		}
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
			status: this.statusList[unknownIndex],
			dependencyType: dependencyType,
			comment: ''
		};

		dataGrid.addDataItem(dependencySupportModel);
		this.onChangeInternalModel();
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
		this.onChangeInternalModel();
	}

	/**
	 * Detects when the value of the dependency has change, then attach it to the model
	 * @param {DependencySupportModel} dataItem
	 */
	public onDependencyChange(dependency: any, dataItem: DependencySupportModel): void {
		if (dependency) {
			let changeParams = {
				assetId: dependency.id,
				dependentId: dataItem.id,
				type: dataItem.dependencyType
			};
			dataItem.assetDepend.id = dependency.id;
			dataItem.assetDepend.text = dependency.text;
			this.assetExplorerService.retrieveChangedBundle(changeParams).subscribe((res: any) => {
				if (res.id && res.id !== dataItem.assetDepend.moveBundle.id) {
					let mb = this.moveBundleList.find((mbi) => mbi.id === res.id);
					if (mb) {
						dataItem.assetDepend.moveBundle = mb;
						this.onChangeInternalModel();
					}
				}
			});
		} else {
			dataItem.assetDepend = {
				id: '',
				text: '',
				moveBundle: dataItem.assetDepend.moveBundle
			};
		}
		this.onChangeInternalModel();
	}

	/**
	 * Clears the filter on supports
	 */
	public showFilterSupports(): void {
		if (this.showFilterSup) {
			this.showFilterSup = false;
			this.dataGridSupportsOnHelper.clearAllFilters(this.supportOnColumnModel.columns);
		} else {
			this.showFilterSup = true;
		}
	}

	/**
	 * Clears the Dependent filter
	 */
	showFilterDependents(): void {
		if (this.showFilterDep) {
			this.showFilterDep = false;
			this.dataGridDependsOnHelper.clearAllFilters(this.supportOnColumnModel.columns);
		} else {
			this.showFilterDep = true;
		}
	}

	/**
	 * Calculate the Color for the Move Bundle
	 * @returns {string}
	 */
	public getMoveBundleColor(dataItem: any): string {
		if (dataItem.assetDepend.moveBundle && dataItem.assetDepend.moveBundle.id !== 0) {
			if (this.model.asset.moveBundle && this.model.asset.moveBundle.id !== dataItem.assetDepend.moveBundle.id && dataItem.status === 'Validated') {
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
	 * Confirm before delete
	 * **/
	public onClickDelete(dataItem: any, dataGrid: DataGridOperationsHelper, dependencyType: DEPENDENCY_TYPE): void {
		this.tdsDialogService.confirm(
			'Confirm Delete',
			'Please confirm delete of this record. This action cannot be undone.'
		).subscribe(
			(data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					if (dependencyType === DEPENDENCY_TYPE.SUPPORT)  {
						this.supportsToDelete.push(dataItem.id);
					} else {
						this.dependentsToDelete.push(dataItem.id);
					}
					this.onDeleteDependencySupport(dataItem, dataGrid);
				}
			}
		);
	}

	/**
	 * Delete the selected element
	 */
	public onDeleteDependencySupport(dataItem: any, dataGrid: DataGridOperationsHelper): void {
		dataGrid.removeDataItem(dataItem);
		this.onChangeInternalModel();
	}

	/**
	 * Open the Dialog to Edit/Add a comment
	 * @param dataItem
	 */
	public onAddEditComment(dataItem: any): void {
		let assetComment: AssetComment = {
			comment: dataItem.comment,
			dialogTitle: dataItem.assetDepend.text + ' (' + dataItem.dependencyType + ')'
		};
		this.dialogService.extra(DependentCommentComponent,
			[UIDialogService,
				{
					provide: AssetComment,
					useValue: assetComment
				}
			], false, false)
			.then((result) => {
				dataItem.comment = result.comment;
			}).catch((error) => console.log(error));
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

	/**
	 * Run a validation to ensure the Asset Name and the Bundle is selected on any change
	 * Emits True when the Form is Valid
	 * Emits False if at least one Asset Name or Bundle is missing
	 * And update the Object Model
	 */
	private onChangeInternalModel(): void {
		let validForm = true;
		// Validate the Supports Table
		this.dataGridSupportsOnHelper.gridData.data.forEach((dataItem: any) => {
			if (validForm) {
				validForm = !(dataItem.assetDepend.moveBundle.id === 0 || dataItem.assetDepend.id === '');
			}
		});

		// Validate the Depends Table
		this.dataGridDependsOnHelper.gridData.data.forEach((dataItem: any) => {
			if (validForm) {
				validForm = !(dataItem.assetDepend.moveBundle.id === 0 || dataItem.assetDepend.id === '');
			}
		});

		if (validForm) {
			this.model.dependencyMap.supportAssets = this.dataGridSupportsOnHelper.gridData.data;
			this.model.dependencyMap.dependentAssets = this.dataGridDependsOnHelper.gridData.data;
			this.model.dependencyMap.dependentsToDelete = this.dependentsToDelete;
			this.model.dependencyMap.supportsToDelete = this.supportsToDelete;
		}

		this.isValidForm.emit(validForm);
	}
}
