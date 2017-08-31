import { Component, Input } from '@angular/core';

import { ViewSpec, ViewColumn } from '../../model/view-spec.model';

@Component({
	selector: 'asset-explorer-view-grid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent {

	@Input() model: ViewSpec;

	protected toggleProperty(column: ViewColumn, property: 'edit' | 'locked') {
		column[property] = !column[property];
	}

}