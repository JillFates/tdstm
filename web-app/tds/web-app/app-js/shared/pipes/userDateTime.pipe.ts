import { Pipe, PipeTransform } from '@angular/core';
import { DateUtils } from '../utils/date.utils';

@Pipe({
	name: 'userDateTime'
})
export class UserDateTime implements PipeTransform {

	transform(value: string, args?: any): any {
		return DateUtils.formatUserDateTime(args, value);
	}
}