import { DefaultBooleanFilterData, Flatten, GridColumnModel } from '../model/data-list-grid.model';
import {
	CompositeFilterDescriptor, DataResult, filterBy, FilterDescriptor, orderBy, process, SortDescriptor,
	State
} from '@progress/kendo-data-query';
import {
	CellClickEvent, GridDataResult, PageChangeEvent, RowArgs,
	SelectableSettings
} from '@progress/kendo-angular-grid';
import { pathOr, uniq } from 'ramda';
import {
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
	MINIMUM_ROWS_TO_MAKE_MENU_COLUMN_POSITIONED_DYNAMIC
} from '../model/constants';
import { DateUtils } from './date.utils';
import { NotifierService } from '../services/notifier.service';

declare var jQuery: any;

export class DataGridOperationsHelper {
	public gridData: GridDataResult = {
		data: [],
		total: 0
	};
	public resultSet: Array<any>;
	public state: State = {
		filter: {
			filters: [],
			logic: 'and'
		},
		skip: 0,
		take: GRID_DEFAULT_PAGE_SIZE
	};
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.index) >= 0;
	public selectedRows = [];
	public bulkItems: any = {};
	public selectAllCheckboxes = false;
	private selectableSettings: SelectableSettings;
	private checkboxSelectionConfig: any;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	private notifier: NotifierService;
	public showFilters;

	constructor(result: any, defaultSort?: Array<SortDescriptor>, selectableSettings?: SelectableSettings, checkboxSelectionConfig?: any, pageSize?: number) {
		// to notify grid height changes
		this.notifier = new NotifierService();
		this.state.sort = defaultSort;
		if (pageSize) {
			this.state.take = pageSize;
		}
		this.resultSet = result;
		if (selectableSettings) {
			this.selectableSettings = selectableSettings;
		}
		if (checkboxSelectionConfig) {
			for (let item of result) {
				this.bulkItems[item[checkboxSelectionConfig.useColumn]] = false;
			}
			this.checkboxSelectionConfig = checkboxSelectionConfig;
		}
		this.loadPageData();
	}

	/**
	 * On Filter column event.
	 * @param column
	 */
	public onFilter(column: GridColumnModel, operator?: string): void {
		let root = this.getFilter(column, operator);
		this.filterChange(root);
	}

	public getFilter(column: GridColumnModel, operator?: string): CompositeFilterDescriptor {
		let root = this.state.filter || { logic: 'and', filters: [] };
		let [filter] = Flatten(root).filter(x => x.field === column.property);
		if (!column.filter && column.type !== 'number' && column.filter !== 0) {
			column.filter = '';
		}
		// check for number types and null value (clear out the filters)
		if (column.type === 'number' && column.filter === null) {
			this.clearValue(column);
			return; // exit
		}
		if (column.type === 'number') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: operator ? operator : 'eq',
					value: Number(column.filter)
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = Number(column.filter);
			}
		}
		if (column.type === 'text') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'contains',
					value: column.filter,
					ignoreCase: true
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}
		if (column.type === 'date' || column.type === 'datetime') {
			const { init, end } = DateUtils.getInitEndFromDate(column.filter);
			if (filter) {
				this.state.filter.filters = this.getFiltersExcluding(column.property);
			}
			root.filters.push({ field: column.property, operator: 'gte', value: init || '', });
			root.filters.push({ field: column.property, operator: 'lte', value: end || '' });
		}
		if (column.type === 'boolean') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: (column.filter === 'True' || column.filter === true)
				});
			} else {
				if (column.filter === DefaultBooleanFilterData) {
					this.clearValue(column);
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True' || column.filter === true);
				}
			}
		}
		return root;
	}

	/**
	 * Update the filters state structure removing the column filter provided
	 * @param {any} column: Column to exclude from filters
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	public clearFilter(column: any, state: State = null): void {
		const currentState: State = state || this.state;

		column.filter = '';
		currentState.filter.filters = this.getFiltersExcluding(
			column.customPropertyName ? column.customPropertyName : column.property);
		this.filterChange(currentState.filter);
	}

	/**
	 * Clears all filters of all filterable columns.
	 * @param columns
	 * @param state: Optional Current filters state
	 */
	public clearAllFilters(columns: Array<GridColumnModel>, state: State = null): void {
		columns
			.filter(column => column.filterable)
			.forEach(column => {
				this.clearFilter(column, state);
			});
	}

	/**
	 * Get the filters state structure excluding the column filter name provided
	 * @param {string} excludeFilterName:  Name of the filter column to exclude
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	private getFiltersExcluding(excludeFilterName: string): any {
		const filters = (this.state.filter && this.state.filter.filters) || [];
		return filters.filter((r) => r['field'] !== excludeFilterName);
	}

	/**
	 * On Clear Value for filter event (Remove the column filter of the current selected filters).
	 * @param column
	 */
	public clearValue(column: GridColumnModel): void {
		column.filter = '';
		if (this.state.filter && this.state.filter.filters.length > 0) {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === column.property);
			this.state.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.state.filter);
		}
	}

	/**
	 * On Filter Change.
	 * @param {CompositeFilterDescriptor} filter
	 */
	public filterChange(filter: CompositeFilterDescriptor, state: State = null): void {
		const currentState = state || this.state;
		currentState.filter = filter;
		this.loadPageData();
	}

	/**
	 * Updates Sort.
	 * @param sort
	 */
	public sortChange(sort): void {
		this.state.sort = sort;
		this.loadPageData();
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	public selectCell(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.selectedRows = [];
			if (this.selectableSettings.mode === 'single') {
				this.selectedRows.push(event.rowIndex);
			} else {
				this.selectedRows.push(event.rowIndex);
			}
		}
	}

	/**
	 * Toggle select all checkboxes.
	 */
	public onSelectAllCheckboxes(): void {
		let currentPageItems: DataResult = process(this.resultSet, this.state);
		currentPageItems.data.forEach(item => {
			// map-key-reference inception here
			this.bulkItems[item[this.checkboxSelectionConfig.useColumn]] = this.selectAllCheckboxes;
		});
	}

	/**
	 * Un selects all checkboxes.
	 */
	public unSelectAllCheckboxes(): void {
		Object.keys(this.bulkItems).forEach(key => {
			this.bulkItems[key] = false;
		});
		this.selectAllCheckboxes = false;
	}

	/**
	 * Get all checkboxes currently selected items.
	 * @returns {Array<any>}
	 */
	public getCheckboxSelectedItems(): Array<any> {
		return Object.keys(this.bulkItems).filter(key => this.bulkItems[key] === true);
	}

	/**
	 * Get all checkbox selected as an array of numbers
	 * @returns {Array<number>}
	 */
	public getCheckboxSelectedItemsAsNumbers(): Array<number> {
		return this.getCheckboxSelectedItems().map(item => parseInt(item, 10));
	}

	/**
	 * On checkbox change event.
	 * @param key
	 */
	public onCheckboxChange(key: any): void {
		if (!this.bulkItems[key]) {
			this.selectAllCheckboxes = false;
		}
	}

	/**
	 * Reloads data for current grid.
	 * @param result
	 */
	public reloadData(result: any): void {
		this.resultSet = result;
		this.loadPageData();
	}

	/**
	 * On Page change, pagination change.
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: PageChangeEvent): void {
		this.state.skip = event.skip;
		this.state.take = event.take;
		if (this.checkboxSelectionConfig && this.checkboxSelectionConfig.useColumn) {
			// reset the select all checkbox to un-selected.
			this.selectAllCheckboxes = false;
			// If current page items all are checked then Select All box should be true, otherwise false.
			let allSelectedOnCurrentPage = true;
			let currentPageItems: DataResult = process(this.resultSet, this.state);
			currentPageItems.data.forEach(item => {
				// map-key-reference inception here
				if (!this.bulkItems[item[this.checkboxSelectionConfig.useColumn]]) {
					allSelectedOnCurrentPage = false;
					return;
				}
			});
			this.selectAllCheckboxes = allSelectedOnCurrentPage;
		}
		this.loadPageData();
	}

	/**
	 * Change the Model to the Page + Filter + Sort
	 */
	public loadPageData(): void {
		this.gridData = process(this.resultSet, this.state);
		// if filtered data is less than the current page set (skip) move to the first page.
		if (this.gridData.total < this.state.skip) {
			this.state.skip = 0;
			this.gridData = process(this.resultSet, this.state);
		}
		this.notifyUpdateGridHeight();
	}

	/**
	 * Add one element to the list
	 * @param item
	 */
	public addDataItem(item: any): void {
		this.gridData.data.unshift(item);
	}

	/**
	 * Remove one element from to the list
	 * @param item
	 */
	public removeDataItem(item: any): void {
		let index = this.gridData.data.indexOf(item);
		if (index >= 0) {
			this.gridData.data.splice(index, 1);
			this.reloadData(this.gridData.data);
		}
	}

	/**
	 * Notify the event to update the grid height
	 */
	public notifyUpdateGridHeight(): void {
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	/**
	 * Calculates and returns the current page number based on the current grid state & skip states.
	 */
	public getCurrentPage(): number {
		return (this.state.skip / this.state.take) + 1;
	}

	/**
	 * Calculates and returns the real row index based on the current pagination state.
	 */
	getRowPaginatedIndex(rowIndex: number): number {
		return ( rowIndex + (this.getCurrentPage() - 1) * this.getPageSize());
	}

	/**
	 * Returns grid take, since is the equivalent to page size number.
	 */
	public getPageSize(): number {
		return this.state.take;
	}

	/**
	 * Determine based upon the rows number if grid can positioned the menu column contextual menu dinamically
	 */
	public canSupportColumnMenuDynamic(minRows = 0): boolean {
		const minimum = minRows || MINIMUM_ROWS_TO_MAKE_MENU_COLUMN_POSITIONED_DYNAMIC;
		return (this.gridData && this.gridData.total) >= minimum;
	}

	/**
	 * Set the value of the filter and call the filtering process
	 * @param value Value to be sent to the filter, could be boolean, date, string, etc...
	 * @param column Column to filtering
	 * @param operator Type of filter operation
	 */
	public onFilterWithValue(value: any, column: GridColumnModel, operator?: string): void {
		column.filter = value;
		let root = this.getFilter(column, operator);
		root = this.removeEmptyFilters(root, column) ;
		this.filterChange(root);
	}

	/**
	 * Remove any empty filters who remains over the currenc selected filters collection
	 * @param root Collection containing all filters
	 * @param column column of the current filter selected
	 */
	public removeEmptyFilters(root: CompositeFilterDescriptor, column: GridColumnModel): CompositeFilterDescriptor {
		// clear (string/date) values
		root.filters = (root.filters || []).filter((filter: any) => filter.value !== '');
		// clear boolean filter value
		if (column.filter === '' && column.type === 'boolean') {
			root.filters = root.filters.filter((filter: any) => filter.field !== column.property);
		}

		return root;
	}

	/**
	 * Returns true if grid has filters applied
	 */
	public hasFilterApplied(): boolean {
		return this.state && this.state.filter && this.state.filter.filters.length > 0;
	}

	/**
	 * Show/Hide filters
	 */
	public toggleFilters(): void {
		this.showFilters = !this.showFilters;
	}

	/**
	 * Returns the number of distinct currently selected filters
	 * @param state optionally pass the state otherwise use the current state
	 */
	public getFilterCounter(state: State = null): number {
		const filters = pathOr(0, ['filter', 'filters'], state || this.state);
		return uniq(filters.map((filter: any) => filter.field)).length;
	}
}
