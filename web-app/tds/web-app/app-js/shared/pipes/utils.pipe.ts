import {Pipe, PipeTransform} from '@angular/core';
import {ValidationUtils} from '../utils/validation.utils';
import {ObjectUtils} from '../utils/object.utils';

export const NULL_OBJECT_PIPE = 'nullObjectFilter';
export const OBJECT_OR_LIST_PIPE = 'objectOrListFilter';

@Pipe({
	name: 'utils'
})
export class UtilsPipe implements PipeTransform {

	transform(value: any, method: string): string {
		switch (method) {
			case NULL_OBJECT_PIPE: {
				return this.nullObjectFilter(value);
			}
			case OBJECT_OR_LIST_PIPE: {
				return this.getValueOrObjectOrListString(value);
			}
			default : {
				return '';
			}
		}
	}

	private nullObjectFilter(value: any): string {
			return !value || value === null || ValidationUtils.isEmptyObject(value) ? '(null)' : value;
	}

	private getValueOrObjectOrListString(value: any): string {
		return ObjectUtils.getValueOrObjectOrListString(value);
	}
}