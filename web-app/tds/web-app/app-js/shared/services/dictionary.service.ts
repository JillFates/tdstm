import { Injectable } from '@angular/core';

@Injectable()
export class DictionaryService {
	content = {};

	get(key: string): any {
		return this.content[key];
	}

	set(key: string, value: any): void {
		this.content[key] = value;
	}
}