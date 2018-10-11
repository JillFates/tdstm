import {Injectable} from '@angular/core';

@Injectable()
export class WindowService {
	getWindow() {
		return window;
	}
}