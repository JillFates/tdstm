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
}