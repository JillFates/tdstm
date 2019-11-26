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
import {NotifierService} from '../../../../shared/services/notifier.service';
import {
	DependenciesService,
	DependenciesRequestParams
} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {
	GRID_DEFAULT_PAGINATION_OPTIONS,
	GRID_DEFAULT_PAGE_SIZE, DIALOG_SIZE
} from '../../../../shared/model/constants';
import {TagState} from '../../model/dependencies.model';
import {BulkCheckboxService} from '../../../../shared/services/bulk-checkbox.service';
import {
	BulkActionResult,
	BulkChangeType
} from '../../../../shared/components/bulk-change/model/bulk-change.model';
import {CheckboxStates} from '../../../../shared/components/tds-checkbox/model/tds-checkbox.model';
import {BulkChangeButtonComponent} from '../../../../shared/components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {DependencyResults} from '../../model/dependencies.model';
import {GridColumnModel, SELECT_ALL_COLUMN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependecyService} from '../../../assetExplorer/service/dependecy.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {OpenAssetDependenciesService, AssetDependency} from '../../service/open-asset-dependencies.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

declare var jQuery: any;

interface ComponentState {
	tagList?: any[];
	gridState?: GridState;
	gridData?: GridDataResult;
}

@Component({
	selector: 'tds-dependencies-view-grid',
	templateUrl: 'dependencies-view-grid.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DependenciesViewGridComponent implements OnInit, OnDestroy {
	@ViewChild('tdsBulkChangeButton', {static: false}) tdsBulkChangeButton: BulkChangeButtonComponent;
	@ViewChild('grid', {static: false}) grid: GridComponent;
	public dependenciesColumnModel: DependenciesColumnModel;
	protected bulkChangeType: BulkChangeType = BulkChangeType.Dependencies;
	public readonly GRID_PAGE_SIZES = GRID_DEFAULT_PAGINATION_OPTIONS;
	private readonly defaultSorting: any = { dir: 'asc', field: 'assetName' };
	protected tagsFieldNames = [];
	private destroySubject: Subject<any>;
	private tagsStateSubject: Subject<TagState>;
	public state: ComponentState;
	private componentState: BehaviorSubject<ComponentState>;
	protected readonly actionableAssets = ['assetName', 'dependentName', 'type'];
	private openAssetsHandler = null;
	SELECT_ALL_COLUMN_WIDTH = SELECT_ALL_COLUMN_WIDTH;

	constructor(
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private dialog: UIDialogService,
		private notifier: NotifierService,
		private bulkCheckboxService: BulkCheckboxService,
		private dependenciesService: DependenciesService,
		private openAssetDependenciesService: OpenAssetDependenciesService,
		private translatePipe: TranslatePipe,
		protected assetService: DependecyService,
		private permissionService: PermissionService) {
	}

	ngOnInit() {
		// set the initial component state
		this.state = this.getInitialComponentState();
		// set the open assets handler
		this.openAssetsHandler = this.openAssetDependenciesService.getOpenAssetsHandler(this.actionableAssets)

		this.destroySubject = new Subject<any>();
		this.componentState = new BehaviorSubject<any>(this.state);
		this.tagsStateSubject = new Subject<TagState>();

		this.setupGridColumns();
		this.setupBulkCheckboxService();

		// Setup state observables
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
		this.componentState
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// accumulate the partial state change into the component state
				scan((accumulator, current) => ({...accumulator, ...current}), {}),
				// query the endpoint
				mergeMap((state: ComponentState) => this.getDataFromEndpoint()(state)
					.pipe(map((results: DependencyResults) => ({...state, gridData: results}) )))
			)
			.subscribe((state: ComponentState) => {
				// with the results update the component state
				this.state = state;
				// update grid state
				this.updateGridState(state.gridState);
				// Notify changes on the state to third components
				this.notifyChangedState(state.gridData.data)
				// notify to angular a change in the component state to update the view
				this.changeDetectorRef.detectChanges();
			}, this.logError('setupComponentStateObservable'))
	}

	/**
	 * Notify an action to change the state
	 */
	private changeState(partialState = {}): void {
		this.componentState.next(partialState);
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
	 * Define the chain of operations that are executed whenever some tag filter has changed
	 * We handle tags filtering outside Kendo default filtering
	 */
	private setupTagsFilterStateObservable(): void {
		this.tagsStateSubject
			.pipe(
				// take events until destroy subject emits
				takeUntil(this.destroySubject),
				// with the latest state
				withLatestFrom(this.componentState),
				// merge the tags filters into the kendo grid filters property
				map(([tagsState, componentState]) =>
					this.mergeTagsFilterIntoGridState(tagsState, componentState))
			)
			.subscribe((componentState: ComponentState) => {
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
	public dataStateChange(state: DataStateChangeEvent): void {
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
	 * Get the current selected items info
	 * @returns {any} Object containing the selected count number and the proper plural/singular entity name
	 */
	protected getSelectedItemsCount(): any {
		const selectedCount = this.bulkCheckboxService.getSelectedItemsCount(this.state.gridData.total);

		const counterInformation = {
			count: selectedCount,
			entityName: selectedCount === 1 ?
				this.translatePipe.transform('DEPENDENCIES.SINGLE_NAME') :
				this.translatePipe.transform('DEPENDENCIES.PLURAL_NAME')
		};

		return selectedCount > 0 ? counterInformation : null;
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
	 * Update each Filter on a manual way
	 * @param column
	 */
	public onFilter(column: any): void {
		const filters = pathOr([], ['gridState', 'filter', 'filters'], this.state)
			.filter((item: any) => item.field !== column.property);

		if (column.filter !== '') {
			filters.push({
				field: column.property,
				operator: 'contains',
				value: column.filter
			});
		}

		const clonedState = clone(this.state);
		clonedState.gridState.filter.filters = filters;
		this.componentState.next(clonedState);
	}

	/**
	 * Ensure the clean and execute the search again
	 * @param column
	 */
	public onClearValue(column: any): void {
		column.filter = '';

		const filters = pathOr([], ['gridState', 'filter', 'filters'], this.state)
			.filter((item: any) => item.field !== column.property);

		const clonedState = clone(this.state);
		clonedState.gridState.filter.filters = filters;
		this.componentState.next(clonedState);
	}

	/**
	 * Log an error to the console along with the function name called
	 */
	private logError(functionName: string): any {
		return (error) => console.error(`Error, function:${functionName} message:${error.message || error}`)
	}

	/**
	 * From columns model set up the grid columns
	 * Gets the names of the tags fields
	 */
	private setupGridColumns(): void {
		this.dependenciesColumnModel = new DependenciesColumnModel();

		this.tagsFieldNames = this.dependenciesColumnModel.columns
			.reduce((accumulator: string[], current: GridColumnModel) => {
				return current.type === 'tags' ? [...accumulator, current.property]  : accumulator;
			}, []);
	}

	/**
	 * Notify to the external components that we have changes in place
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
	 * On cell click event.
	 * Determines if cell clicked property is either asset/dependent asset/dependency detail and opens detail popup.
	 * gridCell field must be an actionable item
	 * @param gridCell Reference to the current grid cell clicked
	 */
	public  onClickActionableColumn(gridCell: any): void {
		const fieldName = gridCell.column.field;
		if (!this.actionableAssets.includes(fieldName)) {
			return;
		}

		const assetDependency: AssetDependency = {
			id: gridCell.dataItem['assetId'],
			class: gridCell.dataItem['assetClass'],
			dependentId: gridCell.dataItem['dependentId'],
			dependentClass: gridCell.dataItem['dependentClass']
		};

		this.openAssetsHandler(fieldName, assetDependency)
			.pipe(
				takeUntil(this.destroySubject)
			)
			.subscribe(
				_ => this.changeState(),
				(error) => {
					console.log(error);
					this.changeState();
				}
			);
	}

	protected isBulkSelectAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDependenciesBulkSelect);
	}

	/**
	 * reloads data when the user clicks on refresh
	 */
	protected reloadData(): void {
		this.setupGridColumns();
		this.openAssetsHandler = this.openAssetDependenciesService.getOpenAssetsHandler(this.actionableAssets);
		this.state = this.getInitialComponentState();
		this.setupBulkCheckboxService();
		this.setupComponentStateObservable();
		this.setupTagsFilterStateObservable();
	}
}
