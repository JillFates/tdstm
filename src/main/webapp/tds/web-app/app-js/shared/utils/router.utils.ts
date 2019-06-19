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
		if (value.indexOf('module') >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Creates from an string a valid Angular Route
	 * @param value of the full URL
	 */
	public static getAngularRoute(value: string): string[] {
		let fullPath = [];
		value.split('/').forEach((path) => {
			if (path && path.length > 0 && path !== 'module') {
				fullPath.push(path);
			}
		});
		return fullPath;
	}

}
