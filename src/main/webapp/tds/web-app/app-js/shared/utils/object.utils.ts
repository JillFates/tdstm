import * as R from 'ramda';

export class ObjectUtils {

	/**
	 * Merge source into destination if the property exist in both
	 * and is not null or undefined to avoid overwrite an initialised property
	 * @param destination
	 * @param source
	 * @returns {any} Composed Object
	 */
	public static compose(destination: any, source: any): any {
		source = ObjectUtils.clean(source, [R.isNil]);
		let composed = R.clone(destination);
		const propNames = R.keys(destination);
		for (let i = 0; i < propNames.length; i++) {
			let property = propNames[i];
			const hasProperty = R.has(property.toString());
			if (hasProperty(source)) {
				if (R.is(Object, composed[property])) {
					composed[property] = ObjectUtils.compose(composed[property], source[property]);
				} else {
					composed[property] = source[property];
				}

			}
		}
		return composed;
	}

	/**
	 * Clean an Object by removing properties based on Filter Conditions
	 * A Filter is a list that can contains any kind of expression or function that result in a evaluable condition
	 * i.e. clean({ a: null, b: undefined, c:3, d:4}, [R.isNil, (k) => k === 4]): will result on {c:3}
	 * @param destination
	 * @param filter
	 * @returns {any}
	 */
	public static clean(destination: any, filter: any[]): any {
		let composed = R.clone(destination);
		for (let i = 0; i < filter.length; i++) {
			composed = R.reject(filter[i], composed);
		}
		return composed;
	}

	/**
	 * Helper method to modify the Data Signature by passing the property and the new value
	 * @param {string} dataSignature
	 * @param property
	 * @param value
	 * @returns {string}
	 */
	public static modifySignatureByProperty(dataSignature: string, property: any, value: any): string {
		let dataInstance = JSON.parse(dataSignature);
		dataInstance[property] = value;
		return JSON.stringify(dataInstance);
	}

	/**
	 * Determine if the value passed is an object.
	 * @param value
	 * @returns {boolean}
	 */
	public static isObject(value: any): boolean {
		return typeof value === 'object';
	}

	/**
	 * Returns either the value, or the Object or the Array parsed as a string.
	 * @param value
	 * @returns {string | any}
	 */
	public static getValueOrObjectOrListString(value: any): string | any {
		if (Array.isArray(value)) {
			return JSON.stringify(value);
			// return '(LIST)'
		} else if (ObjectUtils.isObject(value)) {
			return JSON.stringify(value);
			// return '(OBJECT)'
		} else {
			return value;
		}
	}

	/**
	 * Determines if object has no properties
	 * @param obj - object to evaluate
	 * @returns {boolean}
	 */
	public static isEmpty(obj: any) {
		return Object.keys(obj).length === 0 && obj.constructor === Object;
	}

	/**
	 * Return an object containing just the properties that are not present in the blacklist
	 * @param {any} properties Object containing the properties to work on
	 * @param {any} blackList If some property exists in the blacklist  remove it
	 * @returns {any} Object containing just the properties not present in the black list
	 */
	public static excludeProperties(properties: any, blackList: string[]): any {
		const result = {};

		for (let property in properties) {
			if (properties.hasOwnProperty(property)) {
				if (blackList.indexOf(property) === -1) {
					result[property] = properties[property];
				}
			}
		}

		return result;
	}
}