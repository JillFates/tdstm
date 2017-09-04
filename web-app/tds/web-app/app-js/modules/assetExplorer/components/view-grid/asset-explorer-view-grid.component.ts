import { Component, Input } from '@angular/core';

import { ViewSpec, ViewColumn } from '../../model/view-spec.model';

@Component({
	selector: 'asset-explorer-view-grid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;

	mouseDown = false;
	lastEvent: MouseEvent;
	selectColumn: ViewColumn;

	protected toggleProperty(column: ViewColumn, property: 'edit' | 'locked') {
		column[property] = !column[property];
	}

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
			console.log(width, xValueChange);
			this.selectColumn.width = width < 50 ? 50 : width;
			this.lastEvent = event;
		}
	}

}