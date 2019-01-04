/**
 * Window Service its a service to isolate the access to the global browser variable window
 */
import {Injectable} from '@angular/core';

@Injectable()
export class WindowService {
	getWindow() {
		return window;
	}
}