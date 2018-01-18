import {DefaultBooleanFilterData, Flatten, GridColumnModel} from '../model/data-list-grid.model';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';

export class GridFiltersUtils {

	public static filterColumn(column: GridColumnModel, state: State, gridData: GridDataResult, resultSet: any): void {
		let root = state.filter || { logic: 'and', filters: [] };

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
					this.clearValue(column, state, gridData, resultSet);
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True');
				}
			}
		}

		this.onFilterChange(root, state, gridData, resultSet);
	}

	public static onFilterChange(filter: CompositeFilterDescriptor, state: State, gridData: GridDataResult, resultSet: any): void {
		state.filter = filter;
		gridData = process(resultSet, state);
	}

	public static clearValue(column: GridColumnModel, state: State, gridData: GridDataResult, resultSet: any): void {
		column.filter = '';
		if (state.filter && state.filter.filters.length > 0) {
			const filterIndex = state.filter.filters.findIndex((r: any) => r.field === column.property);
			state.filter.filters.splice(filterIndex, 1);
			this.onFilterChange(state.filter, state, gridData, resultSet);
		}
	}
}