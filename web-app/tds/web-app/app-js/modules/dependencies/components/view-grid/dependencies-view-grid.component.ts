import {Component, OnInit, OnDestroy, ViewChild, ChangeDetectionStrategy} from '@angular/core';
import {State} from '@progress/kendo-data-query';
import {GridComponent, SortSettings} from '@progress/kendo-angular-grid';
import {GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import {Observable, BehaviorSubject, Subject} from 'rxjs';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {DependenciesService, DependenciesRequestParams} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';

import {tap, map, mergeMap, takeUntil} from 'rxjs/operators';
import {BulkCheckboxService} from '../../../assetExplorer/service/bulk-checkbox.service';
import {BulkActionResult, BulkChangeType} from '../../../assetExplorer/components/bulk-change/model/bulk-change.model';
import {CheckboxStates} from '../../../../shared/components/tds-checkbox/model/tds-checkbox.model';
import {BulkChangeButtonComponent} from '../../../assetExplorer/components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {DependencyResults} from '../../model/dependencies.model';

declare var jQuery: any;
@Component({
	selector: 'tds-dependencies-view-grid',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/view-grid/dependencies-view-grid.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DependenciesViewGridComponent implements OnInit, OnDestroy {
	@ViewChild('tdsBulkChangeButton') tdsBulkChangeButton: BulkChangeButtonComponent;
	@ViewChild('grid') grid: GridComponent;
	protected bulkChangeType: BulkChangeType = BulkChangeType.Dependencies;
	// protected pageSize = GRID_DEFAULT_PAGE_SIZE;
	private gridStateSubject: BehaviorSubject<State>;
	private destroySubject: Subject<any>;
	private getDependencies: any;
	protected dependenciesColumnModel: DependenciesColumnModel;
	protected gridData: GridDataResult;
	protected readonly maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	private readonly defaultSorting: any = { dir: 'asc', field: 'assetName' };
	protected readonly tagsFieldNames = ['tagsAsset', 'tagsDependency'];

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService,
		private prompt: UIPromptService,
		private bulkCheckboxService: BulkCheckboxService,
		private dependenciesService: DependenciesService
	) { }

	ngOnInit() {
		this.destroySubject = new Subject<any>();
		this.gridStateSubject = new BehaviorSubject(this.getInitialGridState());
		this.gridData = { data: [], total: 0 };
		this.getDependencies = this.dependenciesService.getDependencies.bind(this.dependenciesService);

		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
		this.bulkCheckboxService.setIdFieldName('id');
		this.setupGridConfiguration();
		this.setupGridStateChanges();
	}

	/**
	 * Set the grid fixed settings, this configuration doesn't change over time
	 */
	private setupGridConfiguration() {
		this.dependenciesColumnModel = new DependenciesColumnModel();

		this.grid.filterable = true;
		this.grid.sortable = <SortSettings>{ allowUnsort: false, mode: 'single'};
		this.grid.resizable = true;
		this.grid.pageable = { pageSizes: GRID_DEFAULT_PAGINATION_OPTIONS, info: true};
	}

	/**
	 * Emit the destroy event to complete all current streams
	 */
	ngOnDestroy() {
		this.destroySubject.next();
	}

	/**
	 * Define the chain of operations that are executed every time the grid state has suffered changes
	 */
	private setupGridStateChanges(): void {
		this.gridStateSubject
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// Grab the reference to the new grid state
				tap(this.setGridState.bind(this)),
				// Extract from the state the parameters required by the endpoint
				map(this.getParametersForEndpoint),
				// Call the endpoint to get new data
				mergeMap(this.getDependencies),
				// Map the endpoint results to the format used by the grid
				map((results: DependencyResults): GridDataResult => ({data: results.dependencies, total: results.total}))
			)
			.subscribe((results) => this.updateGridResults(results), error => console.error(error.message || error));
	}

	/**
	 * Update the grid data with fresh results
	 * @param {GridDataResult} results New results
	 */
	private updateGridResults(results: GridDataResult): void {
		// With the results refresh the grid data
		this.gridData = results;
		// reset the state of the bulk items
		this.bulkCheckboxService.initializeKeysBulkItems(results.data || []);
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});

		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	/**
	 * Extract from the grid state the parameters required bye the endpoint
	 * @param {State} state
	 * @returns {DependenciesRequestParams}
	 */
	private getParametersForEndpoint(state: State): DependenciesRequestParams {
		const {skip, take, sort, filter} = state;

		return {
			page: (skip / take) + 1,
			take,
			filters: filter && filter.filters || [],
			sort: sort && sort[0] || this.defaultSorting
		};
	}

	/**
	 * Set the new grid state
	 * @param {State} state
	 */
	private setGridState(state: State): void {
		const {sort, filter, skip, take} = state;

		this.grid.sort = sort;
		this.grid.filter = filter;
		this.grid.skip = skip;
		this.grid.pageSize = take;
		this.bulkCheckboxService.setPageSize(take);
	}

	/**
	 * Handle all the grid events (pageChange, filterChange, sortChange, ...)
	 * @param {DataStateChangeEvent} state
	 */
	protected dataStateChange(state: DataStateChangeEvent): void {
		this.gridStateSubject.next(state);
	}

	/**
	 * Get the initial state of the grid
	 * @returns {State} default grid state properties
	 */
	private getInitialGridState(): State {
		return {
			sort: [this.defaultSorting],
			filter: {
				filters: [],
				logic: 'and'
			},
			take: GRID_DEFAULT_PAGE_SIZE,
			skip: 0
		}
	}

	/**
	 * Get the current grid state
	 * @returns {State} grid state properties
	 */
	private getCurrentGridState(): State {
		const {sort, pageSize: take, skip, filter} = this.grid;

		return {sort, take, skip, filter};
	}

	/**
	 * Get the current selected items counter
	 * @returns {number}
	 */
	protected getSelectedItemsCount(): number {
		const allCounter = (this.gridData && this.gridData.total) || 0;
		return this.bulkCheckboxService.getSelectedItemsCount(allCounter)
	}

	/**
	 * Ending bulk operation, in case operation result is success
	 * uncheck all dependencies and reload the grid
	 * @param {BulkActionResult} operationResult result of bulk edit or delete operation
	 */
	protected onBulkOperationResult(operationResult: BulkActionResult): void {
		if (operationResult.success) {
			this.bulkCheckboxService.uncheckItems();
			this.gridStateSubject.next(this.getCurrentGridState());
		}
	}

	/**
	 * Get the current bulk selected items
	 * @returns {Observable<any>} bulkItems selected
	 */
	private getCurrentBulkSelectedItems(): Observable<any> {
		return this.bulkCheckboxService
			.getBulkSelectedItems(
				this.getParametersForEndpoint( {...this.getCurrentGridState(), skip: 0, take: this.gridData.total}),
				this.getDependencies)
			.pipe(
				takeUntil(this.destroySubject),
				map(results =>
					({ bulkItems: [...results.selectedAssetsIds], assetsSelectedForBulk: [...results.selectedAssets] }))
			)
	}

	/**
	 * Open the bulk actions selection window
	 */
	protected onClickBulkButton(): void {
		this.getCurrentBulkSelectedItems()
			.subscribe((results) => this.tdsBulkChangeButton.bulkData(results))
	}
}
