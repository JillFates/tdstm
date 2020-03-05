// Angular
import {Component, ViewChild, OnInit, OnDestroy} from '@angular/core';
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
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AssetGlobalFiltersService} from '../../service/asset-global-filters.service';
// Component
import {AssetViewSelectorComponent} from '../asset-view-selector/asset-view-selector.component';
import {AssetViewSaveComponent} from '../../../assetManager/components/asset-view-save/asset-view-save.component';
import {AssetViewExportComponent} from '../../../assetManager/components/asset-view-export/asset-view-export.component';
// Other
import {State} from '@progress/kendo-data-query';
import {AssetViewGridComponent} from '../asset-view-grid/asset-view-grid.component';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
import {SaveOptions} from '../../../../shared/model/save-options.model';
import { faLayerPlus, faLayerMinus } from '@fortawesome/pro-solid-svg-icons';
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

declare var jQuery: any;

interface OverrideState {
	isOverwritten: boolean;
	icon?: IconDefinition;
	color?: string;
	tooltip?: string;
};
const IS_OVERRIDE_CHILD: OverrideState = {
	icon: faLayerMinus,
	color: 'green',
	isOverwritten: true,
	tooltip: 'Revert to Project View',
};
const	IS_OVERRIDE_PARENT: OverrideState = {
	icon: faLayerPlus,
	color: 'blue',
	isOverwritten: false,
	tooltip: 'Display Personal System View'
};
const	IS_NOT_OVERRIDE: OverrideState = {
	isOverwritten: false
};

@Component({
	selector: 'tds-asset-view-show',
	templateUrl: 'asset-view-show.component.html'
})
export class AssetViewShowComponent implements OnInit, OnDestroy {
	private currentId;
	private dataSignature: string;
	public fields: DomainModel[] = [];
	public model: ViewModel = new ViewModel();
	public saveOptions: any;
	protected domains: DomainModel[] = [];
	public metadata: any = {};
	private lastSnapshot: any;
	protected navigationSubscription: any;
	protected justPlanning: boolean;
	protected globalQueryParams = {};
	public data: any;
	protected readonly SAVE_BUTTON_ID = 'btnSave';
	protected readonly SAVEAS_BUTTON_ID = 'btnSaveAs';
	public currentOverrideState: OverrideState;
	// When the URL contains extra parameters we can determinate the form contains hidden filters
	public hiddenFilters = false;
	public gridState: State = {
		skip: 0,
		take: GRID_DEFAULT_PAGE_SIZE,
		sort: []
	};
	private queryParams: any = {};

