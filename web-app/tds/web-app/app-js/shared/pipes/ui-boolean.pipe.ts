/**
 * Created by Jorge Morayta on 3/10/2017.
 */
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'tdsUIBoolean'})
export class UIBooleanPipe implements PipeTransform {
	transform(value: boolean): string {
		return (value) ? 'Yes' : 'No';
	}
}