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
}