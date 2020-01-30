import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {ViewGroupModel, ViewModel, ViewType} from '../../../assetExplorer/model/view.model';
import {Observable, ReplaySubject} from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';
import {takeUntil} from 'rxjs/operators';

import {AssetExplorerService} from '../../service/asset-explorer.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {DictionaryService} from '../../../../shared/services/dictionary.service';
import {LAST_SELECTED_FOLDER} from '../../../../shared/model/constants';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {AssetViewManagerColumnsHelper} from './asset-view-manager-columns.helper';
import { HeaderActionButtonData } from 'tds-component-library';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-asset-view-manager',
	templateUrl: 'asset-view-manager.component.html',
})
export class AssetViewManagerComponent implements OnInit, OnDestroy {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	public reportGroupModels = Array<ViewGroupModel>();
	public searchText: string;
	private viewType = ViewType;
	public selectedFolder: ViewGroupModel;
	public gridColumns: GridColumnModel[];
	private report;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	showAssetsFilter: boolean;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private permissionService: PermissionService,
		private assetExpService: AssetExplorerService,
		private prompt: UIPromptService,
		private notifier: NotifierService,
		private dictionary: DictionaryService,
		private translateService: TranslatePipe,
		private preferenceService: PreferenceService) {
		this.report = this.route.snapshot.data['reports'] as Observable<ViewGroupModel[]>;
	}
	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateReport.bind(this),
			},
		];

		this.gridColumns = AssetViewManagerColumnsHelper.createColumns();
		const preferencesCodes = `${PREFERENCES_LIST.CURRENT_DATE_FORMAT},${PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT}`;
		this.preferenceService.getPreferences(preferencesCodes)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((preferences) => {
			this.setupDefaultSettings(preferences);
			});
	}

	private setupDefaultSettings(preferences: any[]) {
		const userDateFormat = preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		this.gridColumns =  AssetViewManagerColumnsHelper.setFormatToDateColumns(userDateFormat);
		this.reportGroupModels = this.report;
		const lastFolder = this.dictionary.get(LAST_SELECTED_FOLDER);
		this.selectFolder(lastFolder || this.reportGroupModels.find((r) => r.open));
		this.gridColumns =  AssetViewManagerColumnsHelper.setColumnAsSorted(preferences[PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT]);
		this.selectedFolder.views = SortUtils.sort(this.selectedFolder.views, AssetViewManagerColumnsHelper.getCurrentSortedColumnOrDefault() );
	}

	protected selectFolder(folderOpen: ViewGroupModel, $event: MouseEvent = null): void {
		if ($event) {
			$event.preventDefault();
		}
		this.dictionary.set(LAST_SELECTED_FOLDER, folderOpen);
		this.reportGroupModels.forEach((folder) => folder.open = false);
		this.selectedFolder = this.reportGroupModels.filter((folder) => folder.name === folderOpen.name)[0];
		if (this.selectedFolder) {
			this.selectedFolder.open = true;
		}
		this.selectedFolder.views = SortUtils.sort(this.selectedFolder.views, AssetViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
	}

	public onClearTextFilter(): void {
		this.searchText = '';
	}

	protected onCreateReport(): void {
		if (this.isCreateAvailable()) {
			this.router.navigate(['asset', 'views', 'create']);
		}
	}

	protected onEditReport(report: ViewModel): void {
		if (this.isEditAvailable(report)) {
			this.router.navigate(['asset', 'views', report.id, 'edit']);
		}
	}

	protected onShowReport(report: ViewModel): void {
		this.router.navigate(['asset', 'views', report.id, 'show']);
	}

	protected onDeleteReport(report: ViewModel): void {
		if (this.isDeleteAvailable(report)) {
			this.prompt.open('Confirmation Required', 'Are you sure you want to delete this view?', 'Yes', 'No')
				.then((res) => {
					if (res) {
						this.assetExpService.deleteReport(report.id)
						.pipe(takeUntil(this.unsubscribeOnDestroy$))
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
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(result => {
				this.reportGroupModels = result as ViewGroupModel[];
				this.selectedFolder = this.reportGroupModels.find((r) => r.open);

				this.selectedFolder.views =  SortUtils.sort(this.selectedFolder.views, AssetViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
			});
	}

	protected changeSort(propertyName: string) {
		this.gridColumns = AssetViewManagerColumnsHelper.setColumnAsSorted(propertyName);

		this.preferenceService.setPreference(PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT, propertyName)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(() => console.log('Saving sort preference'), (err) => console.log(err.message || err));

		this.selectedFolder.views =  SortUtils.sort(this.selectedFolder.views, AssetViewManagerColumnsHelper.getCurrentSortedColumnOrDefault());
		return ;
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	public isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected isEditAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
	}

	protected isDeleteAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemDelete) :
			report.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerDelete);
	}

	protected toggleFavorite(report: ViewModel): void {
		if (report.isFavorite) {
			this.assetExpService.deleteFavorite(report.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(d => {
				report.isFavorite = false;
				this.loadData();
			});
		} else {
			if (this.assetExpService.hasMaximumFavorites(this.reportGroupModels.filter(x => x.name === 'Favorites')[0].views.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.assetExpService.saveFavorite(report.id)
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(d => {
					report.isFavorite = true;
					this.loadData();
				});
			}
		}
	}

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}

	/**
	 * Toggle filter show/hide flag.
	 */
	public toggleFilter(): void {
		this.showAssetsFilter = !this.showAssetsFilter;
	}

	/**
	 * Update the view based on the filter changes
	 * @param value new filter change
	 * @param column  Column throwing the event
	 */
	protected onFilter(value: string , column: GridColumnModel): void {
		this.searchText = value || '';
		column.filter = this.searchText;
		if (!value) {
			this.showAssetsFilter = false;
		}

		this.loadData();
	}

	/**
	 * Clear the content of all filterable columns
	 */
	protected onClearAllFilters(): void {
		this.gridColumns.forEach((column: GridColumnModel) => {
			this.onFilter(null, column);
		})
	}

	/**
	 * Determine if we need to disable the clear all filters button
	 */
	private onDisableClearFilter(): boolean {
		return !this.searchText;
	}
}
