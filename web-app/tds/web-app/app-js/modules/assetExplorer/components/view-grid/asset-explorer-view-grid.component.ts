import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ViewSpec, ViewColumn, VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { State } from '@progress/kendo-data-query';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
import { PreferenceService } from '../../../../shared/services/preference.service';

@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html',
	styles: [`
	.btnClear {
		margin-right: 20px !important;
	}
	.btnReload{
		padding-top:7px;
	}
	`]
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;
	@Output() modelChange = new EventEmitter<boolean>();
	@Input() edit: boolean;

	justPlanning = false;
	VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	gridMessage = 'ASSET_EXPLORER.GRID.INITIAL_VALUE';

	state: State = {
		skip: 0,
		take: 25,
		sort: []
	};
	gridData: GridDataResult;

	constructor(private userPref: PreferenceService) {
		this.state.take = +this.userPref.preferences['assetListSize'] || 25;
	}

	onClearFilters(): void {
		this.model.columns.forEach((c: ViewColumn) => {
			c.filter = '';
		});
	}

	hasFilterApplied(): boolean {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0;
	}

	clearText(column: ViewColumn): void {
		if (column.filter) {
			column.filter = '';
			this.state.skip = 0;
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

	apply(data: any): void {
		this.gridMessage = 'ASSET_EXPLORER.GRID.NO_RECORDS';
		this.gridData = {
			data: data.assets,
			total: data.pagination.total
		};
	}

	clear(): void {
		this.gridMessage = 'ASSET_EXPLORER.GRID.SCHEMA_CHANGE';
		this.gridData = null;
		this.state = {
			skip: 0,
			take: this.state.take,
			sort: []
		};
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		if (state.sort[0]) {
			let field = state.sort[0].field.split('_');
			this.model.sort.domain = field[0];
			this.model.sort.property = field[1];
			this.model.sort.order = state.sort[0].dir === 'asc' ? 'a' : 'd';
		}
		this.modelChange.emit();
	}

	onWidthChange(data: any) {
		this.model.columns.filter((c: ViewColumn) =>
			data[0].column.field === `${c.domain}_${c.property}`
		).forEach((c: ViewColumn) => {
			c.width = data[0].newWidth;
		});
	}
}