import * as R from 'ramda';

export class ObjectUtils {

	/**
	 * Merge two objects if the property exist in both
	 * and is not null or undefined to avoid overwrite an initialised property
	 * Value from Target persist
	 * Value From Sources remains
	 * @param source
	 * @param target
	 * @returns {any} Composed Object
	 */
	public static compose(source: any, target: any): any {
		target = ObjectUtils.clean(source, [R.isNil]);
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
	 * Clean an Object by removing properties based on Filter Conditions
	 * A Filter is a list that can contains any kind of expression or function that result in a evaluable condition
	 * i.e. clean({ a: null, b: undefined, c:3, d:4}, [R.isNil, (k) => k === 4]): will result on {c:3}
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