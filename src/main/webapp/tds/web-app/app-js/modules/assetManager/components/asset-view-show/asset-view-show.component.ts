// Angular
import {Component, ViewChild, OnInit, OnDestroy, ComponentFactoryResolver} from '@angular/core';
import {ActivatedRoute, Router, NavigationEnd} from '@angular/router';
// Model
import {ViewGroupModel, ViewModel} from '../../../assetExplorer/model/view.model';
import {Permission} from '../../../../shared/model/permission.model';
import {AlertType} from '../../../../shared/model/alert.model';
import {GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {AssetQueryParams} from '../../../assetExplorer/model/asset-query-params';
import {DomainModel} from '../../../fieldSettings/model/domain.model';
import {AssetExportModel} from '../../../assetExplorer/model/asset-export-model';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AssetGlobalFiltersService} from '../../service/asset-global-filters.service';
// Component
import {AssetViewSelectorComponent} from '../asset-view-selector/asset-view-selector.component';
import {AssetViewSaveComponent} from '../../../assetManager/components/asset-view-save/asset-view-save.component';
import {AssetViewExportComponent} from '../../../assetManager/components/asset-view-export/asset-view-export.component';
import {DialogExit, DialogService, HeaderActionButtonData, ModalSize} from 'tds-component-library';
// Other
import {State} from '@progress/kendo-data-query';
import {AssetViewGridComponent} from '../asset-view-grid/asset-view-grid.component';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
import { ASSET_ENTITY_DIALOG_TYPES } from '../../../assetExplorer/model/asset-entity.model';
import { PREFERENCES_LIST, PreferenceService } from '../../../../shared/services/preference.service';
import { takeUntil } from 'rxjs/operators';
import { Observable, ReplaySubject } from 'rxjs';
import { fixContentWrapper } from '../../../../shared/utils/data-grid-operations.helper';
import { ViewColumn, ViewSpec } from '../../../assetExplorer/model/view-spec.model';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-view-show',
	templateUrl: 'asset-view-show.component.html'
})
export class AssetViewShowComponent implements OnInit, OnDestroy {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];

	private currentId;
	private dataSignature: string;
	public fields: DomainModel[] = [];
	public model: ViewModel = new ViewModel();
	protected domains: DomainModel[] = [];
	public metadata: any = {};
	private lastSnapshot;
	protected navigationSubscription;
	protected justPlanning: boolean;
	protected globalQueryParams = {};
	public data: any;
	public gridState: State = {
		skip: 0,
		take: GRID_DEFAULT_PAGE_SIZE,
		sort: []
	};
	protected readonly SAVE_BUTTON_ID = 'btnSave';
	protected readonly SAVEAS_BUTTON_ID = 'btnSaveAs';
	// When the URL contains extra parameters we can determinate the form contains hidden filters
	public hiddenFilters = false;
	private unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	@ViewChild('select', {static: false}) select: AssetViewSelectorComponent;
	@ViewChild('assetExplorerViewGrid', {static: false}) assetExplorerViewGrid: AssetViewGridComponent;
	private TAG_ASSET_COLUMN_WIDTH = 260;
	protected gridMessage;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private route: ActivatedRoute,
		private router: Router,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifier: NotifierService,
		protected translateService: TranslatePipe,
		private assetGlobalFiltersService: AssetGlobalFiltersService,
		private assetTagUIWrapperService: AssetTagUIWrapperService,
		private preferenceService: PreferenceService) {

		this.metadata.tagList = this.route.snapshot.data['tagList'];
		this.fields = this.route.snapshot.data['fields'];
		this.domains = this.route.snapshot.data['fields'];
		this.model = this.route.snapshot.data['report'];
		this.prepareView(this.model);
		this.dataSignature = this.stringifyCopyOfModel(this.model);
	}

	ngOnInit(): void {
		fixContentWrapper();
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'download-cloud',
				title: this.translateService.transform('ASSET_EXPORT.VIEW'),
				show: true,
				onClick: this.onExport.bind(this),
			},
			{
				icon: 'cog',
				title: this.translateService.transform('ASSET_EXPLORER.CONFIGURE_VIEW'),
				show: true,
				onClick: this.onEdit.bind(this),
			},
		];

		// Get all Query Params
		this.route.queryParams.subscribe(map => map);
		this.globalQueryParams = this.route.snapshot.queryParams;
		this.hiddenFilters = !ValidationUtils.isEmptyObject(this.globalQueryParams);
		this.reloadStrategy();
		this.initialiseComponent();
	}

	/**
	 * Configuration for tag assets column width.
	 */
	prepareView(model: ViewModel): void {
		if (model && model.schema && model.schema.columns) {
			model.schema.columns.forEach((column: ViewColumn) => {
				if (column.property === 'tagAssets') {
					column.width = this.TAG_ASSET_COLUMN_WIDTH;
				}
			});
		}
	}

	/**
	 * Ensure the listener is not available after moving away from this component
	 */
	ngOnDestroy(): void {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}

	/**
	 * Reload Strategy keep listen To change to the route so we can reload whatever is inside the component
	 * Increase dramatically the Performance
	 */
	private reloadStrategy(): void {
		// The following code Listen to any change made on the rout to reload the page
		this.navigationSubscription = this.router.events.subscribe((event: any) => {
			if (event.snapshot && event.snapshot.data && event.snapshot.data.fields) {
				this.lastSnapshot = event.snapshot;
			}
			// If it is a NavigationEnd event re-initalise the component
			if (event instanceof NavigationEnd) {
				if (this.currentId && this.currentId !== this.lastSnapshot.params.id) {
					this.metadata.tagList = this.lastSnapshot.data['tagList'];
					this.fields = this.lastSnapshot.data['fields'];
					this.domains = this.lastSnapshot.data['fields'];
					this.model = this.lastSnapshot.data['report'];
					this.dataSignature = this.stringifyCopyOfModel(this.model);
					this.initialiseComponent();
				}
			}
		});
	}

	/**
	 * Calls every time the Component is recreated by calling same URL
	 */
	private initialiseComponent(): void {
		this.justPlanning = false;
		this.currentId = this.model.id;
		this.notifier.broadcast({
			name: 'notificationHeaderTitleChange',
			title: this.model.name
		});
		this.getPreferences()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((preferences: any) => {
				this.justPlanning = (preferences[PREFERENCES_LIST.ASSET_JUST_PLANNING])
					? preferences[PREFERENCES_LIST.ASSET_JUST_PLANNING].toString() === 'true'
					: false;
			});
		this.gridState = Object.assign({}, this.gridState, {sort: [
				{
					field: `${this.model.schema.sort.domain}_${this.model.schema.sort.property}`,
					dir: this.model.schema.sort.order === 'a' ? 'asc' : 'desc'
				}
			]});
	}

	public onQuery(): void {
		this.gridMessage = this.translateService.transform('GLOBAL.LOADING_RECORDS');
		// Timeout exists so assetExplorerViewGrid gets created first
		setTimeout(() => {
			let params = {
				offset: this.gridState.skip,
				limit: this.gridState.take,
				sortDomain: this.model.schema.sort.domain,
				sortProperty: this.model.schema.sort.property,
				sortOrder: this.model.schema.sort.order,
				filters: {
					domains: this.model.schema.domains,
					columns: this.model.schema.columns
				}
			};

			if (this.hiddenFilters) {
				this.assetGlobalFiltersService.prepareFilters(params, this.globalQueryParams);

				let justPlanning = this.assetGlobalFiltersService.getJustPlaningFilter(this.globalQueryParams);
				if (justPlanning !== null) {
					this.assetExplorerViewGrid.justPlanning = justPlanning;
				}
			}

			if (this.justPlanning) {
				params['justPlanning'] = true;
			}

			this.assetExplorerService.query(this.model.id, params).subscribe(result => {
				this.data = result;
				this.data.assets.forEach(asset => {
					if (asset.common_lastUpdated) {
						asset.common_lastUpdated = new Date(asset.common_lastUpdated);
					}
				});
				this.gridMessage = (this.data.assets.length <= 0) ? this.translateService.transform('GLOBAL.NO_RECORDS') : '';
				jQuery('[data-toggle="popover"]').popover();
			}, err => console.log(err));
		}, 2000);
	}

	public onEdit(): void {
		if (this.isEditAvailable()) {
			this.router.navigate(['asset', 'views', this.model.id, 'edit']);
		}
	}

	public onSave() {
		this.assetExplorerService.saveReport(this.model)
			.subscribe(result => {
				this.dataSignature = this.stringifyCopyOfModel(this.model);
			});
	}

	public onSaveAs(): void {
		const selectedData = this.select.data.filter(x => x.name === 'Favorites')[0];
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetViewSaveComponent,
			data: {
				viewModel: this.model,
				viewGroupModel: selectedData
			},
			modalConfiguration: {
				title: 'Save List View',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe( (data: any) => {
			if (data.status === DialogExit.ACCEPT) {
				this.model = data;
				this.dataSignature = this.stringifyCopyOfModel(this.model);
				setTimeout(() => {
					this.router.navigate(['asset', 'views', this.model.id, 'edit']);
				});
			}
		});
	}

	public isDirty(): boolean {
		let result = this.dataSignature !== this.stringifyCopyOfModel(this.model);
		return result;
	}

	public onExport(): void {
		let assetExportModel: AssetExportModel = {
			assetQueryParams: AssetViewShowComponent.getQueryParamsForExport(
				this.data,
				this.gridState,
				this.model,
				this.justPlanning
			),
			domains: this.domains,
			queryId: this.model.id,
			viewName: this.model.name
		};

		if (this.hiddenFilters) {
			this.assetGlobalFiltersService.prepareFilters(assetExportModel.assetQueryParams, this.globalQueryParams);
		}

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetViewExportComponent,
			data: {
				assetExportModel: assetExportModel
			},
			modalConfiguration: {
				title: 'Export to Excel',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe();
	}

	public onFavorite(): void {
		if (this.model.isFavorite) {
			this.assetExplorerService.deleteFavorite(this.model.id)
				.subscribe(d => {
					this.model.isFavorite = false;
					this.select.loadData();
				});
		} else {
			if (this.assetExplorerService.hasMaximumFavorites(this.select.data.filter(x => x.name === 'Favorites')[0].views.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.assetExplorerService.saveFavorite(this.model.id)
					.subscribe(d => {
						this.model.isFavorite = true;
						this.select.loadData();
					});
			}
		}
	}

	/**
	 * Prepare the Params for the Query with the current UI configuration
	 * Params should 'limit' to total number of total from the gridData since we want to export ALL DATA, not
	 * the configured pagination results.
	 * @returns {AssetQueryParams}
	 */
	public static getQueryParamsForExport(data: any, gridState: State, model: ViewModel, justPlanning: boolean): AssetQueryParams {
		let assetQueryParams: AssetQueryParams = {
			offset: data ? 0 : gridState.skip,
			limit: data ? data.pagination.total : gridState.take,
			forExport: true,
			sortDomain: model.schema.sort.domain,
			sortProperty: model.schema.sort.property,
			sortOrder: model.schema.sort.order,
			filters: {
				domains: model.schema.domains,
				columns: model.schema.columns
			}
		};

		if (justPlanning) {
			assetQueryParams['justPlanning'] = justPlanning;
		}

		return assetQueryParams;
	}

	/**
	 * Removes isFavorite property from view model and returns stringified json.
	 * @param {ViewModel} model
	 * @returns {ViewModel}
	 */
	private stringifyCopyOfModel(model: ViewModel): string {
		// ignore 'favorite' property.
		let modelCopy = {...model};
		delete modelCopy.isFavorite;
		return JSON.stringify(modelCopy);
	}

	/**
	 * Determines if show we can show Save/Save All buttons at all.
	 * @returns {boolean}
	 */
	public canShowSaveButton(): boolean {
		return this.model.id && (this.canSave() || this.canSaveAs());
		// long & old validation will leave it for the moment.
		// return this.model.id && (this.model.isOwner || (this.model.isSystem && (this.isSystemSaveAvailable(true) && this.isSystemSaveAvailable(false))));
	}

	/**
	 * Determines if current user can Save view.
	 * @returns {boolean}
	 */
	public canSave(): boolean {
		// If it's all assets view then don't Save is allowed.
		if (this.assetExplorerService.isAllAssets(this.model)) {
			return false;
		}
		// If it's a system view
		if (this.model.isSystem) {
			return this.permissionService.hasPermission(Permission.AssetExplorerEdit) &&
				this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit);
		} else {
			// If it's not a system view.
			return this.model.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerEdit);
		}
	}

	/**
	 * Determine if current user can Save As (new) view.
	 * @returns {boolean}
	 */
	public canSaveAs(): boolean {
		// If it's a system view
		if (this.model.isSystem) {
			return this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) &&
				this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs);
		} else {
			// If it's not a system view
			return this.permissionService.hasPermission(Permission.AssetExplorerCreate) &&
				this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
		}
	}

	public isEditAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit);
	}

	/**
	 * After every time the hidden filter changes, propagate the value
	 * @param hiddenFilters
	 */
	public onHiddenFiltersChange(hiddenFilters: boolean): void {
		this.hiddenFilters = hiddenFilters;
	}

	/**
	 * Whenever the grid state change, grab the new value
	 * @param state New state
	 */
	public onGridStateChange(state: State): void {
		this.gridState = state;
		setTimeout(() => {
			this.assetTagUIWrapperService.updateTagsWidth('.single-line-tags' , 'span.dots-for-tags');
		}, 500);
	}

	/**
	 * Determines if primary button can be Save or Save all based on permissions.
	 */
	public getSaveButtonId(): string {
		return this.canSave() ? this.SAVE_BUTTON_ID : this.SAVEAS_BUTTON_ID;
	}

	/**
	 * Group all the dynamic information required by the view in just one function
	 * @return {any} Object with the values required dynamically by the view
	 */
	public getDynamicConfiguration(): any {
		return {
			isDirty: this.isDirty() ,
			saveButtonId: this.getSaveButtonId(),
			canSave: this.canSave(),
			canSaveAs: this.canSaveAs(),
			isEditAvailable: this.isEditAvailable(),
			canShowSaveButton: this.canShowSaveButton(),
		}
	}

	/**
	 * Returns the grid configuration.
	 */
	getGridConfig(): any {
		return this.assetExplorerViewGrid ?
			this.assetExplorerViewGrid.getDynamicConfiguration() : null;
	}

	/**
	 * On create asset, call grid create asset action.
	 * @param assetEntityType
	 */
	onCreateAsset(assetEntityType: ASSET_ENTITY_DIALOG_TYPES): void {
		this.assetExplorerViewGrid.onCreateAsset(assetEntityType);
		this.assetExplorerViewGrid.setCreatebuttonState(assetEntityType);
	}

	/**
	 * Calls to grid clear all filters.
	 */
	onClearAllFilters(): void {
		this.assetExplorerViewGrid.onClearFilters();
	}

	/**
	 * Returns the number of current filters applied from grid.
	 */
	filterCount(): number {
		return this.assetExplorerViewGrid ? this.assetExplorerViewGrid.filterCount() : 0;
	}

	/**
	 * Save Just Planning preference and launch the search again.
	 * @param isChecked
	 */
	onJustPlanningChange(isChecked = false): void {
		this.preferenceService.setPreference(PREFERENCES_LIST.ASSET_JUST_PLANNING, isChecked.toString())
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(() => {
				this.onQuery();
			});
	}

	/**
	 * Returns the preferences from service.
	 */
	private getPreferences(): Observable<any> {
		return this.preferenceService.getPreferences(PREFERENCES_LIST.ASSET_JUST_PLANNING);
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCount() === 0;
	}
}
