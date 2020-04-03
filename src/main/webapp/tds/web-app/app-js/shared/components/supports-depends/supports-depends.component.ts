/**
 * Structure does not allows to introduce other base Modules
 * So this is not in the Asset Explorer Module and belongs here instead.
 */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DataGridOperationsHelper, RecordState} from '../../utils/data-grid-operations.helper';
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
	template: `
        <kendo-grid
                *ngIf="dataGridSupportsOnHelper"
                class="dependents-grid"
                [pageSize]="dataGridSupportsOnHelper.state.take"
                [skip]="dataGridSupportsOnHelper.state.skip"
                [pageable]="{pageSizes: dataGridSupportsOnHelper.defaultPageOptions, info: true}"
                (pageChange)="dataGridSupportsOnHelper.pageChange($event)"
                [data]="dataGridSupportsOnHelper.gridData"
                [sort]="dataGridSupportsOnHelper.state.sort"
								[sortable]="false"
                [resizable]="true"
                (sortChange)="dataGridSupportsOnHelper.sortChange($event)">

            <!-- Toolbar Template -->
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Supports</label>
                <tds-button-add
                        class="float-right button-header-grid"
						[tooltip]="'Add link to Support Asset'"
                        id="add-support"
						[tabIndex]="449"
                        (click)="onAdd(dependencyType.SUPPORT, dataGridSupportsOnHelper)">
                </tds-button-add>
            </ng-template>

            <!-- Columns -->
            <kendo-grid-column *ngFor="let column of supportOnColumnModel.columns"
                               field="{{column.property}}"
                               [headerClass]="column.headerClass ? column.headerClass : ''"
                               [headerStyle]="column.headerStyle ? column.headerStyle : ''"
                               [class]="column.cellClass ? column.cellClass : ''"
                               [style]="column.cellStyle ? column.cellStyle : ''"
                               [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">

                <!-- Header Template -->
                <ng-template kendoGridHeaderTemplate>
                    <label>{{column.label}}</label>
                </ng-template>

                <!-- Action -->
                <ng-template kendoGridCellTemplate *ngIf="column.type === 'action'" let-dataItem let-rowIndex="rowIndex">
                    <div class="k-grid-ignore-click tds-action-button-set" style="cursor: default;">
						<tds-button-custom
							[id]="'create-button-' + rowIndex"
							icon="comment-o"
							tooltip="Create Comment"
							(click)="onAddEditComment(dataItem)">
                            <span class="glyphicon" [ngClass]="{'glyphicon-plus': dataItem.comment?.length <= 0, 'icon-action': true}"></span>
                            <span class="glyphicon" [ngClass]="{'glyphicon-pencil': dataItem.comment?.length > 0, 'icon-action': true}"></span>
						</tds-button-custom>
						<tds-button-delete
							[id]="'delete-button-' + rowIndex"
							class="command-delete"
							(click)="onDeleteDependencySupport(dataItem, dataGridSupportsOnHelper, SUPPORTS)">
						</tds-button-delete>
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'dataFlowFreq'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="dataFlowFreqList"
                            [(ngModel)]="dataItem.dataFlowFreq"
                            (valueChange)="updateRecordState(dataItem)"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetClass'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="dependencyClassList"
                            [textField]="'text'"
                            [valueField]="'id'"
                            [(ngModel)]="dataItem.assetClass"
                            (valueChange)="onDependencyClassChange(dataItem)"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetName'" let-dataItem let-rowIndex="rowIndex">
                    <tds-combobox
                            [(model)]="dataItem.assetDepend"
                            [(metaParam)]="dataItem.assetClass.id"
                            [serviceRequest]="getAssetListForComboBox"
                            (valueChange)="onDependencyChange($event, dataItem)">
                    </tds-combobox>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'moveBundle'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist #dropdownFooter
                                        class="form-control" style="width: 100%;"
                                        [data]="moveBundleList"
                                        [textField]="'text'"
                                        [valueField]="'id'"
                                        (valueChange)="this.updateRecordState(dataItem)"
                                        [(ngModel)]="dataItem.assetDepend.moveBundle"
                                        [ngClass]="getMoveBundleColor(dataItem)"
                                        (open)="onOpenMoveBundle(dropdownFooter, dataItem)"
                                        required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'type'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="typeList"
                            (valueChange)="this.updateRecordState(dataItem)"
                            [(ngModel)]="dataItem.type"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="statusList"
                            (valueChange)="this.updateRecordState(dataItem)"
                            [(ngModel)]="dataItem.status"
                            required>
                    </kendo-dropdownlist>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-messages noRecords="There are no Support Assets to display."> </kendo-grid-messages>
        </kendo-grid>

        <kendo-grid
                *ngIf="dataGridDependsOnHelper"
                class="dependents-grid is-dependent-on"
                [data]="dataGridDependsOnHelper.gridData"
                [pageSize]="dataGridDependsOnHelper.state.take"
                [skip]="dataGridDependsOnHelper.state.skip"
                [pageable]="{pageSizes: dataGridDependsOnHelper.defaultPageOptions, info: true}"
                (pageChange)="dataGridDependsOnHelper.pageChange($event)"
                [sortable]="false"
                [resizable]="true"
                (sortChange)="dataGridDependsOnHelper.sortChange($event)">

            <!-- Toolbar Template -->
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Is Dependent On </label>
                <tds-button-add
                        class="float-right button-header-grid"
                        [tooltip]="'Add link to Dependent Asset'"
                        id="dependent-support"
						[tabIndex]="450"
                        (click)="onAdd(dependencyType.DEPENDENT, dataGridDependsOnHelper)">
                </tds-button-add>
            </ng-template>

            <!-- Columns -->
            <kendo-grid-column *ngFor="let column of supportOnColumnModel.columns"
                               field="{{column.property}}"
                               [headerClass]="column.headerClass ? column.headerClass : ''"
                               [headerStyle]="column.headerStyle ? column.headerStyle : ''"
                               [class]="column.cellClass ? column.cellClass : ''"
                               [style]="column.cellStyle ? column.cellStyle : ''"
                               [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">

                <!-- Header Template -->
                <ng-template kendoGridHeaderTemplate>
                    <label>{{column.label}}</label>
                </ng-template>

                <!-- Action -->
                <ng-template kendoGridCellTemplate *ngIf="column.type === 'action'" let-dataItem let-rowIndex="rowIndex">
                    <div class="k-grid-ignore-click tds-action-button-set" style="cursor: default;">
                    	<tds-button-custom
							[id]="'dependent-create-button-' + rowIndex"
							icon="comment-o"
							tooltip="Create Comment"
							(click)="onAddEditComment(dataItem)">
                            <span class="glyphicon" [ngClass]="{'glyphicon-plus': dataItem.comment?.length <= 0, 'icon-action': true}"></span>
                            <span class="glyphicon" [ngClass]="{'glyphicon-pencil': dataItem.comment?.length > 0, 'icon-action': true}"></span>
						</tds-button-custom>
						<tds-button-delete
							[id]="'dependent-delete-button-' + rowIndex"
							class="command-delete"
							(click)="onDeleteDependencySupport(dataItem, dataGridDependsOnHelper, DEPENDENT)">
						</tds-button-delete>
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'dataFlowFreq'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
							class="form-control" style="width: 100%;"
                            [data]="dataFlowFreqList"
                            (valueChange)="this.updateRecordState(dataItem)"
                            [(ngModel)]="dataItem.dataFlowFreq"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetClass'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
							class="form-control" style="width: 100%;"
                            [data]="dependencyClassList"
                            [textField]="'text'"
                            [valueField]="'id'"
                            [(ngModel)]="dataItem.assetClass"
                            (valueChange)="onDependencyClassChange(dataItem)"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetName'" let-dataItem let-rowIndex="rowIndex">
                    <tds-combobox
                            [(model)]="dataItem.assetDepend"
                            [(metaParam)]="dataItem.assetClass.id"
                            [serviceRequest]="getAssetListForComboBox"
                            (selectionChange)="onDependencyChange($event, dataItem)">
                    </tds-combobox>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'moveBundle'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist #dropdownFooter
                                        class="form-control" style="width: 100%;"
                                        [data]="moveBundleList"
                                        [textField]="'text'"
                                        [valueField]="'id'"
                                        (valueChange)="this.updateRecordState(dataItem)"
                                        [(ngModel)]="dataItem.assetDepend.moveBundle"
                                        [ngClass]="getMoveBundleColor(dataItem)"
                                        (open)="onOpenMoveBundle(dropdownFooter, dataItem)"
                                        required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'type'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="typeList"
                            (valueChange)="this.updateRecordState(dataItem)"
                            [(ngModel)]="dataItem.type"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem let-rowIndex="rowIndex">
                    <kendo-dropdownlist
                            class="form-control" style="width: 100%;"
                            [data]="statusList"
                            (valueChange)="this.updateRecordState(dataItem)"
                            [(ngModel)]="dataItem.status"
                            required>
                    </kendo-dropdownlist>
                </ng-template>

            </kendo-grid-column>
            <kendo-grid-messages noRecords="There are no Dependent Assets to display."> </kendo-grid-messages>
        </kendo-grid>
	`,
	styles: []
})

