import {DateUtils} from './date.utils';

export interface SortInfo {
	property: string;
	isAscending: boolean;
	type: string;
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
	 * @sortInfo Contains the current state of the sorting
	 * @dateFormat Contains the date format user preference, this is required to order by dates
	 * @returns {number}
	 */
	public static sort(items: any[], sortInfo: SortInfo, dateFormat: string) {

		const applyFormat = (formatItemsToCompare, valA: any, valB: any): {valA: any, valB: any} => {
			const getAs = {
				'boolean': (a, b) => ({valA: a.toString(), valB: b.toString()}),
				'string': (a, b) => ({valA: (a || '').toUpperCase(), valB: (b || '').toUpperCase()}),
				'number': (a, b) => ({a, b}),
				'date': (a, b) => ({valA: DateUtils.convertDateToUnixTime(dateFormat, a), valB: DateUtils.convertDateToUnixTime(dateFormat, b)}),
				'default': (a, b) => ({a, b})
			};

			let formatFunction = 'default';

			if (['boolean', 'number', 'string', 'date'].indexOf(formatItemsToCompare) >= 0) {
				formatFunction = formatItemsToCompare;
			}

			return getAs[formatFunction](valA, valB);
		};

		const compare = (a, b) => {
			const {valA, valB} = applyFormat(sortInfo.type, a[sortInfo.property], b[sortInfo.property]);

			if (sortInfo.isAscending) {
				if (valA < valB) { return -1; }
				if (valA > valB) { return 1;  }
			}

			// descending
			if (valA < valB) { return 1; }
			if (valA > valB) { return -1;  }

			// names must be equal
			return 0;
		};

		return [...items].sort(compare);
	}
}