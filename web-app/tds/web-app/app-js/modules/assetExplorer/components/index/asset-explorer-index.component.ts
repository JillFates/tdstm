import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ViewGroupModel, ViewModel, ViewType } from '../../model/view.model';
import { Observable } from 'rxjs/Observable';

import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { DictionaryService } from '../../../../shared/services/dictionary.service';
import { LAST_SELECTED_FOLDER } from '../../../../shared/model/constants';

@Component({
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent {

	private reportGroupModels = Array<ViewGroupModel>();
	private searchText: String;
	private viewType = ViewType;
	private selectedFolder: ViewGroupModel;

	constructor(
		private stateService: StateService,
		@Inject('reports') report: Observable<ViewGroupModel[]>,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService,
		private notifier: NotifierService,
		private dictionary: DictionaryService) {
		report.subscribe(
			(result) => {
				this.reportGroupModels = result;
				const lastFolder = this.dictionary.get(LAST_SELECTED_FOLDER);
				this.selectFolder(lastFolder || this.reportGroupModels.find((r) => r.open));
			},
			(err) => console.log(err));
	}

	protected selectFolder(folderOpen: ViewGroupModel): void {
		this.dictionary.set(LAST_SELECTED_FOLDER, folderOpen);
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
					// system: this.selectedFolder.type === this.viewType.SYSTEM_VIEWS,
					// shared: this.selectedFolder.type === this.viewType.SHARED_VIEWS
				});
		}
	}

	protected onEditReport(report: ViewModel): void {
		if (this.isEditAvailable(report)) {
			this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: report.id });
		}
	}

	protected onShowReport(report: ViewModel): void {
		this.stateService.go(AssetExplorerStates.REPORT_SHOW.name, { id: report.id });
	}

	protected onDeleteReport(report: ViewModel): void {
		if (this.isDeleteAvailable(report)) {
			this.prompt.open('Confirmation Required', 'Are you sure you want to delete this view?', 'Yes', 'No')
				.then((res) => {
					if (res) {
						this.assetExpService.deleteReport(report.id)
							.subscribe(
							result => {
								this.notifier.broadcast({
									name: AlertType.SUCCESS,
									message: result
								});
								this.loadData();
							},
							error => console.log(error));
					}
				});
		}
	}

	protected loadData() {
		this.assetExpService.getReports()
			.subscribe(result => {
				this.reportGroupModels = result as ViewGroupModel[];
				this.selectedFolder = this.reportGroupModels.find((r) => r.open);
			});
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected isEditAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
		;
	}

	protected isDeleteAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemDelete) :
			report.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerDelete);
	}

	protected toggleFavorite(report: ViewModel): void {
		if (report.isFavorite) {
			this.assetExpService.deleteFavorite(report.id)
				.subscribe(d => {
					report.isFavorite = false;
					this.loadData();
				});
		} else {
			if (this.assetExpService.hasMaximumFavorites(this.reportGroupModels.filter(x => x.name === 'Favorites')[0].items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.assetExpService.saveFavorite(report.id)
					.subscribe(d => {
						report.isFavorite = true;
						this.loadData();
					});
			}
		}
	}

}