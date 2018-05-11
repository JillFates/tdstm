import {DateUtils} from './date.utils';
import { GridColumnModel } from '../model/data-list-grid.model';

export interface SortInfo {
	isSorting: boolean;
	isAscending: boolean;
}

/**
 * Created by Jorge Morayta.
 * The Sort Utils contains several Sort Functions to compare array of Objects
 */

export class SortUtils {

	/**
	 * Compare function by number/string properties
	 * @param a
	 * @param b
	 * @param property
	 * @returns {number}
	 */
	public static compareByProperty(a, b, property) {
		if (a[property]) {
			let propA = a[property];
			let propB = b[property];

			// If the value is not a number, we are testing a String
			if (isNaN(propA)) {
				propA = propA.toUpperCase();
				propB = propB.toUpperCase();
			}

			let comparison = 0;
			if (propA > propB) {
				comparison = 1;
			} else if (propA < propB) {
				comparison = -1;
			}
			return comparison;
		}
	}

	/**
	 * Sort a set of items, ascending/ descending
	 +
	 * @param items Set of items
	 * @param sortInfo Contains the current state of the sorting
	 * @returns {number}
	 */
	public static sort(items: any[], sortInfo: GridColumnModel): any[] {
		return [...items].sort(this.formatAndSort(sortInfo));
	}

	/**
	 * Get the items to compare formatted according to its data type
	 +
	 * @param sortInfo Contains the current property to evaluate and dateFormat if the property is a date
	 * @param itemA First element to compare
	 * @param itemB Second element to compare
	 * @returns {valueA, valueB} formatted according to its data type
	 */
	private static formatItemsToCompare(sortInfo: GridColumnModel,  itemA: any, itemB: any): {valueA: any, valueB: any}  {
		const valueA = itemA[sortInfo.property];
		const valueB = itemB[sortInfo.property];

		const getAs = {
			'boolean': (a, b) => ({valueA: a.toString(), valueB: b.toString()}),
			'string': (a, b) => ({valueA: (a || '').toUpperCase(), valueB: (b || '').toUpperCase()}),
			'number': (a, b) => ({a, b}),
			'date': (a, b) => ({valueA: DateUtils.convertDateToUnixTime(sortInfo.format, a), valueB: DateUtils.convertDateToUnixTime(sortInfo.format, b)}),
			'default': (a, b) => ({a, b})
		};

		let dataType = 'default';

		if (['boolean', 'number', 'string', 'date'].indexOf(sortInfo.type) >= 0) {
			dataType = sortInfo.type;
		}

		return getAs[dataType](valueA, valueB);
	};

	/**
	 * Return the sort predicate used by the sort function with the format function curried
	 +
	 * @param sortInfo Contains the current property to evaluate and format if the property is a date
	 * @returns {function}
	 */
	private static formatAndSort(sortInfo: GridColumnModel): any  {
		return (a, b) => {
			const {valueA, valueB} = this.formatItemsToCompare(sortInfo, a, b);

			if (sortInfo.sort.isAscending) {
				if (valueA < valueB) { return -1; }
				if (valueA > valueB) { return 1;  }
			}

			// descending
			if (valueA < valueB) { return 1; }
			if (valueA > valueB) { return -1;  }

			// values area equal
			return 0;
		};
	};
}