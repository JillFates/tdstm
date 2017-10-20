import * as R from 'ramda';

export class ObjectUtils {

	/**
	 * Merge two objects if the property exist in both
	 * Value from Target persist
	 * Value From Sources remains
	 * @param source
	 * @param target
	 * @returns {any} Composed Object
	 */
	public static compose(source: any, target: any): any {
		let composed = R.clone(source);
		const propNames = R.keys(source);
		for (let i = 0; i < propNames.length; i++) {
			let property = propNames[i];
			const hasProperty = R.has(property);
			if (hasProperty(target)) {
				composed[property] = target[property];
			}
		}
		return composed;
	}

	/**
	 * Clean an Object by removing based on Filter Conditions
	 * A Filter is an object that can contains any kind of expression or function
	 * that result in a evaluable condition
	 * @param value
	 * @param filter
	 * @returns {any}
	 */
	public static clean(source: any, filter: any[]): any {
		let composed = R.clone(source);
		for (let i = 0; i < filter.length; i++) {
			composed = R.reject(filter[i], composed);
		}
		return composed;
	}
}