import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ReportModel } from '../../model/report.model';

@Component({
	moduleId: module.id,
	selector: 'asset-explorer-report-save',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-save/asset-explorer-report-save.component.html'
})
export class AssetExplorerReportSaveComponent {
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