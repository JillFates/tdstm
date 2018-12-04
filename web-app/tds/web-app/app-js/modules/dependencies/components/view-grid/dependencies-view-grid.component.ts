import {
	ChangeDetectionStrategy,
	ChangeDetectorRef,
	Component,
	OnInit,
	OnDestroy,
	ViewChild,
} from '@angular/core';

import {
	BehaviorSubject,
	Observable,
	Subject
} from 'rxjs';

import {
	catchError,
	map,
	mergeMap,
	scan,
	takeUntil,
	withLatestFrom,
} from 'rxjs/operators';

import {
	clone,
	compose,
	pathOr,
} from 'ramda';

import {ActivatedRoute} from '@angular/router';
import {State as GridState} from '@progress/kendo-data-query';
import {GridComponent} from '@progress/kendo-angular-grid';
import {
	GridDataResult,
	DataStateChangeEvent
} from '@progress/kendo-angular-grid';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {
	DependenciesService,
	DependenciesRequestParams
} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {
	GRID_DEFAULT_PAGINATION_OPTIONS,
	GRID_DEFAULT_PAGE_SIZE
} from '../../../../shared/model/constants';
import {TagState} from '../../model/dependencies.model';
import {BulkCheckboxService} from '../../../assetExplorer/service/bulk-checkbox.service';
import {
	BulkActionResult,
	BulkChangeType
} from '../../../assetExplorer/components/bulk-change/model/bulk-change.model';
import {CheckboxStates} from '../../../../shared/components/tds-checkbox/model/tds-checkbox.model';
import {BulkChangeButtonComponent} from '../../../assetExplorer/components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {DependencyResults} from '../../model/dependencies.model';

declare var jQuery: any;

interface ComponentState {
	tagList: any[];
	gridState: GridState;
	gridData: GridDataResult;
}

