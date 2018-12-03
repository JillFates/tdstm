import {Component, EventEmitter, Input, OnInit, Output, ViewChild, OnChanges, SimpleChanges, ChangeDetectionStrategy} from '@angular/core';

import {VIEW_COLUMN_MIN_WIDTH, ViewColumn, ViewSpec} from '../../model/view-spec.model';
import {State} from '@progress/kendo-data-query';
import {DataStateChangeEvent, GridDataResult, RowClassArgs} from '@progress/kendo-angular-grid';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {Observable} from 'rxjs';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {
	DIALOG_SIZE,
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
	KEYSTROKE,
	ModalType,
	SEARCH_QUITE_PERIOD
} from '../../../../shared/model/constants';
import {AssetShowComponent} from '../asset/asset-show.component';
import {
	CUSTOM_FIELD_CONTROL_TYPE,
	FIELD_NOT_FOUND,
	FieldSettingsModel
} from '../../../fieldSettings/model/field-settings.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagModel} from '../../../assetTags/model/tag.model';
import {AssetTagSelectorComponent} from '../../../../shared/components/asset-tag-selector/asset-tag-selector.component';
import {BulkActionResult} from '../bulk-change/model/bulk-change.model';
import {CheckboxState, CheckboxStates} from '../tds-checkbox/model/tds-checkbox.model';
import {BulkCheckboxService} from '../../service/bulk-checkbox.service';
import {ASSET_ENTITY_MENU} from '../../../../shared/modules/header/model/asset-menu.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {AssetCreateComponent} from '../asset/asset-create.component';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';
import {TaskCommentDialogComponent} from '../task-comment/dialog/task-comment-dialog.component';
import {SingleCommentModel} from '../single-comment/model/single-comment.model';
import {SingleCommentComponent} from '../single-comment/single-comment.component';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetEditComponent} from '../asset/asset-edit.component';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {TaskCreateComponent} from '../../../taskManager/components/create/task-create.component';
import {UserService} from '../../../../shared/services/user.service';
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {BulkChangeButtonComponent} from '../bulk-change/components/bulk-change-button/bulk-change-button.component';
import {NumberConfigurationConstraintsModel} from '../../../fieldSettings/components/number/number-configuration-constraints.model';

const {
	ASSET_JUST_PLANNING: PREFERENCE_JUST_PLANNING,
	ASSET_LIST_SIZE: PREFERENCE_LIST_SIZE
} = PREFERENCES_LIST;
declare var jQuery: any;