export class SupportsDependsComponent implements OnInit {
	@Input('model') model: any;
	@Output('isValidForm') isValidForm: EventEmitter<any> = new EventEmitter();
	@Output('initDone')  initDone: EventEmitter<any> = new EventEmitter();
	private supportOnColumnModel: SupportOnColumnsModel;
	private dataFlowFreqList = [];
	private dependencyClassList = [];
	private typeList = [];
	private statusList = [];
	private moveBundleList = [];
	private supportsToDelete = [];
	private dependentsToDelete = [];
	public dependencyType = DEPENDENCY_TYPE;
	public dataGridDependsOnHelper: DataGridOperationsHelper;
	public dataGridSupportsOnHelper: DataGridOperationsHelper;
	public readonly SUPPORTS = 'Supports';
	public readonly DEPENDENT = 'Dependent';

	constructor(private assetExplorerService: AssetExplorerService, private dialogService: UIDialogService) {
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
			this.moveBundleList.push({id: moveBundle.id, text: moveBundle.name});
		});
		for (let prop in this.model.dependencyMap.assetClassOptions) {
			if (this.model.dependencyMap.assetClassOptions[prop]) {
				this.dependencyClassList.push({id: prop, text: this.model.dependencyMap.assetClassOptions[prop]});
			}
		}

		this.getDependencyList('supportAssets', DEPENDENCY_TYPE.SUPPORT).subscribe((dataGridDependsOnHelper) => {
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
						recordState: RecordState.pristine,
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
			observer.next(new DataGridOperationsHelper(dependencies,
				[{ dir: 'asc', field: 'name'}],
				{ mode: 'single', checkboxOnly: false},
				{ useColumn: 'id' },
				25));
		});
	}

	public updateRecordState(dataItem: DependencySupportModel): void {
		if (dataItem.recordState === RecordState.pristine) {
			dataItem.recordState = RecordState.updated;
		}
		// this.dataGridDependsOnHelper.getCreatedUpdatedRecords();
		this.onChangeInternalModel();
	}

	/**
	 * Add a new Dependency
	 */
	public onAdd(dependencyType: string, dataGrid: DataGridOperationsHelper): void {
		// this.dataGridSupportsOnHelper.getCreatedUpdatedRecords();

		let unknownIndex = this.statusList.indexOf('Unknown');
		if (unknownIndex === -1) {
			unknownIndex = 0
		}
		let dependencySupportModel: DependencySupportModel = {
			id: 0,
			recordState: RecordState.created,
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

		// dataGrid.addDataItem(dependencySupportModel);
		dataGrid.addResultSetItem(dependencySupportModel);
		this.onChangeInternalModel();
		this.dataGridDependsOnHelper.getCreatedUpdatedRecords();
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
		this.updateRecordState(dataItem);
	}

	/**
	 * Detects when the value of the dependency has change, then attach it to the model
	 * @param {DependencySupportModel} dataItem
	 */
	public onDependencyChange(dependency: any, dataItem: DependencySupportModel): void {
		this.updateRecordState(dataItem);

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
	 * Delete the selected element
	 */
	public onDeleteDependencySupport(dataItem: any, dataGrid: DataGridOperationsHelper, type: string): void {
		if (dataItem.id) {
			if (type === this.SUPPORTS)  {
				this.supportsToDelete.push(dataItem.id);
			} else {
				this.dependentsToDelete.push(dataItem.id);
			}
		}

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
			], true, false)
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
			this.model.dependencyMap.supportAssets = this.dataGridSupportsOnHelper.getCreatedUpdatedRecords();
			// this.dataGridSupportsOnHelper.gridData.data;

			this.model.dependencyMap.dependentAssets = this.dataGridDependsOnHelper.getCreatedUpdatedRecords();
			// this.dataGridDependsOnHelper.gridData.data;

			this.model.dependencyMap.dependentsToDelete = this.dependentsToDelete;
			this.model.dependencyMap.supportsToDelete = this.supportsToDelete;
		}

		this.isValidForm.emit(validForm);
	}
}
