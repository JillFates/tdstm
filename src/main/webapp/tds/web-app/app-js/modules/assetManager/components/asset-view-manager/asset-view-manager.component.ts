import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { ViewGroupModel, ViewModel, ViewType } from '../../../assetExplorer/model/view.model';
import { Observable, ReplaySubject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { DictionaryService } from '../../../../shared/services/dictionary.service';
import { GRID_DEFAULT_PAGE_SIZE, LAST_SELECTED_FOLDER } from '../../../../shared/model/constants';
import { PreferenceService, PREFERENCES_LIST } from '../../../../shared/services/preference.service';
import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';
import { AssetViewManagerColumnsHelper } from './asset-view-manager-columns.helper';
import { HeaderActionButtonData } from 'tds-component-library';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { SortDescriptor } from '@progress/kendo-data-query';

@Component({
	selector: 'tds-asset-view-manager',
	templateUrl: 'asset-view-manager.component.html',
})
export class AssetViewManagerComponent implements OnInit, OnDestroy {
	public headerActionButtons: HeaderActionButtonData[];
	public reportGroupModels = Array<ViewGroupModel>();
	public searchText: string;
	public selectedFolder: ViewGroupModel;
	public gridColumns: GridColumnModel[];
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	gridHelper: DataGridOperationsHelper;
	showFilters: boolean;
	disableClearFilters: Function;
	userDateFormat: string;
	sortBy: string;
	private viewType = ViewType;
	private report;

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
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateReport.bind(this),
			},
		];
		this.gridColumns = AssetViewManagerColumnsHelper.createColumns();
		const preferencesCodes = `${ PREFERENCES_LIST.CURRENT_DATE_FORMAT },${ PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT }`;
		this.preferenceService.getPreferences(preferencesCodes)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((preferences) => {
				this.setupDefaultSettings(preferences);
			});
	}

	ngOnInit() {
		// remove the min-height that TDSTMLayout.min.js randomly calculates wrong.
		const contentWrapper = document.getElementsByClassName('content-wrapper')[0];
		if (contentWrapper) {
			// TODO: we can test this calculation among several pages and if it works correctly we can apply to the general app layout.
			// 45px is the header height, 31px is the footer height
			(contentWrapper as any).style.minHeight = 'calc(100vh - (45px + 31px))';
		}
	}

	/**
	 * Load & Setup grid settings based on user preferences.
	 * @param preferences
	 */
	private setupDefaultSettings(preferences: any[]) {
		this.userDateFormat = preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		this.gridColumns = AssetViewManagerColumnsHelper.setFormatToDateColumns(this.userDateFormat);
		this.reportGroupModels = this.report;
		const lastFolder = this.dictionary.get(LAST_SELECTED_FOLDER);
		this.sortBy = preferences[PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT];
		if (!this.sortBy) {
			this.sortBy = 'name';
			this.preferenceService.setPreference(PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT, this.sortBy)
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(() => console.log('Saving sort preference'), (err) => console.log(err.message || err));
		}
		this.gridHelper = new DataGridOperationsHelper(
			[],
			[{
				dir: 'asc',
				field: this.sortBy
			}],
			null,
			null,
			GRID_DEFAULT_PAGE_SIZE);
		this.selectFolder(lastFolder || this.reportGroupModels.find((r) => r.open));
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	public isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	/**
	 * Toggle filter show/hide flag.
	 */
	public toggleFilter(): void {
		this.showFilters = !this.showFilters;
	}

	/**
	 * On Folder option change.
	 * @param folderOpen
	 * @param $event
	 */
	protected selectFolder(folderOpen: ViewGroupModel, $event: MouseEvent = null): void {
		if ($event) {
			$event.preventDefault();
		}
		this.dictionary.set(LAST_SELECTED_FOLDER, folderOpen);
		this.selectedFolder = this.reportGroupModels.filter((folder) => folder.name === folderOpen.name)[0];
		this.reloadData();
	}

	/**
	 * Redirects to Create view page.
	 */
	protected onCreateReport(): void {
		if (this.isCreateAvailable()) {
			this.router.navigate(['asset', 'views', 'create']);
		}
	}

	/**
	 * Opens a view in edit mode.
	 * @param report
	 */
	protected onEditReport(report: ViewModel): void {
		if (this.isEditAvailable(report)) {
			this.router.navigate(['asset', 'views', report.id, 'edit']);
		}
	}

	/**
	 * Opens a view.
	 * @param report
	 */
	protected onShowReport(report: ViewModel): void {
		this.router.navigate(['asset', 'views', report.id, 'show']);
	}

	/**
	 * On Delete View button.
	 * @param report
	 */
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
									this.reloadData();
								},
								error => console.log(error));
					}
				});
		}
	}

	/**
	 * Reloads Grid data.
	 */
	protected reloadData() {
		const selectedFolder = this.reportGroupModels.find((r: ViewGroupModel) => r.name === this.selectedFolder.name);
		this.assetExpService.getReports()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(result => {
				this.reportGroupModels = result as ViewGroupModel[];
				this.reportGroupModels.forEach((folder: ViewGroupModel) => {
					folder.open = false;
					if (folder.name === selectedFolder.name) {
						folder.open = true;
						this.selectedFolder = folder;
					}
				});
				this.gridHelper.reloadData(this.selectedFolder.views);
			});
	}

	/**
	 * On sort change, save to preferences.
	 * @param sort
	 */
	protected changeSort(sort: Array<SortDescriptor>): void {
		this.gridHelper.sortChange(sort);
		this.sortBy = sort[0].field;
		this.preferenceService.setPreference(PREFERENCES_LIST.VIEW_MANAGER_DEFAULT_SORT, this.sortBy)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(() => console.log('Saving sort preference'), (err) => console.log(err.message || err));
	}

	/**
	 * Check if Edit operation is available for the view.
	 * @param report
	 */
	protected isEditAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit) ||
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
	}

	/**
	 * Check if delete is available for the view.
	 * @param report
	 */
	protected isDeleteAvailable(report: ViewModel): boolean {
		return report.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemDelete) :
			report.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerDelete);
	}

	/**
	 * Toggle Favorite button
	 * @param report
	 */
	protected toggleFavorite(report: ViewModel): void {
		if (report.isFavorite) {
			this.assetExpService.deleteFavorite(report.id)
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(d => {
					report.isFavorite = false;
					this.reloadData();
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
						this.reloadData();
					});
			}
		}
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.gridHelper.getFilterCounter() === 0;
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
}