@Component({
	selector: 'tds-dependencies-view-grid',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/view-grid/dependencies-view-grid.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DependenciesViewGridComponent implements OnInit, OnDestroy {
	@ViewChild('tdsBulkChangeButton') tdsBulkChangeButton: BulkChangeButtonComponent;
	@ViewChild('grid') grid: GridComponent;
	private destroySubject: Subject<any> = new Subject<any>();
	private tagsStateSubject: Subject<TagState> = new Subject<TagState>();
	protected readonly dependenciesColumnModel: DependenciesColumnModel = new DependenciesColumnModel();
	protected bulkChangeType: BulkChangeType = BulkChangeType.Dependencies;
	protected readonly GRID_PAGE_SIZES = GRID_DEFAULT_PAGINATION_OPTIONS;
	private readonly defaultSorting: any = { dir: 'asc', field: 'assetName' };
	protected readonly tagsFieldNames = ['tagsAsset', 'tagsDependency'];
	protected state: ComponentState;
	private componentState: BehaviorSubject<any>;

	constructor(
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService,
		private prompt: UIPromptService,
		private bulkCheckboxService: BulkCheckboxService,
		private dependenciesService: DependenciesService) {
		// set the initial component state
		this.state = this.getInitialComponentState();
	}

	/**
	 * Setup the state observables
	 */
	ngOnInit() {
		this.setupBulkCheckboxService();
		this.setupComponentStateObservable();
		this.setupTagsFilterStateObservable();
	}

	/**
	 * Emit the destroy event to complete and close all current observables
	 */
	ngOnDestroy() {
		this.destroySubject.next();
	}

	/**
	 *	Set up the configuration for the bulk checkbox service
	 */
	private setupBulkCheckboxService() {
		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
		this.bulkCheckboxService.setIdFieldName('id');
	}

	/**
	 * Define the chain of operations that are executed every time the component state has suffered changes
	 */
	private setupComponentStateObservable(): void {
		const initialState = this.getInitialComponentState();
		const getData = this.getDataFromEndpoint();
		this.componentState = new BehaviorSubject<any>(initialState);

		this.componentState
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// accumulate the partial state change into the component state
				scan((accumulator, current) => ({...accumulator, ...current}), initialState),
				// query the endpoint
				mergeMap((state: ComponentState) => getData(state)
					.pipe(map((results: DependencyResults) => ({...state, gridData: results}) )))
			)
			.subscribe((state: ComponentState) => {
				this.state = state;
				this.updateGridState(state.gridState);
				this.notifyChangedState(state.gridData.data)
			}, this.logError('setupComponentStateObservable'))
	}

	/**
	 * Notify an action to change the state
	 */
	private changeState(partialState = {}): void {
		this.componentState.next(partialState);
	}

	/**
	 * Notify to the external components that we have changes in the state
	 */
	private notifyChangedState(dependencies: any[]): void {
		// reset the state of the bulk items
		this.bulkCheckboxService.initializeKeysBulkItems(dependencies);
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	/**
	 * Get the initial state of the component
	 */
	private getInitialComponentState(): ComponentState {
		return {
			tagList: pathOr([], ['snapshot', 'data', 'tagList'], this.route),
			gridState: this.getInitialGridState(),
			gridData: { data: [], total: 0 },
		}
	}

	/**
	 * Define the chain of operations that are executed every time some tags filter has changed
	 */
	private setupTagsFilterStateObservable(): void {
		this.tagsStateSubject
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// with the latest state
				withLatestFrom(this.componentState),
				// merge the tags filters into the filters property
				map(([tagsState, componentState]) =>
					this.mergeTagsFilterIntoGridState(tagsState, componentState))
			)
			.subscribe((componentState: ComponentState) => {
				// notify changes on the state
				this.changeState(componentState);
			}, this.logError('setupTagsFilterStateObservable'));
	}

	/**
	 * Get the initial state of the grid
	 * @returns {GridState} default grid state properties
	 */
	private getInitialGridState(): GridState {
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
	 * Query the service to get the dependencies
	 */
	private getDataFromEndpoint(): any {
		const getDependencies = this.dependenciesService.getDependencies.bind(this.dependenciesService);
		return compose(getDependencies, this.getParametersForEndpoint.bind(this));
	}

	/**
	 * Extract from the grid state the parameters required bye the endpoint
	 * @param {GridState} state
	 * @returns {DependenciesRequestParams}
	 */
	private getParametersForEndpoint(state: ComponentState): DependenciesRequestParams {
		const {skip, take, sort, filter} = state.gridState;

		return {
			page: (skip / take) + 1,
			take,
			filters: filter && filter.filters || [],
			sort: sort && sort[0] || this.defaultSorting
		};
	}

	/**
	 * Set the new grid state
	 * @param {GridState} state
	 */
	private updateGridState(state: GridState): void {
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
		this.changeState({gridState: state});
	}

	/**
	 * On tags filter change, emit the change tags event
	 * @param {string} field Name of the tags filter column
	 * @param {any}  filter Object containing the tags context information
	 */
	protected onTagFilterChange(field: string, filter: any): void {
		let operator = filter.operator && filter.operator === 'ALL' ? '&' : '|';

		const tags = (filter.tags || [])
			.filter((tag) => !isNaN(tag.id))
			.map((tag) => tag.id).join(operator);

		this.tagsStateSubject.next({field, tags})
	}

	/**
	 * Get the current selected items counter
	 * @returns {number}
	 */
	protected getSelectedItemsCount(): number {
		return this.bulkCheckboxService.getSelectedItemsCount(this.state.gridData.total)
	}

	/**
	 * Ending bulk operation, in case operation result is success
	 * uncheck all dependencies and reload the grid
	 * @param {BulkActionResult} operationResult result of bulk edit or delete operation
	 */
	protected onBulkOperationResult(operationResult: BulkActionResult): void {
		if (operationResult.success) {
			this.bulkCheckboxService.uncheckItems();
			this.changeState();
		}
	}

	/**
	 * Get the current bulk selected items
	 * @returns {Observable<any>} bulkItems selected
	 */
	private getCurrentBulkSelectedItems(): Observable<any> {
		const getDependencies = this.dependenciesService.getDependencies.bind(this.dependenciesService);
		const gridState = { ...this.state.gridState, ...{skip: 0, take: this.state.gridData.total}};
		const state = {...this.state, gridState};

		return this.bulkCheckboxService
			.getBulkSelectedItems(this.getParametersForEndpoint(state), getDependencies)
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// map the results to the format required by the bulkcheckbox service
				map(results =>
					({ bulkItems: [...results.selectedAssetsIds], assetsSelectedForBulk: [...results.selectedAssets] }))
			)
	}

	/**
	 * Open the bulk actions selection window
	 */
	protected onClickBulkButton(): void {
		this.getCurrentBulkSelectedItems()
			.subscribe((results) => this.tdsBulkChangeButton.bulkData(results), this.logError('onClickBulkButton'))
	}

	/**
	 * Take the current tags filter state and merge it into the current gridState filters
	 * @param {TagState} tagsState
	 * @param {ComponentState} componentState
	 */
	private mergeTagsFilterIntoGridState(tagsState: TagState, componentState: ComponentState): ComponentState {
		const filters = pathOr([], ['gridState', 'filter', 'filters'], componentState)
			.filter((item: any) => item.field !== tagsState.field);

		if (tagsState.tags) {
			filters.push({field: tagsState.field, value: tagsState.tags,  operator: null});
		}

		const clonedState = clone(componentState);
		clonedState.gridState.filter.filters = filters;

		return clonedState;
	}

	/**
	 * Log an error to the console along with the function name called
	 */
	private logError(functionName: string): any {
		return (error) => console.log(`Error, function:${functionName} message:${error.message || error}`)

	}

}
