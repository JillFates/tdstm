import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ViewSpec, ViewColumn, VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { State } from '@progress/kendo-data-query';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';

@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;
	@Output() modelChange = new EventEmitter<boolean>();
	@Input() edit: boolean;

	mouseDown = false;
	lastEvent: MouseEvent;
	selectColumn: ViewColumn;
	justPlanning = false;
	VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;

	state: State = {
		skip: 0,
		take: 25,
		sort: []
	};
	gridData: GridDataResult;

	onMouseUp(): void {
		this.mouseDown = false;
		this.selectColumn = null;
	}

	onMouseDown(event: MouseEvent, column: ViewColumn): void {
		this.mouseDown = true;
		this.selectColumn = column;
		this.lastEvent = event;
	}

	onMouseMove(event: MouseEvent): void {
		if (this.mouseDown) {
			let xValueChange = event.clientX - this.lastEvent.clientX;
			let width = this.selectColumn.width + (xValueChange);
			this.selectColumn.width = width < (VIEW_COLUMN_MIN_WIDTH - 50) ? (VIEW_COLUMN_MIN_WIDTH - 50) : width;
			this.lastEvent = event;
		}
	}

	clearText(column: ViewColumn): void {
		column.filter = '';
		this.onReload();
	}

	onReload(): void {
		this.modelChange.emit();
	}

	apply(data: any): void {
		this.gridData = {
			data: data.assets,
			total: data.pagination.total
		};
	}

	clear(): void {
		this.gridData = null;
		this.state = {
			skip: 0,
			take: 25,
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
}