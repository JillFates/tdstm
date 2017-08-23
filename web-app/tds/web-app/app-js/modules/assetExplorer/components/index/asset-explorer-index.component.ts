import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportGroupModel, ReportModel, ViewType } from '../../model/report.model';
import { Observable } from 'rxjs/Observable';

import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';

@Component({
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent {

	private reportGroupModels = Array<ReportGroupModel>();
	private searchText: String;
	private viewType = ViewType;
	private selectedFolder: ReportGroupModel;

	constructor(
		private stateService: StateService,
		@Inject('reports') report: Observable<ReportGroupModel[]>,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService,
		private notifier: NotifierService) {
		report.subscribe(
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
				{
					system: this.selectedFolder.type === this.viewType.SYSTEM_VIEWS,
					shared: this.selectedFolder.type === this.viewType.SHARED_VIEWS
				});
		}
	}

	protected onEditReport(report: ReportModel): void {
		if (this.isEditAvailable(report)) {
			this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: report.id });
		}
	}

	protected onDeleteReport(report: ReportModel): void {
		if (this.isDeleteAvailable(report)) {
			this.prompt.open('Confirmation Required', 'Are you sure you want to delete this view?', 'Yes', 'No')
				.then((res) => {
					if (res) {
						this.assetExpService.deleteReport(report.id)
							.concat(this.assetExpService.getReports())
							.subscribe(
							result => {
								if (typeof result === 'string') {
									this.notifier.broadcast({
										name: AlertType.SUCCESS,
										message: result
									});
								} else {
									this.reportGroupModels = result as ReportGroupModel[];
									this.selectedFolder = this.reportGroupModels.find((r) => r.open);
								}
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
		return this.selectedFolder.type !== this.viewType.ALL &&
			this.selectedFolder.type !== this.viewType.FAVORITES &&
			this.selectedFolder.type !== this.viewType.RECENT;
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	protected isCreateAvailable(): boolean {
		return this.selectedFolder.type === this.viewType.SYSTEM_VIEWS ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) :
			this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected isEditAvailable(report: ReportModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
		;
	}

	protected isDeleteAvailable(report: ReportModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemDelete) :
			report.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerDelete);
	}

}