import {Injectable} from '@angular/core';

@Injectable()
export class WindowService {
	constructor() {}

	getWindow() {
		return window;
	}
}