import {DefaultBooleanFilterData, Flatten} from '../../../../shared/model/data-list-grid.model';
import {CompositeFilterDescriptor, process, SortDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, RowArgs, SelectableSettings} from '@progress/kendo-angular-grid';

export class DataGridOperationsHelper {

	public gridData: GridDataResult;
	public resultSet: Array<any>;
	public state: State = {
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.index) >= 0;
	public selectedRows = [];
	public bulkItems: any = {};
	public selectAllCheckboxes = false;
	private selectableSettings: SelectableSettings;
	private checkboxSelectionConfig: any;

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
		this.gridData = process(this.resultSet, this.state);
	}

	public onFilter(column: any): void {
		let root = this.state.filter || { logic: 'and', filters: [] };

		let [filter] = Flatten(root).filter(x => x.field === column.property);

		if (!column.filter) {
			column.filter = '';
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

	public clearValue(column: any): void {
		column.filter = '';
		if (this.state.filter && this.state.filter.filters.length > 0) {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === column.property);
			this.state.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.state.filter);
		}
	}

	public filterChange(filter: CompositeFilterDescriptor): void {
		console.log(filter);
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	public sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
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

	public onSelectAllCheckboxes(): void {
		Object.keys(this.bulkItems).forEach(key => {
			this.bulkItems[key] = this.selectAllCheckboxes;
		});
	}

	public getCheckboxSelectedItems(): Array<any> {
		return Object.keys(this.bulkItems).filter(key =>  this.bulkItems[key] === true );
	}

	public onCheckboxChange(key: any): void {
		if (!this.bulkItems[key]) {
			this.selectAllCheckboxes = false;
		}
	}

	public reloadData(result: any): void {
		this.resultSet = result;
		this.gridData = process(this.resultSet, this.state);
	}
}