import { Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
import {State} from '@progress/kendo-data-query';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DomainModel} from '../../../fieldSettings/model/domain.model';
import {FieldSettingsModel} from '../../../fieldSettings/model/field-settings.model';
import {ViewGroupModel, ViewModel} from '../../../assetExplorer/model/view.model';
import {ViewColumn, QueryColumn} from '../../../assetExplorer/model/view-spec.model';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {AssetViewSelectorComponent} from '../../../assetManager/components/asset-view-selector/asset-view-selector.component';
import {AssetViewSaveComponent} from '../../../assetManager/components/asset-view-save/asset-view-save.component';
import {AssetViewExportComponent} from '../../../assetManager/components/asset-view-export/asset-view-export.component';
import {Permission} from '../../../../shared/model/permission.model';
import {VIEW_COLUMN_MIN_WIDTH} from '../../../assetExplorer/model/view-spec.model';
import {AssetQueryParams} from '../../../assetExplorer/model/asset-query-params';
import {AssetExportModel} from '../../../assetExplorer/model/asset-export-model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import {clone} from 'ramda';
import { AssetViewShowComponent } from '../asset-view-show/asset-view-show.component';
import {SaveOptions} from '../../../../shared/model/save-options.model';
import {
	ASSET_NOT_OVERRIDE_STATE,
	ASSET_OVERRIDE_CHILD_STATE,
	ASSET_OVERRIDE_PARENT_STATE, OverrideState
} from '../models/asset-view-override-state.model';
declare var jQuery: any;
@Component({
	selector: 'tds-asset-view-config',
	templateUrl: 'asset-view-config.component.html'
})
export class AssetViewConfigComponent implements OnInit, OnDestroy {
	@ViewChild('select') select: AssetViewSelectorComponent;

	public data: any = null;
	private dataSignature: string;
	protected justPlanning: boolean;
	public saveOptions: any;
	public gridState: State = {
		skip: 0,
		take: GRID_DEFAULT_PAGE_SIZE,
		sort: []
	};
	// There will be more custom classes, but this are the list who has already an Icon
	assetClasses = ['APPLICATION', 'DEVICE', 'DATABASE', 'STORAGE'];
	filterModel = {
		assets: {
			'COMMON': true,
			'APPLICATION': false,
			'DATABASE': false,
			'DEVICE': false,
			'STORAGE': false
		},
		asset: 'all',
		selected: 'all',
		search: ''
	};
	collapsed = false;
	collapsedColumnsPreview = false;
	columnIndex = 0;
	rowIndex = 0;

