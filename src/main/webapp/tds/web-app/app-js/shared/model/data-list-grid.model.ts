import { SortInfo } from '../utils/sort.utils';
import { DateUtils } from '../utils/date.utils';

import { State } from '@progress/kendo-data-query';
import { pathOr, uniq } from 'ramda';

export const COLUMN_MIN_WIDTH = 360;
export const SELECT_ALL_COLUMN_WIDTH = 50;
export const BooleanFilterData: Array<string> = ['True', 'False'];
export const DefaultBooleanFilterData = 'All';

export const Flatten = filter => {
	const filters = filter.filters;
	if (filters) {
		return filters.reduce((acc, curr) => acc.concat(curr.filters ? Flatten(curr) : [curr]), []);
	}
	return [];
};

export enum ActionType {
	VIEW,
	CREATE,
	EDIT
};

export class GridColumnModel {
	label: string;
	property: string;
	customPropertyName?: string; // To keep the custome name and dont loose the property name original
	properties ?: Array<string>; // use this when it's a multi-level object accessor. i.e. [dataItem.currentValues.name]
	type: string;
	format?: string;
	width: number;
	locked ? = false;
	hidden ? = false;
	filter?: any;
	filterable ? = false;
	headerStyle?: any;
	headerClass?: Array<string>;
	cellStyle?: any;
	cellClass?: string | Array<string>;
	sort?: SortInfo;
	columnMenu ? = false;
	resizable ? = true;
	sortable ? = true;

	constructor() {
		this.type = 'text';
		this.width = COLUMN_MIN_WIDTH;
	}

	/**
	 * Based on provided column, update the structure which holds the current selected filters
	 * @param {any} column: Column to filter
	 * @param {any} state: Current filters state
	 * @returns {any} Filter structure updated
	 */
	static filterColumn(column: any, state: any): any  {
		let root = state.filter || { logic: 'and', filters: [] };

		let [filter] = Flatten(root).filter(item => item.field === column.property);

		if (column.filter !== false && !column.filter) {
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
			const {init, end} = DateUtils.getInitEndFromDate(column.filter);

			if (filter) {
				const filters = (state.filter && state.filter.filters) || [];
				state.filter.filters = filters.filter((r: any) => r['field'] !== column.property);
			}
			root.filters.push({ field: column.property, operator: 'gte', value: init || '', });
			root.filters.push({ field: column.property, operator: 'lte', value: end || ''});
		}

		if (column.type === 'boolean') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: (column.filter === 'True' || column.filter === true)
				});
			} else {
				if (column.filter !== null && column.filter !== '') {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True' || column.filter === true)
				}
			}
		}

		if (column.type === 'number') {
			if (column.filter === '') {
				const filters = (state.filter && state.filter.filters) || [];
				state.filter.filters = filters.filter((r: any) => r['field'] !== column.property);
			} else {
				if (!filter) {
					root.filters.push({
						field: column.property,
						operator: 'eq',
						value: parseInt(column && column.filter || 0, 10)
					});
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = column.filter;
				}
			}
		}

		return root;
	}

	/**
	 * Returns the number of distinct currently selected filters
	 * - Remove when all grids support data grid operations helper
	 */
	public static getFilterCounter(state: State): number {
		const filters = pathOr(0, ['filter', 'filters'], state);
		return uniq(filters.map((filter: any) => filter.field)).length;
	}

}
