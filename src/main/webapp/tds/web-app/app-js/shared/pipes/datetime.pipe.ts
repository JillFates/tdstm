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

	/**
	 * Used to format an ISO 8601 Date String (e.g. 2018-08-03T20:44:15Z) to the user's preferred
	 * format
	 * @param value a datetime value in the ISO 8601 format (yyyy-mm-DDThh:MM:ssZ)
	 * @param userTimeZone the user's timezone to format the value as
	 * @param timeFormat Optional, used only to overwrite the default time format value
	 * @returns {any}
	 */
	transform(value: string, userTimeZone: any, timeFormat = ''): any {
		return DateUtils.formatUserDateTime(userTimeZone, value, timeFormat);
	}
}