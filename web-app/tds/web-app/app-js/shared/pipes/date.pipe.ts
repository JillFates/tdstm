import {Pipe, PipeTransform} from '@angular/core';
import {DateUtils} from '../utils/date.utils';

/**
 * Usage:
 *		{{ myDateValue | tdsDate : userDateFormat }}
 */
@Pipe({
	name: 'tdsDate'
})
export class DatePipe implements PipeTransform {

	transform(value: string, args?: any): any {
		let dateValue = DateUtils.toDate(value);
		if (isNaN(dateValue.getTime())) {
			return value;
		}
		return DateUtils.formatDate(dateValue, args);
	}
}