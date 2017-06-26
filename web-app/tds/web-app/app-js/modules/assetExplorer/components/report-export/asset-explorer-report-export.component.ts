import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ReportModel } from '../../model/report.model';

@Component({
	moduleId: module.id,
	selector: 'asset-explorer-report-save',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-export/asset-explorer-report-export.component.html'
})
export class AssetExplorerReportExportComponent {
	model: ReportModel;

	constructor(
		model: ReportModel,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService) {

		this.model = { ...model };
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

}