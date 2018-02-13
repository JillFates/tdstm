import {Component, Input, Output, EventEmitter, ViewEncapsulation, Inject, ViewChild} from '@angular/core';

import { ViewSpec, ViewColumn, VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { State } from '@progress/kendo-data-query';
import {GridDataResult, DataStateChangeEvent, RowClassArgs} from '@progress/kendo-angular-grid';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { Observable } from 'rxjs/Rx';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { SEARCH_QUITE_PERIOD, MAX_OPTIONS, MAX_DEFAULT, KEYSTROKE } from '../../../../shared/model/constants';
import { AssetShowComponent } from '../asset/asset-show.component';
import { FieldSettingsModel, FIELD_NOT_FOUND } from '../../../fieldSettings/model/field-settings.model';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';

declare var jQuery: any;
@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html',
	styles: [`
	.btnClear {
		margin-right: 20px !important;
	}
	.btnDelete{
		margin: 5px 0px 0px 15px;
	}
	.btnReload{
		padding-top:7px;
	}
	.grid-message {
		width:60vw;
		text-align:center;
	}
	.grid-message label{
		font-weight: normal;
	}
	.application .device, .application .database, .application .storage,
	.device .application, .device .database, .device .storage,
	.database .device, .database .application, .database .storage,
	.storage .device, .storage .database, .storage .application {
		background-color:#f4f4f4;
	}
	.k-grid-content-locked,
	.k-grid-header-locked {
		border-right-width: 5px;
		border-right-color: #ebebeb;
	}
	`],
	encapsulation: ViewEncapsulation.None
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;
	@Output() modelChange = new EventEmitter<boolean>();
	@Input() edit: boolean;

	fields = [];
	justPlanning = false;
	VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	gridMessage = 'ASSET_EXPLORER.GRID.INITIAL_VALUE';
	showMessage = true;
	typingTimeout: any;
	// Pagination Configuration
	notAllowedCharRegex = /ALT|ARROW|F+|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;
	private maxDefault = MAX_DEFAULT;
	private maxOptions = MAX_OPTIONS;
	public fieldNotFound = FIELD_NOT_FOUND;

	state: State = {
		skip: 0,
		take: this.maxDefault,
		sort: []
	};
	gridData: GridDataResult;
	selectAll = false;
	bulkItems = {};
	bulkSelectedItems: string[] = [];
	private columnFiltersOldValues = [];

	constructor(
		private userPref: PreferenceService,
		@Inject('fields') fields: Observable<DomainModel[]>,
		private prompt: UIPromptService,
		private permissionService: PermissionService,
		private assetService: AssetExplorerService,
		private notifier: NotifierService,
		private dialog: UIDialogService) {
		this.state.take = +this.userPref.preferences['assetListSize'] || 25;
		fields.subscribe((result: DomainModel[]) => {
			this.fields = result.reduce((p, c) => {
				return p.concat(c.fields);
			}, []).map((f: FieldSettingsModel) => {
				return {
					key: `${f['domain']}_${f.field}`,
					label: f.label
				};
			});
		}, (err) => console.log(err));
	}

	/**
	 * Draws the Label property
	 * @param {ViewColumn} column
	 * @returns {string}
	 */
	getPropertyLabel(column: ViewColumn): string {
		let field = this.fields.find(f => f.key === `${column.domain}_${column.property}`);
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
	}

	hasFilterApplied(): boolean {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0;
	}

	hasItensSelected(): boolean {
		return this.bulkSelectedItems.length > 0;
	}

	hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	clearText(column: ViewColumn): void {
		if (column.filter) {
			column.filter = '';
			this.state.skip = 0;
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
		this.state.skip = 0;
		this.onReload();
	}

	private preventFilterSearch(column: ViewColumn): boolean {
		let key = `${column.domain}_${column.property}`;
		let oldVal = this.columnFiltersOldValues[key];
		this.columnFiltersOldValues[key] = column.filter;
		return oldVal === column.filter;
	}

	onFilterKeyUp(e: KeyboardEvent, column?: any): void {
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

	onFilterKeyDown(e: KeyboardEvent): void {
		if (!this.notAllowedCharRegex.test(e.code)) {
			clearTimeout(this.typingTimeout);
		}
	}

	apply(data: any): void {
		jQuery('.k-grid-content-locked').css('height', '0px'); // when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		this.gridMessage = 'ASSET_EXPLORER.GRID.NO_RECORDS';
		this.bulkItems = {};
		data.assets.map(c => c.common_id).forEach(id => {
			this.bulkItems[id] = false;
		});
		this.gridData = {
			data: data.assets,
			total: data.pagination.total
		};
		this.showMessage = data.pagination.total === 0;
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
	}

	clear(): void {
		this.showMessage = true;
		this.gridMessage = 'ASSET_EXPLORER.GRID.SCHEMA_CHANGE';
		this.gridData = null;
		jQuery('.k-grid-content-locked').css('height', '0px'); // when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		this.state = {
			skip: 0,
			take: this.state.take,
			sort: []
		};
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
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
		this.modelChange.emit();
	}

	protected createDependencyPromise(assetClass: string, id: number) {
		setTimeout(() => {
			this.dialog.open(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
				'lg').then(x => {
					this.createDependencyPromise(x.assetClass, x.id);
				}).catch(x => {
					console.log(x);
				});
		}, 500);
	}

	protected onShow(data: any) {
		this.dialog.open(AssetShowComponent, [
			{ provide: 'ID', useValue: data['common_id'] },
			{ provide: 'ASSET', useValue: data['common_assetClass'] }],
			'lg', true).then(x => {
				this.createDependencyPromise(x.assetClass, x.id);
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

	onSelectAll(): void {
		Object.keys(this.bulkItems).forEach(key => {
			this.bulkItems[key] = this.selectAll;
		});
		this.setSelectedItems();
	}

	setSelectedItems(): void {
		this.bulkSelectedItems = Object.keys(this.bulkItems).filter(key => this.bulkItems[key]);
		this.selectAll = this.bulkSelectedItems.length === this.gridData.data.length;
	}

	onBulkDelete(): void {
		if (this.hasAssetDeletePermission()) {
			const message = this.bulkSelectedItems.length === 1 ? 'asset' : 'assets';
			this.prompt.open('Confirmation Required', `You are about to delete ${this.bulkSelectedItems.length} ${message}. Click Confirm to delete the ${message} otherwise click Cancel`, 'Confirm', 'Cancel')
				.then((res) => {
					if (res) {
						this.assetService.deleteAssets(this.bulkSelectedItems)
							.subscribe(result => {
								this.notifier.broadcast({
									name: AlertType.SUCCESS,
									message: result.message
								});
								this.bulkSelectedItems = [];
								this.onReload();
							}, err => console.log(err));
					}
				});
		}
	}

	/**
	 * On cell click event.
	 * Determines if cell clicked property is either assetName or assetId and opens detail popup.
	 * @param e
	 */
	private cellClick(e): void {
		if (['common_assetName', 'common_id'].indexOf(e.column.field) !== -1) {
			jQuery('tr.k-state-selected').removeClass('k-state-selected');
			jQuery(`tr[data-kendo-grid-item-index=${e.rowIndex}]`).addClass('k-state-selected');
			this.onShow(e.dataItem);
		}
	}

	/**
	 * Returns specific class name based on the cell property name.
	 * @param {ViewColumn} column
	 * @returns {string} asset-detail-link for assetName or assetId.
	 */
	private getCellClass(column: ViewColumn): string {
		if (['common_assetName', 'common_id'].indexOf(column.domain + '_' + column.property) !== -1) {
			return 'asset-detail-link';
		}
		return '';
	}
}
