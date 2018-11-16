/**
 * Pipe userDateTime
 *
 * This pipe is used to take a ISO 8601 DateTime value and transform it to the proper
 * format based on the user's selected timezone to display throughout the application.
 *
 * Usage:
 *
 * 		{{dataItem[column.property] | userDateTime : userTimeZone }}
 */

import { Pipe, PipeTransform } from '@angular/core';
import { DateUtils } from '../utils/date.utils';

/**
 * Usage:
 *		{{ ${myDateValue} | tdsDateTime : ${userDateTimeFormat} }}
 *
 *	userDateTimeFormat: taken from user preference settings.
 */
@Pipe({
	name: 'tdsDateTime'
})
export class DateTimePipe implements PipeTransform {

	transform(value: string, args?: any): any {
		return DateUtils.formatUserDateTime(args, value);
	}
}