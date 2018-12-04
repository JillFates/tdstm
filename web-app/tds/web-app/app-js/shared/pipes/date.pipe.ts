import {Pipe, PipeTransform} from '@angular/core';
import {DateUtils} from '../utils/date.utils';

/**
 * Usage:
 *		{{ ${myDateValue} | tdsDate : ${userDateFormat} }}
 *
 * userDateFormat: taken from user preference settings.
 */
@Pipe({
	name: 'tdsDate'
})
export class DatePipe implements PipeTransform {

	transform(value: string, args?: any): any {
		let dateValue = DateUtils.toDateUsingFormat(value, DateUtils.TDS_OUTPUT_DATE_FORMAT);
		if (!dateValue) {
			return '';
		}
		return DateUtils.formatDate(dateValue, args);
	}
}