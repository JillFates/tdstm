import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportGroupModel, ReportModel, ReportFolderIcon } from '../../model/report.model';
import { Observable } from 'rxjs/Observable';

import { PermissionService } from '../../../../shared/services/permission.service';

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
		private permissionService: PermissionService) {
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
		this.stateService.go(AssetExplorerStates.REPORT_CREATE.name);
	}

}