	draggableColumns: QueryColumn[];
	model: ViewModel;
	domains: DomainModel[] = [];
	filteredData: DomainModel[] = [];
	fields: FieldSettingsModel[] = [];
	allFields: FieldSettingsModel[] = [];
	position: any[] = [];
	currentTab = 0;
	public metadata: any = {};
	private queryParams: any = {};
	currentOverrideState: OverrideState;
	private navigationSubscription: any;
	private lastSnapshot: any;
	private lastViewId;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private assetExplorerService: AssetExplorerService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService) {
		this.initResolveData();
	}

	ngOnInit(): void {
		if (this.model && this.model.id) {
			this.notifier.broadcast({
				name: 'notificationHeaderTitleChange',
				title: this.model.name
			});
			this.onPreview();
		}
		this.reloadStrategy();
	}

	/**
	 * After Report Resolver has finished initialize models/variables of the component.
	 */
	initResolveData(isReload = false): void {
		this.justPlanning = false;
		this.metadata.tagList = this.route.snapshot.data['tagList'];
		this.allFields = this.route.snapshot.data['fields'];
		this.fields = this.route.snapshot.data['fields'];
		this.domains = this.route.snapshot.data['fields'];
		const {dataView, saveOptions} = this.route.snapshot.data['report'];
		this.model = dataView || this.route.snapshot.data['report'];
		this.saveOptions = saveOptions;
		this.dataSignature = JSON.stringify(this.model);
		this.draggableColumns = [];
		this.handleQueryParams();
		if (this.model && this.model.id) {
			this.updateFilterbyModel();
			this.currentTab = 1;
			this.draggableColumns = this.model.schema.columns.slice();
			this.handleOverrideState(this.model);
			if (isReload) {
				this.onPreview();
			}
		} else {
			this.currentOverrideState = ASSET_NOT_OVERRIDE_STATE;
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
				this.initResolveData(true);
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
			this.currentOverrideState = ASSET_OVERRIDE_CHILD_STATE;
		} else if (hasOverride) {
			this.currentOverrideState = ASSET_OVERRIDE_PARENT_STATE;
		} else {
			this.currentOverrideState = ASSET_NOT_OVERRIDE_STATE;
		}
	}

	toggleAssetView() {
		let navigateToViewId = this.model.id;
		const _override = !this.queryParams._override;
		if (!_override && this.model.overridesView) {
			navigateToViewId = this.model.overridesView.id
		} else if (this.lastViewId) {
			navigateToViewId = this.lastViewId;
		}
		this.lastViewId = this.model.id;
		return this.router.navigate(['/asset', 'views', navigateToViewId, 'edit'], {
			queryParams: { _override }
		});
	}

	protected updateFilterbyModel() {
		this.model.schema.domains.forEach((domain: string) => this.filterModel.assets[domain.toUpperCase()] = true);
		this.applyFilters();
		let columns = this.model.schema.columns.map(x => `${x.domain}.${x.property}`);
		this.fields
			.filter(f => columns.indexOf(`${f['domain']}.${f.field}`) !== -1)
			.forEach(x => x['selected'] = true);
	}

	protected updateModelbyFilter() {
		this.model.schema.domains = this.selectedAssetClasses().map(x => x.toLowerCase());
		if (!this.isAssetSelected()) {
			this.model.schema.domains = [];
			this.model.schema.columns = [];
			this.draggableColumns = this.model.schema.columns.slice();
			this.clearSorting();
		} else {
			this.model.schema.columns = this.model.schema.columns
				.filter(c => this.model.schema.domains.indexOf(c.domain) !== -1);
			this.draggableColumns = this.model.schema.columns.slice();
		}
		this.fields.filter(x => x['selected'] &&
			this.model.schema.domains.indexOf(x['domain'].toLowerCase()) === -1)
			.forEach(x => delete x['selected']);
		this.applyFilters();
		this.data = null;
	}

	/** Filter Methods */

	protected applyFilters(): void {
		this.applyAssetSelectFilter();
		this.applyAssetClassFilter();
		this.applyFieldFilter();
		this.applySelectedFilter();
		this.fields = this.mapFieldList();
		this.columnIndex = 0;
		this.rowIndex = 0;
		this.position = this.fields.map(x => this.fieldStyle(x['isTitle']));
		setTimeout(() => {
			jQuery('[data-toggle="popover"]').popover();
		}, 200);
	}

	protected applyAssetSelectFilter(): void {
		let classes = this.selectedAssetClasses();
		this.filteredData = this.domains
			.filter(x => classes.indexOf(x.domain.toUpperCase()) !== -1)
			.map(domain => {
				return { ...domain };
			});
	}

	protected applyAssetClassFilter(): void {
		if (this.filterModel.asset !== 'all') {
			this.filteredData = this.filteredData.filter(domain => domain.domain.toUpperCase() === this.filterModel.asset.toUpperCase());
		}
	}

	protected applyFieldFilter(): void {
		if (this.filterModel.search !== '') {
			let regx = new RegExp(this.filterModel.search, 'i');
			this.filteredData.forEach(domain => {
				domain.fields = domain.fields.filter(field =>
					regx.test(field.label) || regx.test(field.field));
			});
		}
	}

	protected applySelectedFilter(): void {
		if (this.filterModel.selected !== 'all') {
			this.filteredData.forEach(domain => {
				domain.fields = domain.fields.filter(field =>
					this.filterModel.selected === 'true' ? field['selected'] : !field['selected']);
			});
		}
	}

	/**Miscellaneus Methods */
	private mapFieldList(): FieldSettingsModel[] {
		return this.filteredData
			.reduce((p: FieldSettingsModel[], c: DomainModel) => {
				if (c.fields.length > 0) {
					let domainTitle = new FieldSettingsModel();
					domainTitle['domain'] = c.domain.toLowerCase();
					domainTitle['isTitle'] = true;
					p.push(domainTitle);
				}
				return p.concat(c.fields);
			}, []);
	}

	protected selectedAssetClasses(): string[] {
		return Object.keys(this.filterModel.assets).filter(key => this.filterModel.assets[key]);
	}

	protected fieldStyle(isTitle: boolean): any {
		if (isTitle && (this.columnIndex !== 0 || this.rowIndex !== 0)) {
			this.columnIndex += 1;
			this.rowIndex = 0;
		} else if (this.rowIndex >= 15) {
			this.columnIndex += 1;
			this.rowIndex = 1;
		}
		let result = {
			'height': '25px',
			'width': '160px',
			'top': this.rowIndex * 25 + 'px',
			'left': this.columnIndex * 160 + 'px',
			'position': 'absolute',
			'margin': '0px',
			'padding-left': '10px',
			'padding-bottom': '5px'
		};
		this.rowIndex += 1;
		return result;
	}

	protected openSaveDialog(): void {
		const selectedData = this.select.data.filter(x => x.name === 'Favorites')[0];
		this.dialogService.open(AssetViewSaveComponent, [
			{ provide: ViewModel, useValue: this.model },
			{ provide: ViewGroupModel, useValue: selectedData },
			{ provide: SaveOptions, useValue: this.saveOptions }
		]).then(result => {
			this.model = result;
			this.dataSignature = JSON.stringify(this.model);
			setTimeout(() => {
				this.router.navigate(['asset', 'views', this.model.id, 'edit']);
			});
		}).catch(result => {
			console.log('error');
		});
	}

	/** Validation and Permission Methods */

	protected isAssetSelected(): boolean {
		return this.model && this.model.schema && this.model.schema.domains.filter(x => x !== 'common').length > 0;
	}

	protected isColumnSelected(): boolean {
		return this.model.schema.columns.length > 0;
	}

	public isValid(): boolean {
		return (
			this.isAssetSelected() &&
			this.isColumnSelected() &&
			this.hasAtLeastOneNonLockedColumnOrEmpty()
		)

	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.model);
		// TODO: hasPendingChanges
		// if (this.state && this.state.$current && this.state.$current.data) {
		// 	this.state.$current.data.hasPendingChanges = result && !this.collapsed;
		// }
		return result;
	}

	public isSaveAvailable(): boolean {
		return this.assetExplorerService.isSaveAvailable(this.model);
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.id ?
			(	this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) && this.canCreateViews() :
				this.permissionService.hasPermission(Permission.AssetExplorerSaveAs) && this.canCreateViews()
			)
			: this.isSaveAvailable();
	}

	/**
	 * Check if user has AssetExplorerCreate permission.
	 */
	canCreateViews(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	public isSystemSaveAvailable(edit): boolean {
		return edit ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs);
	}

	protected isCurrentTab(num: number): boolean {
		return this.currentTab === num;
	}

	protected setCurrentTab(num: number): void {
		this.currentTab = num;
	}

	/**
	 * Get the Icon of the Asset class, if does not exist use the Other generic icon
	 * @param {string} assetName
	 * @returns {string}
	 */
	protected getAssetIcon(assetName: string): string {
		let validAssetIconClass = this.assetClasses.find((r) => r === assetName);
		if (!validAssetIconClass) {
			validAssetIconClass = 'other';
		}
		return validAssetIconClass.toLocaleLowerCase() + '_menu';
	}

	/** Dialog and view Actions methods */

	protected onSaveAs(): void {
		if (this.isSaveAsAvailable()) {
			this.openSaveDialog();
		}
	}

	public onCancel() {
		if (this.model && this.model.id) {
			const _override = this.queryParams._override;
			this.router.navigate(['asset', 'views', this.model.id, 'show'],
				{ queryParams: { _override } });
		} else {
			this.router.navigate(['asset', 'views']);
		}
	}

	protected onSave() {
		if (this.isSaveAvailable()) {
			if (this.model.id) {
				this.assetExplorerService.saveReport(this.model)
					.subscribe(result => {
						this.dataSignature = JSON.stringify(this.model);
						this.select.loadData();
					});
			} else {
				this.openSaveDialog();
			}
		}
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
			viewName: this.model.name
		};

		this.dialogService.open(AssetViewExportComponent, [
			{ provide: AssetExportModel, useValue: assetExportModel }
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error');
		});
	}

	protected onFieldSelection(field: FieldSettingsModel) {
		if (field['selected']) {
			this.model.schema.columns.push({
				domain: field['domain'].toLowerCase(),
				property: field.field,
				width: VIEW_COLUMN_MIN_WIDTH,
				locked: false,
				edit: false,
				label: field.label,
				filter: ''
			});
			if (this.model.schema.columns.length === 1) {
				this.model.schema.sort = {
					domain: field['domain'].toLowerCase(),
					property: field.field,
					order: 'a'
				};
				this.gridState = Object.assign({}, this.gridState,
					{
						sort: [{ field: `${field['domain'].toLowerCase()}_${field.field}`, dir: 'asc' }]
					});
			}
		} else {
			let index = this.model.schema.columns
				.findIndex(x => x.domain === field['domain'].toLowerCase() && x.property === field.field);

			if (index !== -1) {
				this.model.schema.columns.splice(index, 1);
			}
			if (this.gridState.sort.length > 0
				&& this.gridState.sort[0].field === `${field['domain'].toLowerCase()}_${field.field}`
				&& this.model.schema.columns.length > 0) {
				this.gridState.sort = [{ field: `${this.model.schema.columns[0]['domain'].toLowerCase()}_${this.model.schema.columns[0]}`, dir: 'asc' }];
				this.model.schema.sort.domain = this.model.schema.columns[0].domain;
				this.model.schema.sort.property = this.model.schema.columns[0].property;
			}

			if (!this.model.schema.columns.length) {
				this.clearSorting();
			}

		}
		this.draggableColumns = this.model.schema.columns.slice();
		this.data = null;
		this.model.schema = clone(this.model.schema);
	}

	/**
	 * This method allows to modify the signature to allow to change favorite without affect the model.
	 * TODO: It could be moved into a Signature Class Utility to not have all this logic spread on sereval components
	 * @param {boolean} isFavorite
	 */
	private modifyFavoriteSignature(isFavorite: boolean): void {
		let signature = JSON.parse(this.dataSignature);
		signature.isFavorite = isFavorite;
		this.dataSignature = JSON.stringify(signature);
	}

	public onFavorite() {
		if (this.model.isFavorite) {
			if (this.model.id) {
				this.assetExplorerService.deleteFavorite(this.model.id)
					.subscribe(d => {
						this.model.isFavorite = false;
						this.modifyFavoriteSignature(this.model.isFavorite);
						this.select.loadData();
					});
			} else {
				this.model.isFavorite = false;
			}

		} else {
			if (this.assetExplorerService.hasMaximumFavorites(this.select.data.filter(x => x.name === 'Favorites')[0].items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				if (this.model.id) {
					this.assetExplorerService.saveFavorite(this.model.id)
						.subscribe(d => {
							this.model.isFavorite = true;
							this.modifyFavoriteSignature(this.model.isFavorite);
							this.select.loadData();
						});
				} else {
					this.model.isFavorite = true;
				}
			}
		}
	}

	public onPreview(): void {
		if (this.isValid()) {
			let params = this.getQueryParams();
			this.assetExplorerService.previewQuery(params)
				.subscribe(result => {
					this.data = result;
					jQuery('[data-toggle="popover"]').popover();
				}, err => console.log(err));
		} else {
			this.data = null;
		}
	}

	private freezeColumn(item: ViewColumn): void {
		item.locked = !item.locked;
		this.onLockedColumn(item);
	}

	protected onLockedColumn(item: ViewColumn): void {
		let itemIndex = this.model.schema.columns.findIndex(x => x.domain === item.domain && x.property === item.property);
		this.model.schema.columns.splice(itemIndex, 1);
		let lockeds = this.model.schema.columns.filter((x: ViewColumn) => x.locked).length;
		this.model.schema.columns.splice(lockeds, 0, item);
		this.draggableColumns = this.model.schema.columns.slice();
	}

	/**
	 * Prepare the Params for the Query with the current UI configuration
	 * @returns {AssetQueryParams}
	 */
	private getQueryParams(): AssetQueryParams {
		let assetQueryParams = {
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
			assetQueryParams['justPlanning'] = this.justPlanning;
		}
		return assetQueryParams;
	}

	protected onClearTextFilter() {
		this.filterModel.search = '';
		this.applyFilters();
	}

	/**
	 * Switch between View and Edit
	 * if is on the Edit and then move in to the View disables the confirm dialog
	 */
	public onToggleConfig(): void {
		this.collapsed = !this.collapsed;
		setTimeout(() => {
			this.notifier.broadcast({
				name: 'grid.header.position.change'
			});
		}, 1200);
	}

	protected onToggleSelectedColumnsPreview(): void {
		this.collapsedColumnsPreview = !this.collapsedColumnsPreview;
	}

	protected onDragEnd(): void {
		this.model.schema.columns = this.draggableColumns.slice();
	}

	private clearSorting() {
		this.gridState.sort = [];
		delete this.model.schema.sort;
	}

	/**
	 -
	 * Whenever the just planning change, grab the new value
	 * @param justPlanning New value
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
	 -
	 * Determine if exists at least one non locked column
	 * or if the collection is empty
	 * @returns {boolean}
	 */
	private hasAtLeastOneNonLockedColumnOrEmpty(): boolean {
		const columns = this.model.schema && this.model.schema.columns || [];

		if (columns.length === 0) {
			return true;
		}

		const nonLocked =  columns
			.filter((column: ViewColumn) => !column.locked);

		return Boolean(nonLocked.length);
	}

	/**
	 * Store query params from url.
	 */
	private handleQueryParams() {
		this.route.queryParams.subscribe((params) => {
			const _override = !params._override || params._override === 'true' || params._override === true;
			this.queryParams = { _override };
		});
	}

	/**
	 * Checks if current model is a system view
	 */
	isSystemView(): boolean {
		return this.model.isSystem
	}

	/**
	 * Ensure the listener is not available after moving away from this component
	 */
	ngOnDestroy(): void {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}

}
