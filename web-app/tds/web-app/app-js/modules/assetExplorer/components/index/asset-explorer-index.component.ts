import {Component, Inject} from '@angular/core';
import {StateService} from '@uirouter/angular';
import {AssetExplorerStates} from '../../asset-explorer-routing.states';
import {ReportGroupModel, ReportModel, ReportFolderIcon} from '../../model/report.model';
import {Observable} from 'rxjs/Observable';

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

	constructor(private stateService: StateService, @Inject('reports') reportGroupModels: Observable<ReportGroupModel[]>) {
		reportGroupModels.subscribe(
			(result) => {
				let allFavorites = Array<ReportModel>();
				this.reportGroupModels = result;
				this.selectedFolder = this.reportGroupModels[0];
				this.reportGroupModels.filter((folder) => folder.items && folder.items.length > 0).forEach((folder) => {
					this.selectedFolder.items = this.selectedFolder.items.concat(folder.items);
					allFavorites = folder.items.filter((report) => report.favorite).concat(allFavorites);
				});

				let favoriteFolder = this.reportGroupModels.find((folder) => folder.name === 'Favorites');
				if (favoriteFolder) {
					favoriteFolder.items = allFavorites;
				}

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

	protected onCreateNew(): void {
		this.stateService.go(AssetExplorerStates.REPORT_CREATE.name);
	}

}