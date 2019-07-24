/**
 * Local Storage Strategy
 */
import {getActionTypeFromInstance} from '@ngxs/store';
import {tap} from 'rxjs/operators';

export const APP_STATE_KEY = '@@STATE-' + window.location.hostname;

export function LocalStorageProvider(state, action, next) {

	// After every refresh first action fired will be @@INIT
	if (getActionTypeFromInstance(action) === '@@INIT') {

		// reading from local storage and writing into state, when app is refreshed
		let storedStateStr = localStorage.getItem(APP_STATE_KEY);
		let storedState = JSON.parse(storedStateStr);
		state = {...state, ...storedState};
		return next(state, action);
	}

	return next(state, action).pipe(tap(result => {
		localStorage.setItem(APP_STATE_KEY, JSON.stringify(result));
	}));
}
