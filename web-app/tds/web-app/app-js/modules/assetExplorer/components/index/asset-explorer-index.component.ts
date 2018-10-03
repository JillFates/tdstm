import { Component, Inject, OnInit } from '@angular/core';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ViewGroupModel, ViewModel, ViewType } from '../../model/view.model';
import { Observable } from 'rxjs';
import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { DictionaryService } from '../../../../shared/services/dictionary.service';
import { LAST_SELECTED_FOLDER } from '../../../../shared/model/constants';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';
import { SortUtils } from '../../../../shared/utils/sort.utils';
import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';
import { ViewManagerColumnsHelper } from './asset-explorer-index-columns.helper';

@Component({
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html',
})
export class AssetExplorerIndexComponent implements OnInit {

	private reportGroupModels = Array<ViewGroupModel>();
	private searchText: String;
	private viewType = ViewType;
	private selectedFolder: ViewGroupModel;
	private gridColumns: GridColumnModel[];

	constructor(
		@Inject('reports') private report: Observable<ViewGroupModel[]>,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService,
		private notifier: NotifierService,
		private dictionary: DictionaryService,
		private preferenceService: PreferenceService) {
	}
	ngOnInit() {
		this.gridColumns = ViewManagerColumnsHelper.createColumns();
		const preferencesCodes = `${PREFERENCES_LIST.CURRENT_DATE_FORMAT},${PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT}`;
		Observable.zip(this.report, this.preferenceService.getPreferences(preferencesCodes))
			.subscribe(this.setupDefaultSettings.bind(this), (err) => console.log(err.message || err));
	}

	private setupDefaultSettings(defaultSettings: any[]) {
		const [reportResult, preferences] = defaultSettings;

		const userDateFormat = preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		this.gridColumns =  ViewManagerColumnsHelper.setFormatToDateColumns(userDateFormat);
		this.reportGroupModels = reportResult;
		const lastFolder = this.dictionary.get(LAST_SELECTED_FOLDER);
		this.selectFolder(lastFolder || this.reportGroupModels.find((r) => r.open));
		this.gridColumns =  ViewManagerColumnsHelper.setColumnAsSorted(preferences[PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT]);
		this.selectedFolder.items = SortUtils.sort(this.selectedFolder.items, ViewManagerColumnsHelper.getCurrentSortedColumnOrDefault() );
	}

	protected selectFolder(folderOpen: ViewGroupModel): void {

		this.dictionary.set(LAST_SELECTED_FOLDER, folderOpen);
		this.reportGroupModels.forEach((folder) => folder.open = false);
		this.selectedFolder = this.reportGroupModels.filter((folder) => folder.name === folderOpen.name)[0];
		if (this.selectedFolder) {
			this.selectedFolder.open = true;
		}
		this.selectedFolder.items = SortUtils.sort(this.selectedFolder.items, ViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
	}

	protected onClearTextFilter(): void {
		this.searchText = '';
	}

	protected onCreateReport(): void {
		if (this.isCreateAvailable()) {
			const selectedFolderType: ViewType = this.selectedFolder.type;
			const params: ViewModel = Object.assign(new ViewModel(),
				{
					isSystem: selectedFolderType === this.viewType.SYSTEM_VIEWS,
					isShared: selectedFolderType === this.viewType.SHARED_VIEWS,
					isFavorite: selectedFolderType === this.viewType.FAVORITES
				});
			// TODO: STATE SERVICE GO
			// this.stateService.go(AssetExplorerStates.REPORT_CREATE.name, params);
		}
	}

	protected onEditReport(report: ViewModel): void {
		if (this.isEditAvailable(report)) {
			// TODO: STATE SERVICE GO
			// this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: report.id });
		}
	}

	protected onShowReport(report: ViewModel): void {
		// TODO: STATE SERVICE GO
		// this.stateService.go(AssetExplorerStates.REPORT_SHOW.name, { id: report.id });
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

				this.selectedFolder.items =  SortUtils.sort(this.selectedFolder.items, ViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
			});
	}

	protected changeSort(propertyName: string) {
		this.gridColumns = ViewManagerColumnsHelper.setColumnAsSorted(propertyName);

		this.preferenceService.setPreference(PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT, propertyName)
			.subscribe(() => console.log('Saving sort preference'), (err) => console.log(err.message || err));

		this.selectedFolder.items =  SortUtils.sort(this.selectedFolder.items, ViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
		return ;
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