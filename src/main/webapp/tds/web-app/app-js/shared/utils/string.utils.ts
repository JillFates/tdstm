import * as R from 'ramda';

export class StringUtils {

	/**
	 * Get a String and convert each word into capital case, by default only the first Element is converted
	 * @param {string} source
	 * @param {boolean} allCapitalCamelCase
	 * @returns {string}
	 */
	public static toCapitalCase(source: string, allCapitalCase = false): string {
		source = source.toLowerCase();
		let result: any = '';
		const toTitle = R.compose(R.join(''), R.over(R.lensIndex(0), R.toUpper));
		if (!allCapitalCase) {
			result = toTitle(source);
		} else {
			result = R.join(' ', R.map(toTitle, R.split(' ', source)));
		}
		return result;
	}

	/**
	 * Cast the strings Yes/No or any value to boolean
	 * Any other kind of value is casted to boolean
	 * @param {any} value
	 * @param {boolean} Resulting cast convertion
	 * @returns {boolean}
	 */
	public static stringToBoolean(value: any): any {
		if (value === true || value === false) {
			return value;
		}
		if (typeof value === 'undefined' || value === null) {
			return false;
		}

		if (['yes', 'true'].includes(value.toLowercase())) {
			return true;
		}

		if (['no', 'false'].includes(value.toLowerCase())) {
			return false;
		}

		// cast any kind of object to boolean
		return ++value;
	}

	/**
	 * Remove scape sequences such as \' \"
	 * @param {string} value Input string value
	 * @returns {boolean}
	 */
	public static removeScapeSequences(value: string): string {
		return value
			.replace(new RegExp('\\\\/', 'g'), '/')
			.replace(new RegExp('\\\\\'', 'g'), '\'')
			.replace(new RegExp('\\\\\"', 'g'), '\'');
	}
}