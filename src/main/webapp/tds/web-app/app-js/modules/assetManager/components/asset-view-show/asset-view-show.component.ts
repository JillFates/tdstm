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
// Component
import {AssetViewSelectorComponent} from '../asset-view-selector/asset-view-selector.component';
import {AssetViewSaveComponent} from '../../../assetManager/components/asset-view-save/asset-view-save.component';
import {AssetViewExportComponent} from '../../../assetManager/components/asset-view-export/asset-view-export.component';
// Other
import {State} from '@progress/kendo-data-query';
import * as R from 'ramda';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-view-show',
	templateUrl: 'asset-view-show.component.html'
})
export class AssetViewShowComponent implements OnInit, OnDestroy {

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

	@ViewChild('select') select: AssetViewSelectorComponent;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifier: NotifierService,
		protected translateService: TranslatePipe) {

		this.metadata.tagList = this.route.snapshot.data['tagList'];
		this.fields = this.route.snapshot.data['fields'];
		this.domains = this.route.snapshot.data['fields'];
		this.model = this.route.snapshot.data['report'];
		this.dataSignature = this.stringifyCopyOfModel(this.model);
	}

	ngOnInit(): void {

		// Get all Query Params
		this.route.queryParams.subscribe(map => map);
		this.globalQueryParams = this.route.snapshot.queryParams;

		this.reloadStrategy();
		this.initialiseComponent();
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
				console.log(event);
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

		this.gridState = Object.assign({}, this.gridState, {sort: [
				{
					field: `${this.model.schema.sort.domain}_${this.model.schema.sort.property}`,
					dir: this.model.schema.sort.order === 'a' ? 'asc' : 'desc'
				}
			]});
	}

	public onQuery(): void {
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

		if (this.justPlanning) {
			params['justPlanning'] = true;
		}

		this.addGlobalFilters(params.filters);

		this.assetExplorerService.query(this.model.id, params).subscribe(result => {
			this.data = result;
			jQuery('[data-toggle="popover"]').popover();
		}, err => console.log(err));
	}

	/**
	 * Add Global Filters to the Query Params based on the URL
	 */
	private addGlobalFilters(filters: any): void {
		// Verify if we have a global query param
		if (!R.isEmpty(this.globalQueryParams)) {
			// Iterate the URL Params
			Object.entries(this.globalQueryParams).forEach(query => {
				let queryFilter = query[0].split('_');
				// Validate the domain exists
				if (filters.domains.find((x) => x === queryFilter[0])) {
					let filterColumn = filters.columns.find(r => r.domain === queryFilter[0] && r.property === queryFilter[1]);
					// Validate the property exists
					if (filterColumn) {
						filterColumn.filter = query[1];
					}
					// if not, Do we add it  to the filters?
				}
			});
		}
	}

	protected onEdit(): void {
		if (this.isEditAvailable()) {
			this.router.navigate(['asset', 'views', this.model.id, 'edit']);
		}
	}

	protected onSave() {
		this.assetExplorerService.saveReport(this.model)
			.subscribe(result => {
				this.dataSignature = this.stringifyCopyOfModel(this.model);
			});
	}

	protected onSaveAs(): void {
		const selectedData = this.select.data.filter(x => x.name === 'Favorites')[0];
		this.dialogService.open(AssetViewSaveComponent, [
			{ provide: ViewModel, useValue: this.model },
			{ provide: ViewGroupModel, useValue: selectedData }
		]).then(result => {
			this.model = result;
			this.dataSignature = this.stringifyCopyOfModel(this.model);
			setTimeout(() => {
				this.router.navigate(['asset', 'views', this.model.id, 'edit']);
			});
		}).catch(result => {
			console.log('error');
		});
	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== this.stringifyCopyOfModel(this.model);
		return result;
	}

	public onExport(): void {
		let assetExportModel: AssetExportModel = {
			assetQueryParams: this.getQueryParamsForExport(),
			domains: this.domains,
			queryId: this.model.id,
			viewName: this.model.name
		};

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
	private getQueryParamsForExport(): AssetQueryParams {
		let assetQueryParams: AssetQueryParams = {
			offset: this.data ? 0 : this.gridState.skip,
			limit: this.data ? this.data.pagination.total : this.gridState.take,
			forExport: true,
			sortDomain: this.model.schema.sort.domain,
			sortProperty: this.model.schema.sort.property,
			sortOrder: this.model.schema.sort.order,
			filters: {
				domains: this.model.schema.domains,
				columns: this.model.schema.columns
			}
		};
		if (this.justPlanning) {
			assetQueryParams['justPlanning'] = this.justPlanning;
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
	protected canSave(): boolean {
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
	protected canSaveAs(): boolean {
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
	 * Whenever the just planning change, grab the new value
	 * @param justPlanning
	 */
	public onJustPlanningChange(justPlanning: boolean): void {
		this.justPlanning = justPlanning;
	}

	/**
	 -
	 * Whenever the grid state change, grab the new value
	 * @param state New state
	 */
	public onGridStateChange(state: State): void {
		this.gridState = state;
	}

	/**
	 * Determines which save operation to call based on the button id.
	 */
	protected save(saveButtonId: string) {
		saveButtonId === this.SAVE_BUTTON_ID ? this.onSave() : this.onSaveAs()
	}

	/**
	 * Determines if primary button can be Save or Save all based on permissions.
	 */
	protected getSaveButtonId(): string {
		return this.canSave() ? this.SAVE_BUTTON_ID : this.SAVEAS_BUTTON_ID;
	}
}