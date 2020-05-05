import {
	Component,
	EventEmitter,
	Input,
	OnInit,
	Output,
	ViewChild,
	OnChanges,
	SimpleChanges,
	ChangeDetectionStrategy,
	OnDestroy, HostListener, ComponentFactoryResolver
} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
// Model
import {UserContextModel} from '../../../auth/model/user-context.model';
import {VIEW_COLUMN_MIN_WIDTH, VIEW_COLUMN_MIN_WIDTH_SHRINK, ViewColumn, ViewSpec} from '../../../assetExplorer/model/view-spec.model';
import {
	DIALOG_SIZE,
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
	ModalType,
} from '../../../../shared/model/constants';
import {
	CUSTOM_FIELD_CONTROL_TYPE,
	FIELD_NOT_FOUND,
	FieldSettingsModel
} from '../../../fieldSettings/model/field-settings.model';
import {TagModel} from '../../../assetTags/model/tag.model';
import {BulkActionResult, BulkChangeType} from '../../../../shared/components/bulk-change/model/bulk-change.model';
import {CheckboxState, CheckboxStates} from '../../../../shared/components/tds-indeterminate-checkbox/model/tds-indeterminate-checkbox.model';
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {NumberConfigurationConstraintsModel} from '../../../fieldSettings/components/number/number-configuration-constraints.model';
import {SELECT_ALL_COLUMN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {AssetCommentModel} from '../../../assetComment/model/asset-comment.model';
const {
	ASSET_JUST_PLANNING: PREFERENCE_JUST_PLANNING,
	ASSET_LIST_SIZE: PREFERENCE_LIST_SIZE,
	WRAP_TAGS_COLUMN: PREFERENCE_WRAP_TAGS_COLUMN,
} = PREFERENCES_LIST;
import {ASSET_ENTITY_MENU} from '../../../../shared/modules/header/model/asset-menu.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../../assetExplorer/model/asset-entity.model';
import {AssetModalModel} from '../../../assetExplorer/model/asset-modal.model';
import {
	COMMON_SHRUNK_COLUMNS,
	COMMON_SHRUNK_COLUMNS_WIDTH, EXTRA_COLUMN_WIDTH,
	EXTRA_WIDTH_COLUMNS
} from '../../../../shared/constants/common-shrunk-columns';
import {DialogService, ModalSize} from 'tds-component-library';
// Component
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import {TaskCommentDialogComponent} from '../../../assetExplorer/components/task-comment/dialog/task-comment-dialog.component';
import {AssetEditComponent} from '../../../assetExplorer/components/asset/asset-edit.component';
import {AssetCreateComponent} from '../../../assetExplorer/components/asset/asset-create.component';
import {AssetCloneComponent} from '../../../assetExplorer/components/asset-clone/asset-clone.component';
import {TaskEditCreateComponent} from '../../../taskManager/components/edit-create/task-edit-create.component';
import {BulkChangeButtonComponent} from '../../../../shared/components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {AssetTagSelectorComponent} from '../../../../shared/components/asset-tag-selector/asset-tag-selector.component';
import {AssetCommentViewEditComponent} from '../../../assetComment/components/view-edit/asset-comment-view-edit.component';
// Service
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserService} from '../../../auth/service/user.service';
import {BulkCheckboxService} from '../../../../shared/services/bulk-checkbox.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import {Observable, ReplaySubject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {State} from '@progress/kendo-data-query';
import {
	DataStateChangeEvent,
	GridDataResult, PageChangeEvent,
	RowClassArgs
} from '@progress/kendo-angular-grid';
import { ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {AssetGlobalFiltersService} from '../../service/asset-global-filters.service';

declare var jQuery: any;

@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: 'asset-view-grid.component.html'
})
export class AssetViewGridComponent implements OnInit, OnChanges, OnDestroy {
	@Input() data: any;
	@Input() model: ViewSpec;
	@Input() justPlanning: boolean;
	@Input() gridState: State;
	@Output() modelChange = new EventEmitter<void>();
	@Output() removeGlobalQueryParam = new EventEmitter<{domain?: string, property: string}>();
	@Output() gridStateChange = new EventEmitter<State>();
	@Output() hiddenFiltersChange = new EventEmitter<boolean>();
	@Input() edit: boolean;
	@Input() metadata: any;
	@Input() fields: any;
	@Input() hiddenFilters = false;
	@Input() gridMessage;
	@ViewChild('tagSelector', {static: false}) tagSelector: AssetTagSelectorComponent;
	@ViewChild('tdsBulkChangeButton', {static: false}) tdsBulkChangeButton: BulkChangeButtonComponent;
	@Input()
	set viewId(viewId: number) {
		this._viewId = viewId;
		this.displayCreateButton = this.getDisplayCreateButton();
		// changing the view reset selections
		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
		this.setActionCreateButton(viewId);
		this.gridStateChange.emit({...this.gridState, skip: 0});
		this.modelChange.emit();
	}

