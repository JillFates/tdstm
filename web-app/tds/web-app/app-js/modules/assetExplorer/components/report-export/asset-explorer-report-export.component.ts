import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ReportModel } from '../../model/report.model';

@Component({
	selector: 'asset-explorer-report-save',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-export/asset-explorer-report-export.component.html',
	styles: [`
		.has-error,.has-error:focus { border: 1px #f00 solid;}
	`]
})
export class AssetExplorerReportExportComponent {
	private columns: any[];
	protected fileName = 'asset_explorer';
	protected dataToExport = [];

	constructor(
		model: ReportModel,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService) {

		let configuredColumns = { ...model.schema.columns } ;

		this.columns = Object.keys(configuredColumns).map( key => configuredColumns[key]['label']);

		console.log(this.columns);
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

}