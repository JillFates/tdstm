import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ViewSpec, ViewColumn } from '../../model/view-spec.model';

@Component({
	selector: 'asset-explorer-view-grid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;
	@Input() data: [any];
	@Output() modelChange = new EventEmitter<boolean>();
	@Input() edit: boolean;

	mouseDown = false;
	lastEvent: MouseEvent;
	selectColumn: ViewColumn;
	justPlanning = false;

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
			this.selectColumn.width = width < 100 ? 100 : width;
			this.lastEvent = event;
		}
	}

	clearText(column: ViewColumn): void {
		column.filter = '';
		this.onReload();
	}

	onReload(): void {
		this.modelChange.emit(this.justPlanning);
	}

}