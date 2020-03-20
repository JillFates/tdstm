/**
 * Created by David Ontiveros on 7/5/2017.
 */

import {Injectable} from '@angular/core';

declare var jQuery: any;
/**
 * Intended to be a helper validation class that can be used
 * across all components in order to make common validations.
 */
@Injectable()
export class ValidationUtils {

	public static NOT_FOUND = -1;

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

	/**
	 * Checks if object is empty.
	 * @param object
	 * @returns {boolean}
	 */
	public static isEmptyObject(object: any): boolean {
		return object !== null && Object.keys(object).length === 0 && object.constructor === Object;
	}

	/**
	 *  Determine if a string has the format URL or label|URL
	 * @param labelURL {string}
	 * @returns {boolean}
	 */
	public static isValidLabelURL(labelURL = ''): boolean {
		const [label, url, extra] = labelURL
			.split('|')
			.map((part: string) => part.trim());

		return extra ? false :  ValidationUtils.isValidURL(url || label);
	}

	/**
	 *  Determine if a string is an URL
	 * @param url {string}
	 * @returns {boolean}
	 */
	public static isValidURL(url: string): boolean {
		let pattern = new RegExp('^(https?:\\/\\/)?' +
			'((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' +
			'((\\d{1,3}\\.){3}\\d{1,3}))' +
			'(\\:\\d+)?' +
			'(\\/[-a-z\\d%@_.~+&:]*)*' +
			'(\\?[;&a-z\\d%@_.,~+&:=-]*)?' +
			'(\\#[-a-z\\d_]*)?$', 'i');
		return pattern.test(url);
	}

	/**
	 * Determines if the class or one of its parents belongs to a banned class
	 * @param bannedClasses
	 * @param event
	 * @return Boolean
	 */
	static isBannedClass(bannedClasses: string[], event: MouseEvent): boolean {
		const isChildOfBannedClass = bannedClasses.find((className: string) => {
			const name = event.target && event.target['className'] || '';

			// banned by parent
			const counter =  jQuery(event.target).parents(`.${className}`).length;
			// banned by class
			const isBannedClass = bannedClasses.find((item: string) => name.indexOf(item) !== -1);

			return counter > 0 || Boolean(isBannedClass);
		});

		return Boolean(isChildOfBannedClass);
	}
}
