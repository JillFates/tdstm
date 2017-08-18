import { Component, Input } from '@angular/core';

import { ReportSpec, ReportColumn } from '../../model/report-spec.model';

@Component({
	selector: 'asset-explorer-report-grid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-grid/asset-explorer-report-grid.component.html'
})
export class AssetExplorerReportGridComponent {

	@Input() model: ReportSpec;

	protected toggleProperty(column: ReportColumn, property: 'edit' | 'locked') {
		column[property] = !column[property];
	}

}