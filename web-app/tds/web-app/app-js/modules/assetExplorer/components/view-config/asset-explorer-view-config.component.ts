import { Component, Inject, ViewChild, OnInit } from '@angular/core';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { PermissionService } from '../../../../shared/services/permission.service';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { FieldSettingsModel } from '../../../fieldSettings/model/field-settings.model';
import { StateService } from '@uirouter/angular';
import { Observable } from 'rxjs/Rx';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ViewModel } from '../../model/view.model';
import { ViewColumn, QueryColumn } from '../../model/view-spec.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { AssetExplorerViewGridComponent } from '../view-grid/asset-explorer-view-grid.component';
import { AssetExplorerViewSelectorComponent } from '../view-selector/asset-explorer-view-selector.component';
import { AssetExplorerViewSaveComponent } from '../view-save/asset-explorer-view-save.component';
import { AssetExplorerViewExportComponent } from '../view-export/asset-explorer-view-export.component';
import { Permission } from '../../../../shared/model/permission.model';
import { VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { AssetQueryParams } from '../../model/asset-query-params';
import { AssetExportModel } from '../../model/asset-export-model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { DictionaryService } from '../../../../shared/services/dictionary.service';
import { LAST_VISITED_PAGE } from '../../../../shared/model/constants';

@Component({
	selector: 'asset-explorer-View-config',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-config/asset-explorer-view-config.component.html',
	styles: [`
		.pd-top-5 { padding-top:5px; }
		.disabled { border-top-color: #777 !important;}
		.disabled a { color:#777 !important; }
		.C { background-color: #F9FF90;}
        .I { background-color: #D4F8D4;}
        .N { background-color: #FFF;}
		.U { background-color: #F3F4F6;}
		li.active a { font-weight:bold;}
		.drag-icon { padding: 8px 10px 0px 5px; color: #9f9f9f;}
		.drag-label {
			padding: 0;
			overflow: hidden;
			height: 20px;
			word-break: break-all;
		}
	`]
})
export class AssetExplorerViewConfigComponent implements OnInit {
	@ViewChild('grid') grid: AssetExplorerViewGridComponent;
	@ViewChild('select') select: AssetExplorerViewSelectorComponent;

	private dataSignature: string;
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
	position: any[] = [];
	currentTab = 0;
	previewButtonClicked = false;

	constructor(
		@Inject('report') report: Observable<ViewModel>,
		private assetExpService: AssetExplorerService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private state: StateService,
		private notifier: NotifierService,
		@Inject('fields') fields: Observable<DomainModel[]>,
		private prompt: UIPromptService,
		private dictionary: DictionaryService) {
		Observable.zip(fields, report).subscribe((result: [DomainModel[], ViewModel]) => {
			this.domains = result[0];
			this.model = { ...result[1] };
			this.dataSignature = JSON.stringify(this.model);
			if (this.model.id) {
				this.updateFilterbyModel();
				this.currentTab = 1;
				this.state.$current.data.page.title = this.model.name;
				document.title = this.model.name;
				this.draggableColumns = this.model.schema.columns.slice();
			}
		}, (err) => console.log(err));
	}

	ngOnInit(): void {
		if (this.model.id) {
			this.previewButtonClicked = true;
			this.onPreview();
		}
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
		} else {
			this.model.schema.columns = this.model.schema.columns
				.filter(c => this.model.schema.domains.indexOf(c.domain) !== -1);
			this.draggableColumns = this.model.schema.columns.slice();
		}
		this.fields.filter(x => x['selected'] &&
			this.model.schema.domains.indexOf(x['domain'].toLowerCase()) === -1)
			.forEach(x => delete x['selected']);
		this.applyFilters();
		this.grid.clear();
		this.previewButtonClicked = false;
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
		this.dialogService.open(AssetExplorerViewSaveComponent, [
			{ provide: ViewModel, useValue: this.model },
			{ provide: 'favorites', useValue: this.select.data.filter(x => x.name === 'Favorites')[0] }
		]).then(result => {
			this.model = result;
			this.dataSignature = JSON.stringify(this.model);
			setTimeout(() => {
				this.state.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
			});
		}).catch(result => {
			console.log('error');
		});
	}

	/** Validation and Permission Methods */

	protected isAssetSelected(): boolean {
		return this.model.schema.domains.filter(x => x !== 'common').length > 0;
	}

	protected isColumnSelected(): boolean {
		return this.model.schema.columns.length > 0;
	}

	protected isValid(): boolean {
		return this.isAssetSelected() && this.isColumnSelected();
	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.model);
		if (this.state && this.state.$current && this.state.$current.data) {
			this.state.$current.data.hasPendingChanges = result;
		}
		return result;
	}

	protected isSaveAvailable(): boolean {
		return this.model.id ?
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
				this.model.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerEdit) :
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) :
				this.model.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.id ?
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
				this.permissionService.hasPermission(Permission.AssetExplorerSaveAs) :
			this.isSaveAvailable();
	}

	protected isSystemSaveAvailable(edit): boolean {
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

	protected onCancel() {
		const routeState = this.dictionary.get(LAST_VISITED_PAGE);
		if (routeState && routeState === AssetExplorerStates.REPORT_SHOW.name && this.model.id) {
			this.state.go(routeState, { id: this.model.id });
		} else {
			this.state.go(AssetExplorerStates.REPORT_SELECTOR.name);
		}
	}

	protected onSave() {
		if (this.isSaveAvailable()) {
			if (this.model.id) {
				this.assetExpService.saveReport(this.model)
					.subscribe(result => {
						this.dataSignature = JSON.stringify(this.model);
						this.select.loadData();
					});
			} else {
				this.openSaveDialog();
			}
		}
	}

	protected onExport(): void {
		let assetExportModel: AssetExportModel = {
			assetQueryParams: this.getQueryParams(),
			domains: this.domains
		};

		this.dialogService.open(AssetExplorerViewExportComponent, [
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
				this.grid.state.sort = [{ field: `${field['domain'].toLowerCase()}_${field.field}`, dir: 'asc' }];
			}
		} else {
			let index = this.model.schema.columns
				.findIndex(x => x.domain === field['domain'].toLowerCase() && x.property === field.field);

			if (index !== -1) {
				this.model.schema.columns.splice(index, 1);
			}
			if (this.grid.state.sort.length > 0
				&& this.grid.state.sort[0].field === `${field['domain'].toLowerCase()}_${field.field}`
				&& this.model.schema.columns.length > 0) {
				this.grid.state.sort = [{ field: `${this.model.schema.columns[0]['domain'].toLowerCase()}_${this.model.schema.columns[0]}`, dir: 'asc' }];
				this.model.schema.sort.domain = this.model.schema.columns[0].domain;
				this.model.schema.sort.property = this.model.schema.columns[0].property;
			}
		}
		this.draggableColumns = this.model.schema.columns.slice();
		this.grid.clear();
		this.previewButtonClicked = false;
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

	protected onFavorite() {
		if (this.model.isFavorite) {
			if (this.model.id) {
				this.assetExpService.deleteFavorite(this.model.id)
					.subscribe(d => {
						this.model.isFavorite = false;
						this.modifyFavoriteSignature(this.model.isFavorite);
						this.select.loadData();
					});
			} else {
				this.model.isFavorite = false;
			}

		} else {
			if (this.assetExpService.hasMaximumFavorites(this.select.data.filter(x => x.name === 'Favorites')[0].items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				if (this.model.id) {
					this.assetExpService.saveFavorite(this.model.id)
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

	protected onPreview(): void {
		if (this.isValid() && this.previewButtonClicked) {
			let params = this.getQueryParams();
			this.assetExpService.previewQuery(params)
				.subscribe(result => {
					this.grid.apply(result);
				}, err => console.log(err));
		} else {
			this.grid.gridData = null;
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
			offset: this.grid.state.skip,
			limit: this.grid.state.take,
			sortDomain: this.model.schema.sort.domain,
			sortProperty: this.model.schema.sort.property,
			sortOrder: this.model.schema.sort.order,

			filters: {
				domains: this.model.schema.domains,
				columns: this.model.schema.columns
			}
		};
		if (this.grid.justPlanning) {
			assetQueryParams['justPlanning'] = this.grid.justPlanning;
		}
		return assetQueryParams;
	}

	protected onClearTextFilter() {
		this.filterModel.search = '';
		this.applyFilters();
	}

	protected onToggleConfig(): void {
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
}