@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent implements OnInit, OnChanges {
	@Input() data: any;
	@Input() model: ViewSpec;
	@Input() gridState: State;
	@Output() modelChange = new EventEmitter<void>();
	@Output() justPlanningChange = new EventEmitter<boolean>();
	@Output() gridStateChange = new EventEmitter<State>();
	@Input() edit: boolean;
	@Input() metadata: any;
	@Input() fields: any;
	@ViewChild('tagSelector') tagSelector: AssetTagSelectorComponent;
	@ViewChild('tdsBulkChangeButton') tdsBulkChangeButton: BulkChangeButtonComponent;
	@Input()
	set viewId(viewId: number) {
		this._viewId = viewId;
		// changing the view reset selections
		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
		this.setActionCreateButton(viewId);
		this.gridStateChange.emit({...this.gridState, skip: 0});
		this.modelChange.emit();
	}

	public currentFields = [];
	public justPlanning = false;
	public VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	public gridMessage = 'ASSET_EXPLORER.GRID.INITIAL_VALUE';
	public showMessage = true;
	public typingTimeout: any;
	ASSET_ENTITY_MENU = ASSET_ENTITY_MENU;
	ASSET_ENTITY_DIALOG_TYPES = ASSET_ENTITY_DIALOG_TYPES;
	protected userTimeZone: string;
	protected userDateFormat: string;

	// Pagination Configuration
	notAllowedCharRegex = /ALT|ARROW|F+|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;
	private maxDefault = GRID_DEFAULT_PAGE_SIZE;
	private maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	private _viewId: number;
	public fieldNotFound = FIELD_NOT_FOUND;
	gridData: GridDataResult;
	selectAll = false;
	private columnFiltersOldValues = [];
	protected tagList: Array<TagModel> = [];
	public bulkItems: number[] = [];
	protected selectedAssetsForBulk: Array<any>;
	public createButtonState: ASSET_ENTITY_DIALOG_TYPES;
	private currentUser: any;
	protected fieldPipeMap: {pipe: any, metadata: any};

	constructor(
		private preferenceService: PreferenceService,
		private bulkCheckboxService: BulkCheckboxService,
		private notifier: NotifierService,
		private dialog: UIDialogService,
		private permissionService: PermissionService,
		private userService: UserService) {
			this.fieldPipeMap = {pipe: {}, metadata: {}};
			this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();
	}

	ngOnInit(): void {
		this.gridData = {
			data: [],
			total: 0
		};

		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.selectedAssetsForBulk = [];
		this.getPreferences().subscribe((preferences: any) => {
			this.updateGridState({take: parseInt(preferences[PREFERENCE_LIST_SIZE], 10) || 25});

			this.bulkCheckboxService.setPageSize(this.gridState.take);
			this.justPlanning = (preferences[PREFERENCE_JUST_PLANNING]) ? preferences[PREFERENCE_JUST_PLANNING].toString() === 'true' : false;
			this.justPlanningChange.emit(this.justPlanning);
			this.onReload();
		});

		// Iterate Fields to get reference for this context
		this.currentFields = this.fields.reduce((p, c) => {
			return p.concat(c.fields);
		}, []).map((f: FieldSettingsModel) => {
			if (f.control === CUSTOM_FIELD_CONTROL_TYPE.DateTime
				|| f.control === CUSTOM_FIELD_CONTROL_TYPE.Date
				|| f.control === CUSTOM_FIELD_CONTROL_TYPE.Number) {
					this.fieldPipeMap.pipe[`${f['domain']}_${f.field}`] = f.control;
					if (f.control === CUSTOM_FIELD_CONTROL_TYPE.Number) {
						let format = (f.constraints as NumberConfigurationConstraintsModel).format || '0';
						this.fieldPipeMap.metadata[`${f['domain']}_${f.field}`] = format;
					}
			}
			return {
				key: `${f['domain']}_${f.field}`,
				label: f.label
			};
		});
		// Listen to any Changes outside the model, like Asset Edit Views
		this.eventListeners();
		this.getCurrentUser();

	}

	ngOnChanges(changes: SimpleChanges) {
		const dataChange = changes['data'];

		if (dataChange && dataChange.currentValue !== dataChange.previousValue) {
			this.applyData(dataChange.currentValue)
		}

	}

	private getPreferences(): Observable<any> {
		return this.preferenceService.getPreferences(PREFERENCE_LIST_SIZE, PREFERENCE_JUST_PLANNING);
	}

	/**
	 * Reload the current Kendo List when an event that requires the changes occurs completely out of the context.
	 */
	private eventListeners() {
		this.notifier.on('reloadCurrentAssetList', (event) => {
			this.onReload();
		});
	}

	/**
	 * Draws the Label property
	 * @param {ViewColumn} column
	 * @returns {string}
	 */
	getPropertyLabel(column: ViewColumn): string {
		let field = this.currentFields.find(f => f.key === `${column.domain}_${column.property}`);
		if (field) {
			return field.label;
		}
		column.notFound = true;
		return column.label;
	}

	rowCallbackClass(context: RowClassArgs) {
		let obj = {};
		obj[context.dataItem.common_assetClass.toLowerCase()] = true;
		return obj;
	}

	onClearFilters(): void {
		this.model.columns.forEach((c: ViewColumn) => {
			c.filter = '';
		});
		this.onFilter();
		if (this.tagSelector) {
			this.tagSelector.reset();
		}
	}

	hasFilterApplied(): boolean {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0;
	}

	clearText(column: ViewColumn): void {
		this.bulkCheckboxService.handleFiltering();
		if (column.filter) {
			column.filter = '';
			this.updateGridState({skip: 0});
			if ( this.preventFilterSearch(column)) {
				return; // prevent search
			}
			this.onReload();
		}
	}

	onReload(): void {
		this.modelChange.emit();
	}

	onFilter(): void {
		this.updateGridState({skip: 0});
		this.onReload();
	}

	private preventFilterSearch(column: ViewColumn): boolean {
		let key = `${column.domain}_${column.property}`;
		let oldVal = this.columnFiltersOldValues[key];
		this.columnFiltersOldValues[key] = column.filter;
		return oldVal === column.filter;
	}

	protected onFilterKeyUp(e: KeyboardEvent, column?: any): void {
		if ( this.preventFilterSearch(column)) {
			return; // prevent search
		}
		if (e.code === KEYSTROKE.ENTER) {
			this.onFilter();
		} else if (!this.notAllowedCharRegex.test(e.code)) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = setTimeout(() => this.onFilter(), SEARCH_QUITE_PERIOD);
		}
	}

	protected onPaste(column?: any): void {
		if ( this.preventFilterSearch(column)) {
			return; // prevent search
		}
		clearTimeout(this.typingTimeout);
		this.typingTimeout = setTimeout(() => this.onFilter(), SEARCH_QUITE_PERIOD);
	}

	protected onFilterKeyDown(e: KeyboardEvent): void {
		this.bulkCheckboxService.handleFiltering();

		if (!this.notAllowedCharRegex.test(e.code)) {
			clearTimeout(this.typingTimeout);
		}
	}

	private applyData(data: any): void {
		const {assets = null, pagination = null} = data || {};
		const total = pagination && pagination.total || 0;

		this.gridMessage = 'ASSET_EXPLORER.GRID.NO_RECORDS';
		this.bulkCheckboxService.initializeKeysBulkItems(assets || []);

		this.gridData = {
			data: assets || [],
			total
		};

		this.showMessage = total === 0;
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	private clear(): void {
		this.showMessage = true;
		this.gridMessage = 'ASSET_EXPLORER.GRID.SCHEMA_CHANGE';
		this.gridData = null;
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
		this.updateGridState({
			skip: 0,
			take: this.gridState.take,
			sort: []
		});
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		if (state.sort[0]) {
			// Invert the Order to remove the Natural/Default from the UI (no arrow)
			if (!state.sort[0].dir) {
				state.sort[0].dir = (this.model.sort.order === 'a' ? 'desc' : 'asc');
			}

			let field = state.sort[0].field.split('_');
			this.model.sort.domain = field[0];
			this.model.sort.property = field[1];
			this.model.sort.order = state.sort[0].dir === 'asc' ? 'a' : 'd';
		}
		this.updateGridState(state);
		this.modelChange.emit();
	}

	protected createDependencyPromise(assetClass: string, id: number) {
		setTimeout(() => {
			this.dialog.open(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
				DIALOG_SIZE.LG).then(x => {
					this.createDependencyPromise(x.assetClass, x.id);
				}).catch(x => {
					console.log(x);
				});
		}, 500);
	}

	/**
	 * On Show the Dialog for the current selected Asset
	 * @param data
	 */
	protected onShow(data: any) {
		this.dialog.open(AssetShowComponent, [
			{ provide: 'ID', useValue: data['common_id'] },
			{ provide: 'ASSET', useValue: data['common_assetClass'] }],
			DIALOG_SIZE.LG, false)
			.then(asset => {
				if (asset) {
					this.createDependencyPromise(asset.assetClass, asset.id);
				}
				this.onReload();
			}).catch(error => {
				console.log('Error:', error);
				this.onReload();
			});
	}

	/**
	 * display the create asset modal
	 */
	protected onCreateAsset(assetEntityType: string): void {
		if (!assetEntityType) {
			return;
		}
		this.dialog.open(AssetCreateComponent, [
				{ provide: 'ASSET', useValue: assetEntityType }],
			DIALOG_SIZE.LG, false).then(x => {
			if (x) {
				this.createDependencyPromise(x.assetClass, 0);
			}
		}).catch(x => {
			console.log(x);
		});
	}

	onWidthChange(data: any) {
		this.model.columns.filter((c: ViewColumn) =>
			data[0].column.field === `${c.domain}_${c.property}`
		).forEach((c: ViewColumn) => {
			c.width = data[0].newWidth;
		});
	}

	onChangeBulkCheckbox(checkboxState: CheckboxState): void {
		this.bulkCheckboxService.changeState(checkboxState);
	}

	checkItem(id: number, checked: boolean): void {
		this.bulkCheckboxService.checkItem(id, checked, this.gridData.data.length);
	}

	onBulkOperationResult(operationResult: BulkActionResult): void {
		if (operationResult && operationResult.success) {
			this.bulkCheckboxService.uncheckItems();
			this.onReload();
		}
	}

	protected canCreateAssets(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected showTask(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		const assetModalModel: AssetModalModel = {
			assetId: dataItem.common_id,
			assetName: dataItem.common_assetName,
			assetType: dataItem.common_assetClass,
			modalType: 'TASK'
		}

		this.dialog.open(TaskCommentDialogComponent, [
			{provide: AssetModalModel, useValue: assetModalModel},
			{provide: 'currentUserId', useValue: this.currentUser.id}
		], DIALOG_SIZE.LG, true).then(result => {
			if (result) {
				console.log('Show Task Result',  result);
			}
		}).catch(result => {
			console.log(result);
		});

	}

	/**
	 * Open the Task Create
	 * @param dataItem
	 */
	public createTask(dataItem: any, rowIndex: number): void {
		this.highlightGridRow(rowIndex);
		let taskCreateModel: TaskDetailModel = {
			id: dataItem.common_id,
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE
			},
			detail: {
				assetClass: dataItem.common_assetClass,
				assetEntity: dataItem.common_id,
				assetName: dataItem.common_assetName,
				currentUserId: this.currentUser.id
			}
		};

		this.dialog.extra(TaskCreateComponent, [
			{provide: TaskDetailModel, useValue: taskCreateModel}
		], false, false)
			.then(result => {
				if (result) {
					this.onReload();
				}

			}).catch(result => {
			console.log('Cancel:', result);
		});

	}

	protected showComment(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		const assetModalModel: AssetModalModel = {
			assetId: dataItem.common_id,
			assetName: dataItem.common_assetName,
			assetType: dataItem.common_assetClass,
			modalType: 'COMMENT'
		}

		this.dialog.open(TaskCommentDialogComponent, [
			{provide: AssetModalModel, useValue: assetModalModel},
			{provide: 'currentUserId', useValue: this.currentUser.id}
		], DIALOG_SIZE.LG, true).then(result => {
			if (result) {
				console.log('Show Comment Result',  result);
			}
		}).catch(result => {
			console.log(result);
		});
	}

	protected createComment(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		let singleCommentModel: SingleCommentModel = {
			modal: {
				title: 'Create Comment',
				type: ModalType.CREATE
			},
			archive: false,
			comment: '',
			category: '',
			assetClass: {
				text: dataItem.common_assetClass
			},
			asset: {
				id: dataItem.common_id,
				text: dataItem.common_assetName
			}
		};

		this.dialog.extra(SingleCommentComponent, [
			{provide: SingleCommentModel, useValue: singleCommentModel}
		], false, false).then(result => {
			console.log('RESULT SINGLE COMMENT', result);
			this.onReload();
		}).catch(result => {
			console.log(result);
		});
	}

	protected showAssetEditView(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		const componentParameters = [
			{ provide: 'ID', useValue: dataItem.common_id },
			{ provide: 'ASSET', useValue: dataItem.common_assetClass }
		];

		this.dialog.open(AssetEditComponent, componentParameters, DIALOG_SIZE.LG)
			.then(() => this.onReload())
			.catch((err) => this.onReload() )
	}

	/**
	 * Allows to display the clone asset modal
	 */
	protected showAssetCloneView(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);

		const cloneModalModel: AssetModalModel = {
			assetType: dataItem.common_assetClass,
			assetId: dataItem.common_id
		}
		this.dialog.extra(AssetCloneComponent, [
			{provide: AssetModalModel, useValue: cloneModalModel}
		], false, false).then( (result: CloneCLoseModel)  => {

			if (result.clonedAsset && result.showEditView) {
				const componentParameters = [
					{ provide: 'ID', useValue: result.assetId },
					{ provide: 'ASSET', useValue: dataItem.common_assetClass }
				];

				this.dialog.open(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
			} else if (!result.clonedAsset && result.showView) {
				const data: any = {
					common_id: result.assetId,
					common_assetClass: dataItem.common_assetClass
				};
				this.onShow(data);

			}
		}).catch( error => console.log('error', error));
	}

	protected setCreatebuttonState(state: ASSET_ENTITY_DIALOG_TYPES) {
		if (this._viewId === this.ASSET_ENTITY_MENU.All_ASSETS) {
			this.createButtonState = state;
		}
	}

	/**
	 * set the asset type depends on the view that is display in order to set
	 * by default the behavior of the create button
	 * @param viewId
	 */
	protected setActionCreateButton(viewId) {
		switch (viewId) {
			case this.ASSET_ENTITY_MENU.All_APPLICATIONS:
				this.createButtonState = this.ASSET_ENTITY_DIALOG_TYPES.APPLICATION;
				break;
			case this.ASSET_ENTITY_MENU.All_DATABASES:
				this.createButtonState = this.ASSET_ENTITY_DIALOG_TYPES.DATABASE;
				break;
			case this.ASSET_ENTITY_MENU.All_DEVICE:
			case this.ASSET_ENTITY_MENU.All_STORAGE_PHYSICAL:
			case this.ASSET_ENTITY_MENU.All_SERVERS:
				this.createButtonState = this.ASSET_ENTITY_DIALOG_TYPES.DEVICE;
				break;
			case this.ASSET_ENTITY_MENU.All_STORAGE_VIRTUAL:
				this.createButtonState = this.ASSET_ENTITY_DIALOG_TYPES.STORAGE;
				break;
		}
	}

	/**
	 * Validates if should display the create button, depends on the view
	 * that is trying to show.
	 */
	protected displayCreateButton() {
		return this._viewId === this.ASSET_ENTITY_MENU.All_ASSETS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_APPLICATIONS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_DATABASES ||
			this._viewId === this.ASSET_ENTITY_MENU.All_DEVICE ||
			this._viewId === this.ASSET_ENTITY_MENU.All_STORAGE_PHYSICAL ||
			this._viewId === this.ASSET_ENTITY_MENU.All_SERVERS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_STORAGE_VIRTUAL;
	}

	/**
	 * It was fixed by Kendo itself, this just prevent the double click
	 * @param event:any
	 */
	public onClickTemplate(event: any): void {
		event.preventDefault();
	}

	/**
	 * On cell click event.
	 * Determines if cell clicked property is either assetName or assetId and opens detail popup.
	 * @param e
	 */
	protected  cellClick(e): void {
		if (['common_assetName', 'common_id'].indexOf(e.column.field) !== -1) {
			this.onShow(e.dataItem);
		}
	}

	/**
	 * Allow to highlight the row grid
	 */
	private highlightGridRow(rowIndex) {
		jQuery('tr.k-state-selected').removeClass('k-state-selected');
		jQuery(`tr[data-kendo-grid-item-index=${rowIndex}]`).addClass('k-state-selected');
	}

	/**
	 * Returns specific class name based on the cell property name.
	 * @param {ViewColumn} column
	 * @returns {string} asset-detail-link for assetName or assetId.
	 */
	private getCellClass(column: ViewColumn): string {
		if (['common_assetName', 'common_id'].indexOf(column.domain + '_' + column.property) !== -1) {
			let classColumn = 'asset-detail-link';
			if (column.property === 'assetName') {
				classColumn += ' asset-detail-name-column';
			}
			return classColumn;
		}
		return '';
	}

	onChangeJustPlanning(isChecked = false): void {
		this.justPlanningChange.emit(isChecked);

		this.preferenceService.setPreference(PREFERENCE_JUST_PLANNING, isChecked.toString()).subscribe(() => {
			this.onReload();
		});
	}

	/**
	 * On Asset Tag Filter change, run the query.
	 * @param $event
	 */
	protected onTagFilterChange(column: ViewColumn, $event: any): void {
		column.filter = '';
		let operator = $event.operator && $event.operator === 'ALL' ? '&' : '|';
		let selectedTagsFilter = ($event.tags as Array<TagModel>).map( tag => tag.id).join(`${operator}`);
		column.filter = selectedTagsFilter;
		this.onFilter();
	}

	/**
	 * Gather the List of Selected Items for the Bulk Process
	 */
	public onClickBulkButton(): void {
		this.bulkCheckboxService.getBulkSelectedItems(this._viewId, this.model, this.justPlanning).subscribe((results: any) => {
			this.bulkItems = [...results.selectedAssetsIds];
			this.selectedAssetsForBulk = [...results.selectedAssets];
			this.tdsBulkChangeButton.bulkData({bulkItems: this.bulkItems, assetsSelectedForBulk: this.selectedAssetsForBulk});
		}, (err) => console.log('Error:', err));
	}

	hasSelectedItems(): boolean {
		return this.bulkCheckboxService.hasSelectedItems();
	}

	getSelectedItemsCount(): number {
		const allCounter = (this.gridData && this.gridData.total) || 0;
		return this.bulkCheckboxService.getSelectedItemsCount(allCounter)
	}

	private getCurrentUser() {
		this.userService.getUserInfo()
			.subscribe( (user: any) => {
				this.currentUser = user;
			}, (error: any) => console.log(error));
	}

	/**
	 * Set the grid configuration and emit the event to the host component
	 */
	private updateGridState(state: State): void {
		const newState = Object.assign({}, this.gridState, state) ;
		this.gridStateChange.emit(newState);
	}
}
