import { Component, Inject, OnInit } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ViewGroupModel, ViewModel, ViewType } from '../../model/view.model';
import { Observable } from 'rxjs/Observable';
import { zip } from 'rxjs/observable/zip';

import { AssetExplorerService } from '../../service/asset-explorer.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { DictionaryService } from '../../../../shared/services/dictionary.service';
import { LAST_SELECTED_FOLDER } from '../../../../shared/model/constants';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';
import { SortInfo, SortUtils } from '../../../../shared/utils/sort.utils';

@Component({
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent implements OnInit {

	private reportGroupModels = Array<ViewGroupModel>();
	private searchText: String;
	private viewType = ViewType;
	private selectedFolder: ViewGroupModel;

	private sortInfo: SortInfo;
	private sortByName: SortInfo;
	private sortByCreatedBy: SortInfo;
	private sortByCreatedOn: SortInfo;
	private sortByUpdatedOn: SortInfo;
	private sortByIsShared: SortInfo;
	private sortByIsSystem: SortInfo;
	private sortByIsFavorite: SortInfo;
	private userDateFormat: string;

	constructor(
		private stateService: StateService,
		@Inject('reports') private report: Observable<ViewGroupModel[]>,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService,
		private notifier: NotifierService,
		private dictionary: DictionaryService,
		private preferenceService: PreferenceService) {
	}
	ngOnInit() {

		this.sortByName = { property: 'name', isAscending: true, type: 'string'};
		this.sortByCreatedBy =  { property: 'createdBy', isAscending: true, type: 'string'};
		this.sortByIsShared =  { property: 'isShared', isAscending: true, type: 'boolean'};
		this.sortByIsSystem =  { property: 'isSystem', isAscending: true, type: 'boolean'};
		this.sortByCreatedOn =  { property: 'createdOn', isAscending: false, type: 'date'};
		this.sortByUpdatedOn = { property: 'updatedOn', isAscending: false, type: 'date'};
		this.sortByIsFavorite = { property: 'isFavorite', isAscending: true, type: 'boolean'};

		this.sortInfo = this.sortByCreatedOn;

		const getDefaultSorting = (sortPreference: string) => {
			const sortItems = [this.sortByName, this.sortByCreatedBy, this.sortByIsShared, this.sortByIsSystem, this.sortByCreatedOn, this.sortByUpdatedOn, this.sortByIsFavorite];
			if (sortPreference === null) {
				sortPreference = 'createdOn';
			}

			return sortItems.find((sortItem: SortInfo) => sortItem.property === sortPreference);
		};

		const preferencesCodes = `${PREFERENCES_LIST.CURRENT_DATE_FORMAT},${PREFERENCES_LIST.DEFAULT_SORT_VIEW_MANAGER}`;
		zip(this.report, this.preferenceService.getPreferences(preferencesCodes))
			.subscribe((result: any[]) => {
				const [reportResult, preferences] = result;

				this.userDateFormat = preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
				this.sortInfo = getDefaultSorting(preferences[PREFERENCES_LIST.DEFAULT_SORT_VIEW_MANAGER]);

				this.reportGroupModels = reportResult;
				const lastFolder = this.dictionary.get(LAST_SELECTED_FOLDER);
				this.selectFolder(lastFolder || this.reportGroupModels.find((r) => r.open));
				this.selectedFolder.items = SortUtils.sort(this.selectedFolder.items, this.sortInfo, this.userDateFormat);
		}, (err) => console.log(err.message || err));
	}

	protected selectFolder(folderOpen: ViewGroupModel): void {

		this.dictionary.set(LAST_SELECTED_FOLDER, folderOpen);
		this.reportGroupModels.forEach((folder) => folder.open = false);
		this.selectedFolder = this.reportGroupModels.filter((folder) => folder.name === folderOpen.name)[0];
		if (this.selectedFolder) {
			this.selectedFolder.open = true;
		}
		this.selectedFolder.items = SortUtils.sort(this.selectedFolder.items, this.sortInfo, this.userDateFormat);
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
			this.stateService.go(AssetExplorerStates.REPORT_CREATE.name, params);
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

				this.selectedFolder.items =  SortUtils.sort(this.selectedFolder.items, this.sortInfo, this.userDateFormat );
			});
	}

	protected changeSort(sort: SortInfo) {
		if (this.sortInfo.property === sort.property) {
			this.sortInfo.isAscending = !this.sortInfo.isAscending;
		} else {
			sort.isAscending = true; // reset ascending
			this.sortInfo =  sort;
		}

		this.preferenceService.setPreference(PREFERENCES_LIST.DEFAULT_SORT_VIEW_MANAGER, sort.property)
			.subscribe(() => console.log('Saving sort preference'), (err) => console.log(err.message || err));
		

		this.selectedFolder.items =  SortUtils.sort(this.selectedFolder.items, this.sortInfo, this.userDateFormat );
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