/**
 * Created by Jorge Morayta
 */

import {Injectable} from '@angular/core';

/**
 * Intended to be a helper to handle Route URLS
 */
@Injectable()
export class RouterUtils {

	/**
	 *
	 * @param value of the full URL
	 * @returns {boolean} True if it's a valid route, False otherwise.
	 */
	public static isAngularRoute(value: string): boolean {
		if (!value || value.length === 0) {
			return false;
		}
		if (value.indexOf('/module/') >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Creates from an string a valid Angular Route
	 * @param value of the full URL
	 */
	public static getAngularRoute(value: string): any {
		let fullPath = [];
		let queryParams = {};
		const angularPath = value.split('/module/')[1];
		angularPath.split('/').forEach((path) => {
			// Strip off the ?queryString if it exists
			if (path.lastIndexOf('?') > 0) {
				let matches = path.split('?');
				path = matches[0];
				matches[1].split('&').forEach( (nameValue) => {
					let match = nameValue.split('=');
					queryParams[match[0]] = match[1];
				});
			}
			fullPath.push(path);
		});

		return {path: fullPath, queryString: queryParams};
	}

	public static getLegacyRoute(value: string): string {
		const legacyBase = 'tdstm';
		if (value.indexOf(legacyBase) <= 0) {
			value = '/' + legacyBase + value;
		}
		return value;
	}

}
