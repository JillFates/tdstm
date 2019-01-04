import {Pipe, PipeTransform} from '@angular/core';

/**
 * Usage:
 *        {{ ${myTextToEncode} | tdsEscapeURLEncoding }}
 */
@Pipe({
	name: 'tdsEscapeURLEncoding'
})
export class EscapeUrlEncodingPipe implements PipeTransform {

	transform(value: string, args?: any): any {
		if (value) {
			value = encodeURI(value);
		}
		return value;
	}
}