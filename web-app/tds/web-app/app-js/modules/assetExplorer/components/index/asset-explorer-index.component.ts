import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportGroupModel, ReportModel, ReportFolderIcon } from '../../model/report.model';
import { Observable } from 'rxjs/Observable';

import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';

@Component({
	moduleId: module.id,
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent {

	private reportGroupModels = Array<ReportGroupModel>();
	private searchText: String;
	private reportFolderIcon = ReportFolderIcon;
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
				this.selectedFolder = this.reportGroupModels[0];
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
			this.stateService.go(AssetExplorerStates.REPORT_CREATE.name);
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
						this.assetExpService.deleteReport(report.id);
					}
				});
		}
	}

	protected isCreateAvailable(): boolean {
		return this.selectedFolder.name === 'System Reports' ?
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