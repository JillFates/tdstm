import {Pipe, PipeTransform} from '@angular/core';
import { formatNumber } from '@telerik/kendo-intl';

/**
 * Usage:
 *		{{ ${myDateValue} | tdsNumber : ${format} }}
 *	format example: 'n2'
 */
@Pipe({
	name: 'tdsNumber'
})
export class NumericPipe implements PipeTransform {

	/**
	 * Formats a number with the given format.
	 * @param {string} value
	 * @param args should be #NumberConfigurationConstraintsModel type object.
	 * @returns {any}
	 */
	transform(value: string, args?: any): any {
		const format = (args || '').replace(/'/g, '');

		if (isNaN(+value) || value === '' || value === null || value === undefined) {
			return '';
		}
		let number = +value;
		return formatNumber(number, format);
	}
}