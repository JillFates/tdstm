import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportGroupModel, ReportModel, ReportType } from '../../model/report.model';
import { Observable } from 'rxjs/Observable';

import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent {

	private reportGroupModels = Array<ReportGroupModel>();
	private searchText: String;
	private reportType = ReportType;
	private selectedFolder: ReportGroupModel;

	constructor(
		private stateService: StateService,
		@Inject('reports') reportGroupModels: Observable<ReportGroupModel[]>,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService) {
		reportGroupModels.subscribe(
			(result) => {
				this.reportGroupModels = result;
				this.selectedFolder = this.reportGroupModels.find((r) => r.open);
			},
			(err) => console.log(err));
	}

	protected selectFolder(folderOpen: ReportGroupModel): void {
		this.reportGroupModels.forEach((folder) => folder.open = false);
		this.selectedFolder = this.reportGroupModels.filter((folder) => folder.name === folderOpen.name)[0];
		if (this.selectedFolder) {
			this.selectedFolder.open = true;
		}
	}

	protected onClearTextFilter(): void {
		this.searchText = '';
	}

	protected onCreateReport(): void {
		if (this.isCreateAvailable()) {
			this.stateService.go(AssetExplorerStates.REPORT_CREATE.name,
				{ system: this.selectedFolder.type === this.reportType.SYSTEM_REPORTS });
		}
	}

	protected onEditReport(report: ReportModel): void {
		if (this.isEditAvailable(report)) {
			this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: report.id });
		}
	}

	protected onDeleteReport(report: ReportModel): void {
		if (this.isDeleteAvailable(report)) {
			this.prompt.open('Confirmation Required', 'Are you sure you want to delete this report?', 'Yes', 'No')
				.then((result) => {
					if (result) {
						this.assetExpService.deleteReport(report.id
						).subscribe(
							deleted => {
								this.assetExpService.getReports()
									.subscribe(res => this.reportGroupModels = res, err => console.log(err));
							},
							error => console.log(error));
					}
				});
		}
	}

	/**
	 * Show/Hide the Create Button if not in All / Favorites / Recent
	 * @returns {boolean}
	 */
	protected isCreateVisible(): boolean {
		return this.selectedFolder.type !== this.reportType.ALL &&
			this.selectedFolder.type !== this.reportType.FAVORITES &&
			this.selectedFolder.type !== this.reportType.RECENT;
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	protected isCreateAvailable(): boolean {
		return this.selectedFolder.type === this.reportType.SYSTEM_REPORTS ?
			this.permissionService.hasPermission('AssetExplorerSystemCreate') :
			this.permissionService.hasPermission('AssetExplorerCreate');
	}

	protected isEditAvailable(report: ReportModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission('AssetExplorerSystemEdit') ||
			this.permissionService.hasPermission('AssetExplorerSystemSaveAs') :
			this.permissionService.hasPermission('AssetExplorerEdit') ||
			this.permissionService.hasPermission('AssetExplorerSaveAs');
		;
	}

	protected isDeleteAvailable(report: ReportModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission('AssetExplorerSystemDelete') :
			report.isOwner && this.permissionService.hasPermission('AssetExplorerDelete');
	}

}