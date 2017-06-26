import { Component } from '@angular/core';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { StateService } from '@uirouter/angular';

import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportModel } from '../../model/report.model';
import { AssetExplorerReportSaveComponent } from '../report-save/asset-explorer-report-save.component';
import { AssetExplorerReportExportComponent } from '../report-export/asset-explorer-report-export.component';
@Component({
	moduleId: module.id,
	selector: 'asset-explorer-report-config',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-config/asset-explorer-report-config.component.html'
})
export class AssetExplorerReportConfigComponent {

	assetClasses = ['Application', 'Device', 'Database', 'Logical Storage'];
	model: ReportModel;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private stateService: StateService) {
		this.model = new ReportModel();
	}

	protected onSaveAs(): void {
		this.dialogService.open(AssetExplorerReportSaveComponent, [
			{ provide: ReportModel, useValue: this.model }
		]);
	}

	protected onExport(): void {
		this.dialogService.open(AssetExplorerReportExportComponent, [
			{ provide: ReportModel, useValue: this.model }
		]);
	}

	protected onFavorite() {
		this.model.favorite = !this.model.favorite;
	}

	protected onBackToStart(): void {
		this.stateService.go(AssetExplorerStates.REPORT_SELECTOR.name);
	}

}