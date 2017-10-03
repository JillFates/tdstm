import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';

import { ViewSpec, ViewColumn, VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { State } from '@progress/kendo-data-query';
import { GridDataResult, DataStateChangeEvent, RowClassArgs } from '@progress/kendo-angular-grid';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { SEARCH_QUITE_PERIOD, Keystroke } from '../../../../shared/model/constants';

declare var jQuery: any;
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
	.grid-message {
		width:60vw;
		text-align:center;
	}
	.grid-message label{
		font-weight: normal;
	}
	.application .dev, .application .dat, .application .sto,
	.device .app, .device .dat, .device .sto,
	.database .dev, .database .app, .database .sto,
	.storage .dev, .storage .dat, .storage .app {
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

	justPlanning = false;
	VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	gridMessage = 'ASSET_EXPLORER.GRID.INITIAL_VALUE';
	showMessage = true;
	typingTimeout: any;

	state: State = {
		skip: 0,
		take: 25,
		sort: []
	};
	gridData: GridDataResult;

	constructor(private userPref: PreferenceService) {
		this.state.take = +this.userPref.preferences['assetListSize'] || 25;
	}

	rowCallbackClass(context: RowClassArgs) {
		let obj = {};
		obj[context.dataItem.common_assetClass.toLowerCase()] = true;
		return obj;
	}

	cellCallbackClass(domain: string) {
		let obj = {};
		obj[domain.toLowerCase().substr(0, 3)] = true;
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

	onFilterKeyUp(e: KeyboardEvent): void {
		if (e.code === Keystroke.ENTER) {
			this.onFilter();
		} else if (e.code !== Keystroke.TAB && e.code !== Keystroke.SHIFT_RIGHT && e.code !== Keystroke.SHIFT_LEFT) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = setTimeout(() => this.onFilter(), SEARCH_QUITE_PERIOD);
		}
	}

	onFilterKeyDown(e: KeyboardEvent): void {
		if (e.code !== 'Tab') {
			clearTimeout(this.typingTimeout);
		}
	}

	apply(data: any): void {
		jQuery('.k-grid-content-locked').css('height', '0px'); // when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		this.gridMessage = 'ASSET_EXPLORER.GRID.NO_RECORDS';
		this.gridData = {
			data: data.assets,
			total: data.pagination.total
		};
		this.showMessage = data.pagination.total === 0;
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