	@ViewChild('select') select: AssetViewSelectorComponent;
	@ViewChild('assetExplorerViewGrid') assetExplorerViewGrid: AssetViewGridComponent

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifier: NotifierService,
		protected translateService: TranslatePipe,
		private assetGlobalFiltersService: AssetGlobalFiltersService,
		private assetTagUIWrapperService: AssetTagUIWrapperService) {
		this.initResolveData();
	}

	ngOnInit(): void {
		this.handleQueryParams();
		this.globalQueryParams = this.route.snapshot.queryParams;
		this.hiddenFilters = !ValidationUtils.isEmptyObject(this.globalQueryParams);
		this.reloadStrategy();
		this.initialiseComponent();
	}

	/**
	 * After Report Resolver has finished initialize models/variables of the component.
	 */
	initResolveData(): void {
		this.metadata.tagList = this.route.snapshot.data['tagList'];
		this.fields = this.route.snapshot.data['fields'];
		this.domains = this.route.snapshot.data['fields'];
		const {dataView, saveOptions} = this.route.snapshot.data['report'];
		this.model = dataView;
		this.saveOptions = saveOptions;
		this.dataSignature = this.stringifyCopyOfModel(this.model);
		this.handleOverrideState(this.model);
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
					this.initResolveData();
					this.initialiseComponent();
				}
			}
		});
	}

	/**
	 * Set the override state object.
	 * @param isOverride
	 * @param hasOverride
	 */
	private handleOverrideState({isOverride, hasOverride}: ViewModel): void {
		if (isOverride) {
			this.currentOverrideState = IS_OVERRIDE_CHILD;
		} else if (hasOverride) {
			this.currentOverrideState = IS_OVERRIDE_PARENT;
		} else {
			this.currentOverrideState = IS_NOT_OVERRIDE;
		}
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
		this.gridState = Object.assign({}, this.gridState, {sort: [
				{
					field: `${this.model.schema.sort.domain}_${this.model.schema.sort.property}`,
					dir: this.model.schema.sort.order === 'a' ? 'asc' : 'desc'
				}
			]});
	}

	public onQuery(): void {
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
				jQuery('[data-toggle="popover"]').popover();
			}, err => console.log(err));
		}, 500);
	}

	public onEdit(): void {
		if (this.isEditAvailable()) {
			const _override = this.queryParams._override;
			this.router.navigate(['asset', 'views', this.model.id, 'edit'], {
				queryParams: { _override }
			});
		}
	}

	public toggleAssetView() {
		let dataViewId;
		const _override = !this.queryParams._override;

		if (this.model.overridesView) {
			dataViewId = this.model.overridesView.id
		} else {
			dataViewId = this.model.id;
		}
		return this.router.navigate(['/asset', 'views', dataViewId, 'show'], {
			queryParams: { _override }
		});
	}

	public onSave() {
		this.assetExplorerService.saveReport(this.model)
			.subscribe(result => {
				this.dataSignature = this.stringifyCopyOfModel(this.model);
			});
	}

	public onSaveAs(): void {
		const selectedData = this.select.data.filter(x => x.name === 'Favorites')[0];
		this.dialogService.open(AssetViewSaveComponent, [
			{ provide: ViewModel, useValue: this.model },
			{ provide: ViewGroupModel, useValue: selectedData },
			{ provide: SaveOptions, useValue: this.saveOptions }
		]).then(result => {
			this.model = result;
			this.dataSignature = this.stringifyCopyOfModel(this.model);
			const mode = this.model.overridesView ? 'show' : 'edit';
			setTimeout(() => {
				this.select.loadData();
				this.router.navigate(['asset', 'views', this.model.id, mode]);
			});
		}).catch(result => {
			console.log('error');
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

		this.dialogService.open(AssetViewExportComponent, [
			{ provide: AssetExportModel, useValue: assetExportModel }
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error:', result);
		});
	}

	public onFavorite(): void {
		if (this.model.isFavorite) {
			this.assetExplorerService.deleteFavorite(this.model.id)
				.subscribe(d => {
					this.model.isFavorite = false;
					this.select.loadData();
				});
		} else {
			if (this.assetExplorerService.hasMaximumFavorites(this.select.data.filter(x => x.name === 'Favorites')[0].items.length + 1)) {
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

	private handleQueryParams() {
		this.route.queryParams.subscribe((params) => {
			const _override = !params._override || params._override === 'true' || params._override === true;
			this.queryParams = { _override };
		});
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
	public isOverrideView(): boolean {
		return this.model.isOverride;
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
	 * Determines determines the string of the save button label.
	 * @returns {string}
	 */
	public getSaveButtonLabel() {
		if (!this.canSave() && this.canSaveAs()) {
			return 'GLOBAL.SAVE_AS'
		} else {
			return 'GLOBAL.SAVE'
		}
	}

	/**
	 * Determines if current user can Save view.
	 * @returns {boolean}
	 */
	public canSave(): boolean {
		// If it's all assets view then don't Save is allowed.
		return this.saveOptions.save;
	}

	/**
	 * Determine if current user can Save As (new) view.
	 * @returns {boolean}
	 */
	public canSaveAs(): boolean {
		// If it's a system view
		return (
			this.saveOptions &&
			Array.isArray(this.saveOptions.saveAsOptions) &&
			this.saveOptions.saveAsOptions.length > 0
		);
	}

	public isEditAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit);
	}

	/**
	 * Whenever the just planning change, grab the new value
	 * @param justPlanning
	 */
	public onJustPlanningChange(justPlanning: boolean): void {
		this.justPlanning = justPlanning;
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
	 * Determines which save operation to call based on the button id.
	 */
	public save(saveButtonId: string) {
		saveButtonId === this.SAVE_BUTTON_ID ? this.onSave() : this.onSaveAs()
	}

	/**
	 * Determines if primary button can be Save or Save all based on permissions.
	 */
	public getSaveButtonId(): string {
		return this.canSave() ? this.SAVE_BUTTON_ID : this.SAVEAS_BUTTON_ID;
	}

	public isSaveButtonDisabled(): boolean {
		return !this.canSave() && !this.canSaveAs();
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
			saveButtonLabel: this.getSaveButtonLabel(),
			isEditAvailable: this.isEditAvailable(),
			canShowSaveButton: this.canShowSaveButton(),
			disableSaveButton: this.isSaveButtonDisabled()
		}
	}
}
