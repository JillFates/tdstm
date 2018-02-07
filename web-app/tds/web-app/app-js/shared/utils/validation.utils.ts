/**
 * Created by David Ontiveros on 7/5/2017.
 */

import {Injectable} from '@angular/core';

/**
 * Intended to be a helper validation class that can be used
 * across all components in order to make common validations.
 */
@Injectable()
export class ValidationUtils {

	public static NOT_FOUND: number = -1;

	/**
	 *
	 * @param value Value to be compared.
	 * @returns {boolean} True if it's a valid number, False otherwise.
	 */
	public static isValidNumber(value: any): boolean {
		if (isNaN(value) ) {
			return false;
		}
		return true;
	}

	/**
	 * Detects if current browser is Internet Explorer, from version 10 to newer
	 * Code based on --> https://codepen.io/gapcode/pen/vEJNZN
	 * @returns {boolean}
	 */
	public static isIEBrowser(): boolean {
		// Detect IE >= 10
		let msie = window.navigator.userAgent.indexOf('MSIE ');
		// Detect IE == 11
		let trident = window.navigator.userAgent.indexOf('Trident/');
		// Detect IE >= 12 (Edge)
		let edge = window.navigator.userAgent.indexOf('Edge/');
		if (msie > 0 || trident > 0 || edge > 0) {
			return true;
		}
		return false;
	}
}
