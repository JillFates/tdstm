import {DefaultBooleanFilterData, Flatten, GridColumnModel} from '../model/data-list-grid.model';
import {
	CompositeFilterDescriptor, filterBy, FilterDescriptor, orderBy, process, SortDescriptor,
	State
} from '@progress/kendo-data-query';
import {
	CellClickEvent, GridDataResult, PageChangeEvent, RowArgs,
	SelectableSettings
} from '@progress/kendo-angular-grid';
import {MAX_DEFAULT, MAX_OPTIONS} from '../model/constants';

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
		take: MAX_DEFAULT
	};
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.index) >= 0;
	public selectedRows = [];
	public bulkItems: any = {};
	public selectAllCheckboxes = false;
	private selectableSettings: SelectableSettings;
	private checkboxSelectionConfig: any;
	public defaultPageOptions = MAX_OPTIONS;

	constructor(result: any, defaultSort: Array<SortDescriptor>, selectableSettings?: SelectableSettings, checkboxSelectionConfig?: any) {
		this.state.sort = defaultSort;
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
					value: column.filter
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
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

		if (column.type === 'date') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'gte',
					value: column.filter,
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}

		if (column.type === 'boolean') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: (column.filter === 'True')
				});
			} else {
				if (column.filter === DefaultBooleanFilterData) {
					this.clearValue(column);
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True');
				}
			}
		}

		this.filterChange(root);
	}

	/**
	 * On Clear Value for filter event.
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
	public filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
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
		Object.keys(this.bulkItems).forEach(key => {
			this.bulkItems[key] = this.selectAllCheckboxes;
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
		return Object.keys(this.bulkItems).filter(key =>  this.bulkItems[key] === true );
	}

	/**
	 * Get all checkbox selected as an array of numbers
	 * @returns {Array<number>}
	 */
	public getCheckboxSelectedItemsAsNumbers(): Array<number> {
		return this.getCheckboxSelectedItems().map( item => parseInt(item, 10));
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
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: PageChangeEvent): void {
		this.state.skip = event.skip;
		this.state.take = event.take;
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
	}
}