	protected globalQueryParams = {};
	public currentFields = [];
	public toggleTagsColumn = false;
	public VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	public VIEW_COLUMN_MIN_WIDTH_SHRINK = VIEW_COLUMN_MIN_WIDTH_SHRINK;
	public showMessage = true;
	public typingTimeout: any;
	ASSET_ENTITY_MENU = ASSET_ENTITY_MENU;
	ASSET_ENTITY_DIALOG_TYPES = ASSET_ENTITY_DIALOG_TYPES;
	protected userTimeZone: string;
	protected userDateFormat: string;
	protected showAssetsFilter = false;
	private displayCreateButton: boolean;
	private showFullTags = false;

	// Pagination Configuration
	notAllowedCharRegex = /ALT|ARROW|F+|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;
	private maxDefault = GRID_DEFAULT_PAGE_SIZE;
	public maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public currentPageSize = this.maxDefault;
	private _viewId: number;
	public fieldNotFound = FIELD_NOT_FOUND;
	gridData: GridDataResult;
	selectAll = false;
	protected tagList: Array<TagModel> = [];
	public bulkItems: number[] = [];
	protected selectedAssetsForBulk: Array<any>;
	public createButtonState: ASSET_ENTITY_DIALOG_TYPES;
	private currentUser: any;
	protected fieldPipeMap: {pipe: any, metadata: any};
	protected bulkChangeType: BulkChangeType = BulkChangeType.Assets;
	protected SELECT_ALL_COLUMN_WIDTH = SELECT_ALL_COLUMN_WIDTH;
	private canCreateAssets: boolean;
	commonShrunkColumns = COMMON_SHRUNK_COLUMNS;
	commonShrunkColumnWidth = COMMON_SHRUNK_COLUMNS_WIDTH;
	EXTRA_WIDTH_COLUMNS = EXTRA_WIDTH_COLUMNS;
	EXTRA_COLUMN_WIDTH = EXTRA_COLUMN_WIDTH;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private preferenceService: PreferenceService,
		private bulkCheckboxService: BulkCheckboxService,
		private notifier: NotifierService,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private userService: UserService,
		private userContextService: UserContextService,
		private assetTagUIWrapperService: AssetTagUIWrapperService,
		private translateService: TranslatePipe,
		private assetGlobalFiltersService: AssetGlobalFiltersService,
		private route: ActivatedRoute,
		private router: Router) {
		this.fieldPipeMap = {pipe: {}, metadata: {}};
		this.userContextService.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.userDateFormat = userContext.dateFormat;
				this.userTimeZone = userContext.timezone;
			});
	}

	ngOnInit(): void {
		this.router.events.subscribe((e) => {
			if (e instanceof NavigationEnd) {
				this.onClearHiddenFilters();
			}
		});

		this.gridData = {
			data: [],
			total: 0
		};
		this.canCreateAssets = this.permissionService.hasPermission(Permission.AssetCreate);

		this.selectedAssetsForBulk = [];
		this.getPreferences()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((preferences: any) => {
				let pageSize = parseInt(preferences[PREFERENCE_LIST_SIZE], 10) || 25;
				this.updateGridState({take: pageSize});
				this.currentPageSize = pageSize;
				this.bulkCheckboxService.setPageSize(pageSize);
				this.toggleTagsColumn = (preferences[PREFERENCE_WRAP_TAGS_COLUMN]) ? preferences[PREFERENCE_WRAP_TAGS_COLUMN].toString() === 'true' : false;
				this.showFullTags = this.toggleTagsColumn;
				this.onReload();
		});

		this.globalQueryParams = this.route.snapshot.queryParams;
		this.hiddenFilters = !ValidationUtils.isEmptyObject(this.globalQueryParams);

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
		this.gridMessage = (changes['gridMessage'] && changes['gridMessage'].currentValue) ? changes['gridMessage'].currentValue : (this.gridMessage) ? this.gridMessage : '';

		if (dataChange && dataChange.currentValue !== dataChange.previousValue) {
			this.applyData(dataChange.currentValue);
			setTimeout(() => {
				this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
			}, 500);
		}
	}

	private getPreferences(): Observable<any> {
		return this.preferenceService.getPreferences(PREFERENCE_LIST_SIZE, PREFERENCE_WRAP_TAGS_COLUMN);
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

	/**
	 * Gets the data type of the specified column
	 * @param {ViewColumn} column
	 * @returns {string}
	 */
	getType(column: ViewColumn): string {
		const controlMap = {
			'String' : 'text',
			'Date' : 'date',
			'DateTime' : 'dateTime',
			'Number' : 'number',
			'Boolean' : 'boolean'
		}
		let domainFields = this.fields.find(key => key.domain === column.domain.toUpperCase());
		let field = domainFields ? domainFields.fields.find(key => key.field === column.property) : null;
		if (field) {
			return controlMap[field.control];
		}
		column.notFound = true;
		return null;
	}

	rowCallbackClass(context: RowClassArgs) {
		let obj = {};
		obj[context.dataItem.common_assetClass.toLowerCase()] = true;
		return obj;
	}

	/**
	 * Removes all the fiilters selected
	 * First removes the global query parameters
	 */
	public onClearFilters(): void {
		this.removeGlobalQueryParam.emit({property: '*'});

		setTimeout(() => {
			this.model.columns.forEach((c: ViewColumn) => {
				c.filter = '';
			});
			this.onFilter();
			if (this.tagSelector) {
				this.tagSelector.reset();
			}
			this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
		}, 500);
	}

	/**
	 * Clear all hidden filters
	 */
	public onClearHiddenFilters(): void {
		this.hiddenFilters = false;
		this.hiddenFiltersChange.emit(this.hiddenFilters);
		this.onClearFilters();
	}

	hasFilterApplied(): boolean {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0 || this.hiddenFilters;
	}

	onReload(): void {
		this.modelChange.emit();
		setTimeout(() => {
			this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
		}, 500);
	}

	/**
	 * Set the filter value to the new search string and start off the filtering process
	 * if the search value is empty, notify to the host component ir order to remove the global query params
	 * @param {string} search - Current search value
	 * @param {ViewColumn} column - Column of the datagrid which threw the event
	*/
	public setFilter(search: string, column: ViewColumn): void {
		column.filter = search;
		if (search === null || search === '') {
			// removes the filter from the globals in case
			this.removeGlobalQueryParam.emit({domain: column.domain, property: column.property});
			// update the filters
			setTimeout(() => {
				this.onFilter();
			}, 500);
		} else {
			this.onFilter();
		}
	}

	/**
	 * Notify to the bulkcheckbox service about a datagrid operation and execute the datagrid update
	*/
	private onFilter(): void {
		this.bulkCheckboxService.handleFiltering();
		this.updateGridState({skip: 0});
		this.onReload();
	}

	private applyData(data: any): void {
		const {assets = null, pagination = null} = data || {};
		const total = pagination && pagination.total || 0;

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
		this.gridMessage = this.translateService.transform('ASSET_EXPLORER.GRID.SCHEMA_CHANGE');
		this.gridData = null;
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
		this.updateGridState({
			skip: 0,
			take: this.gridState.take,
			sort: []
		});
	}

	public dataStateChange(state: DataStateChangeEvent): void {
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
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AssetShowComponent,
				data: {
					assetId: id,
					assetClass: assetClass
				},
				modalConfiguration: {
					title: '&nbsp;',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			}).subscribe( (data: any) => {
				this.createDependencyPromise(data.assetClass, data.id);
			});
		}, 500);
	}

	/**
	 * On Show the Dialog for the current selected Asset
	 * @param data
	 */
	protected onShow(data: any) {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetShowComponent,
			data: {
				assetId: data['common_id'],
				assetClass: data['common_assetClass']
			},
			modalConfiguration: {
				title: '&nbsp;',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe( (data: any) => {
			if (data.asset) {
				this.createDependencyPromise(data.asset.assetClass, data.asset.id);
			}
			this.onReload();
		});
	}

	/**
	 * display the create asset modal
	 */
	public onCreateAsset(assetEntityType: string): void {
		if (!assetEntityType) {
			return;
		}

		let assetClassReadable = assetEntityType.charAt(0).toUpperCase() + assetEntityType.slice(1).toLowerCase();

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCreateComponent,
			data: {
				assetClass: assetEntityType,
				openFromList: true
			},
			modalConfiguration: {
				title: assetClassReadable + ' Create',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe( (data: any) => {
			if (data.assetClass) {
				this.createDependencyPromise(data.assetClass, 0);
			}
		});
	}

	onWidthChange(data: any) {
		this.model.columns.filter((c: ViewColumn) =>
			data[0].column.field === `${c.domain}_${c.property}`
		).forEach((c: ViewColumn) => {
			c.width = data[0].newWidth;
		});
		this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
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

	protected showTask(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		const assetModalModel: AssetModalModel = {
			assetId: dataItem.common_id,
			assetName: dataItem.common_assetName,
			assetType: dataItem.common_assetClass,
			modalType: 'TASK'
		}

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskCommentDialogComponent,
			data: {
				assetModalModel: assetModalModel,
				currentUserId: this.currentUser.id
			},
			modalConfiguration: {
				title: 'Create',
				draggable: true,
				modalSize: ModalSize.LG
			}
		}).subscribe();
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
				assetName: dataItem && dataItem.common_assetName || '',
				currentUserId: this.currentUser.id
			}
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskEditCreateComponent,
			data: {
				taskDetailModel: taskCreateModel
			},
			modalConfiguration: {
				title: 'Task',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-task-modal-edit-view-create'
			}
		}).subscribe((data: any) => {
			this.onReload();
		});

		this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');

	}

	protected showComment(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		const assetModalModel: AssetModalModel = {
			assetId: dataItem.common_id,
			assetName: dataItem.common_assetName,
			assetType: dataItem.common_assetClass,
			modalType: 'COMMENT'
		}

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskCommentDialogComponent,
			data: {
				assetModalModel: assetModalModel,
				currentUserId: this.currentUser.id
			},
			modalConfiguration: {
				title: 'Create',
				draggable: true,
				modalSize: ModalSize.LG
			}
		}).subscribe();
	}

	protected createComment(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);
		let assetCommentModel: AssetCommentModel = {
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

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCommentViewEditComponent,
			data: {
				assetCommentModel: assetCommentModel
			},
			modalConfiguration: {
				title: 'Comment',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((data: any) => {
			this.onReload();
		});
	}

	protected showAssetEditView(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetEditComponent,
			data: {
				assetId: dataItem.common_id,
				assetClass: dataItem.common_assetClass,
				openFromList: true
			},
			modalConfiguration: {
				title: '&nbsp;',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe((data) => {
			this.onReload();
			this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
		});
	}

	/**
	 * Allows to display the clone asset modal
	 */
	protected showAssetCloneView(dataItem: any, rowIndex: number) {
		this.highlightGridRow(rowIndex);

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCloneComponent,
			data: {
				cloneModalModel: {
					assetId:  dataItem.common_id,
					assetType: dataItem.common_assetClass,
				}
			},
			modalConfiguration: {
				title: 'Clone Asset',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((result: any) => {
			if (result.clonedAsset && result.showEditView) {
				this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: AssetEditComponent,
					data: {
						assetId: result.assetId,
						assetClass: dataItem.common_assetClass
					},
					modalConfiguration: {
						title: '&nbsp;',
						draggable: true,
						modalSize: ModalSize.CUSTOM,
						modalCustomClass: 'custom-asset-modal-dialog'
					}
				}).subscribe();
			} else if (!result.clonedAsset && result.showView) {
				const data: any = {
					common_id: result.assetId,
					common_assetClass: dataItem.common_assetClass
				};
				this.onShow(data);
			}
			this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
		});

	}

	public setCreatebuttonState(state: ASSET_ENTITY_DIALOG_TYPES) {
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
	protected getDisplayCreateButton() {
		return this._viewId === this.ASSET_ENTITY_MENU.All_ASSETS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_APPLICATIONS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_DATABASES ||
			this._viewId === this.ASSET_ENTITY_MENU.All_DEVICE ||
			this._viewId === this.ASSET_ENTITY_MENU.All_STORAGE_PHYSICAL ||
			this._viewId === this.ASSET_ENTITY_MENU.All_SERVERS ||
			this._viewId === this.ASSET_ENTITY_MENU.All_STORAGE_VIRTUAL;
	}

	/**
	 * Group all the dynamic informaction required by the view in just one function
	 * @return {any} Object with the values required dynamically by the view
	 */
	public getDynamicConfiguration(): any {
		return {
			displayCreateButton: this.displayCreateButton,
			canCreateAssets: this.canCreateAssets,
			hasFilterApplied: this.hasFilterApplied(),
			hasSelectedItems: this.hasSelectedItems(),
			getSelectedItemsCount: this.getSelectedItemsCount()
		}
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
	public cellClick(e): void {
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
		this.bulkCheckboxService.getBulkSelectedItems({
			viewId: this._viewId,
			model: this.model,
			justPlanning: this.justPlanning
		}, this.getBulkAssetIdsFromView.bind(this))
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((results: any) => {
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
		this.userService.getUserContext()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
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

	/**
	 *  Get the bulk list of asset ids
	 */
	private getBulkAssetIdsFromView(params): Observable<any> {
		const {viewId, model, justPlanning} = params;

		let payload = {
			forExport: true,
			offset: 0,
			limit: 0,
			sortDomain: model.sort.domain,
			sortProperty: model.sort.property,
			sortOrder: model.sort.order,
			filters: {
				domains: model.domains,
				columns: model.columns
			}
		};

		if (this.hiddenFilters) {
			this.assetGlobalFiltersService.prepareFilters(payload, this.globalQueryParams);
		}
		if (justPlanning) {
			payload['justPlanning'] = true;
		}

		return this.assetExplorerService.query(viewId, payload);
	}

	/**
	 * Toggles and saves the user preference to either hide or show all the tags inline or inline-block
	 * */
	public onToggleTagsView(event): void {
		event.stopPropagation();
		event.preventDefault();
		this.showFullTags = !this.showFullTags;
		this.preferenceService.setPreference(PREFERENCE_WRAP_TAGS_COLUMN, this.showFullTags.toString())
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(() => {
				if (!this.showFullTags) {
					let refreshItem = document.getElementsByClassName('single-line-tags')[0].parentElement;
					refreshItem.style.paddingTop = '2px';
					setTimeout( () => {
						this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
					}, 500);
				}
				this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
			});
	}

	/**
	 * Changes the height of the table row when the user
	 * moves the mouse out of the tags cell
	 * */
	public refreshTableSize(event): void {
		event.target.parentElement.parentElement.style.height = '28px';
	}

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}

	protected isBulkSelectAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetBulkSelect);
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetEdit);
	}

	protected isTaskCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.TaskCreate);
	}

	protected isCommentCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentCreate);
	}

	protected isAssetCloneAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetCreate);
	}
	/**
	 * Filter Assets Toggle
	 */
	public toggleAssetsFilter(): void {
		this.showAssetsFilter = !this.showAssetsFilter;
	}

	/**
	 * Returns the number of current filters applied.
	 */
	public filterCount(): number {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length
	}

	/**
	 * On Page Change, update grid state & handle pagination on server side.
	 */
	onPageChangeHandler({ skip, take }: PageChangeEvent): void {
		this.gridState.skip = skip;
		this.gridState.take = take;
		this.updateGridState(this.gridState);
		this.modelChange.emit();
	}

	/**
	 * ==============
	 * GRID TEST STUFF
	 * ===============
	 * TODO: delete this stuff below
	 */
	public assetsGrid: any = {
		pageable: {
			pageSizes: [5, 10, 25, 50, 100],
			info: true,
			type: 'input',
		},
		filterable: true,
		sortable: true,
		resizable: true,
		columnMenu: true,
	};

	/**
	 * Get the current status of certain properties, like the property that indicates if grid has filters applied
	 * or the current filter, in order that don't repeat functions calls
	 * (add more properties on demand)
	 */
	public getCurrentStatus(): {isFiltering: boolean, filterCounter: number, showAssetsFilter: boolean} {
		return {
			isFiltering: this.hasFilterApplied(),
			showAssetsFilter: this.showAssetsFilter,
			filterCounter: this.filterCount()
		}
	}
}
