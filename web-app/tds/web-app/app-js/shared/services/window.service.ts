/**
 * Window Service its a service to isolate the access to the global browser variable window
 */
import {Injectable} from '@angular/core';

@Injectable()
export class WindowService {
	/**
	 * Get the reference to the object window
	*/
	getWindow() {
		return window;
	}

	/**
 	* Navigate to routes not handled by the Angular router
	 * @param uri
 	*/
	navigateTo(uri: string) {
		this.getWindow().location.assign(uri);
	}
}