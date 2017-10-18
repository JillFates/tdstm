import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
	name: 'clean'
})
export class CleanObjectPipe implements PipeTransform {
	transform(value: any): any {
		const propNames = Object.getOwnPropertyNames(value);
		for (let i = 0; i < propNames.length; i++) {
			let propName = propNames[i];
			if (value[propName] === null || value[propName] === undefined) {
				delete value[propName];
			}
		}
		return value;
	}
}