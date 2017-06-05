import {Inject, Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'translate'})
export class TranslatePipe implements PipeTransform {

	private dictionary: Object = {};

	constructor(@Inject('localizedDictionary') localizedDictionary) {

		/*
		 For performance reasons we will load properties from dictionary at first so we avoid iterate through it
		 everytime a translation is required.
		 */
		for (let property of Object.keys(localizedDictionary)) {
			this.traverse(property, localizedDictionary[property]);
		}
	}

	/**
	 * Traverse to the entire dictionary tree storing it's definitions as a unique key - value pair.
	 * @param key
	 * @param value
	 */
	traverse(key, value): void {

		if (typeof value === 'object') {
			for (let property of Object.keys(value)) {
				this.traverse(key + '.' + property, value[property]);
			}
		} else {
			this.dictionary[key] = value;
		}

	}

	transform(value: string): string {

		let translatedValue: string;

		translatedValue = this.dictionary[value] ? this.dictionary[value] : value;

		return translatedValue;
	}
}