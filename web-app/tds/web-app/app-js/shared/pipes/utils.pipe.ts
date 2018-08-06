import {Pipe, PipeTransform} from '@angular/core';
import {ValidationUtils} from '../utils/validation.utils';

export const NULL_OBJECT_PIPE = 'nullObjectFilter';

@Pipe({
	name: 'utils'
})
export class UtilsPipe implements PipeTransform {

	transform(value: any, method: string): string {
		console.log(value);
		console.log(method);
		switch (method) {
			case NULL_OBJECT_PIPE: {
				return this.nullObjectFilter(value);
			}
			default : {
				return '';
			}
		}
	}

	private nullObjectFilter(value: any): string {
			return !value || value === null || ValidationUtils.isEmptyObject(value) ? '(null)' : value;
	}
}